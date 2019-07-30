package com.square.interviewapp;

import android.os.Parcel;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.square.interviewapp.model.EmployeeDetails;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4ClassRunner.class)
public class EmployeeDetailsTests {

    @Test
    public void testEmployeeDetailsParcel() {
        EmployeeDetails employeeDetails = new EmployeeDetails("UUID", "FULL_NAME", "PHONE", "EMAIL", "BIOGRAPHY", "PHOTO_URL_SMALL", "PHOTO_URL_LARGE", "TEAM", EmployeeDetails.EmployeeType.CONTRACTOR);
        Parcel parcel = Parcel.obtain();
        employeeDetails.writeToParcel(parcel, employeeDetails.describeContents());
        parcel.setDataPosition(0);

        EmployeeDetails parceledEmployeeDetails = EmployeeDetails.CREATOR.createFromParcel(parcel);
        assertEquals(parceledEmployeeDetails.getUuid(), employeeDetails.getUuid());
        assertEquals(parceledEmployeeDetails.getFullName(), employeeDetails.getFullName());
        assertEquals(parceledEmployeeDetails.getPhoneNumber(), employeeDetails.getPhoneNumber());
        assertEquals(parceledEmployeeDetails.getEmailAddress(), employeeDetails.getEmailAddress());
        assertEquals(parceledEmployeeDetails.getBiography(), employeeDetails.getBiography());
        assertEquals(parceledEmployeeDetails.getPhotoUrlSmall(), employeeDetails.getPhotoUrlSmall());
        assertEquals(parceledEmployeeDetails.getPhotoUrlLarge(), employeeDetails.getPhotoUrlLarge());
        assertEquals(parceledEmployeeDetails.getTeam(), employeeDetails.getTeam());
        assertEquals(parceledEmployeeDetails.getEmployeeType(), employeeDetails.getEmployeeType());

    }


}
