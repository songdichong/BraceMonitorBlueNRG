package ca.ualberta.songdichong.bracemonitorbluenrg.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

Project Name       : Brace Monitor Android User Interface - Single

File Name          : AdvancedConfigurationFragment.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description: This file creates a view for the advanced configuration of a brace monitor.

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
public class  AdvancedConfigurationFragment extends PreferenceFragment {
    TextView forceText = null;
    TextView forceVoltage = null;
    TextView temperatureText = null;
    TextView temperatureVoltage = null;
    TextView deviceName;
    BluetoothLeService mBluetoothLeService;
    List<Byte> problematicBlock = new ArrayList<>();
    private byte block = 0;
    private byte status = 0;
    private PopupWindow popupWindowDownloading;
    private TextView mNumSamplesText;
    Thread currentThread = null;
    private Handler handler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothLeService = BluetoothLeService.getmBluetoothLeService();
        if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor){
            initializeActiveBraceMonitorAdvancedPage();
        }else {
            initializePassiveBraceMonitorAdvancedPage();
        }
    }

    /*
    * Function Name:    initializeActiveBraceMonitorAdvancedPage
    * Function Input:   None
    * Function Output:  None
    *
    * Function Detail:  Initialize the page for the active brace monitor.
    *                   Buttons under the pressure calibrations area are different from the passives.
    * */
    private void initializeActiveBraceMonitorAdvancedPage(){
        addPreferencesFromResource(R.xml.advanced_preferences_active);
        Preference setDeviceID = findPreference("set_device_identifier");
        setDeviceID.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showNumberPicker(999,0,0);
                return true;
            }
        });
        SwitchPreference changeLED = (SwitchPreference)findPreference("change_led");
        changeLED.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                mBluetoothLeService.directControlUnit(0,!enabled);
                ((SwitchPreference) preference).setChecked(!enabled);
                return true;
            }
        });
        SwitchPreference changeTemp = (SwitchPreference)findPreference("change_temp");
        changeTemp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                mBluetoothLeService.directControlUnit(1,!enabled);
                ((SwitchPreference) preference).setChecked(!enabled);
                return true;
            }
        });

        SwitchPreference changeLeak = (SwitchPreference)findPreference("change_leak");
        changeLeak.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                mBluetoothLeService.directControlUnit(2,!enabled);
                ((SwitchPreference) preference).setChecked(!enabled);
                return true;
            }
        });

        SwitchPreference changePumpPower = (SwitchPreference)findPreference("change_valve_power");
        changePumpPower.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                mBluetoothLeService.directControlUnit(4,!enabled);
                ((SwitchPreference) preference).setChecked(!enabled);
                return true;
            }
        });

        SwitchPreference changeSensorPower = (SwitchPreference)findPreference("change_sensor_power");
        changeSensorPower.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                mBluetoothLeService.directControlUnit(5,!enabled);
                ((SwitchPreference) preference).setChecked(!enabled);
                return true;
            }
        });
        Preference inflate_1s = findPreference("inflate_1s");
        inflate_1s.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.directControlUnit(6,true);
                return true;
            }
        });
        Preference deflate_1s = findPreference("deflate_1s");
        deflate_1s.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.directControlUnit(7,true);
                return true;
            }
        });

        Preference externalTestPref = findPreference("external_flash_test");
        if (mBluetoothLeService.getDeviceInfoVal() == 1){
            externalTestPref.setEnabled(false);
        }
        externalTestPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final int time = 64;
                builder.setMessage("This will take around " + time + "seconds to run the test")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mBluetoothLeService.externalTest();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setTitle("Warning!")
                        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }

        });

        Preference setThreshold= findPreference("set_threshold");
        setThreshold.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showThresholdInputDialog(0);
                return true;
            }
        });
        Preference readThreshold= findPreference("read_threshold");
        readThreshold.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                 mBluetoothLeService.getCalibrationValue(1);
                return true;
            }
        });
        Preference inputCalibrationVal = findPreference("input_calibration_val");
        inputCalibrationVal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showThresholdInputDialog(1);
                return true;
            }
        });
        Preference resetCalibration = findPreference("check_calibration_status");
        resetCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.getCalibrationValue(0);
                return true;
            }
        });

        Preference saveTempCalibration = findPreference("save_temp_calibration");
        saveTempCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showNumberPicker(30,10,1);
                return true;
            }
        });

        Preference readTempCalibration = findPreference("check_temp_calibration");
        readTempCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.updateTempLevel(false);
                return true;
            }
        });
    }
    /*
     * Function Name:    initializePassiveBraceMonitorAdvancedPage
     * Function Input:   None
     * Function Output:  None
     *
     * Function Detail:  Initialize the page for the passive brace monitor.
     *                   Buttons under the pressure calibrations area are different from the actives.
     * */
    private void initializePassiveBraceMonitorAdvancedPage(){
        addPreferencesFromResource(R.xml.advanced_preferences_passive);
        Preference setDeviceID = findPreference("set_device_identifier");
        setDeviceID.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showNumberPicker(999,0,0);
                return true;
            }
        });
        SwitchPreference changeLED = (SwitchPreference)findPreference("change_led");
        changeLED.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                mBluetoothLeService.directControlUnit(0,!enabled);
                ((SwitchPreference) preference).setChecked(!enabled);
                return true;
            }
        });
        SwitchPreference changeTemp = (SwitchPreference)findPreference("change_temp");
        changeTemp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                mBluetoothLeService.directControlUnit(1,!enabled);
                ((SwitchPreference) preference).setChecked(!enabled);
                return true;
            }
        });
        SwitchPreference changeForce = (SwitchPreference)findPreference("change_force");
        changeForce.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = ((SwitchPreference) preference).isChecked();
                mBluetoothLeService.directControlUnit(2,!enabled);
                ((SwitchPreference) preference).setChecked(!enabled);
                return true;
            }
        });

        Preference set0NCalibration = findPreference("set_0N_value");
        set0NCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.setCalibrationValue(0);
                return true;
            }
        });
        Preference set1NCalibration = findPreference("set_1N_value");
        set1NCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.setCalibrationValue(1);
                return true;
            }
        });
        Preference set2NCalibration = findPreference("set_2N_value");
        set2NCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.setCalibrationValue(2);
                return true;
            }
        });
        Preference set3NCalibration = findPreference("set_6N_value");
        set3NCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.setCalibrationValue(3);
                return true;
            }
        });
        Preference resetCalibration = findPreference("check_calibration_status");
        resetCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.getCalibrationValue(0);
                return true;
            }
        });
        Preference saveCalibration = findPreference("save_calibration");
        saveCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.getCalibrationValue(1);
                return true;
            }
        });
        Preference inputCaliValue = findPreference("input_cali_value");
        inputCaliValue.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showCalibrationInputDialog();
                return true;
            }
        });
        Preference saveTempCalibration = findPreference("save_temp_calibration");
        saveTempCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showNumberPicker(30,10,1);
                return true;
            }
        });

        Preference readTempCalibration = findPreference("check_temp_calibration");
        readTempCalibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.updateTempLevel(false);
                return true;
            }
        });

        Preference externalTestPref = findPreference("external_flash_test");
        if (mBluetoothLeService.getDeviceInfoVal() == 1){
            externalTestPref.setEnabled(false);
        }
        externalTestPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final int time = 64;
                builder.setMessage("This will take around " + time + "seconds to run the test")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mBluetoothLeService.externalTest();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setTitle("Warning!")
                        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }

        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_advanced_preference_layout, null);

        forceText = (TextView) v.findViewById(R.id.force_value);
        forceVoltage = (TextView) v.findViewById(R.id.force_voltage);
        temperatureText = (TextView) v.findViewById(R.id.temperature_value);
        temperatureVoltage = (TextView) v.findViewById(R.id.temperature_voltage);

        deviceName = (TextView) v.findViewById(R.id.device_name);
        deviceName.setText(BluetoothLeService.deviceName);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        filter.addAction(Constants.ACTION_TEMP_UPDATE);
        filter.addAction(Constants.ACTION_TEMPRAW_UPDATE);
        filter.addAction(Constants.ACTION_FORCE_UPDATE);
        filter.addAction(Constants.ACTION_EXTERNAL_TEST);

        getActivity().getApplicationContext().registerReceiver(updateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getApplicationContext().unregisterReceiver(updateReceiver);
    }

    /*
     * Function Name:    showNumberPicker
     * Function Input:   int max, max value of number picker
     *                   int min, min value of number picker
     *                   int type, 0: window for setting device ID, range [0,999]
     *                             1: window for setting current temperature calibration, range [30,10]
     * Function Output:  None
     *
     * Function Detail:  Initialize the page for the passive brace monitor.
     *                   Buttons under the pressure calibrations area are different from the actives.
     * */
    private void showNumberPicker(int max, int min, int type) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.number_picker_dialog, null, false);
        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(layout);
        popupWindow.setAnimationStyle(R.style.Animation);
        final NumberPicker numberPicker = (NumberPicker) layout.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(max);
        numberPicker.setMinValue(min);
        Button button = (Button) layout.findViewById(R.id.confirm);
        if (type == 0)
        {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBluetoothLeService.setDeviceID(numberPicker.getValue());
                    popupWindow.dismiss();
                }
            });
        }
        else if (type == 1)
        {
            numberPicker.setValue(23);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBluetoothLeService.setTempCalibration(numberPicker.getValue());
                    popupWindow.dismiss();
                }
            });
        }

        Button cancelButton = (Button) layout.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);
    }

    public int convertCaliVoltageToADC(double caliVal){
        int resultADCVal = (int)(((caliVal / 3) - 0.6) / 2.4 * 41260);
        return resultADCVal;
    }

    /*
     * Function Name:    showThresholdInputDialog
     * Function Input:   int type, 0: window for setting device ID, range [0,999]
     *                             1: window for setting current temperature calibration, range [30,10]
     * Function Output:  None
     *
     * Function Detail:  Initialize the page for the passive brace monitor.
     *                   Buttons under the pressure calibrations area are different from the actives.
     * */
    private void showThresholdInputDialog(int mode){
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.dialog_input_threshold, null, false);
        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(layout);
        popupWindow.setAnimationStyle(R.style.Animation);
        final EditText firstInput = layout.findViewById(R.id.first_input);
        final EditText secondInput = layout.findViewById(R.id.second_input);
        Button button = (Button) layout.findViewById(R.id.confirm);
        if (mode == 0){
            //calibration mode
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int lowThreshold = (int)Double.parseDouble(firstInput.getText().toString());
                    int highThreshold = (int)Double.parseDouble(secondInput.getText().toString());
                    mBluetoothLeService.setActiveThresholdValue(lowThreshold,highThreshold);
                    popupWindow.dismiss();
                }
            });
        } else if (mode == 1){
            firstInput.setHint("slope");
            secondInput.setHint("intercept");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double slope = Double.parseDouble(firstInput.getText().toString());
                    double intercept = Double.parseDouble(secondInput.getText().toString());
                    mBluetoothLeService.setActiveCalibrationValue(slope,intercept);
                    popupWindow.dismiss();
                }
            });
        }
        Button cancelButton = (Button) layout.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);
    }
    /*
     * Function Name:    showCalibrationInputDialog
     * Function Input:   None
     * Function Output:  None
     * Function Detail:  Show the calibration input dialog for passive brace monitors.
     *                   Then write the user input into the device's flash memory.
     * */
    private void showCalibrationInputDialog()
    {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.dialog_input_calibration, null, false);
        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(layout);
        popupWindow.setAnimationStyle(R.style.Animation);
        final EditText caliVal0 = layout.findViewById(R.id.cali_val_0N);
        final EditText caliVal1 = layout.findViewById(R.id.cali_val_1N);
        final EditText caliVal2 = layout.findViewById(R.id.cali_val_2N);
        final EditText caliVal6 = layout.findViewById(R.id.cali_val_6N);
        Button button = (Button) layout.findViewById(R.id.confirm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double cali0 = Double.parseDouble(caliVal0.getText().toString());
                Double cali1 = Double.parseDouble(caliVal1.getText().toString());
                Double cali2 = Double.parseDouble(caliVal2.getText().toString());
                Double cali6 = Double.parseDouble(caliVal6.getText().toString());
                int caliADC0 = convertCaliVoltageToADC(cali0);
                int caliADC1 = convertCaliVoltageToADC(cali1);
                int caliADC2 = convertCaliVoltageToADC(cali2);
                int caliADC6 = convertCaliVoltageToADC(cali6);
                mBluetoothLeService.setDeviceCalibrationVal(caliADC0,caliADC1,caliADC2,caliADC6);
                popupWindow.dismiss();
            }
        });
        Button cancelButton = (Button) layout.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);
    }

    /*
     * Object Name:    updateReceiver
     * Object Detail:  Receive and handle updates
     * */
    public final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //If disconnected restart app
            if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).restart();
                }
            }
            //If temperature value update is received update the text
            if (Constants.ACTION_TEMP_UPDATE.equals(action)) {
                double temperature = intent.getDoubleExtra(Constants.ACTION_TEMP_UPDATE,0);
                temperatureText.setText(String.format("%.1f",temperature) + "°C");
            }
            //If temperature raw value is received update the text
            else if (Constants.ACTION_TEMPRAW_UPDATE.equals(action)) {
                double temperature = intent.getDoubleExtra(Constants.ACTION_TEMPRAW_UPDATE,0);
                temperatureVoltage.setText(String.format("%.2f",temperature) + "V");
            }
            //If force value update is received update the text
            //Active: forceMeasurement =  slope * f + intercept
            //Passive: equation is a piecewise function.
            else if (Constants.ACTION_FORCE_UPDATE.equals(action)) {
                if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor) {
                    double forceValue = intent.getDoubleExtra(Constants.ACTION_FORCE_UPDATE,0);
                    forceVoltage.setText(String.format("%.2f",forceValue) + "V");
                    double[] forceCalibration = mBluetoothLeService.getForceCalibration();
                    double slope = forceCalibration[0];
                    double intercept = forceCalibration[1];
                    double forceMeasurement = slope * forceValue + intercept;
                    forceText.setText(String.format("%.2f",forceMeasurement) + "mmHg");
                }
                else {
                    double forceValue = intent.getDoubleExtra(Constants.ACTION_FORCE_UPDATE,0);
                    forceVoltage.setText(String.format("%.2f",forceValue) + "V");
                    double[] forceCalibration = mBluetoothLeService.getForceCalibration();
                    double forceMeasurement;
                    if (forceValue <= forceCalibration[1]) {
                        forceMeasurement = (forceValue-forceCalibration[0]) / (forceCalibration[1] - forceCalibration[0]) ;
                    }
                    else if (forceValue <= forceCalibration[2]) {
                        forceMeasurement = (forceValue-forceCalibration[1]) / (forceCalibration[2] - forceCalibration[1]) + 1;
                    }
                    else if (forceValue < forceCalibration[3]) {
                        forceMeasurement = (forceValue-forceCalibration[2]) / (forceCalibration[3] - forceCalibration[2]) * 4 + 2;
                    }
                    else{
                        forceMeasurement = (forceValue-forceCalibration[3]) / (forceCalibration[3] - forceCalibration[2]) * 4 + 6;
                    }
                    if (forceMeasurement<0) forceMeasurement = 0;
                    forceText.setText(String.format("%.2f",forceMeasurement) + "N");
                }
            }
            //If external test update is received update the text
            else if (Constants.ACTION_EXTERNAL_TEST.equals(action)) {
                byte[] array = intent.getByteArrayExtra(Constants.ACTION_EXTERNAL_TEST);
                if (getActivity() != null && (array!= null)) {
                    block = array[0];
                    status = array[1];
                    if (status != 1) {
                        problematicBlock.add(block);
                    }

                    if(popupWindowDownloading == null){
                        showDownloadingDialogue("External Test");
                    }
                    if(currentThread != null && currentThread.isAlive()) {
                        currentThread.interrupt();
                    }
                    currentThread = new UpdateBlockThread();
                    currentThread.start();
                }
            }
        }
    };
    /*
     * Object Name:    UpdateBlockThread
     * Object Detail:  update the text when ACTION_EXTERNAL_TEST update is received
     * */
    private class UpdateBlockThread extends Thread {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (mNumSamplesText != null) {
                        mNumSamplesText.setText("Current block: " + block + "\n" +
                                "Status: " + (status == (byte)1 ? "Success":"Fail"));
                        if (handler != null) {
                            handler.removeCallbacksAndMessages(null);
                        }
                        if (block == 63) {
                            if (problematicBlock.size() == 0){
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("All blocks passed the external memory test")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        })
                                        .setTitle("Success!")
                                        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                                AlertDialog alert = builder.create();
                                alert.show();
                            } else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("There are " + problematicBlock.size()+" blocks that have issues during memory test: \n"
                                        + Arrays.toString(problematicBlock.toArray()))
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        })
                                        .setTitle("Fail!")
                                        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        }
                        handler = new Handler();
                        final Runnable r = new Runnable() {
                            public void run() {
                                if(popupWindowDownloading != null){
                                    popupWindowDownloading.dismiss();
                                }
                            }
                        };
                        handler.postDelayed(r, 2000);
                    }
                    if (Thread.interrupted()) {
                        return;
                    }
                }
            });
        }
    }
    /*
     * Function Name:    showDownloadingDialogue
     * Function Input:   String text. The test that want to show up on the downloading spinner
     * Function Output:  None
     * Function Detail:  Show a download spinner when downloading values.
     * */
    public void showDownloadingDialogue(String text) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.dialog_download, null, false);
        popupWindowDownloading = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindowDownloading.setContentView(layout);
        popupWindowDownloading.setAnimationStyle(R.style.Animation);
        mNumSamplesText =  layout.findViewById(R.id.downloadText);
        mNumSamplesText.setText(text);
        popupWindowDownloading.showAtLocation(layout, Gravity.TOP, 0, 0);
    }

}


