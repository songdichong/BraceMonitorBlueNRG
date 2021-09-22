package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
/*
Copyright © 2020, University of Alberta. All Rights Reserved.

This software is the confidential and proprietary information
of the Department of Electrical and Computer Engineering at the University of Alberta (UofA).
You shall not disclose such Confidential Information and shall use it only in accordance with the
terms of the license agreement you entered into at the UofA.

No part of the project, including this file, may be copied, propagated, or
distributed except with the explicit written permission of Dr. Edmond Lou
(elou@ualberta.ca).

Project Name       : Brace Monitor Android User Interface - Single

File Name          : Days.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description   : This is the class of days. It is identified by a java.Util.Date object and contains
                     information of all records within this day.

Data Structure:
                    Analyzer:   List<Days>: contains days information
                                List<Records>: contains all records information

                    Days:       List<Records>: contains records information for that specific day

                    Records(abstract)----!isHeader---->NonHeaderRecords(abstract)------->ActiveRecords
                       |                                                        |------->PassiveRecords
                      |----------------------isHeader---------------------------------->HeaderRecords
*/
public class Days implements Comparable<Days>{
    Date date;
    List<Records> recordsList;

    public Days(int year, int month, int date) {
        this.recordsList =  new ArrayList<>();
        try{
            this.date = new Date(year-1900,month-1,date);
        }
        catch (Exception e){

        }
    }
    public Date getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Days days = (Days) o;
        return date.equals(days.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash( date);
    }

    public boolean belongsTo(NonHeaderRecords rec){
        if (date == null) return false;
        return date.getYear() == rec.getDate().getYear() &&
                date.getMonth() == rec.getDate().getMonth() &&
                date.getDate() == rec.getDate().getDate();
    }

    public List<Records> getRecordsList() {
        return recordsList;
    }

    @Override
    public int compareTo(Days o) {
        return date.compareTo(o.date);

    }
    @Override
    public String toString(){
        return "yyyy-mm-dd: "+this.date.toString();
    }
}
