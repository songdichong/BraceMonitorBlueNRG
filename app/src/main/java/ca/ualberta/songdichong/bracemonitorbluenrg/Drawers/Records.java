package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;

/**
 * @author songdichong
 *
 * @object Records: record specific time, force and temperature information to draw graph
 *
 * @constructor  int year, int month, int date, int hour, int minute, double force, double temperature
 *
 * @return int year, int month, int date, int hour, int minute, double force, double temperature
 */
public class Records {
    public boolean isHeader;
    int year;
    int month;
    int date;
    int hour;
    int minute;
    double forceVal;
    double tempVal;

    public int[] dateTime;
    public int[] time;

    public int subjectNumber;
    public double targetForce;
    public int sampleRate;

    public int targetPressure;
    public int allowance;
    Integer longTermFlag;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Records records = (Records) o;
        return year == records.year &&
                month == records.month &&
                date == records.date &&
                hour == records.hour &&
                minute == records.minute&&
                forceVal == records.forceVal&&
                tempVal == records.tempVal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, date, hour, minute, forceVal, tempVal);
    }

    public boolean dateEquals(Records r){
        return year == r.year &&
                month == r.month &&
                date == r.date &&
                hour == r.hour &&
                minute == r.minute ;
    }

    public Records(int year, int month, int date, int hour, int minute, double force, double temperature){
        this.year = year;
        this.month = month;
        this.date = date;
        this.hour = hour;
        this.minute = minute;
        this.forceVal = force;
        this.tempVal = temperature;
        this.isHeader = false;
        this.dateTime = new int[]{year, month, date, hour, minute};
        this.time = new int[]{hour,minute};
    }
    public Records(int year, int month, int date, int hour, int minute, double force, double temperature, int longTermFlag){
        this.year = year;
        this.month = month;
        this.date = date;
        this.hour = hour;
        this.minute = minute;
        this.forceVal = force;
        this.tempVal = temperature;
        this.longTermFlag = longTermFlag;
        this.isHeader = false;
        this.dateTime = new int[]{year, month, date, hour, minute};
        this.time = new int[]{hour,minute};
    }

    public Records(int subject_number, double target_force, int sampleRate)
    {
        this.isHeader = true;
        this.subjectNumber = subject_number;
        this.targetForce = target_force;
        this.sampleRate = sampleRate;
    }
    public Records(int subject_number, int targetPressure, int allowance, int sampleRate)
    {
        this.isHeader = true;
        this.subjectNumber = subject_number;
        this.targetPressure = targetPressure;
        this.allowance = allowance;
        this.sampleRate = sampleRate;
    }

    public String getHeaderStringPassive()
    {
        return "Subject number: " + subjectNumber + " Target Force (N): " + String.format("%.2f",targetForce) + " Sample rate: " + sampleRate + "\n";
    }

    public String getHeaderStringActive()
    {
        return "Subject number: " + subjectNumber + " Target Pressure (mmHg): " + targetPressure +  " Allowance: " + allowance + " Sample rate: " + sampleRate + "\n";
    }

    public double getForceVal() {
        return forceVal;
    }

    public double getTempVal() {
        return tempVal;
    }

    public int getMonth() {
        return month;
    }

    public int getDate() {
        return date;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getYear() {
        return year;
    }

    public Integer getLongTermFlag(){return longTermFlag;}
}
