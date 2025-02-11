package edu.cwu.catmap;

import android.graphics.Color;

import java.time.LocalDate;
import java.util.ArrayList;

public class Schedule {
    private final ArrayList<Course> courses;
    private final ArrayList<EventGroup> eventGroups;
    private final EventGroup ungroupedEvents;
    private LocalDate quarterStartDate, quarterEndDate;

    public Schedule(LocalDate quarterEndDate, LocalDate quarterStartDate) {
        this.quarterEndDate = quarterEndDate;
        this.quarterStartDate = quarterStartDate;
        courses = new ArrayList<>();
        eventGroups = new ArrayList<>();
        ungroupedEvents = new EventGroup(false, false, Color.BLACK);
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    public ArrayList<Course> getCourses() {
        return new ArrayList<>(courses);
    }

    public void addEventGroup(EventGroup eventGroup) {
        eventGroups.add(eventGroup);
    }

    /**
     * Remove an event group and its courses if cascadeDelete is enabled
     * @param eventGroup event group to remove
     */
    public void removeEventGroup(EventGroup eventGroup) {
        if(!eventGroup.isCascadeDelete()) {
            for(Event event : eventGroup.getEvents()) {
                ungroupedEvents.addEvent(event);
            }
        }

        eventGroups.remove(eventGroup);
    }

    public ArrayList<EventGroup> getEventGroups() {
        return new ArrayList<>(eventGroups);
    }

    /**
     * Add an event to an event group. This removes the event from all other groups and removes
     * it from the ungrouped group.
     * @param event event to add to the group
     * @param group group to add the event to
     */
    public void moveEventToGroup(Event event, EventGroup group) {
        //remove event from all other groups
        for(EventGroup tempGroup : eventGroups) {
            tempGroup.removeEvent(event);
        }

        //remove even from the ungrouped group
        ungroupedEvents.removeEvent(event);

        //add even to specified group
        group.addEvent(event);
    }

    /**
     * Remove an event from a specified group and add it to the ungroupedEvents group.
     * @param event event to remove
     * @param eventGroup group to remove it from
     */
    public void removeEventFromGroup(Event event, EventGroup eventGroup) {
        eventGroup.removeEvent(event);
        ungroupedEvents.addEvent(event);
    }

}
