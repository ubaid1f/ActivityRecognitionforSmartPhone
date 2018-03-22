package com.example.euguzns.arforphone;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ubaidurrehman on 05/02/2018.
 */

public class restclient {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("time")
    private String time;
    @JsonProperty("senID")
    private String senID;
    @JsonProperty("activity")
    private String activity;
    @JsonProperty("name")
    private String name;
    @JsonProperty("AndroidTimeStamp")
    private String AndroidTimeStamp;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }


    public void setTime(String time) {
        this.time = time;

    }

    public String getSenID() {
        return senID;
    }

    public void setSenID(String senID) {
        this.senID = senID;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAndroidTimeStamp() {
        return AndroidTimeStamp;

    }

    public void setAndroidTimeStamp(String AndroidTimeStamp) {
        this.AndroidTimeStamp = AndroidTimeStamp;
    }




}
