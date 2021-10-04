package ca.ualberta.songdichong.bracemonitorbluenrg;

import java.util.UUID;
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

File Name          : Constants.java

Original Author    : Dichong Song

File Creation Date : 2021/03/26

File Description   : Some constants used in this project
 *
*/
public class Constants {
    public static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    //Battery service UUID
    public static final UUID UUID_BATT_SERV                 = UUID.fromString("00001818-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_BATT_VALUE                = UUID.fromString("00002B00-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DIRECT_CONTROL            = UUID.fromString("00002B20-0000-1000-8000-00805f9b34fb");
    //Temp service UUID
    public static final UUID UUID_TEMP_SERV                 = UUID.fromString("00001817-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_TEMP_VALUE                = UUID.fromString("00002B02-0000-1000-8000-00805f9b34fb");
    //Data download service UUID
    public static final UUID UUID_FLASH_SERV                = UUID.fromString("00001815-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DOWNLOAD_VALUE            = UUID.fromString("00002B06-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DEVICE_HOLDER_VALUE       = UUID.fromString("00002BA0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_EXTERNAL_TEST_VALUE       = UUID.fromString("00002BA2-0000-1000-8000-00805f9b34fb");
    //Force calibration service UUID
    public static final UUID UUID_FORCE_CALIBRATION_SERV    = UUID.fromString("0000181A-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_FORCE_0N_CALIBRATION      = UUID.fromString("00002B07-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_FORCE_1N_CALIBRATION      = UUID.fromString("00002B08-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_FORCE_2N_CALIBRATION      = UUID.fromString("00002B09-0000-1000-8000-00805f9b34fb");
    //Long term service UUID
    public static final UUID UUID_LNG_TRM_SERV              = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_LNG_TRM_MODE              = UUID.fromString("00002B03-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_LNG_SAMPLING_RATE         = UUID.fromString("00002B04-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_LNG_TRM_DATE              = UUID.fromString("00002B05-0000-1000-8000-00805f9b34fb");
    //Force sensor service UUID
    public static final UUID UUID_FORCE_SENSOR_SERV         = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_FORCE_SENSOR_VALUE        = UUID.fromString("00002B01-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_TARGET_FORCE_VALUE        = UUID.fromString("00002BA1-0000-1000-8000-00805f9b34fb");
    //unit identifier UUID
    public static final UUID UUID_IDENTIFIER_SERV           = UUID.fromString("0000191E-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_IDENTIFIER_VAL            = UUID.fromString("00002B0D-0000-1000-8000-00805f9b34fb");
    //device info UUID
    public static final UUID UUID_DEVICEINFO_SERV           = UUID.fromString("0000181F-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DEVICEINFO_VAL            = UUID.fromString("00002B0F-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DEVICE_VERSION_VAL        = UUID.fromString("00002B1F-0000-1000-8000-00805f9b34fb");

    public static final String FILENAME = "BraceMonitorLog.csv";
    public static final String REALTIMEDATAFILENAME = "RealTimeData.csv";
    public static final String ACTIVE_FILENAME = "ActiveBraceMonitorLog.csv";
    public static final String ACTION_GATT_CONNECTED = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SCAN_COMPLETE = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_SCAN_COMPLETE";
    public static final String ACTION_BATTERY_READ = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_BATTERY_READ";
    public static final String ACTION_TEMP_UPDATE = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_TEMP_UPDATE";
    public static final String ACTION_TEMPRAW_UPDATE = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_TEMPRAW_UPDATE";
    public static final String ACTION_FORCE_UPDATE = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_FORCE_UPDATE";
    public static final String ACTION_DATA_DOWNLOAD = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_DATA_DOWNLOAD";
    public static final String ACTION_EXTERNAL_TEST = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_EXTERNAL_TEST";
    public static final String ACTION_DATA_ERASE = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_DATA_ERASE";
    public static final String ACTION_VERSION_UPDATE = "ca.ualberta.songdichong.bracemonitorbluenrg.ACTION_VERSION_UPDATE";

    public static final int externalFlashBraceMonitor = 0;
    public static final int internalFlashBraceMonitor = 1;
    public static final int activeBraceMonitor = 2;

    public static final int batterySensor = 0;
    public static final int ADC_Input_AdcPin2 = 1;
    public static final int ADC_Input_AdcPin1 = 2;
    public static final int temperatureSensor = 3;
    public static final int memoryEndAddress = 0x3CFFFF;


}
