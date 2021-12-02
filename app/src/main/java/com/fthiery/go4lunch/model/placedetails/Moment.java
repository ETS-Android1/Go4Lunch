
package com.fthiery.go4lunch.model.placedetails;

import com.google.firebase.firestore.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Moment {

    private Integer day;
    private String time;

    public Moment() {}

    public Moment(Integer day, String time) {
        this.day = day;
        this.time = time;
    }

    public Moment(Calendar calendar) {
        this.day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (day == 0) day += 7;
        this.time = new SimpleDateFormat("HHmm", Locale.getDefault()).format(calendar.getTime());
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Exclude public int getHour() {
        return Integer.parseInt(this.time) / 100;
    }

    @Exclude public int getMinute() {
        return Integer.parseInt(this.time) - getHour() * 100;
    }

    @Exclude public int toInt() {
        int dayTime = day * 10000 + Integer.parseInt(time);
        return dayTime;
    }

    @Exclude public Calendar getNextTime(Calendar time) {
        Calendar nextTime = getPreviousTime(time);
        nextTime.add(Calendar.DAY_OF_YEAR,7);
        return nextTime;
    }

    @Exclude public Calendar getPreviousTime(Calendar time) {
        Calendar previousTime = (Calendar) time.clone();

        int day = getDay() - 6;
        previousTime.set(Calendar.DAY_OF_WEEK, day);
        //if (day >= 0) previousTime.add(Calendar.DAY_OF_YEAR,-7);

        previousTime.set(Calendar.HOUR_OF_DAY, getHour());
        previousTime.set(Calendar.MINUTE, getMinute());
        previousTime.set(Calendar.SECOND, 0);

        previousTime.getTime();
        if (previousTime.after(time)) previousTime.add(Calendar.DAY_OF_YEAR, -7);

        return previousTime;
    }
}
