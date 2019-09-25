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


    /**
     * @func    Verifies if the two sets are equal
     * @param   sortedSet1
     * @param   sortedSet2
     * @return
     */
    public Boolean equalsSet(SortedSet sortedSet1, SortedSet sortedSet2)
    {
        if (sortedSet1 == null && sortedSet2 == null)
            return true;
        if (sortedSet1 != null && sortedSet2 != null)
        {
            return sortedSet1.equals(sortedSet2);
        }
        else
            return false;
    }

    /**
     * @func    Overrides the equals function of the SearchModel object.
     *          Checks that the attributes : search , or, and and not are the same
     * @param   overObject
     * @return
     */
    @Override
    public boolean equals(Object overObject) {
        if (!(overObject instanceof SearchModel))
            return false;

        SearchModel overSearchModel = (SearchModel) overObject;

        Boolean sameSearch = true;

        String search1 = this.search;
        String search2 = overSearchModel.search;

        SortedSet andSet1 = this.and;
        SortedSet andSet2 = overSearchModel.and;

        SortedSet orSet1 = this.or;
        SortedSet orSet2 = overSearchModel.or;

        SortedSet notSet1 = this.not;
        SortedSet notSet2 = overSearchModel.not;

        if (search1 != null && search2 != null)
            sameSearch = search1.equals(search2);
        else if (!(search1 == null && search2 == null))
            sameSearch = false;

        if (!equalsSet(andSet1, andSet2))
            sameSearch = false;

        if (!equalsSet(orSet1, orSet2))
            sameSearch = false;

        if (!equalsSet(notSet1, notSet2))
            sameSearch = false;

        return sameSearch;
    }
}
