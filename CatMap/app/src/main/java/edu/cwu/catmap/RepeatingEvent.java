package edu.cwu.catmap;

import android.graphics.Color;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class RepeatingEvent extends Event {
    private ArrayList<OneTimeEvent> oneTimeEvents;
    private boolean scheduleOnHoliday, deleteAtQuarterEnd;
    private LocalDate startDate, endDate;
    private ArrayList<DayOfWeek> repeatDays;

    public RepeatingEvent(String name, String room, Location building, Color color, LocalDateTime startTime, LocalDateTime endTime, boolean scheduleOnHoliday, boolean deleteAtQuarterEnd, LocalDate startDate, LocalDate endDate, ArrayList<DayOfWeek> repeatDays) {
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

    public ArrayList<OneTimeEvent> getOneTimeEvents() {
        return oneTimeEvents;
    }
}
