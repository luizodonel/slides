package com.criadorslides.dto;

import java.util.List;

public class GeneratePresentationRequest {
    private String idea;
    private String subjectTheme;
    private String templateStyle;
    private Integer totalPages;
    private Boolean autoColors;
    private String primaryColor;
    private String secondaryColor;
    private List<SlidePageRequest> pages;

    public String getIdea() { return idea; }
    public void setIdea(String idea) { this.idea = idea; }
    public String getSubjectTheme() { return subjectTheme; }
    public void setSubjectTheme(String subjectTheme) { this.subjectTheme = subjectTheme; }
    public String getTemplateStyle() { return templateStyle; }
    public void setTemplateStyle(String templateStyle) { this.templateStyle = templateStyle; }
    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    public Boolean getAutoColors() { return autoColors; }
    public void setAutoColors(Boolean autoColors) { this.autoColors = autoColors; }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
    public List<SlidePageRequest> getPages() { return pages; }
    public void setPages(List<SlidePageRequest> pages) { this.pages = pages; }
}
