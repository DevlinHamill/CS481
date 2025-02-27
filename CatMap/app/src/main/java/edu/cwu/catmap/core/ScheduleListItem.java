package edu.cwu.catmap.core;

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

        public Event(String title, String time) {
            this.title = title;
            this.time = time;
        }

        public String getTitle() {
            return title;
        }

        public String getTime() {
            return time;
        }
    }
}
