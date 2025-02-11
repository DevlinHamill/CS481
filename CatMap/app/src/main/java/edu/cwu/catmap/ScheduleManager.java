package edu.cwu.catmap;

import android.os.Build;

import java.time.LocalDate;

public class ScheduleManager {
    private Schedule schedule;

    public ScheduleManager() {
        schedule = new Schedule(LocalDate.MIN, LocalDate.MAX);
    }
}
