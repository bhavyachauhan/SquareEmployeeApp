package com.square.interviewapp.retrofit;

import com.square.interviewapp.model.EmployeeDetailsArray;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SquareEmployeeService {

    @GET("employees.json")
    Call<EmployeeDetailsArray> getEmployeeList();

    @GET("employees_empty.json")
    Call<EmployeeDetailsArray> getEmptyEmployeeList();

    @GET("employees_malformed.json")
    Call<EmployeeDetailsArray> getMalformedEmployeeList();

}
