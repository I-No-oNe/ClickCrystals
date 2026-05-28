package io.github.itzispyder.clickcrystals.gui.elements.common.interactive;

import io.github.itzispyder.clickcrystals.gui.GuiElement;
import io.github.itzispyder.clickcrystals.gui.GuiScreen;
import io.github.itzispyder.clickcrystals.gui.elements.common.Typeable;
import io.github.itzispyder.clickcrystals.gui.misc.ChatColor;
import io.github.itzispyder.clickcrystals.util.MathUtils;
import io.github.itzispyder.clickcrystals.util.StringUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.render.RenderUtils;
import io.github.itzispyder.clickcrystals.util.misc.Pair;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class TextFieldElement extends GuiElement implements Typeable {

    private static final int UNDO_LIMIT    = 100;
    private static final int GUTTER_COLOR  = 0xFF4A5568;
    private static final int SEL_COLOR     = 0xA07E75FF;
    private static final int CURSOR_COLOR  = 0xE0FFFFFF;

    private TextHighlighter highlighter = new TextHighlighter();
    private ChatColor backgroundColor = ChatColor.BLACK;
    private ChatColor textColor = ChatColor.WHITE;
    // selectionStart = caret; selectionEnd = selection anchor (== selectionStart when no range)
    private int selectionStart, selectionEnd;
    private Point selectedStartPoint, selectedEndPoint;
    private int textY = 5, textHeight;
    private String content = "";
    private String styledContent;
    private boolean selectionBlinking, selectedAll;
    private int selectionBlink;
    private final ArrayDeque<String> undoStack = new ArrayDeque<>();
    private int preferredCol = -1; // preserved column for Up/Down navigation
    private int dragAnchor = -1;   // content index where drag started
    private Function<Integer, Boolean> keyInterceptor; // set by parent screen to intercept keys before default handling
    private Runnable onContentChanged; // called after any content or cursor change

    public TextFieldElement(String preText, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.selectedStartPoint = new Point();
        this.selectedEndPoint = new Point();
        this.content = preText;
        this.styledContent = style(content);
        this.resetSelection();
    }

    public TextFieldElement(int x, int y, int width, int height) {
        this("", x, y, width, height);
    }

    // ── Navigation helpers ────────────────────────────────────────────────────

    private int lineStart(int pos) {
        int i = Math.min(pos, content.length());
        while (i > 0 && content.charAt(i - 1) != '\n') i--;
        return i;
    }

    private int lineEnd(int pos) {
        int i = Math.min(pos, content.length());
        while (i < content.length() && content.charAt(i) != '\n') i++;
        return i;
    }

    private int prevWordBoundary(int pos) {
        int i = pos;
        while (i > 0 && Character.isWhitespace(content.charAt(i - 1))) i--;
        while (i > 0 && !Character.isWhitespace(content.charAt(i - 1))) i--;
        return i;
    }

    private int nextWordBoundary(int pos) {
        int i = pos;
        while (i < content.length() && !Character.isWhitespace(content.charAt(i))) i++;
        while (i < content.length() && Character.isWhitespace(content.charAt(i))) i++;
        return i;
    }

    private boolean hasRange() {
        return !selectedAll && selectionStart != selectionEnd;
    }

    private int selMin() { return Math.min(selectionStart, selectionEnd); }
    private int selMax() { return Math.max(selectionStart, selectionEnd); }

    private void deleteRange() {
        pushUndo();
        int lo = selMin(), hi = selMax();
        content = content.substring(0, lo) + content.substring(hi);
        selectionStart = selectionEnd = lo;
        selectedAll = false;
        styledContent = style(content);
        updateSelection();
    }

    private void pushUndo() {
        if (!undoStack.isEmpty() && undoStack.peek().equals(content)) return;
        undoStack.push(content);
        while (undoStack.size() > UNDO_LIMIT) undoStack.removeLast();
    }

    // ── Input handling ────────────────────────────────────────────────────────

    @Override
    public void onChar(char chr) {
        if (Character.isISOControl(chr)) return;
        if (hasRange() || selectedAll) deleteRange();
        onInput(input -> insertInput(String.valueOf(chr)));
        shiftRight();
        preferredCol = -1;
    }

    @Override
    public boolean onKey(int key, int scan) {
        if (!(mc.screen instanceof GuiScreen screen)) return false;

        if (keyInterceptor != null && keyInterceptor.apply(key)) return true;

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (hasRange() || selectedAll) {
                selectedAll = false;
                selectionEnd = selectionStart;
                return true;
            }
            screen.selected = null;
            return true;
        }

        // Ctrl combos
        if (screen.ctrlKeyPressed) {
            switch (key) {
                case GLFW.GLFW_KEY_A -> { selectedAll = true; preferredCol = -1; return true; }
                case GLFW.GLFW_KEY_C -> {
                    if (selectedAll) mc.keyboardHandler.setClipboard(content);
                    else if (hasRange()) mc.keyboardHandler.setClipboard(content.substring(selMin(), selMax()));
                    return true;
                }
                case GLFW.GLFW_KEY_X -> {
                    if (selectedAll) { mc.keyboardHandler.setClipboard(content); clear(); }
                    else if (hasRange()) { mc.keyboardHandler.setClipboard(content.substring(selMin(), selMax())); deleteRange(); }
                    preferredCol = -1;
                    return true;
                }
                case GLFW.GLFW_KEY_V -> {
                    if (hasRange() || selectedAll) deleteRange();
                    onInput(input -> insertInput(mc.keyboardHandler.getClipboard()));
                    shiftRight();
                    preferredCol = -1;
                    return true;
                }
                case GLFW.GLFW_KEY_Z -> {
                    if (!undoStack.isEmpty()) {
                        content = undoStack.pop();
                        selectionStart = selectionEnd = MathUtils.clamp(selectionStart, 0, content.length());
                        styledContent = style(content);
                        updateSelection();
                    }
                    preferredCol = -1;
                    return true;
                }
                case GLFW.GLFW_KEY_D -> {
                    int ls = lineStart(selectionStart);
                    int le = lineEnd(selectionStart);
                    String line = content.substring(ls, le);
                    pushUndo();
                    String ins = "\n" + line;
                    content = content.substring(0, le) + ins + content.substring(le);
                    selectionStart = selectionEnd = le + ins.length();
                    styledContent = style(content);
                    updateSelection();
                    preferredCol = -1;
                    return true;
                }
                case GLFW.GLFW_KEY_SLASH -> {
                    int ls = lineStart(selectionStart);
                    int le = lineEnd(selectionStart);
                    String line = content.substring(ls, le);
                    pushUndo();
                    String newLine;
                    int delta;
                    if (line.startsWith("// ")) { newLine = line.substring(3); delta = -3; }
                    else if (line.startsWith("//")) { newLine = line.substring(2); delta = -2; }
                    else { newLine = "// " + line; delta = 3; }
                    content = content.substring(0, ls) + newLine + content.substring(le);
                    selectionStart = selectionEnd = MathUtils.clamp(selectionStart + delta, ls, ls + newLine.length());
                    styledContent = style(content);
                    updateSelection();
                    preferredCol = -1;
                    return true;
                }
                case GLFW.GLFW_KEY_LEFT -> {
                    selectionStart = selectionEnd = prevWordBoundary(selectionStart);
                    selectedAll = false;
                    updateSelection();
                    preferredCol = -1;
                    return true;
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    selectionStart = selectionEnd = nextWordBoundary(selectionStart);
                    selectedAll = false;
                    updateSelection();
                    preferredCol = -1;
                    return true;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (hasRange() || selectedAll) { deleteRange(); preferredCol = -1; return true; }
                    int wordStart = prevWordBoundary(selectionStart);
                    if (wordStart < selectionStart) {
                        pushUndo();
                        content = content.substring(0, wordStart) + content.substring(selectionStart);
                        selectionStart = selectionEnd = wordStart;
                        styledContent = style(content);
                        updateSelection();
                    }
                    preferredCol = -1;
                    return true;
                }
            }
        }

        // Basic keys
        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (hasRange() || selectedAll) { deleteRange(); preferredCol = -1; return true; }
                onInput(cur -> selectionStart > 0 && !cur.isEmpty()
                        ? cur.substring(0, selectionStart - 1) + cur.substring(selectionStart)
                        : cur);
                shiftLeft();
                preferredCol = -1;
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (hasRange() || selectedAll) { deleteRange(); preferredCol = -1; return true; }
                onInput(input -> StringUtils.insertString(content, selectionStart + 1, null));
                preferredCol = -1;
                return true;
            }
            case GLFW.GLFW_KEY_ENTER -> {
                if (hasRange() || selectedAll) deleteRange();
                int ls = lineStart(selectionStart);
                String before = content.substring(ls, selectionStart);
                int spaces = 0;
                while (spaces < before.length() && before.charAt(spaces) == ' ') spaces++;
                String indent = "\n" + " ".repeat(spaces);
                pushUndo();
                content = content.substring(0, selectionStart) + indent + content.substring(selectionStart);
                selectionStart = selectionEnd = selectionStart + indent.length();
                styledContent = style(content);
                updateSelection();
                preferredCol = -1;
                return true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                if (hasRange() || selectedAll) deleteRange();
                onInput(input -> insertInput("    "));
                for (int i = 0; i < 4; i++) shiftRight();
                preferredCol = -1;
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (hasRange() || selectedAll) { selectionStart = selectionEnd = selMin(); selectedAll = false; updateSelection(); }
                else shiftLeft();
                preferredCol = -1;
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (hasRange() || selectedAll) { selectionStart = selectionEnd = selMax(); selectedAll = false; updateSelection(); }
                else shiftRight();
                preferredCol = -1;
                return true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                selectionStart = selectionEnd = lineStart(selectionStart);
                selectedAll = false;
                updateSelection();
                preferredCol = -1;
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                selectionStart = selectionEnd = lineEnd(selectionStart);
                selectedAll = false;
                updateSelection();
                preferredCol = -1;
                return true;
            }
            case GLFW.GLFW_KEY_UP -> { navigateVertical(-1); return true; }
            case GLFW.GLFW_KEY_DOWN -> { navigateVertical(1); return true; }
        }
        return false;
    }

    private void navigateVertical(int dir) {
        selectedAll = false;
        int col = preferredCol >= 0 ? preferredCol : selectionStart - lineStart(selectionStart);
        preferredCol = col;

        String[] lines = content.split("\n", -1);
        int charPos = 0, curLine = 0;
        for (int i = 0; i < lines.length; i++) {
            if (charPos + lines[i].length() >= selectionStart) { curLine = i; break; }
            charPos += lines[i].length() + 1;
        }

        int targetLine = MathUtils.clamp(curLine + dir, 0, lines.length - 1);
        int targetPos = 0;
        for (int i = 0; i < targetLine; i++) targetPos += lines[i].length() + 1;
        targetPos += Math.min(col, lines[targetLine].length());
        selectionStart = selectionEnd = targetPos;
        updateSelection();
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        selectedAll = false;
        if (!isHovered((int) mouseX, (int) mouseY)) return;

        if (button == 0) {
            int pos = pixelToContentPos(mouseX, mouseY);
            selectionStart = selectionEnd = pos;
            dragAnchor = pos;
            preferredCol = -1;
            updateSelection();
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if (button == 0) dragAnchor = -1;
    }

    // Called from IDE's mouseDragListeners for drag-select
    public void onDrag(double mouseX, double mouseY) {
        if (dragAnchor < 0 || !isHovered((int) mouseX, (int) mouseY)) return;
        int pos = pixelToContentPos(mouseX, mouseY);
        selectionEnd = dragAnchor;
        selectionStart = pos;
        selectedAll = false;
        updateSelection();
    }

    private int pixelToContentPos(double mouseX, double mouseY) {
        List<FormattedCharSequence> rows = mc.font.split(FormattedText.of(styledContent), width - 25);
        int targetRow = MathUtils.clamp((int) ((mouseY - (y + textY)) / 9), 0, Math.max(0, rows.size() - 1));

        int pos = 0, row = 0, rowStart = 0;
        while (pos <= content.length() && row < targetRow) {
            int next = pos + 1;
            String sub = content.substring(0, next);
            List<FormattedCharSequence> r = mc.font.split(FormattedText.of(style(sub)), width - 25);
            if (r.size() > row + 1) { row++; rowStart = next; }
            pos = next;
        }

        int best = rowStart;
        double bestDist = Double.MAX_VALUE;
        double targetX = mouseX - (x + 20);

        for (int p = rowStart; p <= content.length(); p++) {
            String sub = content.substring(0, p);
            List<FormattedCharSequence> r = mc.font.split(FormattedText.of(style(sub)), width - 25);
            if (r.size() > targetRow + 1) break;
            double cx = r.isEmpty() ? 0 : mc.font.width(r.get(Math.min(targetRow, r.size() - 1)));
            double dist = Math.abs(cx - targetX);
            if (dist <= bestDist) { bestDist = dist; best = p; }
        }

        return MathUtils.clamp(best, 0, content.length());
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        context.pose().pushMatrix();
        context.enableScissor(x, y, x + width, y + height);

        RenderUtils.fillRect(context, x, y, width, height, backgroundColor.getHex());
        List<FormattedCharSequence> text = mc.font.split(FormattedText.of(styledContent), width - 25);
        textHeight = text.size() * 9;

        // Line number gutter — numbers only, no background fill
        int caret = y + textY;
        int lineCount = Math.max(1, text.size());
        for (int i = 0; i < lineCount; i++) {
            RenderUtils.drawDefaultScaledText(context, Component.literal(String.valueOf(i + 1)), x + 5, caret + 1, 1.0F, false, GUTTER_COLOR);
            caret += 9;
        }

        // Range selection highlight
        if (hasRange()) renderRangeHighlight(context, text);

        // Full-select highlight
        if (selectedAll) {
            caret = y + textY;
            for (FormattedCharSequence line : text) {
                RenderUtils.fillRect(context, x + 20, caret - 1, mc.font.width(line), 9, SEL_COLOR);
                caret += 9;
            }
        }

        // Text
        caret = y + textY;
        for (FormattedCharSequence line : text) {
            context.text(mc.font, line, x + 20, caret, textColor.getHex(), false);
            caret += 9;
        }

        // Cursor
        if (selectionBlinking) {
            RenderUtils.drawVerLine(context, x + 20 + selectedStartPoint.x, y - 1 + textY + selectedStartPoint.y, 9, CURSOR_COLOR);
        }

        context.disableScissor();
        context.pose().popMatrix();
    }

    private void renderRangeHighlight(GuiGraphicsExtractor context, List<FormattedCharSequence> rows) {
        Point startPt = computePointFor(selMin());
        Point endPt   = computePointFor(selMax());
        int startRow  = startPt.y / 9;
        int endRow    = endPt.y / 9;

        for (int row = startRow; row <= endRow; row++) {
            int rx0 = (row == startRow) ? startPt.x : 0;
            int rx1 = (row == endRow) ? endPt.x : (row < rows.size() ? mc.font.width(rows.get(row)) : 0);
            int ry  = y + textY - 1 + row * 9;
            RenderUtils.fillRect(context, x + 20 + rx0, ry, Math.max(1, rx1 - rx0), 9, SEL_COLOR);
        }
    }

    private Point computePointFor(int pos) {
        String sub = content.substring(0, MathUtils.clamp(pos, 0, content.length()));
        List<FormattedCharSequence> lines = mc.font.split(FormattedText.of(style(sub)), width - 25);
        if (lines == null || lines.isEmpty()) return new Point(0, 0);
        return new Point(mc.font.width(lines.get(lines.size() - 1)), (lines.size() - 1) * 9);
    }

    @Override
    public void onTick() {
        super.onTick();
        if (mc.screen instanceof GuiScreen screen) {
            if (screen.selected != this) { selectionBlinking = false; return; }
            if (selectionBlink++ >= 20) selectionBlink = 0;
            if (selectionBlink % 10 == 0 && selectionBlink > 0) selectionBlinking = !selectionBlinking;
        }
    }

    @Override
    public void onInput(Function<String, String> factory) {
        if (selectedAll) { content = styledContent = ""; selectedAll = false; resetSelection(); }
        pushUndo();
        content = factory.apply(content);
        updateSelection();
        this.styledContent = style(content);
        if (onContentChanged != null) onContentChanged.run();
    }

    // ── Cursor movement ───────────────────────────────────────────────────────

    public void shiftRight() {
        selectionStart = MathUtils.clamp(selectionStart + 1, 0, content.length());
        selectionEnd = selectionStart;
        updateSelection();
    }

    public void shiftLeft() {
        selectionStart = MathUtils.clamp(selectionStart - 1, 0, content.length());
        selectionEnd = selectionStart;
        updateSelection();
    }

    public void shiftStart() {
        selectionStart = selectionEnd = 0;
        textY = 5;
        updateSelection();
    }

    public void shiftEnd() {
        selectionStart = selectionEnd = content.length();
        textY = 5 - textHeight;
        updateSelection();
    }

    public String insertInput(String input) {
        return StringUtils.insertString(content, selectionStart, input);
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, int amount) {
        super.mouseScrolled(mouseX, mouseY, amount);
        for (int i = 0; i < ScrollPanelElement.SCROLL_MULTIPLIER; i++) {
            textY = MathUtils.clamp(textY + amount, 5 - textHeight, 5);
        }
    }

    public String style(String s) {
        if (s == null || s.isEmpty()) return " ";
        this.updateSelection();
        return highlighter.highlightText(s);
    }

    public void resetSelection() {
        selectionStart = selectionEnd = 0;
        updateSelection();
    }

    public void updateSelection() {
        String str = content.substring(0, MathUtils.clamp(selectionStart, 0, content.length()));
        List<FormattedCharSequence> lines = mc.font.split(FormattedText.of(str), width - 25);
        if (lines == null || lines.isEmpty()) { selectedStartPoint.setLocation(0, 0); return; }
        selectedStartPoint.x = mc.font.width(lines.get(Math.max(0, lines.size() - 1)));
        selectedStartPoint.y = lines.size() * 9 - 9;
    }

    // ── Accessors for IDE ─────────────────────────────────────────────────────

    public String getWordBeforeCursor() {
        int start = selectionStart;
        while (start > 0 && content.charAt(start - 1) != '\n' && !Character.isWhitespace(content.charAt(start - 1))) start--;
        return content.substring(start, selectionStart);
    }

    public String getCurrentLine() {
        return content.substring(lineStart(selectionStart), lineEnd(selectionStart));
    }

    public int getCursorColInLine() {
        return selectionStart - lineStart(selectionStart);
    }

    public int getCursorPixelX() { return x + 20 + selectedStartPoint.x; }
    public int getCursorPixelY() { return y + textY + selectedStartPoint.y; }

    public void insertCompletion(String completion) {
        int wordStart = selectionStart;
        while (wordStart > 0 && content.charAt(wordStart - 1) != '\n' && !Character.isWhitespace(content.charAt(wordStart - 1))) wordStart--;
        pushUndo();
        content = content.substring(0, wordStart) + completion + content.substring(selectionStart);
        selectionStart = selectionEnd = wordStart + completion.length();
        styledContent = style(content);
        updateSelection();
    }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public String[] getLines() { return content.lines().toArray(String[]::new); }
    public String getContent() { return content; }

    public ChatColor getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(ChatColor c) { this.backgroundColor = c; }

    public ChatColor getTextColor() { return textColor; }
    public void setTextColor(ChatColor c) { this.textColor = c; }

    public TextHighlighter getHighlighter() { return highlighter; }
    public void setHighlighter(TextHighlighter h) { this.highlighter = h; }

    public void setKeyInterceptor(Function<Integer, Boolean> interceptor) {
        this.keyInterceptor = interceptor;
    }

    public void setOnContentChanged(Runnable callback) {
        this.onContentChanged = callback;
    }

    public void clear() {
        content = styledContent = "";
        undoStack.clear();
        dragAnchor = -1;
        preferredCol = -1;
        resetSelection();
    }

    // ── TextHighlighter ───────────────────────────────────────────────────────

    public static class TextHighlighter {
        private List<HighlightFactory> stringFactories = new ArrayList<>();
        private ChatColor originalColor;

        public TextHighlighter(ChatColor originalColor) { this.originalColor = originalColor; }
        public TextHighlighter() { this(ChatColor.WHITE); }

        public String highlightText(String text) {
            String[] lines = text.lines().toArray(String[]::new);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                String[] words = lines[i].split(" ");
                for (int j = 0; j < words.length; j++) {
                    String word = words[j];
                    if (word.isEmpty()) { result.append(" "); continue; }
                    String r = word;
                    for (HighlightFactory factory : stringFactories) {
                        Pair<String, Boolean> product = factory.process(r);
                        if (product.right) { r = product.left; break; }
                    }
                    result.append(r).append(j < words.length - 1 ? " " : "");
                }
                if (i < lines.length - 1) result.append("\n");
            }
            return result.toString();
        }

        private HighlightFactory colorStringFactory(ChatColor color, String str) {
            return new HighlightFactory(s -> s.replace("\n", "").equals(str), s -> "%s%s%s".formatted(color, s, originalColor));
        }

        private HighlightFactory predicateStringFactory(ChatColor color, Predicate<String> predicate) {
            return new HighlightFactory(predicate, s -> "%s%s%s".formatted(color, s, originalColor));
        }

        public TextHighlighter put(ChatColor color, String... keys) {
            for (String key : keys) if (key != null && !key.isEmpty()) stringFactories.add(colorStringFactory(color, key));
            return this;
        }

        public TextHighlighter put(ChatColor color, Iterable<String> keys) {
            for (String key : keys) if (key != null && !key.isEmpty()) stringFactories.add(colorStringFactory(color, key));
            return this;
        }

        public TextHighlighter put(ChatColor color, Predicate<String> predicate) {
            stringFactories.add(predicateStringFactory(color, predicate));
            return this;
        }

        public TextHighlighter put(Predicate<String> predicate, Function<String, String> factory) {
            stringFactories.add(new HighlightFactory(predicate, factory));
            return this;
        }

        public TextHighlighter setStringFactory(List<HighlightFactory> factories) { this.stringFactories = factories; return this; }
        public void clearFactories() { stringFactories.clear(); }
        public ChatColor getOriginalColor() { return originalColor; }
        public void setOriginalColor(ChatColor c) { this.originalColor = c; }

        public record HighlightFactory(Predicate<String> predicate, Function<String, String> factory) {
            public Pair<String, Boolean> process(String str) {
                if (predicate.test(str)) return Pair.of(factory.apply(str), true);
                return Pair.of(str, false);
            }
        }
    }
}
