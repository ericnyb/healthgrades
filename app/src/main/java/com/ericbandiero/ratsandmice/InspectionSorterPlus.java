package com.ericbandiero.ratsandmice;

import java.util.Comparator;

import healthdeptdata.InspectionSorts;
import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 11/14/2015.
 */
public class InspectionSorterPlus extends InspectionSorts {

    public InspectionSorterPlus() {

    }

    //Test to see how this works - if tied score sort by dba descending.
    public static final Comparator<Inspections> SORT_BY_SCORE_NAME_DESC=new Comparator<Inspections>() {
        @Override
        public int compare(Inspections o1, Inspections o2){

            int scoreCompare=(o1.getScore() < o2.getScore() ? -1 :
                    (o1.getScore() == o2.getScore() ? 0 : 1));

            if (scoreCompare!=0){
                return scoreCompare;
            }


            return o2.getDba().compareTo(o1.getDba());
        }
    };

    //Used to sort violation by critical flag
    public static final Comparator<Inspections> SORT_BY_CRITICAL_FLAG=new Comparator<Inspections>() {
        @Override
        public int compare(Inspections o1, Inspections o2){
            return o1.getCritical_flag().toLowerCase().compareTo(o2.getCritical_flag().toLowerCase());
        }
    };

    //Used to sort violation by critical flag
    public static final Comparator<Inspections> SORT_BY_FOOD_TYPE=new Comparator<Inspections>() {
        @Override
        public int compare(Inspections o1, Inspections o2){
            return o1.getCuisine_description().toLowerCase().compareTo(o2.getCuisine_description().toLowerCase());
        }
    };
}
