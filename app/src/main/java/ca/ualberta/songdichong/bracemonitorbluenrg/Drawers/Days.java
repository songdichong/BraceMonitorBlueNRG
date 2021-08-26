package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
/**
 * @author songdichong
 *
 * @object Drawers.Days: record Drawers.Records within the same day
 *
 * @constructor  int year, int month, int date
 *
 * @return List<Drawers.Records> recordsList
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
