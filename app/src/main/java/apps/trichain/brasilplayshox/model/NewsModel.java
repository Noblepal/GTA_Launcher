package apps.trichain.brasilplayshox.model;

import java.io.Serializable;

public class NewsModel implements Serializable {
    private String newsID;
    private String title;
    private String date;
    private String url;

    public NewsModel() {
    }

    public NewsModel(String newsID, String title, String date, String url) {
        this.newsID = newsID;
        this.title = title;
        this.date = date;
        this.url = url;
    }

    public String getNewsID() {
        return newsID;
    }

    public void setNewsID(String newsID) {
        this.newsID = newsID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
