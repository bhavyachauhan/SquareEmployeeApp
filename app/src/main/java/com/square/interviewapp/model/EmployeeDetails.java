package com.square.interviewapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.square.interviewapp.utils.gson.Required;

public class EmployeeDetails implements Parcelable {

    @Required
    @SerializedName("uuid")
    private String uuid;

    @Required
    @SerializedName("full_name")
    private String fullName;

    @Required
    @SerializedName("phone_number")
    private String phoneNumber;

    @Required
    @SerializedName("email_address")
    private String emailAddress;

    @Required
    @SerializedName("biography")
    private String biography;

    @Required
    @SerializedName("photo_url_small")
    private String photoUrlSmall;

    @Required
    @SerializedName("photo_url_large")
    private String photoUrlLarge;

    @Required
    @SerializedName("team")
    private String team;

    @Required
    @SerializedName("employee_type")
    private EmployeeType employeeType;

    public EmployeeDetails(String uuid, String fullName, String phoneNumber, String emailAddress, String biography, String photoUrlSmall, String photoUrlLarge, String team, EmployeeType employeeType) {
        this.uuid = uuid;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.biography = biography;
        this.photoUrlSmall = photoUrlSmall;
        this.photoUrlLarge = photoUrlLarge;
        this.team = team;
        this.employeeType = employeeType;
    }

    public EmployeeDetails(Parcel in) {
        uuid = in.readString();
        fullName = in.readString();
        phoneNumber = in.readString();
        emailAddress = in.readString();
        biography = in.readString();
        photoUrlSmall = in.readString();
        photoUrlLarge = in.readString();
        team = in.readString();
        employeeType = EmployeeType.values()[in.readInt()];
    }

    public static final Creator<EmployeeDetails> CREATOR = new Creator<EmployeeDetails>() {
        @Override
        public EmployeeDetails createFromParcel(Parcel in) {
            return new EmployeeDetails(in);
        }

        @Override
        public EmployeeDetails[] newArray(int size) {
            return new EmployeeDetails[size];
        }
    };

    public String getUuid() {
        return uuid;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getBiography() {
        return biography;
    }

    public String getPhotoUrlSmall() {
        return photoUrlSmall;
    }

    public String getPhotoUrlLarge() {
        return photoUrlLarge;
    }

    public String getTeam() {
        return team;
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uuid);
        parcel.writeString(fullName);
        parcel.writeString(phoneNumber);
        parcel.writeString(emailAddress);
        parcel.writeString(biography);
        parcel.writeString(photoUrlSmall);
        parcel.writeString(photoUrlLarge);
        parcel.writeString(team);
        parcel.writeInt(employeeType.ordinal());
    }

    public enum EmployeeType {
        FULL_TIME, PART_TIME, CONTRACTOR;

        public String description() {
            switch (this) {
                case PART_TIME:
                    return "Part time Employee";
                case CONTRACTOR:
                    return "Contractor";
                default:
                    return "Full time Employee";
            }
        }
    }

}
