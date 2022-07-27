package com.example.drbotv2;

import java.util.ArrayList;

public class RecordConsumptionHelperClass {

    String nameDateTime;
    String remarks;

    public RecordConsumptionHelperClass() {
    }

    public RecordConsumptionHelperClass(String nameDateTime, String remarks) {
        this.nameDateTime = nameDateTime;
        this.remarks = remarks;
    }

    public String getNameDateTime() {
        return nameDateTime;
    }

    public void setNameDateTime(String nameDateTime) {
        this.nameDateTime = nameDateTime;
    }
}
