package com.afp.medialab.weverify.social.model.twint;

import java.util.List;

public class StopWords {
    private List<String> global;
    private List<String> fr;
    private List<String> en;

    //<editor-fold desc="GETTER & SETTERS">
    public List<String> getEn() {
        return en;
    }

    public void setEn(List<String> en) {
        this.en = en;
    }

    public List<String> getFr() {
        return fr;
    }

    public void setFr(List<String> fr) {
        this.fr = fr;
    }

    public List<String> getGlobal() {
        return global;
    }

    public void setGlobal(List<String> global) {
        this.global = global;
    }
    //</editor-fold>
}
