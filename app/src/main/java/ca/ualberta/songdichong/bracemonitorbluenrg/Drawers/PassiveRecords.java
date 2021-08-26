package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import android.util.Log;

import java.util.*;

public class PassiveRecords extends NonHeaderRecords {

    public PassiveRecords(int year, int month, int date, int hour, int minute, float force, float temperature){
        try{
            this.date = new Date(year-1900,month-1,date,hour,minute);
            this.forceVal = force;
            this.tempVal = temperature;
            this.averageForceVal = force;
            this.averageTempVal = temperature;
            this.longTermFlag = null;
        }
        catch (Exception e){

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassiveRecords records = (PassiveRecords) o;
        return date.equals(records.date) &&
                forceVal == records.forceVal&&
                tempVal == records.tempVal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, forceVal, tempVal);
    }

    public boolean dateEquals(Date r){
        return date.equals(r) ;
    }

}
