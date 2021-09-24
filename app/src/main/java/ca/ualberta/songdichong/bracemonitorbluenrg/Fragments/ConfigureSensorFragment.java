package ca.ualberta.songdichong.bracemonitorbluenrg.Fragments;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

File Name          : ConfigureSensorFragment.java

Original Author    : Dichong Song

File Creation Date : 2021/03/26

File Description   : Configure Sensor Fragment. It extends preference fragment as super class.
This fragment can be accessed by pressing "Configuration" in the NavigationView.

Main Layout File:    fragment_preferences_layout
 *
*/
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Arrays;
import java.util.prefs.PreferenceChangeEvent;

import ca.ualberta.songdichong.bracemonitorbluenrg.BluetoothLeService;
import ca.ualberta.songdichong.bracemonitorbluenrg.Constants;
import ca.ualberta.songdichong.bracemonitorbluenrg.MainActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.R;
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

File Name          : ConfigureSensorFragment.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description: This file creates a view for configure settings of a brace monitor.

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
public class ConfigureSensorFragment extends PreferenceFragment{
    TextView batteryText;
    TextView temperatureText;
    TextView deviceName;
    TextView versionText;
    TextView memoryText;
    BluetoothLeService mBluetoothLeService;
    /*1. Assign buttons(preference) using findPreference function.
     2. Assign onPreferenceClickListener for the buttons(preference)*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothLeService = BluetoothLeService.getmBluetoothLeService();
        addPreferencesFromResource(R.xml.configure_preferences_passive);
        Preference samplingRate = findPreference("sampling_rate");
        samplingRate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity)getActivity()).showNumberPicker(0);
                return true;
            }
        });

        Preference deviceHolder = findPreference("set_subject");
        deviceHolder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity)getActivity()).showNumberPicker(1);
                return true;
            }
        });
        Preference setSleepButton = findPreference("sleep_time");
        if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor){
            setSleepButton.setEnabled(true);
        }
        setSleepButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showTimePopup(1);
                return true;
            }
        });
        Preference setWakeButton = findPreference("wake_time");
        if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor){
            setWakeButton.setEnabled(true);
        }
        setWakeButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showTimePopup(2);
                return true;
            }
        });

        Preference saveButton = findPreference("save");
        saveButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.directControlUnit(3,true);
                return true;
            }
        });


        Preference startLongTerm = findPreference("start_long_term");
        startLongTerm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to begin long-term sampling?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (BluetoothLeService.calibrated) {
                                    mBluetoothLeService.startLongTerm();
                                } else {
                                    Toast.makeText(getActivity(), "Device not calibrated!", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setTitle("Confirm")
                        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_info));
                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_preferences_layout, null);
        batteryText = (TextView) v.findViewById(R.id.battery_value);
        temperatureText = (TextView) v.findViewById(R.id.temperature_value);
        deviceName = (TextView) v.findViewById(R.id.device_name);
        versionText = (TextView) v.findViewById(R.id.version_value);
        memoryText = (TextView) v.findViewById(R.id.memory_value);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        batteryText.setText(String.format("%.2f",mBluetoothLeService.batteryVal) + "V");
        deviceName.setText(mBluetoothLeService.deviceName);
        versionText.setText("v"+mBluetoothLeService.getDeviceVersionVal());
        Log.v("currentAddress",mBluetoothLeService.getTotalAddress()+"");
        if (mBluetoothLeService.getTotalAddress() != Integer.MIN_VALUE) {
            double percentage = ((double)mBluetoothLeService.getTotalAddress() / 0x3FFFFF * 100);
            memoryText.setText(String.format("%.1f",percentage)+"%");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_TEMP_UPDATE);
        filter.addAction(Constants.ACTION_BATTERY_READ);
        filter.addAction(Constants.ACTION_VERSION_UPDATE);
        filter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        getActivity().getApplicationContext().registerReceiver(updateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getApplicationContext().unregisterReceiver(updateReceiver);
    }
    /*
     * Object Name: updateReceiver
     *
     * Object Detail: When receive specific actions (packaged by specific Constants), update current fragment text
     * */
    public final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //When ACTION_TEMP_UPDATE of current braceMonitorDevice is received, update temperatureText
            if (Constants.ACTION_TEMP_UPDATE.equals(action)) {
                double temperature = intent.getDoubleExtra(Constants.ACTION_TEMP_UPDATE,0);
                temperatureText.setText(String.format("%.1f",temperature) + "°C");
            }

            else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).restart();
                }
            }

            else if (Constants.ACTION_BATTERY_READ.equals(action)) {
                double batt = intent.getDoubleExtra(Constants.ACTION_BATTERY_READ,0);
                batteryText.setText(String.format("%.2f",batt) + "V");
            }

            else if (Constants.ACTION_VERSION_UPDATE.equals(action)) {
                int[] array = intent.getIntArrayExtra(Constants.ACTION_VERSION_UPDATE);
                if (array!= null) {
                    int version = array[0];
                    int address = array[1];
                    versionText.setText("v"+version);
                    if (mBluetoothLeService.getTotalAddress() != Integer.MIN_VALUE) {
                        double percentage = ((double)address / 0x3FFFFF * 100);
                        memoryText.setText(String.format("%.1f",percentage)+"%");
                    }
                }
            }
        }
    };
    /*
     * Function Name: showTimePopup
     *
     * Function Input: None
     * Function Output: None
     * Function Detail: pop up a window for 2 times and let user select time between 0-24.
     * */
    private void showTimePopup(int sleepOrWake) {
        //sleep
        if (sleepOrWake == 1){
            TimePickerDialog tpd = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            byte[] sendingWake = new byte[]{(byte)0xFF,(byte)0xFF, (byte)hourOfDay,(byte)minute,(byte)0xFF,(byte)0xFF};
                            mBluetoothLeService.setDeviceWakeupTime(sendingWake);
                        }
                    }, 22,0, true);
            tpd.show();
        }

        else if (sleepOrWake == 2){
            TimePickerDialog tpd = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            byte[] sendingWake = new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte) hourOfDay, (byte) minute};
                            mBluetoothLeService.setDeviceWakeupTime(sendingWake);
                        }
                    }, 8,0, true);
            tpd.show();
        }

    }
}
