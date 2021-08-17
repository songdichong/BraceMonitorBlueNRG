package ca.ualberta.songdichong.bracemonitorbluenrg.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothGatt;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;

import ca.ualberta.songdichong.bracemonitorbluenrg.AvgForcePlotActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.AvgTemperaturePlotActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.BluetoothLeService;
import ca.ualberta.songdichong.bracemonitorbluenrg.Constants;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Analyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Days;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
import ca.ualberta.songdichong.bracemonitorbluenrg.ForcePlotActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.R;
import ca.ualberta.songdichong.bracemonitorbluenrg.TemperaturePlotActivity;

public class ConfigureDrawerFragment extends Fragment {
    static public Analyzer analyzer;
    TextView startDateTime;
    TextView endDateTime;
    EditText referenceForceValue;
    EditText referenceTemperatureValue;
    ImageButton configStartDateTime;
    ImageButton configEndDateTime;
    ImageButton configReferenceForce;
    ImageButton configReferenceTemperature;
    Button drawForcePlotButton;
    Button drawTemperaturePlotButton;
    Button drawAvgForcePlotButton;
    Button drawAvgTemperaturePlotBUtton;
    int startYear;
    int startMonth;
    int startDayofMonth;
    int startHourofDay;
    int startMinuteofHour;
    int endYear;
    int endMonth;
    int endDayofMonth;
    int endHourofDay;
    int endMinuteofHour;
    double force = 1.00;
    double temperature = 28.0;
    BluetoothLeService bluetoothLeService = BluetoothLeService.getmBluetoothLeService();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_config_drawer_layout, container,false);
        startDateTime = rootView.findViewById(R.id.start_datetime);
        endDateTime = rootView.findViewById(R.id.end_datetime);
        configStartDateTime = rootView.findViewById(R.id.config_start_datetime_button);
        configEndDateTime = rootView.findViewById(R.id.config_end_datetime_button);
        referenceForceValue = rootView.findViewById(R.id.reference_force_value);
        referenceTemperatureValue = rootView.findViewById(R.id.reference_temperature_value);
        configReferenceForce = rootView.findViewById(R.id.config_reference_force_value_button);
        configReferenceTemperature = rootView.findViewById(R.id.config_reference_temperature_value_button);
        drawForcePlotButton = rootView.findViewById(R.id.draw_force_plot_button);
        drawTemperaturePlotButton = rootView.findViewById(R.id.draw_temperature_plot_button);
        drawAvgForcePlotButton = rootView.findViewById(R.id.draw_avg_force_plot_button);
        drawAvgTemperaturePlotBUtton = rootView.findViewById(R.id.draw_avg_temperature_plot_button);


        if (BluetoothLeService.downloadedData.size() == 0 ){
            Log.v("123","0");
            File file = new File(Environment.getExternalStorageDirectory(), Constants.FILENAME);
            analyzer = new Analyzer(file);
        }else{
            analyzer = new Analyzer(BluetoothLeService.downloadedData);
        }

        if (analyzer.getMyDaysList().size() == 0){
            Toast.makeText(getContext(), "No data is saved", Toast.LENGTH_LONG).show();
        }
        else{
            setStartEndDateTime();
            referenceForceValue.setText(String.format("%.2f", analyzer.getTargetForce() == 0 ? 1.00 : analyzer.getTargetForce()));
            configReferenceForce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String forceString = referenceForceValue.getText().toString();
                    if (!forceString.equals("")){
                        force = Double.parseDouble(forceString);
                    }
                    else{
                        force = analyzer.getTargetForce() == 0 ? 1.00 : analyzer.getTargetForce();
                    }
                    referenceForceValue.setText(String.format("%.2f",force));
                }
            });

            configReferenceTemperature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String temperatureString = referenceTemperatureValue.getText().toString();
                    if (!temperatureString.equals("")){
                        temperature = Double.valueOf(temperatureString);
                    }
                    else{
                        temperature = 28.0;
                    }
                    referenceTemperatureValue.setText(String.format("%.1f",temperature));
                }
            });
            if (bluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor){
                return rootView;
            }
            drawForcePlotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ForcePlotActivity.class);
                    intent.putExtra("force", force);
                    int[] startTime = new int[5];
                    startTime[0] = startYear;
                    startTime[1] = startMonth;
                    startTime[2] = startDayofMonth;
                    startTime[3] = startHourofDay;
                    startTime[4] = startMinuteofHour;
                    int[] endTime = new int[5];
                    endTime[0] = endYear;
                    endTime[1] = endMonth;
                    endTime[2] = endDayofMonth;
                    endTime[3] = endHourofDay;
                    endTime[4] = endMinuteofHour;
                    int[] startEndIndex = analyzer.getStartEndIndex(startTime,endTime);
                    if ( startEndIndex[1] == 0 || startEndIndex[0]>startEndIndex[1]){
                        Toast.makeText(getContext(), "Date selection is invalid.", Toast.LENGTH_LONG).show();
                    }else{
                        intent.putExtra("startEndIndex",startEndIndex);

                        startActivity(intent);
                    }
                }
            });

            drawTemperaturePlotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("here","drawtemp");
                    Intent intent = new Intent(getActivity(), TemperaturePlotActivity.class);
                    intent.putExtra("temperature", temperature);
                    int[] startTime = new int[5];
                    startTime[0] = startYear;
                    startTime[1] = startMonth;
                    startTime[2] = startDayofMonth;
                    startTime[3] = startHourofDay;
                    startTime[4] = startMinuteofHour;
                    int[] endTime = new int[5];
                    endTime[0] = endYear;
                    endTime[1] = endMonth;
                    endTime[2] = endDayofMonth;
                    endTime[3] = endHourofDay;
                    endTime[4] = endMinuteofHour;
                    int[] startEndIndex = analyzer.getStartEndIndex(startTime,endTime);
                    if (startEndIndex[1] == 0 || startEndIndex[0]>startEndIndex[1]){
                        Toast.makeText(getContext(), "Date selection is invalid", Toast.LENGTH_LONG).show();
                    }else{
                        intent.putExtra("startEndIndex",startEndIndex);
                        startActivity(intent);
                    }
                }
            });

            drawAvgForcePlotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), AvgForcePlotActivity.class);
                    int[] startTime = new int[5];
                    startTime[0] = startYear;
                    startTime[1] = startMonth;
                    startTime[2] = startDayofMonth;
                    startTime[3] = startHourofDay;
                    startTime[4] = startMinuteofHour;
                    int[] endTime = new int[5];
                    endTime[0] = endYear;
                    endTime[1] = endMonth;
                    endTime[2] = endDayofMonth;
                    endTime[3] = endHourofDay;
                    endTime[4] = endMinuteofHour;
                    int[] startEndIndex = analyzer.getStartEndIndex(startTime,endTime);
                    if ( startEndIndex[1] == 0 || startEndIndex[0]>startEndIndex[1]){
                        Toast.makeText(getContext(), "Date selection is invalid", Toast.LENGTH_LONG).show();
                    }else{
                        intent.putExtra("startEndIndex",startEndIndex);
                        startActivity(intent);
                    }
                }
            });

            drawAvgTemperaturePlotBUtton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), AvgTemperaturePlotActivity.class);
                    int[] startTime = new int[5];
                    startTime[0] = startYear;
                    startTime[1] = startMonth;
                    startTime[2] = startDayofMonth;
                    startTime[3] = startHourofDay;
                    startTime[4] = startMinuteofHour;
                    int[] endTime = new int[5];
                    endTime[0] = endYear;
                    endTime[1] = endMonth;
                    endTime[2] = endDayofMonth;
                    endTime[3] = endHourofDay;
                    endTime[4] = endMinuteofHour;
                    int[] startEndIndex = analyzer.getStartEndIndex(startTime,endTime);
                    if ( startEndIndex[1] == 0 || startEndIndex[0]>startEndIndex[1]){
                        Toast.makeText(getContext(), "Date selection is invalid", Toast.LENGTH_LONG).show();
                    }else{
                        intent.putExtra("startEndIndex",startEndIndex);
                        startActivity(intent);
                    }
                }
            });
        }
        return rootView;

    }

    public String getTimeString(int time){
        String result = String.valueOf(time);
        if (time<10){
            result = "0"+result;
        }
        return result;
    }


    public void setStartEndDateTime(){
        //downloaded data --> file --> nothing
        Days startDay = analyzer.getMyDaysList().get(0);
        Log.v("startDay",startDay.toString());
        Days endDay = analyzer.getMyDaysList().get(analyzer.getMyDaysList().size()-1);
        Log.v("endDay",endDay.toString());
        if (startDay.compareTo(endDay) > 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Some data are corrupted. The analyzer will analyze up to the last correct data.")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .setTitle("Warning!")
                    .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
            AlertDialog alert = builder.create();
            alert.show();
            if ( analyzer.getMyDaysList().size() > 1){
                for (int i = 1; i < analyzer.getMyDaysList().size(); i++){
                    Days currentDay = analyzer.getMyDaysList().get(i);
                    Days previousDay = analyzer.getMyDaysList().get(i-1);
                    if (currentDay.compareTo(previousDay) < 0){
                        endDay = previousDay;
                    }
                }
            } else{
                endDay = startDay;
            }
        }
        Records startRecord = startDay.getRecordsList().get(0);
        Records endRecord = endDay.getRecordsList().get(endDay.getRecordsList().size()-1);

        startYear = startRecord.getYear();
        startMonth = startRecord.getMonth();
        startDayofMonth = startRecord.getDate();
        startHourofDay = startRecord.getHour();
        startMinuteofHour = startRecord.getMinute();

        endYear = endRecord.getYear();
        endMonth = endRecord.getMonth();
        endDayofMonth = endRecord.getDate();
        endHourofDay = endRecord.getHour();
        endMinuteofHour = endRecord.getMinute();

        Calendar cal = Calendar.getInstance();
        cal.set(startYear,startMonth-1,startDayofMonth);
        final long startDateLong = cal.getTime().getTime();
        cal.set(endYear,endMonth-1,endDayofMonth);
        final long endDateLong = cal.getTime().getTime();

        startDateTime.setText(String.valueOf(startYear) + "-" + getTimeString(startMonth) + "-" +getTimeString(startDayofMonth) + " " + getTimeString(startHourofDay)+":"+getTimeString(startMinuteofHour));
        endDateTime.setText(String.valueOf(endYear) + "-" + getTimeString(endMonth) + "-" +getTimeString(endDayofMonth) + " " + getTimeString(endHourofDay)+":"+getTimeString(endMinuteofHour));

        configStartDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendarPopup(startDateLong,endDateLong,true);
            }
        });
        configEndDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendarPopup(startDateLong,endDateLong,false);
            }
        });
    }

    public void showCalendarPopup(long startDateLong, long endDateLong,final boolean isStart) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.calendar, null, false);

        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(layout);
        popupWindow.setAnimationStyle(R.style.Animation);
        CalendarView calendar = layout.findViewById(R.id.calendar);
        TextView calendarTitle = layout.findViewById(R.id.calendar_title);
        Button button = layout.findViewById(R.id.confirm);
        calendar.setMinDate(startDateLong);
        calendar.setMaxDate(endDateLong);

        if (isStart){
            calendarTitle.setText("Start Date");
            calendar.setDate(startDateLong);
            calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month,
                                                int dayOfMonth) {
                    startYear = year;
                    startMonth = month+1;
                    startDayofMonth = dayOfMonth;
                }
            });
        }else{
            calendarTitle.setText("End Date");
            calendar.setDate(endDateLong);
            calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month,
                                                int dayOfMonth) {
                    endYear = year;
                    endMonth = month+1;
                    endDayofMonth = dayOfMonth;
                }
            });
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                showTimePopup(isStart);
            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);
    }

    public void showTimePopup(boolean isStart) {
        if (isStart) {
            TimePickerDialog tpd = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            startHourofDay = hourOfDay;
                            startMinuteofHour = minute;
                            startDateTime.setText(String.valueOf(startYear) + "-" + getTimeString(startMonth) + "-" +getTimeString(startDayofMonth) + " " + getTimeString(startHourofDay)+":"+getTimeString(startMinuteofHour));
                        }
                    }, startHourofDay,startMinuteofHour, true);
            tpd.show();
        } else {
            TimePickerDialog tpd = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            endHourofDay = hourOfDay;
                            endMinuteofHour = minute;
                            endDateTime.setText(String.valueOf(endYear) + "-" + getTimeString(endMonth) + "-" +getTimeString(endDayofMonth) + " " + getTimeString(endHourofDay)+":"+getTimeString(endMinuteofHour));
                        }
                    }, endHourofDay, endMinuteofHour, true);
            tpd.show();
        }
    }
}
