package io.github.itzispyder.clickcrystals.modules.settings;

import io.github.itzispyder.clickcrystals.modules.ModuleSetting;

import java.util.function.Supplier;

public abstract class SettingBuilder<T, B extends SettingBuilder<T, B, S>, S extends ModuleSetting<T>> {

    protected SettingChangeCallback<S> changeAction;
    protected Supplier<Boolean> visibility;
    protected String name, description;
    protected T def, val;

    public SettingBuilder() {
        name = description = "";
        def = val = null;
        changeAction = setting -> {};
        visibility = () -> true;
    }

    protected <T> T getOrDef(T val, T def) {
        return val != null ? val : def;
    }

    public B name(String name) {
        this.name = name;
        return (B)this;
    }

    public B description(String description) {
        this.description = description;
        return (B)this;
    }

    public B def(T def) {
        this.def = def;
        return (B)this;
    }

    public B val(T val) {
        this.val = val;
        return (B)this;
    }

    public B onSettingChange(SettingChangeCallback<S> changeAction) {
        this.changeAction = changeAction;
        return (B)this;
    }

    // Only show this setting in GUIs while the condition holds.
    public B visibleWhen(Supplier<Boolean> visibility) {
        this.visibility = visibility;
        return (B)this;
    }

    protected abstract S buildSetting();

    public final S build() {
        S setting = buildSetting();
        setting.setChangeAction((SettingChangeCallback<ModuleSetting<T>>)changeAction);
        setting.setVisibility(visibility);
        return setting;
    }
}
