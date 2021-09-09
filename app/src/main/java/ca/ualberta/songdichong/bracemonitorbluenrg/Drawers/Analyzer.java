package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import java.util.ArrayList;
import java.util.List;

public abstract class Analyzer {
    List<Days> myDaysList;
    List<Records> myRecordsList;
    int subjectNumber;
    int sampleRate;
    float targetForce;
    boolean corrupted = false;
    String deviceName;
    public List<Days> getMyDaysList() {
        return myDaysList;
    }

    float calculateAvg(float[] arrays){
        float total = 0;
        for(int i=0; i<arrays.length; i++){
            total = total + arrays[i];
        }
        return total/arrays.length;
    }


    public double getTargetForce() {
        return targetForce;
    }
    public List<Records> getMyRecordsList() {
        return myRecordsList;
    }
    public boolean isBlankData(){return corrupted;}

}
