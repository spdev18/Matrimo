package com.techotd.matrimo.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class UserData implements Parcelable {

    // Personal details
    public String userId;  // Firebase user ID
    public String name;
    public String age;
    public String gender;
    public String religion;
    public String caste;
    public String profession;
    public String qualification;

    // Family details
    public String fatherName;
    public String fatherOccupation;
    public String motherName;
    public String motherOccupation;
    public String brothers;
    public String sisters;

    // Contact details
    public String contact;
    public String address;
    public String hobbies;
    public String additionalInfo;

    public String height;
    public String weight;

    // Photo
    public String photoPath;

    public UserData() {
        // Required empty constructor for Firebase
    }

    protected UserData(Parcel in) {
        userId = in.readString();
        name = in.readString();
        age = in.readString();
        gender = in.readString();
        religion = in.readString();
        caste = in.readString();
        height = in.readString();
        weight = in.readString();
        profession = in.readString();
        qualification = in.readString();
        fatherName = in.readString();
        fatherOccupation = in.readString();
        motherName = in.readString();
        motherOccupation = in.readString();
        brothers = in.readString();
        sisters = in.readString();
        contact = in.readString();
        address = in.readString();
        hobbies = in.readString();
        additionalInfo = in.readString();
        photoPath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(name);
        dest.writeString(age);
        dest.writeString(gender);
        dest.writeString(religion);
        dest.writeString(caste);
        dest.writeString(height);
        dest.writeString(weight);
        dest.writeString(profession);
        dest.writeString(qualification);
        dest.writeString(fatherName);
        dest.writeString(fatherOccupation);
        dest.writeString(motherName);
        dest.writeString(motherOccupation);
        dest.writeString(brothers);
        dest.writeString(sisters);
        dest.writeString(contact);
        dest.writeString(address);
        dest.writeString(hobbies);
        dest.writeString(additionalInfo);
        dest.writeString(photoPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserData> CREATOR = new Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };
}