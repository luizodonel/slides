package com.criadorslides.dto;

import java.util.List;

public class SlidePageDto {
    private Integer pageNumber;
    private String layoutType;
    private String badge;
    private String title;
    private String subtitle;
    private String bodyText;
    private List<String> bullets;
    private List<ColumnItemDto> columns;
    private List<StatItemDto> stats;
    private String highlightQuote;
    private String photoBase64;
    private String photoCaption;

    public static class ColumnItemDto {
        private String number;
        private String title;
        private String description;

        public ColumnItemDto() {
        }

        public ColumnItemDto(String number, String title, String description) {
            this.number = number;
            this.title = title;
            this.description = description;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class StatItemDto {
        private String value;
        private String label;
        private String description;

        public StatItemDto() {
        }

        public StatItemDto(String value, String label, String description) {
            this.value = value;
            this.label = label;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public List<String> getBullets() {
        return bullets;
    }

    public void setBullets(List<String> bullets) {
        this.bullets = bullets;
    }

    public List<ColumnItemDto> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnItemDto> columns) {
        this.columns = columns;
    }

    public List<StatItemDto> getStats() {
        return stats;
    }

    public void setStats(List<StatItemDto> stats) {
        this.stats = stats;
    }

    public String getHighlightQuote() {
        return highlightQuote;
    }

    public void setHighlightQuote(String highlightQuote) {
        this.highlightQuote = highlightQuote;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    public String getPhotoCaption() {
        return photoCaption;
    }

    public void setPhotoCaption(String photoCaption) {
        this.photoCaption = photoCaption;
    }
}
