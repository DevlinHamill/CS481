package edu.cwu.catmap.core;

import java.util.HashMap;

public class ScheduleListItem {
    public static class SectionHeader extends ScheduleListItem {
        private final String date;

        public SectionHeader(String date) {
            this.date = date;
        }

        public String getDate() {
            return date;
        }
    }

    public static class Event extends ScheduleListItem {
        private final String title;

        private final String time;

        private HashMap<String, String> map;



        public Event(String title, String time, HashMap<String, String> map) {
            this.title = title;
            this.time = time;
            this.map = map;

        }

        public String getTitle() {
            return title;
        }

        public String getTime() {
            return time;
        }

        public HashMap<String, String> getMap(){return map;}
    }
}
