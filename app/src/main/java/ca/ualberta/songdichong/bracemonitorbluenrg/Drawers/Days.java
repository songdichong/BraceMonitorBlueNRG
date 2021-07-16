package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author songdichong
 *
 * @object Days: record Records within the same day
 *
 * @constructor  int year, int month, int date
 *
 * @return List<Records> recordsList
 */
public class Days implements Comparable<Days>{
    int year;
    int month;
    int date;
    List<Records> recordsList;

    public Days(int year, int month, int date) {
        this.year = year;
        this.month = month;
        this.date = date;
        this.recordsList =  new ArrayList<>();
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Days days = (Days) o;
        return year == days.year &&
                month == days.month &&
                date == days.date;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, date);
    }

    public boolean isEqual(int year, int month, int day){
        return year == this.year &&
                month == this.month &&
                day == this.date;
    }

    public List<Records> getRecordsList() {
        return recordsList;
    }

    @Override
    public int compareTo(Days o) {
        if (this.year != o.year){
            return this.year > o.year ? 1:-1;
        }
        else if (this.year == o.year && this.month != o.month){
            return this.month > o.month ? 1: -1;
        }
        else if (this.year == o.year &&  this.month == o.month && this.date != o.date) {
            return this.date > o.date ? 1:-1;
        }
        return 0;
    }
    @Override
    public String toString(){
        return "yyyy-mm-dd: "+this.year+"-"+this.month+"-"+this.date;
    }
}
