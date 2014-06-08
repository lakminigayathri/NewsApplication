package com.gaya.newsapplication.news;

/**
 * This is the News class. All the properties related to an News item should
 * have getters and setters.
 * 
 * @author Gaya
 * 
 */
public class News {

    private String headLine;
    private String slugLine;
    private String thumbnailImageHref;
    private Long   dateTime;
    private String tinyUrl;

    public String getHeadLine() {
        return headLine;
    }

    public void setHeadLine(String headLine) {
        this.headLine = headLine;
    }

    public String getSlugLine() {
        return slugLine;
    }

    public void setSlugLine(String slugLine) {
        this.slugLine = slugLine;
    }

    public String getThumbnailImageHref() {
        return thumbnailImageHref;
    }

    public void setThumbnailImageHref(String thumbnailImageHref) {
        this.thumbnailImageHref = thumbnailImageHref;
    }

    public Long getDateTime() {
        return dateTime;
    }

    public void setDateTime(Long dateTime) {
        this.dateTime = dateTime;
    }

    public String getTinyUrl() {
        return tinyUrl;
    }

    public void setTinyUrl(String tinyUrl) {
        this.tinyUrl = tinyUrl;
    }

}
