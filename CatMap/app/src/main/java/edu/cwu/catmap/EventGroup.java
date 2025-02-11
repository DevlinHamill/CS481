package edu.cwu.catmap;

import android.graphics.Color;

import java.util.ArrayList;

public class EventGroup {
    private final ArrayList<Event> events;
    private boolean cascadeDelete, useEventColor;
    private int groupColor;

    public EventGroup(boolean cascadeDelete, boolean useEventColor, int groupColor) {
        this.cascadeDelete = cascadeDelete;
        this.useEventColor = useEventColor;
        this.groupColor = groupColor;
        events = new ArrayList<>();
    }

    public boolean containsEvent(Event event) {
        return getEvents().contains(event);
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public void removeEvent(Event event) {
        events.remove(event);
    }

    public ArrayList<Event> getEvents() {
        return new ArrayList<>(events);
    }

    public boolean isCascadeDelete() {
        return cascadeDelete;
    }

    public void setCascadeDelete(boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
    }

    public int getGroupColor() {
        return groupColor;
    }

    public void setGroupColor(int groupColor) {
        this.groupColor = groupColor;
    }
}
