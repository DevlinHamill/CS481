package edu.cwu.catmap;

import android.graphics.Color;

import java.time.LocalDateTime;

public class Event {
    private String name, room;
    private Location building;
    private int color;
    private LocalDateTime startTime, endTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Location getBuilding() {
        return building;
    }

    public void setBuilding(Location building) {
        this.building = building;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Event(String name, String room, Location building, int color, LocalDateTime startTime, LocalDateTime endTime) {
        this.name = name;
        this.room = room;
        this.building = building;
        this.color = color;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
