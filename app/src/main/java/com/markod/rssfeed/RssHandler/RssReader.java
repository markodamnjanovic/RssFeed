package com.markod.rssfeed.rssHandler;


import android.util.Log;

import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class RssReader {

    private String rssUrl;

    public RssReader(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    public List<RssItem> getItems() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            RssParseHandler parseHandler = new RssParseHandler();
            saxParser.parse(rssUrl, parseHandler);
            return parseHandler.getRssItems();
        } catch (Exception e) {
            Log.d("exception", "Exception thrown in SAX parser: " + e.getMessage());
            return null;
        }
    }
}
