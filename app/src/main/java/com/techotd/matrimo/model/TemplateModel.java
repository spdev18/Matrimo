package com.techotd.matrimo.model;

public class TemplateModel {
    private String name;
    private int previewResId;
    private int layoutId;

    public TemplateModel(String name, int previewResId, int layoutId) {
        this.name = name;
        this.previewResId = previewResId;
        this.layoutId = layoutId;
    }

    public String getName() {
        return name;
    }

    public int getPreviewResId() {
        return previewResId;
    }

    public int getLayoutId() {
        return layoutId;
    }
}