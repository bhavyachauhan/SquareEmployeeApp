package com.square.interviewapp.model;

import com.google.gson.annotations.SerializedName;

public class EmployeeDetailsArray {

    @SerializedName("employees")
    private EmployeeDetails[] employeeDetails;


    public EmployeeDetails[] getEmployeeDetails() {
        return employeeDetails;
    }

}
