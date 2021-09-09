package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

public class HeaderRecords extends Records {
    public int subjectNumber;
    public int sampleRate;
    public float targetForce;

    public int allowance;

    public HeaderRecords(int subjectNumber, float targetForce, int sampleRate){
        this.subjectNumber = subjectNumber;
        this.sampleRate = sampleRate;
        this.targetForce = targetForce;
        this.isHeader = true;
    }

    public HeaderRecords(int subjectNumber, float targetPressure, int sampleRate, int allowance){
        this.subjectNumber = subjectNumber;
        this.sampleRate = sampleRate;
        this.targetForce = targetPressure;
        this.allowance = allowance;
        this.isHeader = true;
    }

    @Override
    public String getString(boolean isActive)
    {
        if (isActive){
            return "Subject number: " + subjectNumber + " Target Pressure (mmHg): " + String.format("%.0f",targetForce) +
                    " Allowance: " + allowance + " Sample rate: " + sampleRate + "\n";
        }else {
            return "Subject number: " + subjectNumber + " Target Force (N): " + String.format("%.2f",targetForce) + " Sample rate: " + sampleRate + "\n";
        }
    }
}
