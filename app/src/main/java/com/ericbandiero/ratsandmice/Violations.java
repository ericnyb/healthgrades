package com.ericbandiero.ratsandmice;

import java.util.Comparator;

import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 6/3/2016.
 */
public class Violations {

    private String code;
    private String description;
    private boolean isSelected;


    public Violations(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
       }

    //Used to sort violation by critical flag
    public static final Comparator<Violations> SORT_BY_SELECTED=new Comparator<Violations>() {
        @Override
        public int compare(Violations o1, Violations o2){
            //return Boolean.compare( o1.isSelected(), o2.isSelected() );
            return Boolean.valueOf(o2.isSelected).compareTo(Boolean.valueOf(o1.isSelected));
            //return o1.isSelected().compareTo(o2.isSelected());
        }
    };
}



