package edu.cwu.catmap.core;

import android.graphics.Color;

import java.time.LocalDateTime;

public class OneTimeEvent extends Event{

    public OneTimeEvent(String name, String room, Location building, Color color, LocalDateTime startTime, LocalDateTime endTime) {
        super(name, room, building, color, startTime, endTime);
    }

}
