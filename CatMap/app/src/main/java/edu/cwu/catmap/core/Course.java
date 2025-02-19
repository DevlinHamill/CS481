package edu.cwu.catmap.core;

public class Course extends EventGroup {
    private boolean scheduleOnHoliday, deleteAtQuarterEnd;

    public Course(boolean cascadeDelete, int groupColor, boolean scheduleOnHoliday, boolean deleteAtQuarterEnd) {
        super(true, true, groupColor);
        this.scheduleOnHoliday = scheduleOnHoliday;
        this.deleteAtQuarterEnd = deleteAtQuarterEnd;
    }

    public boolean isScheduleOnHoliday() {
        return scheduleOnHoliday;
    }

    public void setScheduleOnHoliday(boolean scheduleOnHoliday) {
        this.scheduleOnHoliday = scheduleOnHoliday;
    }

    public boolean isDeleteAtQuarterEnd() {
        return deleteAtQuarterEnd;
    }

    public void setDeleteAtQuarterEnd(boolean deleteAtQuarterEnd) {
        this.deleteAtQuarterEnd = deleteAtQuarterEnd;
    }

}
