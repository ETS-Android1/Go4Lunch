
package com.fthiery.go4lunch.model.placedetails;

import com.google.firebase.firestore.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

public class Period {

    @SerializedName("close")
    @Expose
    private Moment close;
    @SerializedName("open")
    @Expose
    private Moment open;

    public Period() {
    }

    public Period(Moment open, Moment close) {
        this.open = open;
        this.close = close;
    }

    public Moment getClose() {
        return close;
    }

    public void setClose(Moment close) {
        this.close = close;
    }

    public Moment getOpen() {
        return open;
    }

    public void setOpen(Moment open) {
        this.open = open;
    }

    @Exclude
    public boolean isOpenAt(Calendar time) {
        if (open != null && close != null) {
            int i = new Moment(time).toInt();
            int openInt = open.toInt();
            int closeInt = close.toInt();
            if (openInt > closeInt) {
                openInt -= 70000;
                if (i > closeInt) i -= 70000;
            }
            return openInt <= i && closeInt > i;
        } else return false;
    }

    @Exclude
    public Calendar getNextOpenTime(Calendar time) {
        return open.getNextTime(time);
    }

    @Exclude
    public Calendar getPreviousOpenTime(Calendar time) {
        return open.getPreviousTime(time);
    }

    @Exclude
    public Calendar getNextCloseTime(Calendar time) {
        return close.getNextTime(time);
    }

    @Exclude
    public Calendar getPreviousCloseTime(Calendar time) {
        return close.getPreviousTime(time);
    }
}
