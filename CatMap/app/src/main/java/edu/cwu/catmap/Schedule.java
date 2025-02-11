package edu.cwu.catmap;

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
        ungroupedEvents = new EventGroup(false, null);
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

    public void removeEventGroup(EventGroup eventGroup) {
        eventGroups.remove(eventGroup);
    }

    public ArrayList<EventGroup> getEventGroups() {
        return new ArrayList<>(eventGroups);
    }




}
