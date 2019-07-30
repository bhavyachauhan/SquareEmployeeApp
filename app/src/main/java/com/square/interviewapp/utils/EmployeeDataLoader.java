package com.square.interviewapp.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.square.interviewapp.model.EmployeeDetails;
import com.square.interviewapp.model.EmployeeDetailsArray;
import com.square.interviewapp.retrofit.SquareAPIService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class EmployeeDataLoader {

    @SuppressWarnings("WeakerAccess")
    public static final String TAG = EmployeeDataLoader.class.getCanonicalName();

    private static List<EmployeeDetails> employeeDetailsList;

    private static void fetchEmployeeDetailsList(@NonNull Call<EmployeeDetailsArray> call) throws IOException, RuntimeException {
        Response<EmployeeDetailsArray> response = call.execute();
        if (response.isSuccessful() && response.body() != null) {
            employeeDetailsList = Arrays.asList(response.body().getEmployeeDetails());
        } else if (response.errorBody() != null) {
            Log.e(TAG, "Error downloading employee data. " + response.errorBody().string());
        }
    }

    public static List<EmployeeDetails> getEmployeeDetailsList(@NonNull final Context context) throws IOException, RuntimeException {
        if (employeeDetailsList == null) {
            Call<EmployeeDetailsArray> call = SquareAPIService.getSquareAPIService(context).getEmployeeList();
            fetchEmployeeDetailsList(call);
        }
        return employeeDetailsList;
    }

    public static List<EmployeeDetails> getEmptyEmployeeDetailsList(@NonNull final Context context) throws IOException, RuntimeException {
        if (employeeDetailsList == null) {
            Call<EmployeeDetailsArray> call = SquareAPIService.getSquareAPIService(context).getEmptyEmployeeList();
            fetchEmployeeDetailsList(call);
        }
        return employeeDetailsList;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static List<EmployeeDetails> getMalformedEmployeeDetailsList(@NonNull final Context context) throws IOException, RuntimeException {
        if (employeeDetailsList == null) {
            Call<EmployeeDetailsArray> call = SquareAPIService.getSquareAPIService(context).getMalformedEmployeeList();
            fetchEmployeeDetailsList(call);
        }
        return employeeDetailsList;
    }

}
