package edu.cwu.catmap.core;

import java.util.HashMap;

public class MainListItem {



    public static class Event extends MainListItem {
        private final String title;


        private HashMap<String, String> map;



        public Event(String title, HashMap<String, String> map) {
            this.title = title;
            this.map = map;

        }

        public String getTitle() {
            return title;
        }

        public HashMap<String, String> getMap(){return map;}

        public String getTime(){
            return map.get("Event_Time");
        }
    }
}
