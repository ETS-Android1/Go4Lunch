
package com.fthiery.go4lunch.model.placedetails;

import com.google.firebase.firestore.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.List;

public class OpeningHours {

    @SerializedName("periods")
    @Expose
    private List<Period> periods = null;

    public List<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    @Exclude public boolean isOpenAt(Calendar time) {
        for (Period period : periods) {
            if (period.isOpenAt(time)) return true;
        }
        return false;
    }

    @Exclude public Calendar nextTime(Calendar time) {
        Calendar next = (Calendar) time.clone();
        next.add(Calendar.YEAR,10);

        for (Period period : periods) {
            if (period.isOpenAt(time)) return period.getNextCloseTime(time);
            else {
                if (next.after(period.getNextOpenTime(time))) next = period.getNextOpenTime(time);
            }
        }

        return next;
    }
}
