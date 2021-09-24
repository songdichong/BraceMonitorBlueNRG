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

File Name          : MainActivity.java

Original Author    : Dichong Song

File Creation Date : 2021/03/26

File Description   : The main activity of the android project. The default fragment is DeviceScanFragment.

Main Layout File    : activity_main (an empty layout) and nav_view (NavigationView)
*/
package ca.ualberta.songdichong.bracemonitorbluenrg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.AdvancedConfigurationFragment;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.CalibrationFragment;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.ConfigureDrawerFragment;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.ConfigureSensorFragment;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.DeviceScanFragment;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.OutPutDataFragment;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.RealTimePlotFragment;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {
    //Bluetooth low energy object. The read/write/notify methods are all included in this object
    static private BluetoothLeService mBluetoothLeService;
    //Fragment Manager object. It is used to switch the fragment in the project
    public FragmentManager fragmentManager;
    //CharSequence Object. It is used to update the title of the activity when fragment is changed.
    private CharSequence mTitle;
    //The project needs bluetooth permission and access fine location when using bluetooth
    //The project needs read external storage permission and write external storage permission to download and send downloaded file
    String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean advancedModeUnlocked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getFragmentManager();
        mTitle = getTitle();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        requestPermission();
        if (savedInstanceState == null){
            mBluetoothLeService = BluetoothLeService.getmBluetoothLeService();
            mBluetoothLeService.setContext(this);
            startScanFragment();
        } else {
            restart();
        }
    }

    //When press back. If there is previous fragment, pop back. Otherwise hang the app.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor){
            NavigationView navigationView = findViewById(R.id.nav_view);
            Menu menuNav=navigationView.getMenu();
            MenuItem nav_item2 = menuNav.findItem(R.id.nav_calibration);
            nav_item2.setEnabled(true);
        }
        if (id == R.id.nav_realtime_plot) {
            if (BluetoothLeService.connected){
                mTitle = "Real Time Force Plot";
                fragmentManager.beginTransaction().replace(R.id.container,new RealTimePlotFragment()).addToBackStack(null).commit();
            }
        }

        else if (id == R.id.nav_config_sensor) {
            if (BluetoothLeService.connected){
                mTitle = "Configuration";
                fragmentManager.beginTransaction().replace(R.id.container,new ConfigureSensorFragment()).addToBackStack(null).commit();
            }
        }

        else if (id == R.id.nav_download_data){
            if (BluetoothLeService.connected){
                mTitle = "Output Data";
                fragmentManager.beginTransaction().replace(R.id.container,new OutPutDataFragment()).addToBackStack(null).commit();
            }
        }

        else if (id == R.id.nav_advanced_config) {
            if (BluetoothLeService.connected){
                if (advancedModeUnlocked){
                    mTitle = "Advanced Configuration";
                    fragmentManager.beginTransaction().replace(R.id.container,new AdvancedConfigurationFragment()).addToBackStack(null).commit();
                }
                else{
                    showNumberPicker(2);
                }
            }
        }

        else if (id == R.id.nav_analyze_data) {
            fragmentManager.beginTransaction().replace(R.id.container, new ConfigureDrawerFragment()).commit();
        }

        else if (id == R.id.nav_calibration) {
            fragmentManager.beginTransaction().replace(R.id.container, new CalibrationFragment()).commit();
        }

        else if (id == R.id.nav_terminate_link) {
            mTitle = "home";
            cancelConnectDialogue();
            restart();
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);
        return true;
    }

    public void cancelConnectDialogue() {
        mBluetoothLeService.scanBluetooth(false);
        mBluetoothLeService.terminateConnection();
        finish();
        restart();
    }

    public void restart() {
        Intent intent = new Intent(this, this.getClass());
        startActivity(intent);
    }

    public void showUploadDialog() {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.dialog_upload, null, false);
        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(layout);
        popupWindow.setAnimationStyle(R.style.Animation);
        Button button = layout.findViewById(R.id.confirm);
        Button cancelButton =  layout.findViewById(R.id.cancel);
        final String currentTime = (String) DateFormat.format("yyyy-MM-dd hh:mm", new Date());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor) {
                        mBluetoothLeService.writeFileToDiskForActive(BluetoothLeService.deviceName);
                        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "ActiveBraceMonitorLog " + currentTime);
                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                        File attachment = new File(Environment.getExternalStorageDirectory(), Constants.ACTIVE_FILENAME );
                        if (!attachment.exists() || !attachment.canRead()) {
                            Toast.makeText(getApplication(), "Attachment Error", Toast.LENGTH_SHORT).show();
                        } else {
                            Uri uri = FileProvider.getUriForFile(getApplicationContext(),"ca.ualberta.songdichong.bracemonitorbluenrg.fileprovider", attachment);
                            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            try {
                                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(getApplication().getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        attachment.deleteOnExit();
                    }
                    else {
                        mBluetoothLeService.writeFileToDisk(BluetoothLeService.deviceName);
                        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "BraceMonitorLog " + currentTime);
                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                        File attachment = new File(Environment.getExternalStorageDirectory(), Constants.FILENAME );
                        if (!attachment.exists() || !attachment.canRead()) {
                            Toast.makeText(getApplication(), "Attachment Error", Toast.LENGTH_SHORT).show();
                        } else {
                            Uri uri = FileProvider.getUriForFile(getApplicationContext(),"ca.ualberta.songdichong.bracemonitorbluenrg.fileprovider", attachment);
                            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            try {
                                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(getApplication().getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        attachment.deleteOnExit();
                    }
                } catch (Throwable t) {
                    Toast.makeText(getApplicationContext(), "Request failed try again: " + t.toString(), Toast.LENGTH_LONG).show();
                }
                popupWindow.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);
    }

    /*
    * Function Name: requestPermission
    *
    * Function Detail: If the user does not give the requested permission, ask for the permission.
    *                  The permissions should be previously declared in Manifests.
    *
    * Input: None
    *
    * Output: None
    * */
    public void requestPermission(){
        final Context mainActivityContext = this.getApplicationContext();
        final Activity mainActivity = this;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if ((ContextCompat.checkSelfPermission(mainActivityContext, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(mainActivityContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(mainActivityContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(mainActivityContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        )
        {
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    ActivityCompat.requestPermissions(mainActivity, permissions, 1);
                }
            });
            builder.show();
        }
    }

    public void startScanFragment(){
        fragmentManager.beginTransaction().replace(R.id.container, new DeviceScanFragment()).commit();
    }

    public void showNumberPicker(int numberPickerType) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout;
        if (numberPickerType == 0){
            layout = layoutInflater.inflate(R.layout.number_picker_dialog, null, false);
        }else{
            layout = layoutInflater.inflate(R.layout.number_input_dialog, null, false);
        }
        Button button = layout.findViewById(R.id.confirm);
        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(layout);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.Animation);

        if (numberPickerType == 0){
            final NumberPicker numberPicker = layout.findViewById(R.id.numberPicker);
            String[] rate = { "1", "3", "5", "8", "10","0" };
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(6);
            numberPicker.setDisplayedValues(rate);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selection = numberPicker.getValue();
                    switch (selection){
                        case 1:
                            mBluetoothLeService.setSamplingRate(1*6);
                            break;
                        case 2:
                            mBluetoothLeService.setSamplingRate(3*6);
                            break;
                        case 3:
                            mBluetoothLeService.setSamplingRate(5*6);
                            break;
                        case 4:
                            mBluetoothLeService.setSamplingRate(8*6);
                            break;
                        case 5:
                            mBluetoothLeService.setSamplingRate(10*6);
                            break;
                        case 6:
                            mBluetoothLeService.setSamplingRate(0);
                            break;
                        default:
                            break;
                    }
                    popupWindow.dismiss();
                }
            });
        }
        else if(numberPickerType  == 1){
            final EditText numberInput =  layout.findViewById(R.id.numberInput);
            numberInput.setHint("subject ID (range: [0,9999])");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        int sending = Integer.parseInt(numberInput.getText().toString()) ;
                        if (sending < 9999){
                            mBluetoothLeService.setDeviceHolder(sending);
                        }
                        else{
                            mBluetoothLeService.makeToast("sending value range is from 0 to 9999.");
                        }
                    }
                    catch (Exception e){
                        mBluetoothLeService.makeToast("set sample rate fail");
                    }
                    popupWindow.dismiss();
                }
            });
        }
        else if(numberPickerType  == 2){
            final EditText numberInput =  layout.findViewById(R.id.numberInput);
            numberInput.setHint("Password");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        int pass = Integer.parseInt(numberInput.getText().toString()) ;
                        if (pass == 999){
                            advancedModeUnlocked = true;
                            fragmentManager.beginTransaction().replace(R.id.container,new AdvancedConfigurationFragment()).addToBackStack(null).commit();
                        } else{
                            mBluetoothLeService.makeToast("Password is wrong!");
                        }
                    }
                    catch (Exception e){
                        mBluetoothLeService.makeToast("Password is wrong!");
                    }
                    popupWindow.dismiss();
                }
            });
        }

        Button cancelButton = layout.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);
    }

}
