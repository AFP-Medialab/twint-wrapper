package com.afp.medialab.weverify.social.model.twint;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = TwitieDeserializer.class)
public class TwitieResponse {

    private List<TwitieEntityJson<Person>> person;
    private List<TwitieEntityJson<UserID>> userID;
    private List<TwitieEntityJson<Location>> location;
    private List<TwitieEntityJson<Organization>> organization;

    //<editor-fold desc="GETTERS & SETTERS">
    public List<TwitieEntityJson<Person>> getPerson() {
        return person;
    }

    public void setPerson(List<TwitieEntityJson<Person>> person) {
        this.person = person;
    }

    public List<TwitieEntityJson<UserID>> getUserID() {
        return userID;
    }

    public void setUserID(List<TwitieEntityJson<UserID>> userID) {
        this.userID = userID;
    }

    public List<TwitieEntityJson<Location>> getLocation() {
        return location;
    }

    public void setLocation(List<TwitieEntityJson<Location>> location) {
        this.location = location;
    }

    public List<TwitieEntityJson<Organization>> getOrganization() {
        return organization;
    }

    public void setOrganization(List<TwitieEntityJson<Organization>> organization) {
        this.organization = organization;
    }
    //</editor-fold>

    public static  class TwitieEntityJson<T extends TwitieFeatures> {
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
    abstract public static class TwitieFeatures {
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


    public static class Person extends TwitieFeatures{
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

    public static class UserID extends TwitieFeatures {
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

    public static class Location extends TwitieFeatures {
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

    public static class Organization extends TwitieFeatures {
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

