package edu.cwu.catmap;

import android.graphics.Color;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class RepeatingEvent extends Event {
    private final ArrayList<OneTimeEvent> oneTimeEvents;
    private boolean scheduleOnHoliday, deleteAtQuarterEnd;
    private LocalDate startDate, endDate;
    private ArrayList<DayOfWeek> repeatDays;

    public RepeatingEvent(String name, String room, Location building, int color, LocalDateTime startTime, LocalDateTime endTime, boolean scheduleOnHoliday, boolean deleteAtQuarterEnd, LocalDate startDate, LocalDate endDate, ArrayList<DayOfWeek> repeatDays) {
        super(name, room, building, color, startTime, endTime);
        this.scheduleOnHoliday = scheduleOnHoliday;
        this.deleteAtQuarterEnd = deleteAtQuarterEnd;
        this.startDate = startDate;
        this.endDate = endDate;
        this.repeatDays = repeatDays;
        oneTimeEvents = new ArrayList<>();

        generateEvents();
    }

    public void generateEvents() {
        //generate a one time event for each repeat day that this event will repeat on
    }

    public boolean isScheduleOnHoliday() {
        return scheduleOnHoliday;
    }

    public void setScheduleOnHoliday(boolean scheduleOnHoliday) {
        this.scheduleOnHoliday = scheduleOnHoliday;
    }

    public boolean isDeleteAtQuarterEnd() {
        return deleteAtQuarterEnd;
    }

    public void setDeleteAtQuarterEnd(boolean deleteAtQuarterEnd) {
        this.deleteAtQuarterEnd = deleteAtQuarterEnd;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ArrayList<DayOfWeek> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(ArrayList<DayOfWeek> repeatDays) {
        this.repeatDays = repeatDays;
    }

    public ArrayList<OneTimeEvent> getOneTimeEvents() {
        return oneTimeEvents;
    }
}
