package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import java.util.Arrays;
import java.util.Date;

public abstract class NonHeaderRecords extends Records implements Comparable<Date>{
    Integer longTermFlag = null;
    Date date = null;
    float forceVal;
    float tempVal;
    float averageForceVal;
    float averageTempVal;

    public Date getDate(){
        return date;
    }

    public int[] getTime(){
        return new int[]{date.getHours(),date.getMinutes()};
    }

    public float getForceVal() {
        return forceVal;
    }

    public void setForceVal(float forceVal) {
        this.forceVal = forceVal;
    }

    public float getTempVal() {
        return tempVal;
    }

    public void setTempVal(float tempVal) {
        this.tempVal = tempVal;
    }

    public float getAverageForceVal() {
        return averageForceVal;
    }

    public void setAverageForceVal(float averageForceVal) {
        this.averageForceVal = averageForceVal;
    }

    public float getAverageTempVal() {
        return averageTempVal;
    }

    public void setAverageTempVal(float averageTempVal) {
        this.averageTempVal = averageTempVal;
    }

    @Override
    public int compareTo(Date o) {
        return this.date.compareTo(o);
    }

    public Integer getLongTermFlag() {
        return longTermFlag;
    }

    @Override
    public String getString(boolean isActive)
    {
        if (isActive){
            String[] outputArray = new String[]{String.format("%.1f",forceVal),String.format("%.1f",tempVal),
                    String.valueOf(date.getYear()+1900), String.valueOf(date.getMonth()+1),
                    String.valueOf(date.getDate()),String.valueOf(date.getHours()),
                    String.valueOf(date.getMinutes())};
            String output = Arrays.toString(outputArray);
            return output;
        }else {
            String[] outputArray = new String[]{String.format("%.1f",forceVal),String.format("%.1f",tempVal),
                    String.valueOf(longTermFlag),
                    String.valueOf(date.getYear()+1900), String.valueOf(date.getMonth()+1),
                    String.valueOf(date.getDate()),String.valueOf(date.getHours()),
                    String.valueOf(date.getMinutes())};
            String output = Arrays.toString(outputArray);
            return output;
        }
    }
}
