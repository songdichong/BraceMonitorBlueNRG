package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

public class HeaderRecords extends Records {
    public int subjectNumber;
    public int sampleRate;
    public float targetForce;

    public int targetPressure;
    public int allowance;

    public HeaderRecords(int subjectNumber, float targetForce, int sampleRate){
        this.subjectNumber = subjectNumber;
        this.sampleRate = sampleRate;
        this.targetForce = targetForce;
        this.isHeader = true;
    }

    public HeaderRecords(int subjectNumber, int targetPressure, int allowance, int sampleRate){
        this.subjectNumber = subjectNumber;
        this.sampleRate = sampleRate;
        this.targetPressure = targetPressure;
        this.allowance = allowance;
        this.isHeader = true;
    }

    @Override
    public String getString(boolean isActive)
    {
        if (isActive){
            return "Subject number: " + subjectNumber + " Target Pressure (mmHg): " + targetPressure +
                    " Allowance: " + allowance + " Sample rate: " + sampleRate + "\n";

        }else {
            return "Subject number: " + subjectNumber + " Target Force (N): " + String.format("%.2f",targetForce) + " Sample rate: " + sampleRate + "\n";
        }
    }
}
