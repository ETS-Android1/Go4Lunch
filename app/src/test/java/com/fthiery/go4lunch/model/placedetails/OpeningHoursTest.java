package com.fthiery.go4lunch.model.placedetails;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;

public class OpeningHoursTest {

    @Test
    public void checkIsOpenAt() {
        // GIVEN
        Period period = new Period(new Moment(WeekDay.SUNDAY,"1400"), new Moment(WeekDay.WEDNESDAY,"1730"));
        // WHEN
        Calendar time1 = new Calendar.Builder().setDate(2021,10,16).setTimeOfDay(14,27,35).build();
        Calendar time2 = new Calendar.Builder().setDate(2021,10,16).setTimeOfDay(17,33,15).build();
        Calendar time3 = new Calendar.Builder().setDate(2021,10,17).setTimeOfDay(14,45,26).build();
        Calendar time4 = new Calendar.Builder().setDate(2021,10,14).setTimeOfDay(13,45,26).build();
        Calendar time5 = new Calendar.Builder().setDate(2021,10,17).setTimeOfDay(18,27,35).build();
        // THEN
        assertTrue(period.isOpenAt(time1));
        assertTrue(period.isOpenAt(time2));
        assertTrue(period.isOpenAt(time3));
        assertFalse(period.isOpenAt(time4));
        assertFalse(period.isOpenAt(time5));
    }

    @Test
    public void checkMomentGetDayTime() {
        // GIVEN
        Moment moment1 = new Moment(WeekDay.TUESDAY,"1427");
        Moment moment2 = new Moment(new Calendar.Builder().setDate(2021,10,24).setTimeOfDay(15,30,35).build());
        // WHEN
        int i1 = moment1.toInt();
        int i2 = moment2.toInt();
        // THEN
        assertEquals(i1,21427);
        assertEquals(i2,31530);
    }

    @Test
    public void checkMomentPreviousNextTime() {
        // GIVEN
        Moment moment1 = new Moment(WeekDay.WEDNESDAY,"1427");
        Moment moment2 = new Moment(WeekDay.SATURDAY,"1800");
        Moment moment3 = new Moment(WeekDay.SUNDAY,"700");
        // WHEN
        Calendar cal = new Calendar.Builder().setDate(2021,10,28).setTimeOfDay(13,30,35).build();
        // THEN
        assertEquals(
                moment1.getPreviousTime(cal),
                new Calendar.Builder().setDate(2021,10,24).setTimeOfDay(14,27,0).build()
        );
        assertEquals(
                moment1.getNextTime(cal),
                new Calendar.Builder().setDate(2021,11,1).setTimeOfDay(14,27,0).build()
        );
        assertEquals(
                moment2.getPreviousTime(cal),
                new Calendar.Builder().setDate(2021,10,27).setTimeOfDay(18,0,0).build()
        );
        assertEquals(
                moment2.getNextTime(cal),
                new Calendar.Builder().setDate(2021,11,4).setTimeOfDay(18,0,0).build()
        );
        assertEquals(
                moment3.getPreviousTime(cal),
                new Calendar.Builder().setDate(2021,10,28).setTimeOfDay(7,0,0).build()
        );
        assertEquals(
                moment3.getNextTime(cal),
                new Calendar.Builder().setDate(2021,11,5).setTimeOfDay(7,0,0).build()
        );
    }

    @Test
    public void checkNextTime() {
        // GIVEN
        OpeningHours openingHours = new OpeningHours();
        openingHours.setPeriods(Arrays.asList(
                new Period(new Moment(WeekDay.TUESDAY,"1400"), new Moment(WeekDay.TUESDAY,"1730")),
                new Period(new Moment(WeekDay.WEDNESDAY,"1200"), new Moment(WeekDay.WEDNESDAY,"1830")),
                new Period(new Moment(WeekDay.SATURDAY,"1500"), new Moment(WeekDay.MONDAY,"2000"))
                ));
        // WHEN
        Calendar time1 = openingHours.nextTime(new Calendar.Builder().setDate(2021,10,23).setTimeOfDay(14,27,35).build());
        Calendar time2 = openingHours.nextTime(new Calendar.Builder().setDate(2021,10,24).setTimeOfDay(19,27,35).build());
        Calendar time3 = openingHours.nextTime(new Calendar.Builder().setDate(2021,10,28).setTimeOfDay(21,27,35).build());
        // THEN
        assertEquals(
                time1,
                new Calendar.Builder().setDate(2021,10,23).setTimeOfDay(17,30,0).build());
        assertEquals(
                time2,
                new Calendar.Builder().setDate(2021,10,27).setTimeOfDay(15,0,0).build());
        assertEquals(
                time3,
                new Calendar.Builder().setDate(2021,10,29).setTimeOfDay(20,0,0).build());
    }
}