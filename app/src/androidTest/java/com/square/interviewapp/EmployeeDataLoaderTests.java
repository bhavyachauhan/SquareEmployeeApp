package com.square.interviewapp;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.google.gson.JsonParseException;
import com.square.interviewapp.model.EmployeeDetails;
import com.square.interviewapp.utils.EmployeeDataLoader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

@RunWith(AndroidJUnit4ClassRunner.class)
public class EmployeeDataLoaderTests {

    @Test
    public void testGetEmployeeList() {
        Context context = ApplicationProvider.getApplicationContext();
        List<EmployeeDetails> list;
        try {
            list = EmployeeDataLoader.getEmployeeDetailsList(context);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
            return;
        }
        assertNotNull(list);
        assertTrue(!list.isEmpty());
    }

    @Test
    public void testGetEmptyEmployeeList() {
        Context context = ApplicationProvider.getApplicationContext();
        List<EmployeeDetails> list;
        try {
            list = EmployeeDataLoader.getEmptyEmployeeDetailsList(context);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
            return;
        }
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test(expected = JsonParseException.class)
    public void testGetMalformedEmployeeList() {
        Context context = ApplicationProvider.getApplicationContext();
        try {
            EmployeeDataLoader.getMalformedEmployeeDetailsList(context);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
