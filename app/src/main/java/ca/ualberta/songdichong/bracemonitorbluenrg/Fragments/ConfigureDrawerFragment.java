package ca.ualberta.songdichong.bracemonitorbluenrg.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import ca.ualberta.songdichong.bracemonitorbluenrg.AdjustmentHistoryActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.AvgForcePlotActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.AvgTemperaturePlotActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.BluetoothLeService;
import ca.ualberta.songdichong.bracemonitorbluenrg.Constants;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.ActiveAnalyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Analyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.NonHeaderRecords;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.PassiveAnalyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
import ca.ualberta.songdichong.bracemonitorbluenrg.ForcePlotActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.ForceTemperaturePlotActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.R;
import ca.ualberta.songdichong.bracemonitorbluenrg.TemperaturePlotActivity;
/*
Copyright © 2020, University of Alberta. All Rights Reserved.

This software is the confidential and proprietary information
of the Department of Electrical and Computer Engineering at the University of Alberta (UofA).
You shall not disclose such Confidential Information and shall use it only in accordance with the
terms of the license agreement you entered into at the UofA.

No part of the project, including this file, may be copied, propagated, or
distributed except with the explicit written permission of Dr. Edmond Lou
(elou@ualberta.ca).

Project Name       : Brace Monitor Android User Interface

File Name          : ConfigureDrawerFragment.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description: This file creates a view for configure analyze tools for the downloaded data from a brace monitor.

Project Structure:
 MainActivity : main activity of the project, all fragments are commit up on it

             ----> DeviceScanFragment (default): scan and connect with brace monitor devices
             ----> ConfigureDrawerFragment: configure analyze tools for the downloaded data from a brace monitor
                        ----> Other PlotActivities are started here
   navigator ----> ConfigureSensorFragment: configure settings of a brace monitor
             ----> GraphConfigurationFragment: change number of graph displaced in RealTimePlotFragment
             ----> OutputDataFragment: download long-term data from a brace monitor and export it
             ----> RealTimePlotFragment: plot the real-time force/pressure figure for all connected brace monitors
             ----> AdvancedConfigurationFragment: configure advanced settings of a brace monitor
             ----> CalibrationFragment: calibrate an active brace monitor (active only)

 singleton object:  1.  mBluetoothLeService, handle all the communications of all connected device
                    2.  analyzer, handle the analysis tools using Android device
 */
public class ConfigureDrawerFragment extends Fragment {
    static public Analyzer analyzer; //Singleton object to handle the analysis tools using Android device
    static int analyzeMode = 0; //0 == passive, 1 == active. Should have use enum
    TextView startDateTime;
    TextView endDateTime;
    TextView referenceForceValueTitle;
    EditText referenceForceValue;
    EditText referenceTemperatureValue;
    ImageButton configStartDateTime;
    ImageButton configEndDateTime;
    ImageButton configReferenceForce;
    ImageButton configReferenceTemperature;
    Button drawForcePlotButton;
    Button drawTemperaturePlotButton;
    Button drawForceTemperaturePlotButton;
    Button drawAvgForcePlotButton;
    Button drawAvgTemperaturePlotButton;
    Button showAdjustDetailButton;
    Switch analyzerModeSwitch;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*Assign fields to view*/
        View rootView = inflater.inflate(R.layout.fragment_config_drawer_layout, container,false);
        startDateTime = rootView.findViewById(R.id.start_datetime);
        endDateTime = rootView.findViewById(R.id.end_datetime);
        configStartDateTime = rootView.findViewById(R.id.config_start_datetime_button);
        configEndDateTime = rootView.findViewById(R.id.config_end_datetime_button);
        referenceForceValue = rootView.findViewById(R.id.reference_force_value);
        referenceForceValueTitle = rootView.findViewById(R.id.reference_force_value_title);
        referenceTemperatureValue = rootView.findViewById(R.id.reference_temperature_value);
        configReferenceForce = rootView.findViewById(R.id.config_reference_force_value_button);
        configReferenceTemperature = rootView.findViewById(R.id.config_reference_temperature_value_button);
        drawForcePlotButton = rootView.findViewById(R.id.draw_force_plot_button);
        drawTemperaturePlotButton = rootView.findViewById(R.id.draw_temperature_plot_button);
        drawForceTemperaturePlotButton = rootView.findViewById(R.id.draw_force_temperature_plot_button);
        drawAvgForcePlotButton = rootView.findViewById(R.id.draw_avg_force_plot_button);
        drawAvgTemperaturePlotButton = rootView.findViewById(R.id.draw_avg_temperature_plot_button);
        showAdjustDetailButton = rootView.findViewById(R.id.show_adjustment_enabled_button);
        analyzerModeSwitch = rootView.findViewById(R.id.mode_switch);
        /*set listeners for switch/button*/
        analyzerModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //If set to checked, switch to active mode
                if (isChecked) {
                    referenceForceValueTitle.setText(R.string.reference_force_active);
                    drawForcePlotButton.setText(R.string.force_plot_active);
                    drawForceTemperaturePlotButton.setText(R.string.force_temp_active);
                    drawAvgForcePlotButton.setText(R.string.force_average_active);
                    showAdjustDetailButton.setEnabled(true);
                    if (BluetoothLeService.downloadedData.size() == 0 ){
                        File file = new File(Environment.getExternalStorageDirectory(), Constants.ACTIVE_FILENAME);
                        analyzer = new ActiveAnalyzer(file);
                    }else{
                        analyzer = new ActiveAnalyzer(BluetoothLeService.downloadedData);
                    }

                    analyzeMode = 1;
                    Toast.makeText(getContext(), "Analyze active brace monitor now.", Toast.LENGTH_LONG).show();
                }
                //If set to unchecked, switch to passive mode
                else{
                    referenceForceValueTitle.setText(R.string.reference_force_passive);
                    drawForcePlotButton.setText(R.string.force_plot_passive);
                    drawForceTemperaturePlotButton.setText(R.string.force_temp_passive);
                    drawAvgForcePlotButton.setText(R.string.force_average_passive);
                    showAdjustDetailButton.setEnabled(false);
                    if (BluetoothLeService.downloadedData.size() == 0 ){
                        File file = new File(Environment.getExternalStorageDirectory(), Constants.FILENAME);
                        analyzer = new PassiveAnalyzer(file);
                    }else{
                        analyzer = new PassiveAnalyzer(BluetoothLeService.downloadedData);
                    }

                    analyzeMode = 0;
                    Toast.makeText(getContext(), "Analyze passive brace monitor now.", Toast.LENGTH_LONG).show();
                }
                setStartEndDateTime();
                referenceForceValue.setText(String.format("%.1f", analyzer.getTargetForce()));
                referenceTemperatureValue.setText(String.format("%.1f", analyzer.getTargetTemperature()));
            }
        });

        configReferenceForce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String forceString = referenceForceValue.getText().toString();
                if (!forceString.equals("")){
                    analyzer.setTargetForce(Float.parseFloat(forceString));
                }
                referenceForceValue.setText(String.format("%.1f",analyzer.getTargetForce()));
            }
        });

        configReferenceTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temperatureString = referenceTemperatureValue.getText().toString();
                if (!temperatureString.equals("")){
                    analyzer.setTargetTemperature(Float.parseFloat(temperatureString));
                }

                referenceTemperatureValue.setText(String.format("%.1f",analyzer.getTargetTemperature()));
            }
        });

        drawForcePlotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ForcePlotActivity.class);
                intent.putExtra("force", analyzer.getTargetForce());
                int[] startTime = new int[10];
                startTime[0] = startYear;
                startTime[1] = startMonth;
                startTime[2] = startDayofMonth;
                startTime[3] = startHourofDay;
                startTime[4] = startMinuteofHour;
                startTime[5] = endYear;
                startTime[6] = endMonth;
                startTime[7] = endDayofMonth;
                startTime[8] = endHourofDay;
                startTime[9] = endMinuteofHour;
                Date start = new Date(startYear,startMonth,startDayofMonth,startHourofDay,startMinuteofHour);
                Date end = new Date(endYear,endMonth,endDayofMonth,endHourofDay,endMinuteofHour);
                if (start.compareTo(end) >= 0){
                    Toast.makeText(getContext(), "Date selection is invalid.", Toast.LENGTH_LONG).show();
                }else{
                    intent.putExtra("startEndIndex",startTime);
                    startActivity(intent);
                }
            }
        });

        drawTemperaturePlotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TemperaturePlotActivity.class);
                intent.putExtra("temperature", analyzer.getTargetTemperature());
                int[] startTime = new int[10];
                startTime[0] = startYear;
                startTime[1] = startMonth;
                startTime[2] = startDayofMonth;
                startTime[3] = startHourofDay;
                startTime[4] = startMinuteofHour;
                startTime[5] = endYear;
                startTime[6] = endMonth;
                startTime[7] = endDayofMonth;
                startTime[8] = endHourofDay;
                startTime[9] = endMinuteofHour;
                Date start = new Date(startYear,startMonth,startDayofMonth,startHourofDay,startMinuteofHour);
                Date end = new Date(endYear,endMonth,endDayofMonth,endHourofDay,endMinuteofHour);
                if (start.compareTo(end) >= 0){
                    Toast.makeText(getContext(), "Date selection is invalid", Toast.LENGTH_LONG).show();
                }else{
                    intent.putExtra("startEndIndex",startTime);
                    startActivity(intent);
                }
            }
        });

        drawForceTemperaturePlotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ForceTemperaturePlotActivity.class);
                intent.putExtra("temperature", analyzer.getTargetTemperature());
                intent.putExtra("force", analyzer.getTargetForce());
                int[] startTime = new int[10];
                startTime[0] = startYear;
                startTime[1] = startMonth;
                startTime[2] = startDayofMonth;
                startTime[3] = startHourofDay;
                startTime[4] = startMinuteofHour;
                startTime[5] = endYear;
                startTime[6] = endMonth;
                startTime[7] = endDayofMonth;
                startTime[8] = endHourofDay;
                startTime[9] = endMinuteofHour;
                Date start = new Date(startYear,startMonth,startDayofMonth,startHourofDay,startMinuteofHour);
                Date end = new Date(endYear,endMonth,endDayofMonth,endHourofDay,endMinuteofHour);
                if (start.compareTo(end) >= 0){
                    Toast.makeText(getContext(), "Date selection is invalid", Toast.LENGTH_LONG).show();
                }else{
                    intent.putExtra("startEndIndex",startTime);
                    startActivity(intent);
                }
            }
        });


        drawAvgForcePlotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AvgForcePlotActivity.class);
                int[] startTime = new int[10];
                startTime[0] = startYear;
                startTime[1] = startMonth;
                startTime[2] = startDayofMonth;
                startTime[3] = startHourofDay;
                startTime[4] = startMinuteofHour;
                startTime[5] = endYear;
                startTime[6] = endMonth;
                startTime[7] = endDayofMonth;
                startTime[8] = endHourofDay;
                startTime[9] = endMinuteofHour;
                Date start = new Date(startYear,startMonth,startDayofMonth,startHourofDay,startMinuteofHour);
                Date end = new Date(endYear,endMonth,endDayofMonth,endHourofDay,endMinuteofHour);
                if (start.compareTo(end) >= 0){
                    Toast.makeText(getContext(), "Date selection is invalid", Toast.LENGTH_LONG).show();
                }else{
                    intent.putExtra("startEndIndex",startTime);
                    startActivity(intent);
                }
            }
        });

        drawAvgTemperaturePlotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AvgTemperaturePlotActivity.class);
                int[] startTime = new int[10];
                startTime[0] = startYear;
                startTime[1] = startMonth;
                startTime[2] = startDayofMonth;
                startTime[3] = startHourofDay;
                startTime[4] = startMinuteofHour;
                startTime[5] = endYear;
                startTime[6] = endMonth;
                startTime[7] = endDayofMonth;
                startTime[8] = endHourofDay;
                startTime[9] = endMinuteofHour;
                Date start = new Date(startYear,startMonth,startDayofMonth,startHourofDay,startMinuteofHour);
                Date end = new Date(endYear,endMonth,endDayofMonth,endHourofDay,endMinuteofHour);
                if (start.compareTo(end) >= 0){
                    Toast.makeText(getContext(), "Date selection is invalid", Toast.LENGTH_LONG).show();
                }else{
                    intent.putExtra("startEndIndex",startTime);
                    startActivity(intent);
                }
            }
        });
        showAdjustDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (analyzeMode == 1){
                    Intent intent = new Intent(getActivity(), AdjustmentHistoryActivity.class);
                    int[] startTime = new int[10];
                    startTime[0] = startYear;
                    startTime[1] = startMonth;
                    startTime[2] = startDayofMonth;
                    startTime[3] = startHourofDay;
                    startTime[4] = startMinuteofHour;
                    startTime[5] = endYear;
                    startTime[6] = endMonth;
                    startTime[7] = endDayofMonth;
                    startTime[8] = endHourofDay;
                    startTime[9] = endMinuteofHour;
                    Date start = new Date(startYear,startMonth,startDayofMonth,startHourofDay,startMinuteofHour);
                    Date end = new Date(endYear,endMonth,endDayofMonth,endHourofDay,endMinuteofHour);
                    if (start.compareTo(end) >= 0){
                        Toast.makeText(getContext(), "Date selection is invalid", Toast.LENGTH_LONG).show();
                    }else{
                        intent.putExtra("startEndIndex",startTime);
                        startActivity(intent);
                    }
                }
            }
        });
        return rootView;
    }

    /**
     * 1. Generate analyzer object when return back to the page, if the analyzer is not assigned yet
     * 2. Set UI accordingly
     * */
    @Override
    public void onResume() {
        super.onResume();
        analyzerModeSwitch.setChecked(analyzeMode == 1);
        if (analyzeMode == 0) {
            if (BluetoothLeService.downloadedData.size() == 0 ){
                File file = new File(Environment.getExternalStorageDirectory(), Constants.FILENAME);
                analyzer = new PassiveAnalyzer(file);
            }else{
                analyzer = new PassiveAnalyzer(BluetoothLeService.downloadedData);
            }
            referenceForceValueTitle.setText(R.string.reference_force_passive);
            drawForcePlotButton.setText(R.string.force_plot_passive);
            drawForceTemperaturePlotButton.setText(R.string.force_temp_passive);
            drawAvgForcePlotButton.setText(R.string.force_average_passive);
            showAdjustDetailButton.setEnabled(false);
        } else if (analyzeMode == 1)  {
            if (BluetoothLeService.downloadedData.size() == 0 ){
                File file = new File(Environment.getExternalStorageDirectory(), Constants.ACTIVE_FILENAME);
                analyzer = new ActiveAnalyzer(file);
            }else{
                analyzer = new ActiveAnalyzer(BluetoothLeService.downloadedData);
            }
            referenceForceValueTitle.setText(R.string.reference_force_active);
            drawForcePlotButton.setText(R.string.force_plot_active);
            drawForceTemperaturePlotButton.setText(R.string.force_temp_active);
            drawAvgForcePlotButton.setText(R.string.force_average_active);
            showAdjustDetailButton.setEnabled(true);
        }

        referenceForceValue.setText(String.format("%.1f", analyzer.getTargetForce() == 0 ? 1.00 : analyzer.getTargetForce()));


        if (analyzer.getMyDaysList().size() == 0){
            Toast.makeText(getContext(), "No data is saved", Toast.LENGTH_LONG).show();
        }
        else{
            setStartEndDateTime();
        }
    }

    /*
     * Function Name: getTimeString
     *
     * Function Input: int time
     * Output: String time:
     *
     * Function Detail: if time<10 add a 0. Otherwise return string(time).
     * */
    public String getTimeString(int time){
        String result = String.valueOf(time);
        if (time<10){
            result = "0"+result;
        }
        return result;
    }
    /*
     * Function Name: setStartEndDateTime
     *
     * Function Input/Output: None
     *
     * Function Detail: find start and end time of analyzer and set start end time in UI
     *
     * Use first one and last correct one as start and end, since there could be corruption of data.
     * Corruption happens when brace monitor loss the power but restored immediately, then the time data is lost and date is reset to 2000/Jan/01
     * In clinical trials, since all data are erased, the start should always be correct, and we find the last correct data based on start.
     *
     * */
    public void setStartEndDateTime(){
        //downloaded data --> file --> nothing
        if (analyzer.isBlankData()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Some data are corrupted. The analyzer will analyze the correct data only.")
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
        }

        NonHeaderRecords startRecord = null, endRecord = null;
        for (int i = 0; i < analyzer.getMyRecordsList().size(); i++){
            Records current = analyzer.getMyRecordsList().get(i);
            if (!current.isHeader) {
                startRecord = (NonHeaderRecords) current;
                break;
            }
        }
        for (int i = analyzer.getMyRecordsList().size()-1; i >= 0; i--){
            Records current = analyzer.getMyRecordsList().get(i);
            if (!current.isHeader) {
                endRecord = (NonHeaderRecords) current;
                break;
            }
        }

        if (startRecord != null){
            startYear = startRecord.getDate().getYear()+1900;
            startMonth = startRecord.getDate().getMonth()+1;
            startDayofMonth = startRecord.getDate().getDate();
            startHourofDay = startRecord.getDate().getHours();
            startMinuteofHour = startRecord.getDate().getMinutes();
        }

        if (endRecord != null){
            endYear = endRecord.getDate().getYear()+1900;
            endMonth = endRecord.getDate().getMonth()+1;
            endDayofMonth = endRecord.getDate().getDate();
            endHourofDay = endRecord.getDate().getHours();
            endMinuteofHour = endRecord.getDate().getMinutes();
        }


        Calendar cal = Calendar.getInstance();
        cal.set(startYear,startMonth-1,startDayofMonth);
        final long startDateLong = cal.getTime().getTime();
        cal.set(endYear,endMonth-1,endDayofMonth);
        final long endDateLong = cal.getTime().getTime();

        if (startRecord == null && endRecord == null){
            startDateTime.setText("");
            endDateTime.setText("");
            configStartDateTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    return;
                }
            });
            configEndDateTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    return;
                }
            });
        }else{
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

    }
    /*
     * Function Name: showCalendarPopup
     *
     * Function Input: long startDateLong: start date in milliseconds
     *                 long endDateLong: end date in milliseconds
     *                 boolean isStart: whether the window is for choosing start date or not
     * Function Output: None
     * Function Detail: pop up a window and let user select date between start end and end time
     *
     * */
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
    /*
     * Function Name: showTimePopup
     *
     * Function Input: boolean isStart: whether the window is for choosing start date or not
     * Function Output: None
     * Function Detail: pop up a window and let user select time between 0-24
     * */
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
    public static int getAnalyzeMode() {
        return analyzeMode;
    }
    public static void setAnalyzeMode(int mode) {
        analyzeMode = mode;
    }
}
