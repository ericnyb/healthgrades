package com.ericbandiero.ratsandmice;

import android.util.Log;

import java.util.Collections;
import java.util.List;

import healthdeptdata.HealthDataFilter;
import healthdeptdata.HealthDataRestaurants;
import healthdeptdata.InspectionSorts;
import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 11/11/2015.
 */
public class TestSorts {


    public static void main(String[] args) {
        TestSorts testSorts=new TestSorts();
        testSorts.runner();
    }

    private void runner() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HealthDataRestaurants socrataTest=new HealthDataRestaurants();

                List<HealthDataFilter> allFilters = socrataTest.getAllFilters();

                for (HealthDataFilter o : allFilters) {
                    if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", o.toString());
                }


                // socrataTest.setLimit(50);
                List<Inspections> data = socrataTest.fetchQueryDataLastInspection(HealthDataFilter.BROOKLYN,7);

               Collections.sort(data,Collections.reverseOrder(InspectionSorts.SORT_BY_SCORE));
                showData(data);
             //   Collections.sort(data,InspectionSorts.SORT_BY_NAME);
            //    showData(data);
            //    Collections.sort(data, InspectionSorts.);
           //     showData(data);
           //     Collections.sort(data, InspectionSorts.SORT_BY_GRADE);
           //     showData(data);
           //     Collections.sort(data, InspectionSorterPlus.SORT_BY_SCORE_NAME_DESC);
           //     showData(data);
                new Thread(new Runnable() {
                    public void run() {
                    }
                }).start();

            }
        }).start();


    }

    private void showData(List<Inspections> data) {
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Sorted data --------------------------------");
        for (Inspections next : data) {
            // System.out.println(next.getDba()+":"+next.getScore()+":"+next.getGrade()+":"+next.getBuilding()+" " +next.getStreet());
            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">",next.getCamis()+":"+next.getDba() + ":" + next.getScore() + ":" + next.getGrade() + ":" + next.getBuilding() + " " + next.getStreet());
        }
    }
}
