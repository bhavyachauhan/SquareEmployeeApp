package com.square.interviewapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.square.interviewapp.utils.gson.DataDeserializer;
import com.square.interviewapp.model.EmployeeDetails;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SerializationTests {

    class TestClass {

        String str1, str2;

        TestClass(String s1, String s2) {
            str1 = s1;
            str2 = s2;
        }
    }

    @Test
    public void testDataSerializationDeserialization() {
        TestClass tc = new TestClass("value1", "value2");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(EmployeeDetails.class, new DataDeserializer<EmployeeDetails>())
                .create();

        String serialized = gson.toJson(tc);
        TestClass tc2 = gson.fromJson(serialized, TestClass.class);

        assertEquals(tc.str1, tc2.str1);
        assertEquals(tc.str2, tc2.str2);
    }

}
