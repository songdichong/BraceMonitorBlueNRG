package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import java.util.Date;

public class ActiveRecords extends NonHeaderRecords{
    public ActiveRecords(int year, int month, int date, int hour, int minute, float force, float temperature, int longTermFlag){
        try{
            this.date = new Date(year-1900,month-1,date,hour,minute);
            this.forceVal = force;
            this.averageForceVal = force;
            this.tempVal = temperature;
            this.averageTempVal = temperature;
            this.longTermFlag = longTermFlag;
            this.isHeader = false;
        }
        catch (Exception e){

        }
    }
}
