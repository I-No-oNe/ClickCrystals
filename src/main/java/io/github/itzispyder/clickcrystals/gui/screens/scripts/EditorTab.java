package io.github.itzispyder.clickcrystals.gui.screens.scripts;

import java.io.File;

public class EditorTab {

    public final String filepath;
    public final String filename;
    public int cursorPos;
    public int scrollY;

    public EditorTab(File file) {
        this.filepath = file.getPath();
        this.filename = file.getName();
        this.cursorPos = 0;
        this.scrollY = 5;
    }
}
