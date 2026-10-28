package com.criadorslides.dto;

public class SlidePageRequest {
    private Integer pageNumber;
    private String layoutType;
    private String customTitle;
    private String customNotes;
    private String photoBase64;
    private String photoCaption;

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    public String getLayoutType() { return layoutType; }
    public void setLayoutType(String layoutType) { this.layoutType = layoutType; }
    public String getCustomTitle() { return customTitle; }
    public void setCustomTitle(String customTitle) { this.customTitle = customTitle; }
    public String getCustomNotes() { return customNotes; }
    public void setCustomNotes(String customNotes) { this.customNotes = customNotes; }
    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }
    public String getPhotoCaption() { return photoCaption; }
    public void setPhotoCaption(String photoCaption) { this.photoCaption = photoCaption; }
}
