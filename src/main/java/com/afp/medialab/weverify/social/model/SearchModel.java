package com.afp.medialab.weverify.social.model;

import com.afp.medialab.weverify.social.twint.TwintThread;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;
import java.util.TreeSet;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchModel {

    private static org.slf4j.Logger Logger = LoggerFactory.getLogger(TwintThread.class);
    private String search;
    private SortedSet<String> or;
    private SortedSet<String> and;
    private SortedSet<String> not;

    public SearchModel(){}

    public SearchModel(String search, SortedSet<String> or, SortedSet<String> and, SortedSet<String> not)
    {
        this.search = search.toLowerCase();
        this.or = new TreeSet<>();
        this.and = new TreeSet<>();
        this.not = new TreeSet<>();

        if (or != null)
            for (String str: or)
                this.or.add(str.toLowerCase());

        if (and != null)
            for (String str: and)
                this.and.add(str.toLowerCase());

        if (not != null)
            for (String str: not)
                this.not.add(str.toLowerCase());
    }

    public SortedSet<String> getOr() {
        return or;
    }

    public void setOr(SortedSet<String> or) {
        this.or = or;
    }

    public SortedSet<String> getAnd() {
        return and;
    }

    public void setAnd(SortedSet<String> and) {
        this.and = and;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public SortedSet<String> getNot() {
        return not;
    }

    public void setNot(SortedSet<String> not) {
        this.not = not;
    }
}
