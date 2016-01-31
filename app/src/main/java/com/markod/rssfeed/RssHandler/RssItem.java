package com.markod.rssfeed.rssHandler;

public class RssItem {
    String title;
    String pubDate;
    String link;
    String channelImageUrl;
    String channelTitle;
    String description;
    int favorite;

    public RssItem(String title, String pubDate, String link, String channelTitle, String description) {
        this.title = title;
        this.pubDate = pubDate;
        this.link = link;
        this.channelTitle = channelTitle;
        this.description = description;
        this.channelImageUrl = null;
    }

    public RssItem(){

    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public void setPubDate(String link) {
        this.pubDate = link;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) { this.link = link; }

    public void setChannelImageUrl(String channelImageUrl) { this.channelImageUrl = channelImageUrl; }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFavorite() {
        return favorite;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getChannelImageUrl() {
        return channelImageUrl;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return channelTitle;
    }
}
