package com.criadorslides.dto;

import java.util.List;

public class PresentationResponse {
    private Long id;
    private String presentationTitle;
    private String idea;
    private String subjectTheme;
    private String templateStyle;
    private String primaryColor;
    private String secondaryColor;
    private Boolean autoColors;
    private Integer totalPages;
    private String createdAt;
    private List<SlidePageDto> slides;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPresentationTitle() { return presentationTitle; }
    public void setPresentationTitle(String presentationTitle) { this.presentationTitle = presentationTitle; }
    public String getIdea() { return idea; }
    public void setIdea(String idea) { this.idea = idea; }
    public String getSubjectTheme() { return subjectTheme; }
    public void setSubjectTheme(String subjectTheme) { this.subjectTheme = subjectTheme; }
    public String getTemplateStyle() { return templateStyle; }
    public void setTemplateStyle(String templateStyle) { this.templateStyle = templateStyle; }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
    public Boolean getAutoColors() { return autoColors; }
    public void setAutoColors(Boolean autoColors) { this.autoColors = autoColors; }
    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public List<SlidePageDto> getSlides() { return slides; }
    public void setSlides(List<SlidePageDto> slides) { this.slides = slides; }
}
