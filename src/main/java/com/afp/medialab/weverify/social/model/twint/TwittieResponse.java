package com.afp.medialab.weverify.social.model.twint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(using = TwittieDeserializer.class)
public class TwittieResponse {

    private List<TwittieEntityJson<Person>> person;
    private List<TwittieEntityJson<UserID>> userID;
    private List<TwittieEntityJson<Location>> location;
    private List<TwittieEntityJson<Organization>> organization;

    //<editor-fold desc="GETTERS & SETTERS">
    public List<TwittieEntityJson<Person>> getPerson() {
        return person;
    }

    public void setPerson(List<TwittieEntityJson<Person>> person) {
        this.person = person;
    }

    public List<TwittieEntityJson<UserID>> getUserID() {
        return userID;
    }

    public void setUserID(List<TwittieEntityJson<UserID>> userID) {
        this.userID = userID;
    }

    public List<TwittieEntityJson<Location>> getLocation() {
        return location;
    }

    public void setLocation(List<TwittieEntityJson<Location>> location) {
        this.location = location;
    }

    public List<TwittieEntityJson<Organization>> getOrganization() {
        return organization;
    }

    public void setOrganization(List<TwittieEntityJson<Organization>> organization) {
        this.organization = organization;
    }
    //</editor-fold>

    public static  class TwittieEntityJson<T extends TwittieFeatures> {
        private int start;
        private int end;
        private T features;

        //<editor-fold desc="GETTERS & SETTERS">
        public void setFeatures(T features) {
            this.features = features;
        }

        public T getFeatures() {
            return features;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
        //</editor-fold>
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract public static class TwittieFeatures {
        private String string;

        //<editor-fold desc="GETTERS & SETTERS">
        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        //</editor-fold>

    }


    public static class Person extends TwittieFeatures{
        private String gender;
        private String firstName;
        private String surname;
        private String middleName;


        //<editor-fold desc="GETTERS & SETTERS">
        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }
        //</editor-fold>
    }

    public static class UserID extends TwittieFeatures {
        private String user;


        //<editor-fold desc="GETTERS & SETTERS">


        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        //</editor-fold>
    }

    public static class Location extends TwittieFeatures {
        private String locType;


        //<editor-fold desc="GETTERS & SETTERS">
        public String getLocType() {
            return locType;
        }

        public void setLocType(String locType) {
            this.locType = locType;
        }

        //</editor-fold>
    }

    public static class Organization extends TwittieFeatures {
        private String orgType;

        //<editor-fold desc="GETTERS & SETTERS">
        public String getOrgType() {
            return orgType;
        }

        public void setOrgType(String orgType) {
            this.orgType = orgType;
        }

        //</editor-fold>

    }
}

