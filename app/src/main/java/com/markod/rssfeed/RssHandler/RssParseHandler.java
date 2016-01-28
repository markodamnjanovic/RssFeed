package com.markod.rssfeed.rssHandler;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class RssParseHandler extends DefaultHandler {

    private List<RssItem> rssItems;
    private String channelImageUrl = null;
    private String channelTitle;
    private RssItem currentItem;

    private Boolean parsingTitle = false;
    private Boolean parsingPubDate;
    private Boolean parsingImageUrl = false;
    private Boolean parsingLink;
    private Boolean parsingDescription;


    public RssParseHandler() {
        rssItems = new ArrayList<>();
    }

    public List<RssItem> getRssItems() {
        return rssItems;
    }
    public StringBuilder stringBuilder;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "url":
                parsingImageUrl = true; //url of channel, not url of item
            case "item":
                currentItem = new RssItem();
                currentItem.setChannelImageUrl(channelImageUrl);
                currentItem.setChannelTitle(channelTitle);
            case "title":
                parsingTitle = true;
            case "link":
                parsingLink = true;
            case "pubDate":
                parsingPubDate = true;
            case "description":
                stringBuilder = new StringBuilder();
                parsingDescription = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "item":
                rssItems.add(currentItem);
                currentItem = null;
            case "title":
                parsingTitle = false;
            case "link":
                parsingLink = false;
            case "pubDate":
                parsingPubDate = false;
            case "description":
                parsingDescription = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (parsingImageUrl && channelImageUrl == null) {
            channelImageUrl = new String(ch, start, length);
        }
        if (parsingTitle && channelTitle == null) {
            channelTitle = new String(ch, start, length);
        }


        if (currentItem != null) {
            if (parsingTitle) {
                currentItem.setTitle(new String(ch, start, length));
            } else if (parsingLink) {
                currentItem.setLink(new String(ch, start, length));
            } else if (parsingPubDate) {
                currentItem.setPubDate(new String(ch, start, 25));
            } else if (parsingDescription) {
                for (int i=start; i<start+length; i++) {
                    stringBuilder.append(ch[i]);
                }
                currentItem.setDescription(stringBuilder.toString());
            }
        }


    }
}
