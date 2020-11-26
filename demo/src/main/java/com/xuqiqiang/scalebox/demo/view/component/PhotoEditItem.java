package com.xuqiqiang.scalebox.demo.view.component;

/**
 * Created by xuqiqiang on 2020/07/07.
 */
public class PhotoEditItem {

    private int icon;
    private String text;
    private boolean isOn;
    private boolean disabled;

    public PhotoEditItem(String text) {
        this.text = text;
    }

    public PhotoEditItem(int icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public PhotoEditItem(int icon, String text, boolean isOn) {
        this.icon = icon;
        this.text = text;
        this.isOn = isOn;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}