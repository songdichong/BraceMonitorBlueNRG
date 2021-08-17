/*
Copyright © 2020, University of Alberta. All Rights Reserved.

This software is the confidential and proprietary information
of the Department of Electrical and Computer Engineering at the University of Alberta (UofA).
You shall not disclose such Confidential Information and shall use it only in accordance with the
terms of the license agreement you entered into at the UofA.

No part of the project, including this file, may be copied, propagated, or
distributed except with the explicit written permission of Dr. Edmond Lou
(elou@ualberta.ca), Director of CETT.

Project Name       : Brace Monitor Android User Interface

File Name          : BluetoothLeService.java

Original Author    : Dichong Song

File Creation Date : 2021/03/26

File Description   : The file configures all bluetooth activities needed for this project
1. Initialize bluetooth function
2. Create a scanner and create scanCallback
3. Scan function using scanner
4. Connect and disconnect function
5. Create BluetoothGattCallback. (https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback)
    The following methods should be overwritten:
    5.1 onConnectionStateChange.
        Happens after disconnected->connected or connected->disconnected.
        If we are using bluetooth 5.0, we should call gatt.requestMtu(220) otherwise the default
        Mtu size is still 23.
    5.2 onMtuChanged.
        Happens after the mtu size is changed successfully.
        Call discover services in this function
    5.3 onServicesDiscovered
        Happens after services are discovered. You can check if all the services are matched with
        what you want.
    5.4 onCharacteristicRead
        Happens after you send a read request to the firmware. If the firmware replies, you get
        the replication in this method.
    5.5 onCharacteristicWrite
        Happens after you send a write request to the firmware. If the firmware replies, you get
        the replication in this method.
    5.6 onDescriptorWrite
        Happens after you enable a notification to the firmware.
    5.7 onCharacteristicChanged
        Happens after a notification is enabled, and you keep receiving data from firmware.
        You get the data in this method.
    Note:
     if you want to initialize a bluetooth in the following order
             a. Read battery (readBatt)
             b. Write time  (setTime)
             c. Enable notification (enableNoti)
             d. Read deviceInfo (readInfo)
     You cannot send these 3 commands at the same time. (Wrong:
        void foo(){
            readBatt();
            setTime();
            enableNoti();
            readInfo();
        }
        Only readBatt will be executed.)
     You need to call readBatt() first. In "onCharacteristicRead" method call setTime().
     In "onCharacteristicWrite" method call enableNoti(). In "onDescriptorWrite" call readInfo().
6. All kinds of methods for
    6.1 reading a characteristic
    6.2 writing a characteristic
    6.3 enabling or disabling a notification
*/

package ca.ualberta.songdichong.bracemonitorbluenrg;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncRequest;
import android.net.wifi.aware.Characteristics;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
import static ca.ualberta.songdichong.bracemonitorbluenrg.Constants.*;

public class BluetoothLeService {
    //Singleton global object to handle all ble services
    static private final BluetoothLeService mBluetoothLeService = new BluetoothLeService();
    //A static object to record current connection status.(Also used in MainActivity)
    static public boolean connected = false;
    static public boolean calibrated = false;
    static public boolean realTimeSampling = false;
    //Strings for broadcasting
    static public String deviceName;
    static public double batteryVal = 0;
    public BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private Context context;
    private boolean mScanning;
    Handler mHandler;
    private List<BluetoothDevice> mBluetoothDevices = new ArrayList<>();
    List<String> mBluetoothDeviceList = new ArrayList<>();
    private static ArrayAdapter<String> mBluetoothListAdapter;
    private BluetoothLeScanner scanner;
    private boolean firstRead = true;
    private boolean initializing = true;
    int MTU = 0;
    static double forceCaliVal0 = Double.NaN;
    static double forceCaliVal1 = Double.NaN;
    static double forceCaliVal2 = Double.NaN;
    static double forceCaliVal3 = Double.NaN;

    static int tempCaliRealVal = 23;
    static double tempCaliADCVal = 0.73;

    double tempCaliVal = 0;
    static int sampleRate = 1;
    private int recordYear = 0;
    private int recordMonth = 0;
    private int recordDay = 0;
    private int recordHour = 0;
    private int recordMinute = 0;
    private int recordSecond = 0;

    private int deviceInfoVal = -1;
    private int deviceVersionVal = 0;

    static public List<Records> downloadedData = new ArrayList<>();
    public boolean adjustmentEnabled = false;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    /*
     * Function Name: setContext
     *
     * Function Detail: In the main function, by given the current context, create bluetoothManClass
     * then get adapter and create the scanner.
     *
     * Input: None.
     *
     * Output: boolean. True if success. False if fail.
     * */
    public void setContext(Context context) {
        this.context = context;
        BluetoothManager bluetoothManClass = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothListAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, mBluetoothDeviceList);
        // if adpter is null then quit
        mBluetoothAdapter = bluetoothManClass.getAdapter();
        if (mBluetoothAdapter==null){
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("No bluetooth in this device");
            builder.setMessage("There is no bluetooth function in this device");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    System.exit(0);
                }
            });
            builder.show();
        }
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothAdapter.enable();
        mHandler = new Handler();
        initBluetooth();
    }
    /*
     * Function Name: initBluetooth
     *
     * Function Detail: Initialize the bluetooth adapter.
     *
     * Input: None.
     *
     * Output: boolean. True if success. False if fail.
     * */
    private boolean initBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, 1);
            return false;
        }
        return true;
    }
    /*
     * Function Name: scanBluetooth
     *
     * Function Detail: Enable ble scanning or disable ble scanning
     *
     * Input: boolean. True to start scan. False to stop scan.
     *
     * Output: None.
     * */
    public void scanBluetooth(boolean enable) {
        final long SCAN_PERIOD = 8000;
        if (enable) {
            mBluetoothDeviceList.clear();
            mBluetoothDevices.clear();
            if (mBluetoothGatt != null){
                mBluetoothGatt.close();
            }
            mBluetoothListAdapter.notifyDataSetChanged();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    scanner.stopScan(scan_callback);
                    broadcastUpdate(ACTION_GATT_SCAN_COMPLETE, "");
                }
            }, SCAN_PERIOD);
            mScanning = true;
            scanner.startScan(scan_callback);
        } else {
            mBluetoothDeviceList.clear();
            mBluetoothDevices.clear();
            mBluetoothListAdapter.notifyDataSetChanged();
            mScanning = false;
            scanner.stopScan(scan_callback);
        }
    }

    /*
     * Object Name: scanCallback
     *
     * Object Detail: Initialize ScanCallback object with overriding the onScanResult method.
     *      If a device is scanned and its name contains "brace" I add it to the scanned device list.
     * */
    private ScanCallback scan_callback = new ScanCallback() {
        public void onScanResult(int callbackType, final ScanResult result) {
            if (!mScanning) {
                return;
            }
            if (result.getDevice()!= null && result.getDevice().getName() != null && result.getDevice().getName().contains("Brace")) {
                Log.e("Found device", result.getDevice().getName());
                int rssi = result.getRssi();
                if (mBluetoothDevices.contains(result.getDevice())){
                    int index = mBluetoothDevices.indexOf(result.getDevice());
                    if (index < mBluetoothDeviceList.size() && index >= 0){
                        mBluetoothDeviceList.set(index,result.getDevice().getName()+"        " +rssi+"dbm");
                        mBluetoothListAdapter.notifyDataSetChanged();
                    }
                }else{
                    mBluetoothDeviceList.add(result.getDevice().getName()+"        " +rssi+"dbm");
                    mBluetoothDevices.add(result.getDevice());
                    mBluetoothListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    /*
     * Function Name: initConnection
     *
     * Function Detail: Connect to given bluetooth device address.
     *
     * Input: int deviceNum. Position of the selected device in addressList from deviceScanFragment.
     *
     * Output: None.
     * */
    public void initConnection(int deviceNum) {
        scanner.stopScan(scan_callback);
        mScanning = false;
        initializing = true;
        BluetoothDevice device = mBluetoothDevices.get(deviceNum);
        if (initBluetooth()) {
            if (mBluetoothGatt == null) {
                mBluetoothGatt = device.connectGatt(context, false,
                        mGattCallback);

            }
            else{
                terminateConnection();
                initBluetooth();
                mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
            }
        }
    }

    /*
     * Function Name: terminateConnection
     *
     * Function Detail: Disconnect to current connected bluetooth device address.
     *
     * Input: None
     *
     * Output: None.
     * */
    public void terminateConnection() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            connected = false;
            calibrated = false;
            realTimeSampling = false;
            adjustmentEnabled = false;
            mBluetoothGatt.close();
            broadcastUpdate(ACTION_GATT_DISCONNECTED,"Disconnected");
        }
    }
    /**
     * Object Name: BluetoothGattCallback
     *
     * Object Detail: The callback of bluetooth. Refer to the top of this page.
     */
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    if ((characteristic.getUuid().equals(UUID_LNG_TRM_DATE))) {
                        if (initializing){
                            getTempCalibration();
                        }
                    }
                }

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.v("here0","connection success");
                        //important: modify mtu to 220 so download data size can increase to 100 (max = 220-3)
                        gatt.requestMtu(220);
                        connected = true;
                        makeToast("Connected");
                    }
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.v("here1","connection terminated");
                        terminateConnection();
                        broadcastUpdate(ACTION_RESTARTAPP,"");
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    List<BluetoothGattService> servicesList = gatt.getServices();
                    for (BluetoothGattService service: servicesList){
                        Log.v("service",String.valueOf(service.getUuid()));
                        for (BluetoothGattCharacteristic characteristic:service.getCharacteristics()){
                            Log.v("characteristic",String.valueOf(characteristic.getUuid()));
                        }
                    }
                    if (servicesList.size() > 0) {
                        try{
                            getDeviceInfo();
                            firstRead = true;
                            broadcastUpdate(ACTION_GATT_CONNECTED, gatt.getDevice().getName());
                        }
                        catch (NullPointerException e){
                            makeToast("Error finding device services");
                            broadcastUpdate(ACTION_RESTARTAPP, "" );
                        }
                    }
                    else {
                        makeToast("Error finding device services");
                        broadcastUpdate(ACTION_RESTARTAPP, "" );
                    }
                }
                //firmware bluenrg always return 0 on the first read of any characteristic, so if we read for the first time we read again
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.v("onCharacteristicRead",Integer.toString(status));
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (characteristic.getUuid().equals(UUID_DEVICEINFO_VAL)){
                            if (firstRead){
                                getDeviceInfo();
                                firstRead = false;
                            }
                            else{
                                if (initializing){
                                    boolean versionResult = getDeviceVersion();
                                    if (!versionResult){
                                        getBatteryLevel();
                                    }
                                }
                                deviceInfoVal = characteristic.getValue()[0];
                            }
                        }

                        if (characteristic.getUuid().equals(UUID_DEVICE_VERSION_VAL)) {
                            if (firstRead){
                                getDeviceVersion();
                                firstRead = false;
                            }
                            else{
                                if (initializing) {
                                    getBatteryLevel();
                                }
                                deviceVersionVal = characteristic.getValue()[0];
                                Log.v("Version", String.valueOf(characteristic.getValue()[0]));
                            }
                        }

                        if (characteristic.getUuid().equals(UUID_BATT_VALUE)) {
                            if (firstRead){
                                getBatteryLevel();
                                firstRead = false;
                            }
                            else{
                                if (initializing) {
                                    getCalibrationValue(0);
                                }
                                if (deviceInfoVal == activeBraceMonitor){
                                    batteryVal =  (double)characteristic.getValue()[0] + (double)characteristic.getValue()[1]/100;
                                }else{
                                    batteryVal = convertADC(characteristic.getValue(),batterySensor);
                                }
                            }
                        }

                        if (characteristic.getUuid().equals(UUID_TEMP_VALUE)) {
                            if (firstRead){
                                getTempCalibration();
                                firstRead = false;
                            }
                            else{
                                updateTempLevel(true);
                                Log.v("UUID_TEMP_VALUE",Arrays.toString(characteristic.getValue()));
                                byte[] emptyArray = new byte[]{-1,-1,-1,-1};
                                if (Arrays.equals(characteristic.getValue(),emptyArray))
                                {
                                    makeToast("Device Temperature Is Not Calibrated");
                                } else{
                                    if (deviceInfoVal == activeBraceMonitor){
                                        byte[] tempCali = new byte[]{characteristic.getValue()[0],characteristic.getValue()[1]};
                                        tempCaliADCVal = convertADC(tempCali, temperatureSensor);
                                        tempCaliRealVal =  characteristic.getValue()[2];
                                        makeToast("Device temperature is calibrated as:" + String.format("%.2f",tempCaliADCVal) + " ℃ -- "+ tempCaliRealVal + " ℃");
                                    }else{
                                        byte[] tempCali = new byte[]{characteristic.getValue()[0],characteristic.getValue()[1]};
                                        tempCaliADCVal = convertADC(tempCali, ADC_Input_AdcPin2);
                                        tempCaliRealVal =  characteristic.getValue()[2];
                                        makeToast("Device temperature is calibrated as:" + String.format("%.2f",tempCaliADCVal) + " V -- "+ tempCaliRealVal + " ℃");
                                    }
                                }
                            }
                        }

                        if (characteristic.getUuid().equals(UUID_DOWNLOAD_VALUE)){
                            if (firstRead){
                                eraseAllFlash();
                                firstRead = false;
                                Log.v("1","1st read to UUID_DOWNLOAD_VALUE");
                            }else{
                                firstRead = true;
                                int timeToEraseAll = characteristic.getValue()[0];
                                Log.v("time to erase all",String.valueOf(timeToEraseAll));
                                broadcastUpdate(ACTION_DATA_ERASE,(double) timeToEraseAll);
                            }
                        }

//                        if (characteristic.getUuid().equals(UUID_TARGET_FORCE_VALUE)){
//                            if (firstRead){
//                                getTargetForce();
//                                firstRead = false;
//                            }else{
//                                int digits =  characteristic.getValue()[0];
//                                if (digits < 0) digits += 256;
//                                int decimals = characteristic.getValue()[1];
//                                if (decimals < 0) decimals += 256;
//                                double force = digits + (double)(decimals/100);
//                                makeToast("target force set to" + force);
//                            }
//                        }
                        if (characteristic.getUuid().equals(UUID_FORCE_1N_CALIBRATION)){
                            if (deviceInfoVal == activeBraceMonitor){
                                if (firstRead){
                                    getCalibrationValue(1);
                                    firstRead = false;
                                }
                                else{
                                    forceCaliVal2 = characteristic.getValue()[0] < 0 ? characteristic.getValue()[0]+256:characteristic.getValue()[0];
                                    forceCaliVal3 = characteristic.getValue()[1] < 0 ? characteristic.getValue()[1]+256:characteristic.getValue()[1];
                                    makeToast(String.format("low: %.0f",forceCaliVal2)+"\n"+ String.format("high: %.0f",forceCaliVal3));
                                }
                            }
                        }

                        if (characteristic.getUuid().equals(UUID_FORCE_0N_CALIBRATION)){
                            Log.v("calibration0", Arrays.toString(characteristic.getValue()));
                            byte[] emptyArray = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
                            //first read always give us 0s, so just ignore and read again
                            if (firstRead)
                            {
                                getCalibrationValue(0);
                                firstRead = false;
                            }

                            //if all values are -1, this suggests that data are not calibrated yet
                            else if (Arrays.equals(characteristic.getValue(),emptyArray))
                            {
                                if (initializing){
                                    setDeviceTime();
                                }
                                calibrated = false;
                                makeToast("Device Force Is Not Calibrated");
                            }

                            else{
                                if (initializing){
                                    setDeviceTime();
                                }
                                if (deviceInfoVal == activeBraceMonitor)
                                {
                                    byte[] array1 = new byte[4];
                                    byte[] array2 = new byte[4];
                                    for (int i = 0; i < 8; i++){
                                        if (i<4){
                                            array1[3-i] = characteristic.getValue()[i];
                                        } else {
                                            array2[7-i] = characteristic.getValue()[i];
                                        }
                                    }
                                    forceCaliVal0 = ByteBuffer.wrap(array1).getFloat();
                                    forceCaliVal1 = ByteBuffer.wrap(array2).getFloat();
                                    calibrated = true;
                                    makeToast(String.format("slope: %.2f",forceCaliVal0)+"\n"+ String.format("intercept: %.2f",forceCaliVal1));
                                }
                                else{
                                    byte[] cali0 = new byte[]{characteristic.getValue()[0],characteristic.getValue()[1]};
                                    byte[] cali1 = new byte[]{characteristic.getValue()[2],characteristic.getValue()[3]};
                                    byte[] cali2 = new byte[]{characteristic.getValue()[4],characteristic.getValue()[5]};
                                    byte[] cali3 = new byte[]{characteristic.getValue()[6],characteristic.getValue()[7]};
                                    forceCaliVal0 = convertADC(cali0,ADC_Input_AdcPin1);
                                    forceCaliVal1 = convertADC(cali1,ADC_Input_AdcPin1);
                                    forceCaliVal2 = convertADC(cali2,ADC_Input_AdcPin1);
                                    forceCaliVal3 = convertADC(cali3,ADC_Input_AdcPin1);
                                    calibrated = true;
                                    makeToast(String.format("%.3f",forceCaliVal0)+"\n" +String.format("%.3f",forceCaliVal1)+"\n"+ String.format("%.3f",forceCaliVal2)+"\n"+ String.format("%.3f",forceCaliVal3));
                                }
                            }
                        }
                    }else{
                        makeToast("Error reading device services");
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    Log.v(String.valueOf(characteristic.getUuid()),characteristic.getValue().length+ Arrays.toString(characteristic.getValue()));
                    if (characteristic.getUuid().equals(UUID_DOWNLOAD_VALUE)){
                        if (deviceInfoVal == internalFlashBraceMonitor) {
                            FormatDownloadedData_Internal(characteristic.getValue());
                        }
                        else if (deviceInfoVal == externalFlashBraceMonitor) {
                            FormatDownloadedData_External(characteristic.getValue());
                        }
                        else if (deviceInfoVal == activeBraceMonitor){
                            FormatDownloadedData_Active(characteristic.getValue());
                        }
                        broadcastUpdate(ACTION_DATA_DOWNLOAD,characteristic.getValue().length);
                    }
                    else if  (characteristic.getUuid().equals(UUID_FORCE_SENSOR_VALUE)){
                        double forceADCVoltage;
                        if (deviceInfoVal == activeBraceMonitor){
                            forceADCVoltage = convertADC(characteristic.getValue(),ADC_Input_AdcPin2);
                        }else{
                            forceADCVoltage = convertADC(characteristic.getValue(),ADC_Input_AdcPin1);
                        }
                        broadcastUpdate(ACTION_FORCE_UPDATE,forceADCVoltage);
                    }
                    else if (characteristic.getUuid().equals(UUID_TEMP_VALUE)){
                        if (deviceInfoVal == activeBraceMonitor){
                            double temperature = convertADC(characteristic.getValue(), temperatureSensor);
                            temperature = temperature + (tempCaliRealVal-tempCaliADCVal);
                            broadcastUpdate(ACTION_TEMP_UPDATE,temperature);
                        }else{
                            double tempADCVoltage = convertADC(characteristic.getValue(), ADC_Input_AdcPin2);
                            double temperature = (tempADCVoltage - tempCaliADCVal)/0.01 + tempCaliRealVal;
                            broadcastUpdate(ACTION_TEMP_UPDATE,temperature);
                            broadcastUpdate(ACTION_TEMPRAW_UPDATE,tempADCVoltage);
                        }
                    }
                    else if (characteristic.getUuid().equals(UUID_EXTERNAL_TEST_VALUE)) {
                        Log.v("EXTERNAL_TEST",Arrays.toString(characteristic.getValue()));
                        broadcastUpdate(ACTION_EXTERNAL_TEST, characteristic.getValue());
                    }
                }

                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status){
                    gatt.discoverServices();
                    MTU = mtu;
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                    if ((descriptor.getCharacteristic().getUuid().equals(UUID_TEMP_VALUE))) {
                        if (Arrays.equals(descriptor.getValue(),descriptor.DISABLE_NOTIFICATION_VALUE))
                        {
                            getTempCalibration();
                        }
                        else{
                            initializing = false;
                        }
                    }
                }
            };

    /*
     * Function Name: getBatteryLevel
     *
     * Function Detail: Read battery value from connected device.
     * Search UUID_BATT_VALUE in onCharacteristicRead of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: None. Use current device's bluetoothgatt object by default.
     *
     * Output: None.
     * */
    public void getBatteryLevel() {
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_BATT_SERV);
                mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID_BATT_VALUE));
                firstRead = true;
            }
            catch (Exception e){
                makeToast("Read battery failed. Please connect again and try.");
            }
        }
    }

    /*
     * Function Name: getDeviceInfo
     *
     * Function Detail: Get the current device's information.
     *                  0 == passive brace monitor with external flash memory
     *                  1 == passive brace monitor without external flash memory
     *                  2 == active brace monitor with flash memory
     * Search UUID_DEVICEINFO_VAL in onCharacteristicRead of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input:  None. Use current device's bluetoothgatt object by default.
     *
     * Output: None.
     * */
    public void getDeviceInfo() {
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_DEVICEINFO_SERV);
                mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID_DEVICEINFO_VAL));
            }
            catch (Exception e){
                makeToast("Get device info failed. Please connect again and try.");
            }
        }
    }

    /*
     * Function Name: getTempCalibration
     *
     * Function Detail: Read currently connected device's temperature calibration value.
     * Search UUID_TEMP_VALUE in onCharacteristicRead of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input:  None. Use current device's bluetoothgatt object by default.
     *
     * Output: None.
     * */
    public void getTempCalibration() {
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_TEMP_SERV);
                mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID_TEMP_VALUE));
                firstRead = true;
            }
            catch (Exception e){
                makeToast("Get device info failed. Please connect again and try.");
            }
        }
    }
    /*
     * Function Name: getCalibrationValue
     * The method has been modified as we add a 6N calibration.
     * Originally it read UUID_FORCE_0N_CALIBRATION,UUID_FORCE_1N_CALIBRATION,UUID_FORCE_2N_CALIBRATION
     * to get the corresponding force calibration. Now it read UUID_FORCE_0N_CALIBRATION to get all force calibration.
     *
     * Function Detail: If choice == 0, the function send a read request to read force calibration value.
     *                  If choice == 1, the function send a read request to let firmware write
     *                      current force calibration into internal memory.
     * Search UUID_FORCE_0N_CALIBRATION in onCharacteristicRead of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input:  int choice.
     *
     * Output: None.
     * */
    public void getCalibrationValue(int choice) {
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_FORCE_CALIBRATION_SERV);
                switch (choice){
                    case 0:
                        mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID_FORCE_0N_CALIBRATION));
                        firstRead = true;
                        break;
                    case 1:
                        mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID_FORCE_1N_CALIBRATION));
                        firstRead = true;
                        if (deviceInfoVal != activeBraceMonitor){
                            makeToast("Successfully saved calibration status.");
                            calibrated = true;
                        }
                        break;
                }
            }
            catch (Exception e){
                makeToast("Get calibration info failed. Please connect again and try.");
            }
        }
    }

    public boolean getDeviceVersion(){
        if (mBluetoothGatt != null && connected) {
            BluetoothGattService service = mBluetoothGatt.getService(UUID_DEVICEINFO_SERV);
            BluetoothGattCharacteristic characteristics = service.getCharacteristic(UUID_DEVICE_VERSION_VAL);
            if (characteristics == null){
                return false;
            }else {
                mBluetoothGatt.readCharacteristic(characteristics);
                firstRead = true;
                return true;
            }
        }
        return false;
    }

    public void getSamplingRate(){
        if (mBluetoothGatt != null && connected) {
            BluetoothGattService service = mBluetoothGatt.getService(UUID_LNG_TRM_SERV);
            mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID_LNG_SAMPLING_RATE));
            firstRead = true;
        }
    }

    public void getDeviceHolder(){
        if (mBluetoothGatt != null && connected) {
            BluetoothGattService service = mBluetoothGatt.getService(UUID_FLASH_SERV);
            mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID_DEVICE_HOLDER_VALUE));
            firstRead = true;
        }
    }
    /*
     * Function Name: setDeviceHolder
     *
     * Function Detail: Write device holder value to UUID_DEVICE_HOLDER_VALUE to the firmware,
     *                  so it knows what device holder number is.
     * Search UUID_DEVICE_HOLDER_VALUE in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: yte holder. Current device holder's subject number.
     *
     * Output: None.
     * */
    public void setDeviceHolder(int holder){
        try{
            BluetoothGattService service = mBluetoothGatt.getService(UUID_FLASH_SERV);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_DEVICE_HOLDER_VALUE);
            characteristic.setValue(new byte[]{(byte)(holder>>8), (byte)holder});
            mBluetoothGatt.writeCharacteristic(characteristic);
            makeToast("Successfully set device holder info.");
        }
        catch (Exception e){
            makeToast("Set device holder failed. Please connect again and try.");
        }
    }

    public void getTargetForce(){
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_FORCE_SENSOR_SERV);
                mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID_TARGET_FORCE_VALUE));
                firstRead = true;
            }
            catch (Exception e){
                makeToast("Get target force failed. Please connect again and try.");
            }
        }
    }
    /*
     * Function Name: setTempCalibration
     *
     * Function Detail: Write currently connected device's temperature calibration value.
     * Search UUID_TEMP_VALUE in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: int temp. The current real-world temperature in int format. send in {LSB, MSB} format
     *
     * Output: None.
     * */
    public void setTempCalibration(int temp)
    {
        try{
            BluetoothGattService service = mBluetoothGatt.getService(UUID_TEMP_SERV);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_TEMP_VALUE);
            characteristic.setValue(new byte[]{(byte)temp,(byte)(temp>>8)});
            mBluetoothGatt.writeCharacteristic(characteristic);
            makeToast("Successfully set temperature.");
        }
        catch (Exception e){
            makeToast("Set target force failed. Please connect again and try.");
        }
    }
    /*
     * Function Name: setTargetForce
     *
     * Function Detail: The function send a write request to UUID_TARGET_FORCE_VALUE to
     *                  set target force value.
     *
     * Search UUID_TARGET_FORCE_VALUE in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: double targetForce. Target force value to be sent, format in {digits of force, decials of force}
     *
     * Output: None.
     * */
    public void setTargetForce(double targetForce){
        try{
            BluetoothGattService service = mBluetoothGatt.getService(UUID_FORCE_SENSOR_SERV);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_TARGET_FORCE_VALUE);
            byte digits = (byte) targetForce;
            byte decimals = (byte)(100 * (targetForce - (int) targetForce));
            characteristic.setValue(new byte[]{digits,decimals});
            mBluetoothGatt.writeCharacteristic(characteristic);
            makeToast(String.format("target force set to " + String.format("%.2f", targetForce)));
        }
        catch (Exception e){
            makeToast("Set target force failed. Please connect again and try.");
        }
    }

    public void setTargetPressure(int targetPressure, int allowance){
        try{
            BluetoothGattService service = mBluetoothGatt.getService(UUID_FORCE_SENSOR_SERV);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_TARGET_FORCE_VALUE);
            characteristic.setValue(new byte[]{(byte)targetPressure,(byte)allowance});
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
        catch (Exception e){
            makeToast("Set target force failed. Please connect again and try.");
        }
    }

    /*
     * Function Name: eraseAllFlash
     *
     * Function Detail: The function sends a read request to UUID_DOWNLOAD_VALUE to
     *                  let firmware erase its external memory.
     *                  The firmware will tell ui how long it will take to erase as callback.
     *
     * Search UUID_DOWNLOAD_VALUE in onCharacteristicRead of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: None. Use current device's bluetoothgatt object by default.
     *
     * Output: None.
     * */
    public void eraseAllFlash(){
        if (mBluetoothGatt != null && connected) {
            try{
                firstRead = true;
                BluetoothGattService service = mBluetoothGatt.getService(UUID_FLASH_SERV);
                mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID_DOWNLOAD_VALUE));
            }
            catch (Exception e){
                makeToast("Erase flash memory failed. Please connect again and try.");
            }
        }
    }

    /*
     * Function Name: externalTest
     *
     * Function Detail: The function sends a notification request to UUID_EXTERNAL_TEST_VALUE to
     *                  let firmware start testing its external memory.
     *                  And report which block has been tested and its result.
     *
     * Search UUID_EXTERNAL_TEST_VALUE in onCharacteristicChanged of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: None. Use current device's bluetoothgatt object by default.
     *
     * Output: None.
     * */
    public void externalTest(){
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_FLASH_SERV);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_EXTERNAL_TEST_VALUE);
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(desc);
            }
            catch (Exception e){
                makeToast("External flash test failed to start. Please connect again and try.");
            }
        }
    }
    /*
     * Function Name: updateTempLevel
     *
     * Function Detail: The function enable/disable the temperature notification.
     *
     * Search UUID_TEMP_VALUE in onCharacteristicChanged of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: boolean enable. True to enable notification. False to stop notification.
     *
     * Output: None.
     * */
    public void updateTempLevel(boolean enable){
        if (mBluetoothGatt != null && connected) {
            /*enable notification*/
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_TEMP_SERV);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_TEMP_VALUE);
                mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
                BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
                if (enable)
                    desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                else
                    desc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(desc);
            }
            catch (Exception e){
                makeToast("Update temperature level failed. Please connect again and try.");
            }
        }
    }

    /*
     * Function Name: downloadData
     *
     * Function Detail: The function enable the download notification.
     *
     * Search UUID_DOWNLOAD_VALUE in onCharacteristicChanged of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: None
     *
     * Output: None.
     * */
    public void downloadData() {
        if (mBluetoothGatt != null && connected) {
            try{
                downloadedData.clear();
                BluetoothGattService service = mBluetoothGatt.getService(UUID_FLASH_SERV);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_DOWNLOAD_VALUE);
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(desc);
            }
            catch (Exception e){
                makeToast("Download data failed. Please connect again and try.");
            }
        }
    }

    void stopDownloadData(){
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_FLASH_SERV);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_DOWNLOAD_VALUE);
                mBluetoothGatt.setCharacteristicNotification(characteristic, false);
                BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(desc);
            }
            catch (Exception e){

            }
        }
    }
    /*
     * Function Name: formatDownloadedDataExternal
     *
     * Function Detail: The function takes an input array, traverses through it, and identify if a
     *  package of data is header or record. Then save the record to braceMonitorDevice's downloaded
     *  data list.
     *
     * Input: byte[] arrays. downloaded data byte of array, usually the size is 200.
     *
     * Output: None.
     * */
    public void FormatDownloadedData_External(byte[] arrays){
        int index = 0;
        int[] maxDays = {31,28,31,30,31,30,31,31,30,31,30,31};
        while(index < arrays.length-1){
            if ((arrays.length >= (index+12)) && (arrays[index] == -18) && (arrays[index+1] == -18)){
                //So the next 6 are time info: year month day hour minute sampleRate
                //Then 4 are holder subject number. target force digit, target force decimal, 0
                recordYear = arrays[index+2] + 2000;
                recordMonth = arrays[index+3];
                recordDay = arrays[index+4];
                if (recordMonth == 0) recordMonth = 1;
                if (recordDay == 0) recordDay = 1;
                recordHour = arrays[index+5];
                recordMinute = arrays[index+6];
                sampleRate =  arrays[index+7] >= 0 ? arrays[index+7]:arrays[index+7]+256;
                if (sampleRate == 0){
                    //For test
                    sampleRate = 6;
                }
                Log.v("subject_number",String.valueOf(arrays[index+8]+" "+arrays[index+9]));
                int subject_number_msd = arrays[index+8] >= 0 ? arrays[index+8]*256:(arrays[index+8]+256)*256;
                int subject_number_lsd = arrays[index+9] >= 0 ? arrays[index+9]:(arrays[index+9]+256);
                int subject_number =  subject_number_msd + subject_number_lsd;
                int forceDigits =  arrays[index+10] >= 0 ? arrays[index+10]:arrays[index+10]+256;
                int forceDecimals =  arrays[index+11] >= 0 ? arrays[index+11]:arrays[index+11]+256;
                double target_force = forceDigits + forceDecimals*0.01;
                Records records = new Records(subject_number, target_force, sampleRate);
                downloadedData.add(records);
                index += 12;
                //If all info are 0, that means date info is missing
            }
            else if ( (arrays[index] == -1) && (arrays[index+1] == -1)
                    && (arrays[index+2] == -1) && (arrays[index+3] == -1)){
                //so reach the end, break immediately
                stopDownloadData();
                break;
            }
            else {
                // create Records object and add it into list
                byte[] forceArray = new byte[]{arrays[index], arrays[index+1]};
                byte[] temperatureArray = new byte[]{arrays[index+2], arrays[index+3]};
                double temperatureVoltage = convertADC(temperatureArray, ADC_Input_AdcPin2);
                double forceVoltage = convertADC(forceArray,ADC_Input_AdcPin1);
                double temperature = (temperatureVoltage - tempCaliADCVal)/0.01 + tempCaliRealVal;

                double forceMeasurement;
                if (forceVoltage <= forceCaliVal1) {
                    forceMeasurement = (forceVoltage-forceCaliVal0) / (forceCaliVal1 - forceCaliVal0);
                }
                else if (forceVoltage <= forceCaliVal2) {
                    forceMeasurement = (forceVoltage-forceCaliVal1) / (forceCaliVal2 - forceCaliVal1) + 1;
                }
                else if (forceVoltage < forceCaliVal3) {
                    forceMeasurement = (forceVoltage-forceCaliVal2) / (forceCaliVal3- forceCaliVal2) * 4 + 2;
                }
                else{
                    forceMeasurement = (forceVoltage-forceCaliVal3) / (forceCaliVal3- forceCaliVal2) * 4 + 6;
                }
                if (forceMeasurement<0) {forceMeasurement = 0;}

                Records records = new Records(recordYear,recordMonth,recordDay,recordHour,recordMinute,forceMeasurement,temperature);

                downloadedData.add(records);

                index += 4;
                recordSecond += sampleRate*10;

                if (recordSecond >= 60){
                    recordMinute += recordSecond/60;
                    recordSecond %= 60;
                }

                if (recordMinute >= 60){
                    recordHour += recordMinute/60;
                    recordMinute %= 60;
                }

                if (recordHour >= 24){
                    recordDay += recordHour/24;
                    recordHour %= 24;
                }

                if (recordMonth == 0){
                    recordMonth = 1;
                }
                if ((recordYear % 4 == 0 && recordYear % 100 != 0) || (recordYear % 400 == 0 )){
                    maxDays[1] = 29;
                }else {
                    maxDays[1] = 28;
                }
                if (recordDay > maxDays[recordMonth-1]){
                    recordMonth += 1;
                    recordDay = 1;
                }

                if (recordMonth>12){
                    recordYear += 1;
                    recordMonth = 1;
                }
            }
        }
    }


    public void FormatDownloadedData_Active(byte[] arrays){
        int index = 0;
        int[] maxDays = {31,28,31,30,31,30,31,31,30,31,30,31};
        while(index < arrays.length-1){
            if ((arrays.length > (index+12)) && (arrays[index] == -18) && (arrays[index+1] == -18)){
                //So the next 6 bytes are time info: year month day hour minute sampleRate
                //Then next 8 bytes are holder subject number MSD, holder LSD, target force digit, target force decimal
                recordYear = arrays[index+2] + 2000;
                recordMonth = arrays[index+3];
                recordDay = arrays[index+4];
                if (recordMonth == 0) recordMonth = 1;
                if (recordDay == 0) recordDay = 1;
                recordHour = arrays[index+5];
                recordMinute = arrays[index+6];
                sampleRate =  arrays[index+7] >= 0 ? arrays[index+7]:arrays[index+7]+256;
                if (sampleRate == 0){
                    //For test
                    sampleRate = 6;
                }
                Log.v("subject_number",String.valueOf(arrays[index+8]+" "+arrays[index+9]));
                int subject_number_msd = arrays[index+8] >= 0 ? arrays[index+8]*256:(arrays[index+8]+256)*256;
                int subject_number_lsd = arrays[index+9] >= 0 ? arrays[index+9]:(arrays[index+9]+256);
                int subject_number =  subject_number_msd + subject_number_lsd;
                int forceDigits =  arrays[index+10] >= 0 ? arrays[index+10]:arrays[index+10]+256;
                int forceDecimals =  arrays[index+11] >= 0 ? arrays[index+11]:arrays[index+11]+256;
                double target_force = forceDigits + forceDecimals*0.01;
                Records records = new Records(subject_number, target_force, sampleRate);
                downloadedData.add(records);
                index += 12;
                //If all info are 0, that means date info is missing
            }
            else if ( (arrays[index] == -1) && (arrays[index+1] == -1)
                    && (arrays[index+2] == -1) && (arrays[index+3] == -1)){
                stopDownloadData();
                break;
            }
            else {
                // create Records object and add it into list
                byte[] forceArray = new byte[]{arrays[index], arrays[index+1]};
                byte[] temperatureArray = new byte[]{arrays[index+2], arrays[index+3]};
                double temperature = convertADC(temperatureArray, temperatureSensor);
                double forceVoltage = convertADC(forceArray, ADC_Input_AdcPin2);
                int longTermFlag = (arrays[index+4] < 0? arrays[index+4] + 256 : arrays[index+4]) + arrays[index+5]*256;
                Log.v("ltf",arrays[index+4]+" "+arrays[index+5]+" "+longTermFlag);
                //forceCaliVal0 == slope forceCaliVal1 == intercept
                double forceMeasurement = forceCaliVal0 * forceVoltage + forceCaliVal1;

                Records records = new Records(recordYear,recordMonth,recordDay,recordHour,recordMinute,forceMeasurement,temperature,longTermFlag);

                downloadedData.add(records);
                index += 6;
                recordSecond += sampleRate*10;

                if (recordSecond >= 60){
                    recordMinute += recordSecond/60;
                    recordSecond %= 60;
                }

                if (recordMinute >= 60){
                    recordHour += recordMinute/60;
                    recordMinute %= 60;
                }

                if (recordHour >= 24){
                    recordDay += recordHour/24;
                    recordHour %= 24;
                }

                if (recordMonth == 0){
                    recordMonth = 1;
                }
                if ((recordYear % 4 == 0 && recordYear % 100 != 0) || (recordYear % 400 == 0 )){
                    maxDays[1] = 29;
                }
                if (recordDay > maxDays[recordMonth-1]){
                    recordMonth += 1;
                    recordDay = 1;
                }

                if (recordMonth>12){
                    recordYear += 1;
                    recordMonth = 1;
                }
            }
        }
    }

    /*
     * Function Name: formatDownloadedDataExternal
     *
     * Function Detail: The function takes an input array, traverses through it, and identify if a
     *  package of data is header or record. Then save the record to braceMonitorDevice's downloaded
     *  data list.
     *
     * Input: byte[] arrays. downloaded data byte of array, usually the size is 200.
     *
     * Output: None.
     * */
    public void FormatDownloadedData_Internal(byte[] arrays){
        int index = 0;
        int[] maxDays = {31,28,31,30,31,30,31,31,30,31,30,31};
        while(index < arrays.length-1){
            if ((arrays.length > (index+6)) && (arrays[index] == -18) && (arrays[index+1] == -18)){
                //So the next 6 are time info: year month day hour minute sampleRate
                //Then 4 are holder subject number. target force digit, target force decimal, 0
                recordYear = arrays[index+2] + 2000;
                recordMonth = arrays[index+3];
                recordDay = arrays[index+4];
                if (recordMonth == 0) recordMonth = 1;
                if (recordDay == 0) recordDay = 1;
                recordHour = arrays[index+5];
                recordMinute = arrays[index+6];
                sampleRate =  arrays[index+7] >= 0 ? arrays[index+7]:arrays[index+7]+256;
                int subject_number = arrays[index+8] >= 0 ? arrays[index+8]:arrays[index+8]+256;
                int forceDigits =  arrays[index+9] >= 0 ? arrays[index+9]:arrays[index+9]+256;
                int forceDecimals =  arrays[index+10] >= 0 ? arrays[index+10]:arrays[index+10]+256;
                double target_force = forceDigits + forceDecimals*0.01;
                Records records = new Records(subject_number, target_force, sampleRate);
                downloadedData.add(records);
                index += 12;
                //If all info are 0, that means date info is missing
            }
            else if ((arrays.length == index+4) && (arrays[index] == -1) && (arrays[index+1] == -1)
                    && (arrays[index+2] == -1) && (arrays[index+3] == -1)){
                //so reach the end, break immediately
                break;
            }
            else {
                // create Records object and add it into list
                int  temperature = arrays[index];
                double forceMeasurement  = arrays[index+1]  * 0.05;
                if (temperature != 0 || forceMeasurement!=0){
                    Records records = new Records(recordYear,recordMonth,recordDay,recordHour,recordMinute,forceMeasurement,temperature);
                    downloadedData.add(records);
                    recordSecond += sampleRate*10;

                    if (recordSecond >= 60){
                        recordMinute += recordSecond/60;
                        recordSecond %= 60;
                    }

                    if (recordMinute >= 60){
                        recordHour += recordMinute/60;
                        recordMinute %= 60;
                    }

                    if (recordHour >= 24){
                        recordDay += recordHour/24;
                        recordHour %= 24;
                    }

                    if (recordMonth == 0){
                        recordMonth = 1;
                    }
                    if ((recordYear % 4 == 0 && recordYear % 100 != 0) || (recordYear % 400 == 0 )){
                        maxDays[1] = 29;
                    }
                    if (recordDay > maxDays[recordMonth-1]){
                        recordMonth += 1;
                        recordDay = 1;
                    }

                    if (recordMonth>12){
                        recordYear += 1;
                        recordMonth = 1;
                    }
                }
                index += 2;
            }
        }
    }

    /*
     * Function Name: writeFileToDisk
     *
     * Function Detail: The function writes brace monitor device's downloaded record
     *                  list to external memory.
     *
     * Input:  String deviceName. The current brace monitor device name.
     *
     * Output: None.
     * */
    public void writeFileToDisk(String deviceName) {
        File file = new File(Environment.getExternalStorageDirectory(),  FILENAME);
        if (file.exists()) {
            deleteFile();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            List<Records> data = downloadedData;
            out.write((deviceName+"\n").getBytes());
            out.write(("Force [N], Temperature [°C], Year, Month, Day, Hour, Minute\n").getBytes());
            out.write('\n');
            for (int i = 0; i < data.size(); i++) {
                Records record = data.get(i);
                if (record.isHeader){
                    String output = record.getHeaderString();
                    out.write(output.getBytes());
                }else{
                    String[] outputArray = new String[]{String.format("%.2f",record.getForceVal()),String.format("%.2f",record.getTempVal()),String.valueOf(record.getYear()),
                            String.valueOf(record.getMonth()),String.valueOf(record.getDate()),String.valueOf(record.getHour()),String.valueOf(record.getMinute())};
                    String output = Arrays.toString(outputArray);
                    out.write(output.substring(1, output.length() - 1).getBytes());
                    out.write('\n');
                }
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeFileToDiskForActive(String deviceName) {
        File file = new File(Environment.getExternalStorageDirectory(), ACTIVE_FILENAME);
        if (file.exists()) {
            deleteFile();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            List<Records> data = downloadedData;
            out.write((deviceName+"\n").getBytes());
            out.write(("Pressure [mmHg], Temperature [°C], LongTermFlag, Year, Month, Day, Hour, Minute\n").getBytes());
            out.write('\n');
            for (int i = 0; i < data.size(); i++) {
                Records record = data.get(i);
                if (record.isHeader){
                    String output = record.getHeaderString();
                    out.write(output.getBytes());
                }else{
                    String[] outputArray = new String[]{String.format("%.2f",record.getForceVal()),String.format("%.2f",record.getTempVal()),String.valueOf(record.getLongTermFlag()),String.valueOf(record.getYear()),
                            String.valueOf(record.getMonth()),String.valueOf(record.getDate()),String.valueOf(record.getHour()),String.valueOf(record.getMinute())};
                    String output = Arrays.toString(outputArray);
                    out.write(output.substring(1, output.length() - 1).getBytes());
                    out.write('\n');
                }
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * Function Name: deleteFile
     *
     * Function Detail: The function writes brace monitor device's downloaded record
     *                  list to external memory.
     *
     * Input:  None
     *
     * Output: None.
     * */
    public void deleteFile() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), FILENAME);
        if (file != null) {
            file.delete();
        }
    }
    /*
     * Function Name: startLongTerm
     *
     * Function Detail: Write 0x01 to UUID_LNG_TRM_MODE to the firmware,
     *                  so it knows that it should start long term sample.
     * Search UUID_LNG_TRM_MODE in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: None.
     *
     * Output: None.
     * */
    public void startLongTerm(){
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID_LNG_TRM_SERV).getCharacteristic(UUID_LNG_TRM_MODE);
                characteristic.setValue(new byte[]{0x01});
                mBluetoothGatt.writeCharacteristic(characteristic);
                makeToast("Successfully go into the long-term mode.");
            }
           catch (Exception e){
               makeToast("Start the long-term failed. Please connect again and try.");
            }
        }
    }
    /*
     * Function Name: convertADC
     *
     * Function Detail: The calculates adc voltage value based on the raw value received from firmware.
     *
     * Input:   byte[] adcRawVal. The byte array which stores the raw value. length must be 2.
     *          int sensorType. The type of sensor
     *
     * Output: None.
     * */
    public double convertADC(byte[] adcRawVal, int sensorType){
        if (adcRawVal == null || adcRawVal.length < 2) {
            //size must >= 2.
            return 0;
        }

        double resultVoltage = 0;

        //We are sending unsigned byte but java by default byte is signed.
        int digits = adcRawVal[0];
        //So if the first value is negative we need to change it to positive
        if (digits < 0){
            digits += 256;
        }
        //So if the second value is negative, then it is actually negative.
        int tens = adcRawVal[1];
        double rawVal = digits + tens*256;
        switch (sensorType){
            case batterySensor:
                resultVoltage = 4.36 * (0.6 - (rawVal/41260.0) * 2.4);
                break;
                
            case ADC_Input_AdcPin2:
                resultVoltage = 3 * (0.6 - (rawVal/41260.0) * 2.4);
                break;

            case ADC_Input_AdcPin1:
                resultVoltage = 3 * (0.6 + (rawVal/41260.0) * 2.4);
                break;

            case temperatureSensor:
                resultVoltage = 401 * (0.6 - (rawVal/41260.0) * 2.4) - 267;
                break;
        }
        if (resultVoltage>0)
            return resultVoltage;
        return 0;
    }
    /*
     * Function Name: setSamplingRate
     *
     * Function Detail: Write sample rate value to UUID_LNG_SAMPLING_RATE to the firmware,
     *                  so it knows what sample rate is.
     * Search UUID_LNG_SAMPLING_RATE in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: int sample rate. Current device's sample rate
     *
     *
     * Output: None.
     * */
    public void setSamplingRate(int samplingRate) {
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID_LNG_TRM_SERV).getCharacteristic(UUID_LNG_SAMPLING_RATE);
                characteristic.setValue(new byte[]{(byte)samplingRate});
                mBluetoothGatt.writeCharacteristic(characteristic);
                makeToast("Set sample rate successfully");
            }
            catch (Exception e){
                makeToast("Set sample rate failed. Please connect again and try.");
            }
        }
    }
    /*
     * Function Name: directControlUnit
     *
     * Function Detail: Write 2 specific bytes to the device so the device will do something (ex: turn on/off LED)
     *                  according to the firmware's code.
     * Search UUID_DIRECT_CONTROL in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: int device. First byte to write to device.
     *        boolean enable. Second byte to write to device.
     *
     * Output: None.
     * */
    public void directControlUnit(int device, boolean enable){
        if (mBluetoothGatt != null && connected) {
            /*enable notification*/
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_BATT_SERV);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_DIRECT_CONTROL);
                if (enable){
                    characteristic.setValue(new byte[]{(byte)device, 1});
                }
                else{
                    characteristic.setValue(new byte[]{(byte)device, 0});
                }
                mBluetoothGatt.writeCharacteristic(characteristic);
                makeToast("Send control info successfully");
            }
            catch (Exception e){
                makeToast("Send control info failed. Please connect again and try.");
            }
        }
    }
    /*
     * Function Name: setCalibrationValue
     *
     * Function Detail: Write to the firmware to let it record current force sensor's ADC value, what
     *                  is sending is the force value that this ADC value should correspond to.
     * Search UUID_FORCE_0N_CALIBRATION in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: int newton. The real world force value sent to the device.
     *
     * Output: None.
     * */
    public void setCalibrationValue(int newton) {
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_FORCE_CALIBRATION_SERV);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_FORCE_0N_CALIBRATION);
                characteristic.setValue(new byte[]{(byte)newton});
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            catch (Exception e){
                makeToast("Set calibration value failed. Please connect again and try.");
            }
        }
    }

    public void setActiveCalibrationValue(double slope, double intercept) {
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_FORCE_CALIBRATION_SERV);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_FORCE_0N_CALIBRATION);
                byte[] sendingBytes = ByteBuffer.allocate(8).putFloat(0, (float) intercept).putFloat(4,(float) slope).array();
                byte[] sendingBytesInverted = new byte[8];
                for (int i = 0; i < 8;i++){
                    sendingBytesInverted[i] = sendingBytes[7-i];
                }
                characteristic.setValue(sendingBytesInverted);
                Log.v("sendingBytes",Arrays.toString(sendingBytes));
                Log.v("sendingBytesInverted",Arrays.toString(sendingBytesInverted));
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            catch (Exception e){
                makeToast("Set calibration value failed. Please connect again and try.");
            }
        }
    }

    public void setActiveThresholdValue(int low, int high) {
        if (mBluetoothGatt != null && connected) {
            try{
                BluetoothGattService service = mBluetoothGatt.getService(UUID_FORCE_CALIBRATION_SERV);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_FORCE_1N_CALIBRATION);
                characteristic.setValue(new byte[]{(byte)low,(byte)high});
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            catch (Exception e){
                makeToast("Set calibration value failed. Please connect again and try.");
            }
        }
    }

    /*
     * Function Name: updateForceSampling
     *
     * Function Detail: Enable or disable force notification of current connected device.
     * Search UUID_FORCE_SENSOR_VALUE in onCharacteristicChanged of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input: boolean enable. True to start notification. False to end notification.
     *
     * Output: None.
     * */
    public void updateForceSampling(boolean enable) {
        if (mBluetoothGatt != null  && connected) {
            if(calibrated || deviceInfoVal == activeBraceMonitor) {
                try{
                    BluetoothGattService service = mBluetoothGatt.getService(UUID_FORCE_SENSOR_SERV);
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_FORCE_SENSOR_VALUE);
                    mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
                    BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
                    if (enable){
                        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    }else{
                        desc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                    mBluetoothGatt.writeDescriptor(desc);
                }
                catch (Exception e){
                    makeToast("Update force failed. Please connect again and try.");
                }
            }
            else{
                Toast.makeText(context, "Device not calibrated!", Toast.LENGTH_LONG).show();
            }
        }
    }
    /*
     * Function Name: setDeviceTime
     *
     * Function Detail: Write current android's local time to the firmware.
     * Search UUID_LNG_TRM_DATE in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input:  None.
     *
     * Output: None.
     * */
    public void setDeviceTime() {
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID_LNG_TRM_SERV).getCharacteristic(UUID_LNG_TRM_DATE);
        if (characteristic != null) {
            try{
                Calendar c = Calendar.getInstance();
                int seconds = c.get(Calendar.SECOND);
                int minutes = c.get(Calendar.MINUTE);
                int hours = c.get(Calendar.HOUR_OF_DAY);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH)+1;
                int year = c.get(Calendar.YEAR) - 2000;

                byte[] dateTime = new byte[]{(byte) year,(byte)month,(byte)day,(byte)hours,(byte)minutes,(byte)seconds};
                characteristic.setValue(dateTime);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }catch (Exception e) {
                makeToast("Set device time failed. Please connect again and try.");
            }
        }
    }

    public void setDeviceWakeupTime(byte[] array) {
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID_LNG_TRM_SERV).getCharacteristic(UUID_LNG_TRM_DATE);
        if (characteristic != null) {
            try{
                characteristic.setValue(array);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }catch (Exception e) {
                makeToast("Set device time failed. Please connect again and try.");
            }
        }
    }
    /*
     * Function Name: setDeviceCalibrationVal
     *
     * Function Detail: Manually input ADC voltage of 0,1,2,6 N's calibration.
     *                  Force brace monitor to use these values for calibration.
     * Search UUID_FORCE_1N_CALIBRATION in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input:  int cali0, int cali1,int cali2, int cali3
     *
     * Output: None.
     * */
    public void setDeviceCalibrationVal(int cali0, int cali1,int cali2, int cali3) {
        if (mBluetoothGatt != null) {
            BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID_FORCE_CALIBRATION_SERV).getCharacteristic(UUID_FORCE_1N_CALIBRATION);
            if (characteristic != null) {
                try{
                    characteristic.setValue(new byte[]{(byte)cali0,(byte)(cali0>>8), (byte)cali1, (byte)(cali1>>8),(byte)cali2, (byte)(cali2>>8),(byte)cali3, (byte)(cali3>>8)});
                    //characteristic.setValue(new byte[]{(byte)(cali0>>8),(byte)cali0,(byte)(cali1>>8), (byte)cali1, (byte)(cali2>>8),(byte)cali2, (byte)(cali3>>8), (byte)cali3});
                    Log.v("cali",Arrays.toString(new byte[]{(byte)cali0, (byte)(cali0>>8), (byte)cali1, (byte)(cali1>>8),(byte)cali2, (byte)(cali2>>8),(byte)cali3, (byte)(cali3>>8)}));
                    makeToast("Set device calibration value successfully.");
                    mBluetoothGatt.writeCharacteristic(characteristic);
                }
                catch (Exception e) {
                    makeToast("Set device calibration failed. Please connect again and try.");
                }
            }
        } else {
            makeToast("No device connected!");
        }
    }
    /*
     * Function Name: setDeviceID
     *
     * Function Detail: Set Current Device's ID. (ex: if set as 12, the name
     *                  Brace monitor000 will change to BraceMonitor012)
     * Search UUID_IDENTIFIER_VAL in onCharacteristicWrite of mGattCallback to
     * see what to do when callback is received from the firmware.
     * (If nothing then no callback or nothing to do after sending this)
     *
     * Input:  int id
     *
     * Output: None.
     * */
    public void setDeviceID(int id) {
        String idString = Integer.toString(id);

        if (idString.length() == 1){
            idString = "00" + idString;
        }else if (idString.length() == 2){
            idString = "0" + idString;
        }
        if (mBluetoothGatt != null) {
            BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID_IDENTIFIER_SERV).getCharacteristic(UUID_IDENTIFIER_VAL);
            if (characteristic != null) {
                try{
                    characteristic.setValue(new byte[]{(byte)Character.getNumericValue(idString.charAt(0)), (byte)Character.getNumericValue(idString.charAt(1)), (byte) Character.getNumericValue(idString.charAt(2))});
                    makeToast("Set device ID successfully.");
                    mBluetoothGatt.writeCharacteristic(characteristic);
                }
                catch (Exception e) {
                    makeToast("Set device ID failed. Please connect again and try.");
                }
            }
        } else {
            makeToast("No device connected!");
        }
    }

    public double[] getForceCalibration(){
        return new double[]{forceCaliVal0,forceCaliVal1,forceCaliVal2,forceCaliVal3};
    }
    /*
     * Function Name: broadcastUpdate
     *
     * Function Detail: The function broadcast actions to fragments. Fragments receive updates with updateReceiver
     *
     * Input: String action, Constants of action.
     *          Object data, data to be sent. Data have to be type of int/string/double.
     *
     * Output: None.
     * */
    private void broadcastUpdate(String action, double data) {
        Intent intent = new Intent(action);
        if (ACTION_FORCE_UPDATE.equals(action)){
            intent.putExtra("forceVal",data);
        }
        else if (ACTION_TEMP_UPDATE.equals(action)){
            intent.putExtra("temperatureVal",data);
        }
        else if (ACTION_DATA_DOWNLOAD.equals(action)){
            intent.putExtra("downloadVal",data);
        }
        else if (ACTION_DATA_ERASE.equals(action)){
            intent.putExtra("erase",data);
        }
        else{
            intent.putExtra("adcVoltage",data);
        }
        context.sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, String data) {
        Intent intent = new Intent(action);
        if (ACTION_GATT_CONNECTED.equals(action)) {
            intent.putExtra("deviceName", data);
        }
        else if (ACTION_GATT_DISCONNECTED.equals(action)){
            intent.putExtra("status",data);
        }

        else {
            intent.putExtra("","");
        }
        context.sendBroadcast(intent);
    }
    /*
     * Function Name: broadcastUpdate
     *
     * Function Detail: The function broadcast actions to fragments. Fragments receive updates with updateReceiver
     *
     * Input: String action, Constants of action.
     *        byte[] data
     *
     * Output: None.
     * */
    private void broadcastUpdate(final String action, byte[] data) {
        Intent intent = new Intent(action);
        if (ACTION_EXTERNAL_TEST.equals(action)) {
            intent.putExtra("block", data[0]);
            intent.putExtra("status", data[1]);
        }
        context.sendBroadcast(intent);
    }

    public void makeToast(final String message) {
        ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        final Toast jam = Toast.makeText(context, message, Toast.LENGTH_LONG);
                        jam.show();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                jam.cancel();
                            }
                        }, 3000);
            }
        });
    }

    public ArrayAdapter<String> getmBluetoothListAdapter() {
        return mBluetoothListAdapter;
    }
    public int getDeviceInfoVal(){return deviceInfoVal;}
    public static BluetoothLeService getmBluetoothLeService() {
        return mBluetoothLeService;
    }
    public int getDeviceVersionVal() {
        return deviceVersionVal;
    }
}

