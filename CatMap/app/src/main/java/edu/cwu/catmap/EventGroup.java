package edu.cwu.catmap;

import android.graphics.Color;

import java.util.ArrayList;

public class EventGroup {
    private final ArrayList<Event> events;
    private boolean cascadeDelete;
    private Color groupColor;

    public EventGroup(boolean cascadeDelete, Color groupColor) {
        this.cascadeDelete = cascadeDelete;
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

    public Color getGroupColor() {
        return groupColor;
    }

    public void setGroupColor(Color groupColor) {
        this.groupColor = groupColor;
    }
}
