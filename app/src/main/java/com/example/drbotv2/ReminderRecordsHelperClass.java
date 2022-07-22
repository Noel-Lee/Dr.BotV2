package com.example.drbotv2;

public class ReminderRecordsHelperClass {

    String consumable_name, duration, untilDate, reminder_times, remarks;


    public ReminderRecordsHelperClass() {

    }

    public ReminderRecordsHelperClass(String consumable_name, String duration, String untilDate, String reminder_times, String remarks) {
        this.consumable_name = consumable_name;
        this.duration = duration;
        this.untilDate = untilDate;
        this.reminder_times = reminder_times;
        this.remarks = remarks;
    }

    public String getConsumable_name() {
        return consumable_name;
    }

    public void setConsumable_name(String consumable_name) {
        this.consumable_name = consumable_name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getReminder_times() {
        return reminder_times;
    }

    public String getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(String untilDate) {
        this.untilDate = untilDate;
    }

    public void setReminder_times(String reminder_times) {
        this.reminder_times = reminder_times;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String toString() {
        return String.format("Consumable Name: %s\nDuration in days: %s\nUntil: %s\nReminder Times: %s\nRemarks: %s", this.consumable_name, this.duration, this.untilDate, this.reminder_times, this.remarks);
    }


}
