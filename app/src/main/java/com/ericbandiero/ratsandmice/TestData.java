package com.ericbandiero.ratsandmice;

import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import healthdeptdata.HealthDataFilter;
import healthdeptdata.HealthDataRestaurants;
import healthdeptdata.Inspections;
import com.ericbandiero.myframework.Utility;
/**
 * Created by ${"Eric Bandiero"} on 11/6/2015.
 */
public class TestData {
    private final SortedSet<String> sortedSet=new TreeSet();
    private static final String NEWLINE=System.getProperty("line.separator");
    public TestData() {

    }

    public static void main(String[] args) {
        TestData data=new TestData();
        //data.runner();
        data.runSplitTest();

    }

    private void runSplitTest(){

    String zipcodelist="10023,10024,10025a";

        //System.out.println(zipcodelist.matches(".*[A-Za-z].*"));
      System.out.println(Utility.stringContainsAnyCharacter_A_Z(zipcodelist));



        String[] split = zipcodelist.split(",");

        for (int i = 0; i < split.length; i++) {
            System.out.println("split:"+i+" = " + split[i]);
        }

        Set<String> s=new HashSet<>(Arrays.asList(split));

        System.out.println("s = " + s);
        String []newArray = new String[0];
        s.toArray(newArray);
        return;
    }



    private void runner() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                HealthDataRestaurants socrataTest=new HealthDataRestaurants();
                //final List<Inspections> data = socrataTest.getLatestData();
                //final List<Inspections> data = socrataTest.getDataClosedInPastYear();

               // socrataTest.addHealthDataFilter(HealthDataFilter.UPPER_WEST_SIDE_ZIPS_10023_10024_10025);

//                for (HealthDataFilter healthDataFilter : socrataTest.getAllFilters()) {
//                    System.out.println(healthDataFilter.getFilterType());
//                }
                final List<Inspections> data = socrataTest.fetchQueryDataLastInspection(HealthDataFilter.MANHATTAN,7);

                final StringBuilder dbaName = new StringBuilder(50);
                if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "Data size!" + data.size());

                for (int i = 0; i < data.size(); i++) {
                    System.out.println(data.get(i).toString());
                }

                new Thread(new Runnable() {
                    public void run() {

                        for(String dataname:sortedSet){
                            dbaName.append(dataname+NEWLINE);
                        }
                        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Sorted set size:"+sortedSet.size());
                        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Sorted set size:"+dbaName);
                        for (String data:sortedSet){
                            if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Data:"+data);
                        }
                    }
                }).start();

            }
        }).start();

    }
}
