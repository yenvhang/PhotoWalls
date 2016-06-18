package com.example.eggache.photowalls.Model;


import java.sql.Timestamp;

/**
 * Created by eggache on 2016/5/16.
 */
public class AlbumModel {
    private String name;
    private String createby;
    private String description;
    private String url;
    private Timestamp timestamp;






    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateby() {
        return createby;
    }

    public void setCreateby(String createby) {
        this.createby = createby;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return name;
    }


}
