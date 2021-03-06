/*
 *  Class Name: HabitEvent
 *  Version: 1.0
 *  Date: December 6th, 2017
 *  Copyright (c) TEAM SSMAD, CMPUT 301, University of Alberta - All Rights Reserved.
 *  You may use, distribute, or modify this code under terms and conditions of the
 *  Code of Students Behaviour at University of Alberta
 *
 */

package ssmad.habitizer;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a Habit Event object
 * @author Sadman
 * @version 1.0
 * @see Habit
 * @since 0.5
 */
public class HabitEvent {
    private String title;
    private Date completionDate;

    private String habit_id;
    private String pic_id;

    /**
     * Gets picture id
     * @return
     */
    public String getPic_id() {
        return pic_id;
    }

    /**
     * Sets picture id
     * @param pic_id
     */
    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    private String id;
    private byte[] pic;
    private double[] location;

    private String comment;

    private boolean hasPic;
    private boolean hasLoc;
    private String username;

    public HabitEvent() {

    }

    /**
     * Boolean for if event has picture
     * @return
     */
    public boolean hasPicture() {
        return hasPic;
    }

    /**
     * Boolean for if event has location tagged
     * @return
     */
    public boolean hasLocation() {
        return hasLoc;
    }

    /**
     * Constructor for habti event object
     * @param title
     * @param completionDate
     * @param pic
     * @param location
     * @param comment
     */
    public HabitEvent(String title, Date completionDate, byte[] pic, double[] location, String
            comment) {
        this.title = title;
        this.completionDate = completionDate;
        if (pic == null) {
            this.hasPic = Boolean.FALSE;
        } else {
            this.hasPic = Boolean.TRUE;
            this.pic = pic;
        }
        if (location == null) {
            this.hasLoc = Boolean.FALSE;
        } else {
            this.hasLoc = Boolean.TRUE;
            this.location = location;
        }
        if (comment == null) {
            this.comment = "";
        } else {
            this.comment = comment;
        }

    }

    /**
     * Gets Json string
     * @return
     */
    public String getJsonString() {
        JsonObject j = new JsonObject();
        j.addProperty("habitid", this.getHabit_id());
        j.addProperty("title", this.getTitle());
        j.addProperty("comment", this.getComment());
        if (this.hasPicture()) {
            JsonArray picarr = new JsonArray();
            for (int i = 0; i < this.pic.length; i++) {
                picarr.add(this.pic[i]);
            }
            j.add("pic", picarr);
            //j.addProperty("picId", this.getPic_id());
        }
        if (this.hasLocation()) {
            j.addProperty("lat", this.getLocation()[0]);
            j.addProperty("lng", this.getLocation()[1]);
        }
        j.addProperty("hasPic", this.hasPicture());
        j.addProperty("hasLoc", this.hasLocation());
        j.addProperty("username", this.getUsername());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(this.getCompletionDate());
        j.addProperty("completionDate", dateStr);

        Gson g = new Gson();
        String s = g.toJson(j);
        Log.d("Event.Json", s);
        return s;
    }

    /**
     * Sets event according to json job passed in
     * @param job
     * @throws ParseException
     */
    public void fromJsonObject(JsonObject job) throws ParseException {
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        String s = g.toJson(job);
        Log.d("HABIT.json", s);
        JsonObject j = job.get("_source").getAsJsonObject();
        this.setHabit_id(j.get("habitid").getAsString());
        this.setComment(j.get("comment").getAsString());
        this.setTitle(j.get("title").getAsString());
        this.setUsername(j.get("username").getAsString());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = sdf.parse(j.get("completionDate").getAsString());
        this.setCompletionDate(d);


        this.setId(job.get("_id").getAsString());
        this.hasPic = j.get("hasPic").getAsBoolean();
        this.hasLoc = j.get("hasLoc").getAsBoolean();
        if (this.hasPicture()) {
            JsonArray picarr = j.get("pic").getAsJsonArray();
            byte[] b = new byte[picarr.size()];
            for (int i = 0; i < b.length; i++) {
                b[i] = picarr.get(i).getAsByte();
            }
            this.setPicBytes(b);
        }
        if (this.hasLocation()) {
            double[] loc = {j.get("lat").getAsDouble(), j.get("lng").getAsDouble()};
            this.setLocation(loc);
        }
    }

    /**
     * Getter for title
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter for title
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for completion date
     * @return
     */
    public Date getCompletionDate() {
        return completionDate;
    }

    /**
     * Setter for completion date
     * @param completionDate
     */
    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    /**
     * Getter for picture bytes
     * @return
     */
    public byte[] getPicBytes() {
        return pic;
    }

    /**
     * Setter for picture bytes
     * @param pic
     */
    public void setPicBytes(byte[] pic) {
        if(pic == null){
            this.hasPic = false;
        }else{
            this.hasPic = true;
        }
        this.pic = pic;
    }


    /**
     * Getter for location of event
     * @return
     */
    public double[] getLocation() {
        return location;
    }

    /**
     * Getter for ID of habit
     * @return
     */
    public String getHabit_id() {
        return habit_id;
    }

    /**
     * Setter for ID of habit
     * @param habit_id
     */
    public void setHabit_id(String habit_id) {
        this.habit_id = habit_id;
    }

    /**
     * Setter location for habit
     * @param location
     */
    public void setLocation(double[] location) {
        if(location == null){
            this.hasLoc = false;
        }else{
            this.hasLoc = true;
        }
        this.location = location;
    }

    /**
     * Getter for comment
     * @return
     */
    public String getComment() {
        return comment;
    }

    /**
     * Setter for comment
     * @param comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Getter for ID
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for ID
     * @param id
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Getter for username
     * @return
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Setter for username
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets Json string for picutre
     * @return
     */
    public String getPictureJsonString() {
        JsonObject j = new JsonObject();
        j.addProperty("pic", this.getPicBytes().toString());


        Gson g = new Gson();
        String s = g.toJson(j);
        return s;
    }

    /**
     * Sets picture from Json job
     * @param job
     */
    public void setPicFromJsonObject(JsonObject job){
        JsonObject j = job.get("_source").getAsJsonObject();
        this.setPicBytes(j.get("pic").getAsString().getBytes());
    }
}
