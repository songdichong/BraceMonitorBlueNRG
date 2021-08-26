package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

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

}
