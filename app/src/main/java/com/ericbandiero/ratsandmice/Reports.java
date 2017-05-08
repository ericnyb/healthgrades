package com.ericbandiero.ratsandmice;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by ${"Eric Bandiero"} on 3/21/2016.
 */
public class Reports implements Parcelable{

    //These are the default report names.
    //public static final String LATEST_INSPECTION_SCORES = "Latest Inspection Scores";
    public static final String LATEST_INSPECTION_SCORES = "Inspections in Last 7 Days";
    public static final String LATEST_INSPECTION_SCORES_MONTH = "Inspections in Last 30 Days";
    public static final String ALL_PLACES_FOR_ONE_ZIP_CODE = "All restaurants in current zip code";
    public static final String NEW_RESTAURANTS = "New Restaurants";

    public static final String WORST_150_SCORES_IN_PAST_YEAR = "Worst 150 Scores in Past Year";
    public static final String CLOSED_BY_THE_DOHMH = "Closed by the DOHMH";
    public static final String NO_GRADE_SIGN_POSTED_IN_PAST_YEAR = "Grade Sign Not Posted in Past Year";
    public static final String SCORES_AND_GRADES = "Scores and Grades";
    public static final String RATS_OR_MICE_IN_PAST_YEAR = "Rats or Mice in Past Year";
    public static final String CLOSED_BY_DOHMH_WITHIN_LAST_YEAR = "Closed By DOHMH Within Last Year";
    public static final String CLOSED_BY_DOHMH_ON_LAST_INSPECTION = "Closed by DOHMH On Last Inspection";
    public static final String GRADE_A_NEVER_RECEIVED_LESS_THAN_AN_A = "Grade A - Never Scored Less Than an A";
    public static final String NEVER_GOT_AN_A = "Never Got an A";
    public static final String C_YOU_LATER_NEVER_AN_A_OR_B = "C You later! - Only got C Grade";
    public static final String OLDEST_INSPECTIONS = "Oldest Inspections"; //11
    public static final String ROACHES = "Roaches in Past Year"; //12
    public static final String WORST_25_SCORES_EVER = "Worst 25 Scores Ever"; //13
    public static final String VIOLATIONS_USER_CREATE = "Create your own violation report"; //14
    public static final String USER_SELECTED_VIOLATIONS_SEARCH = "User Violation Search"; //15
    public static final String USER_REPORTS_HEADER = "My Saved Reports"; //16
    public static final String TOILET_MAINTENANCE = "Toilet maintenance"; //201
    public static final String TOILET_MISSING = "Toilet required but not provided"; //201
    public static final String TOILET_ISSUES_ALL = "Toilet issues in Past Year"; //201
    public static final String FAVORITE_RESTAURANTS = "Favorite Restaurants"; //17
    public static final String A_GRADE_LAST_INSPECTION = "A Grade on latest inspection"; //18
    public static final String C_GRADE_LAST_INSPECTION = "C Grade on latest inspection"; //18
    public static final String B_GRADE_LAST_INSPECTION = "B Grade on latest inspection"; //19;
    public static final String BY_VIOLATION = "Violation Reports";
    public static final String META_DATA = "General Reports";
    public static final String CLOSED_MOST_TIMES_PAST_YEAR = "Closed Most Times in Last Year Top 25";
    public static final String CLOSED_MOST_TIMES_EVER = "Closed Most Times Ever Top 25";
    public static final String HAND_ISSUES = "Hand cleanliness issues in Past Year";
    public static final String SMOKE_SIGN_ISSUES = "Smoking signage issues in Past Year";
    public static final String ALLERGY_SIGN_ISSUES = "Allergy signage issues in Past Year";
    public static final String CALORIE_SIGN_ISSUES = "Calorie signage issues in Past Year";



    //Meta reports
    public static final String META_CUISINES_BY_BORO = "Cuisines by Borough";
    public static final String META_CUISINES_BY_FILTER = "Cuisines by Current Filter";
    public static final String META_CHAINS = "Restaurant Chains";
    public static final String META_RESTAURANTS_BY_BORO = "Restaurant Count By Borough";
    public static final String META_RESTAURANTS_BY_FILTER = "Restaurants Count By Current Filter";
    public static final String META_RESTAURANTS_BY_ZIP_CODE = "Restaurant Count by Zip Code";

    //These aren't actually reports - they are used in Data Provider
    public static final String GET_BY_CAMIS = "CAMIS";
    public static final String DBA_SEARCH = "DBA_SEARCH";
    public static final String FIELD_SEARCH = "FIELD_SEARCH";
    public static final String LAST_INSPECTION_DATE = "LAST INSPECTION DATE";
    public static final String VIOLATIONS_USER_FLAG_SET = "USER SELECTED FLAGS FOR VIOLATIONS";


    //Non report fields
    public static final String PARCEBLABLE_TEXT_FOR_EXTRA = "REPORT_TO_RUN";

   //Report types
    //TODO Clean these p - maybe add field to reports for type separate from report id?
    public static final int REPORTS_USER_REPORT_ID = 101;
    public static final int REPORTS_VIOLATION_CODES_QUERY = 102;
    public static final int REPORTS_NO_DATA_ACTIVITY_NEEDED = 103;
    public static final int REPORT_ID_META=400;
    public static final int REPORT_ID_META_NO_INSPECTION_OBJECT_LIST_RETURNED=500;

    public static final int REPORT_ID_GENERIC=999;


    private String reportName;
    private String reportDesc;
    private int reportId;
    private boolean headerNameHolder; //Not an actual report - but parent for one

    private static String thisClassName;

    public String[] getCodeArray() {
        return codeArray;
    }

    public void setCodeArray(String[] codeArray) {
        this.codeArray = codeArray;

    }

    private String [] codeArray;


    private static Reports lastReportRun;

    public Reports() {

    }

    /**
     * Report class
     * @param reportName The name of the report
     * @param reportDesc The description
     * @param reportId   We give this an ID
     * @param isheader   True if Header, False if not
     */
    public Reports(String reportName, String reportDesc, int reportId,boolean isheader) {
        this.reportName = reportName;
        this.reportDesc = reportDesc.isEmpty()?reportName:reportDesc;
        this.reportId = reportId;
        this.setHeaderNameHolder(isheader);
        thisClassName=getClass().getSimpleName();
    }


    public Reports(String reportName, String reportDesc, int reportId, boolean headerNameHolder, String [] violation_codes) {
        this.reportName = reportName;
        this.reportDesc = reportDesc;
        this.reportId = reportId;
        this.setHeaderNameHolder(headerNameHolder);
        this.codeArray=violation_codes;
        thisClassName=getClass().getSimpleName();
        for (int i = 0; i < codeArray.length; i++) {
            String s = codeArray[i];
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Reports Array:"+s);
        }

    }

    public Reports(int reportId) {
        this.reportId = reportId;
    }

    //TODO Check to see when if paramater isheader is used
    //We will populate these with the data from main report screen
    //You need to add a case statement in dataprovider
    public static void loadMainExpandData(List<Reports> repHeader,HashMap<Reports,List<Reports>> repChildren){

        //TODO Note: This will not let app compile - not sure why...
        //if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Loading reports!");

        if (AppConstant.DEBUG) Log.d("Reports:"+">","Loading reports");

        //Header reports (just titles) need id of less than 0. No report is run when they are clicked.

        //Main menu
        repHeader.add(new Reports(META_DATA,META_DATA,-5,true));
        repHeader.add(new Reports(CLOSED_BY_THE_DOHMH,"Closed Header",-1,true));
        repHeader.add(new Reports(SCORES_AND_GRADES,"Grades",-2,true));
        repHeader.add(new Reports(BY_VIOLATION,"",-3,true));
        repHeader.add(new Reports(USER_REPORTS_HEADER,"",-4,true));
        repHeader.add(new Reports(FAVORITE_RESTAURANTS,"",17,true));

        List<Reports> listChildren=new ArrayList<>();
        Reports reportHeaderRecordToFind;

        //Meta data
        reportHeaderRecordToFind=new Reports(-5);
        listChildren=new ArrayList<>();
        listChildren.add(new Reports(LATEST_INSPECTION_SCORES,LATEST_INSPECTION_SCORES, 1, false));
        listChildren.add(new Reports(LATEST_INSPECTION_SCORES_MONTH,LATEST_INSPECTION_SCORES_MONTH, 25, false));
        listChildren.add(new Reports(ALL_PLACES_FOR_ONE_ZIP_CODE,ALL_PLACES_FOR_ONE_ZIP_CODE,2, false));
        listChildren.add(new Reports(NEW_RESTAURANTS, "New places", 3, false));

        listChildren.add(new Reports(META_CUISINES_BY_BORO, META_CUISINES_BY_BORO, 4, false));
        listChildren.add(new Reports(META_CUISINES_BY_FILTER, META_CUISINES_BY_FILTER, 5, false));
        //This takes too long
        //listChildren.add(new Reports(META_CHAINS, "Most popular chains", REPORT_ID_META_NO_INSPECTION_OBJECT_LIST_RETURNED, false));
        listChildren.add(new Reports(META_RESTAURANTS_BY_BORO, META_RESTAURANTS_BY_BORO, REPORT_ID_META_NO_INSPECTION_OBJECT_LIST_RETURNED, false));
        listChildren.add(new Reports(META_RESTAURANTS_BY_FILTER, META_RESTAURANTS_BY_FILTER, REPORT_ID_META_NO_INSPECTION_OBJECT_LIST_RETURNED, false));
        listChildren.add(new Reports(META_RESTAURANTS_BY_ZIP_CODE, "Restaurant count by zip code", 26, false));
        repChildren.put(repHeader.get(repHeader.indexOf(reportHeaderRecordToFind)),listChildren);


        //Closed
        reportHeaderRecordToFind=new Reports(-1);
        listChildren=new ArrayList<>();
        listChildren.add(new Reports(CLOSED_BY_DOHMH_WITHIN_LAST_YEAR, "Was closed by DOHMH in Past Year", 6, false));
        listChildren.add(new Reports(CLOSED_BY_DOHMH_ON_LAST_INSPECTION, "Closed by DOHMH on last inspection", 7, false));
        listChildren.add(new Reports(CLOSED_MOST_TIMES_PAST_YEAR, CLOSED_MOST_TIMES_PAST_YEAR, 8, false));
        listChildren.add(new Reports(CLOSED_MOST_TIMES_EVER, CLOSED_MOST_TIMES_EVER, 9, false));

        repChildren.put(repHeader.get(repHeader.indexOf(reportHeaderRecordToFind)),listChildren);

        //Scores and grades
        reportHeaderRecordToFind=new Reports(-2);
        listChildren=new ArrayList<>();
        //listChildren.add(new Reports(VIOLATIONS,"",14,false));
        listChildren.add(new Reports(WORST_150_SCORES_IN_PAST_YEAR,"",10,false));
        listChildren.add(new Reports(WORST_25_SCORES_EVER,"",11,false));
        listChildren.add(new Reports(GRADE_A_NEVER_RECEIVED_LESS_THAN_AN_A,"Never below an A score, and at least two A's",12,false));
        listChildren.add(new Reports(NEVER_GOT_AN_A,"Never got an A",13,false));
        //listChildren.add(new Reports(C_YOU_LATER_NEVER_AN_A_OR_B,"Score greater than C",14,false));
        listChildren.add(new Reports(A_GRADE_LAST_INSPECTION,"",27,false));
        listChildren.add(new Reports(B_GRADE_LAST_INSPECTION,"",15,false));
        listChildren.add(new Reports(C_GRADE_LAST_INSPECTION,"",16,false));


        repChildren.put(repHeader.get(repHeader.indexOf(reportHeaderRecordToFind)),listChildren);

        //Violations
        reportHeaderRecordToFind=new Reports(-3);

        listChildren=new ArrayList<>();
        //listChildren.add(new Reports(USER_SELECTED_VIOLATIONS_SEARCH,"",14,false));
        listChildren.add(new Reports(VIOLATIONS_USER_CREATE,"",17,false));
        listChildren.add(new Reports(RATS_OR_MICE_IN_PAST_YEAR,"",18,false));
        listChildren.add(new Reports(ROACHES,"",19,false));
        //listChildren.add(new Reports(TOILET_MAINTENANCE,"Toilet facility not maintained",AppConstant.REPORTS_VIOLATION_CODES_QUERY,false,new String[]{"10A","22B"}));
        //listChildren.add(new Reports(TOILET_MISSING,"Toilet required but not provided",AppConstant.REPORTS_VIOLATION_CODES_QUERY,false,new String[]{"05E"}));
        listChildren.add(new Reports(TOILET_ISSUES_ALL,"Toilet issues", REPORTS_VIOLATION_CODES_QUERY,false,new String[]{"10A","22B","05E"}));
        listChildren.add(new Reports(NO_GRADE_SIGN_POSTED_IN_PAST_YEAR,"Grade sign not posted",20,false));
        listChildren.add(new Reports(HAND_ISSUES,"Hand cleanliness issues",21,false));
        listChildren.add(new Reports(CALORIE_SIGN_ISSUES,"Calorie sign issues",22,false));
        listChildren.add(new Reports(SMOKE_SIGN_ISSUES,"Smoking sign issues",23,false));
        listChildren.add(new Reports(ALLERGY_SIGN_ISSUES,"Allergy sign issues",24,false));



        repChildren.put(repHeader.get(repHeader.indexOf(reportHeaderRecordToFind)),listChildren);


        //User saved reports.
        reportHeaderRecordToFind=new Reports(-4);
        listChildren=new ArrayList<>();

        //Add user reports
        //Map<String, ?> userViolationReports = PreferenceUtility.getUserViolationReports();
        Map<String, Set<String>> userViolationReports = (Map<String, Set<String>>) PreferenceUtility.getUserViolationReports();

        for (String s : userViolationReports.keySet()) {
            listChildren.add(new Reports(s,s, REPORTS_USER_REPORT_ID,false,userViolationReports.get(s).toArray(new String[userViolationReports.get(s).size()])));
            if (AppConstant.DEBUG) Log.d(thisClassName+">","Load violations report:"+userViolationReports.get(s).toString());

        }

        repChildren.put(repHeader.get(repHeader.indexOf(reportHeaderRecordToFind)),listChildren);

        //Temp test for dup ids - may not matter?
     /*
        Collection<List<Reports>> values = repChildren.values();

        Set <Integer> setRepId=new HashSet<>();

        for (List<Reports> value : values) {
            for (Reports reports : value) {
                if (!setRepId.add(reports.getReportId())){

                    if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Report name:"+reports.getReportName());
                    if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Dup id:"+reports.getReportId());
                }
            }
        }
*/

    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">", "Setting report name to:"+reportName);
    }

    public String getReportDesc() {
        return reportDesc;
    }

    public void setReportDesc(String reportDesc) {
        this.reportDesc = reportDesc;
    }
    public boolean getHeaderNameHolder() {
        return isHeaderNameHolder();
    }

    public void setHeaderNameHolder(boolean headerNameHolder) {
        this.headerNameHolder = headerNameHolder;
    }

    @Override
    public boolean equals(Object o) {
         if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reports reports = (Reports) o;

        if (reportId != reports.reportId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return reportId;
    }


    //Used to sort violation by critical flag
    public static final Comparator<Reports> SORT_BY_CRITICAL_FLAG=new Comparator<Reports>() {
        @Override
        public int compare(Reports o1, Reports o2){
            return o1.getReportName().compareTo(o2.getReportName());
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reportName);
        dest.writeString(reportDesc);
        dest.writeInt(reportId);
        dest.writeByte((byte) (isHeaderNameHolder() ? 1 : 0));
        dest.writeStringArray(codeArray);
    }


    // Creator
    public static final Parcelable.Creator<Reports> CREATOR
            = new Parcelable.Creator<Reports>() {
        public Reports createFromParcel(Parcel in) {
            return new Reports(in);
        }

        public Reports[] newArray(int size) {
            return new Reports[size];
        }
    };
    private Reports(Parcel in) {
        reportName = in.readString();
        reportDesc = in.readString();
        reportId=in.readInt();
        setHeaderNameHolder(in.readByte() != 0);
        List<String> stringList = null;
        codeArray=in.createStringArray();
    }

    @Override
    public String toString() {
        return "Reports{" +
                "reportName='" + reportName + '\'' +
                ", reportDesc='" + reportDesc + '\'' +
                ", reportId=" + reportId +
                ", headerNameHolder=" + isHeaderNameHolder() +
                '}';
    }

    public synchronized static Reports getLastReportRun() {
        return lastReportRun;
    }

    public synchronized static void setLastReportRun(Reports lastReportRun) {
        Reports.lastReportRun = lastReportRun;
    }


    public boolean isHeaderNameHolder() {
        return headerNameHolder;
    }
}
