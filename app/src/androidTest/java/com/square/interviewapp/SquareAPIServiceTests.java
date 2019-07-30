package com.square.interviewapp;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.square.interviewapp.utils.gson.DataDeserializer;
import com.square.interviewapp.model.EmployeeDetails;
import com.square.interviewapp.retrofit.SquareAPIService;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class SquareAPIServiceTests {

    @Test
    public void testRetrofitBuilder() {
        Context appContext = ApplicationProvider.getApplicationContext();
        SquareAPIService squareAPIService = SquareAPIService.getRetrofitService(appContext);
        assertNotNull("Null SquareAPIService", squareAPIService);
        assertNotNull("Null Retrofit", squareAPIService.getRetrofit());
        assertNotNull("Null SquareEmployeeService", SquareAPIService.getSquareAPIService(appContext));
    }

    @Test
    public void testEmployeeDetailsJson() {
        String employeeDetailsJson = "{\"uuid\":\"0d8fcc12-4d0c-425c-8355-390b312b909c\",\"full_name\" : \"Justine Mason\",\"phone_number\" : \"5553280123\",\"email_address\" : \"jmason.demo@squareup.com\",\"biography\" : \"Engineer on the Point of Sale team.\",\"photo_url_small\" : \"https://s3.amazonaws.com/sq-mobile-interview/photos/16c00560-6dd3-4af4-97a6-d4754e7f2394/small.jpg\",\"photo_url_large\" : \"https://s3.amazonaws.com/sq-mobile-interview/photos/16c00560-6dd3-4af4-97a6-d4754e7f2394/large.jpg\",\"team\" : \"Point of Sale\",\"employee_type\" : \"FULL_TIME\"}";
        EmployeeDetails details = new Gson().fromJson(employeeDetailsJson, EmployeeDetails.class);
        assertNotNull(details);
    }

    @Test(expected = JsonParseException.class)
    public void testMalformedEmployeeDetailsGson() {
        String employeeDetailsJson = "{\"uuid\":\"0d8fcc12-4d0c-425c-8355-390b312b909c\",\"full_name\" : \"Justine Mason\",\"phone_number\" : \"5553280123\",\"email_address\" : \"jmason.demo@squareup.com\",\"biography\" : \"Engineer on the Point of Sale team.\",\"photo_url_small\" : \"https://s3.amazonaws.com/sq-mobile-interview/photos/16c00560-6dd3-4af4-97a6-d4754e7f2394/small.jpg\",\"photo_url_large\" : \"https://s3.amazonaws.com/sq-mobile-interview/photos/16c00560-6dd3-4af4-97a6-d4754e7f2394/large.jpg\",\"employee_type\" : \"FULL_TIME\"}";
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(EmployeeDetails.class, new DataDeserializer<EmployeeDetails>())
                .create();
        gson.fromJson(employeeDetailsJson, EmployeeDetails.class);
    }

}

