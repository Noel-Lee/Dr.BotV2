package com.example.drbotv2;

import java.util.ArrayList;

public class UserHelperClass {

    String teleHandle, password, password2, gender, birthYear, fullName, email, phoneNo;

    long exp, timelyConsumption, totalConsumption;

    ArrayList<ReminderRecordsHelperClass> reminderRecordsHelperClass;
    ArrayList recordConsumptionHelperClass;

    public UserHelperClass(String teleHandle, String password, String password2, String gender, String birthYear, String fullName, String email, String phoneNo, long exp, long timelyConsumption, long totalConsumption, ArrayList<ReminderRecordsHelperClass> reminderRecordsHelperClass, ArrayList recordConsumptionHelperClass) {
        this.teleHandle = teleHandle;
        this.password = password;
        this.password2 = password2;
        this.gender = gender;
        this.birthYear = birthYear;
        this.fullName = fullName;
        this.email = email;
        this.phoneNo = phoneNo;
        this.exp = exp;
        this.timelyConsumption = timelyConsumption;
        this.totalConsumption = totalConsumption;
        this.reminderRecordsHelperClass = reminderRecordsHelperClass;
        this.recordConsumptionHelperClass = recordConsumptionHelperClass;
    }

    public UserHelperClass() {

    }

    public String getTeleHandle() {
        return teleHandle;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getPassword2() {
        return password2;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setTeleHandle(String teleHandle) {
        this.teleHandle = teleHandle;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public ArrayList<ReminderRecordsHelperClass> getReminderRecordsHelperClass() {
        return reminderRecordsHelperClass;
    }

    public void setReminderRecordsHelperClass(ArrayList<ReminderRecordsHelperClass> reminderRecordsHelperClass) {
        this.reminderRecordsHelperClass = reminderRecordsHelperClass;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getTimelyConsumption() {
        return timelyConsumption;
    }

    public void setTimelyConsumption(long timelyConsumption) {
        this.timelyConsumption = timelyConsumption;
    }

    public long getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(long totalConsumption) {
        this.totalConsumption = totalConsumption;
    }

    public ArrayList getRecordConsumptionHelperClass() {
        return recordConsumptionHelperClass;
    }

    public void setRecordConsumptionHelperClass(ArrayList recordConsumptionHelperClass) {
        this.recordConsumptionHelperClass = recordConsumptionHelperClass;
    }
}
