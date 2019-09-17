package com.afp.medialab.weverify.social.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchModel {
    private String search;
    private ArrayList<String> or;
    private ArrayList<String> and;
    private ArrayList<String> not;

    public ArrayList<String> getOr() {
        return or;
    }

    public void setOr(ArrayList<String> or) {
        this.or = or;
    }

    public ArrayList<String> getAnd() {
        return and;
    }

    public void setAnd(ArrayList<String> and) {
        this.and = and;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public ArrayList<String> getNot() {
        return not;
    }

    public void setNot(ArrayList<String> not) {
        this.not = not;
    }
}
