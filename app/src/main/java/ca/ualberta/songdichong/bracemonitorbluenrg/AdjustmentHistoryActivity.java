package ca.ualberta.songdichong.bracemonitorbluenrg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.ActiveAnalyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Analyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.NonHeaderRecords;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.ConfigureDrawerFragment;

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

File Name          : AdjustmentHistoryActivity.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description: This file creates an activity for viewing details of active brace monitor's adjustment history
*/
public class AdjustmentHistoryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_history);

        List<String> stringList = new ArrayList<>();
        ActiveAnalyzer analyzer = (ActiveAnalyzer) ConfigureDrawerFragment.analyzer;
        int[] startEndIndex = getIntent().getIntArrayExtra("startEndIndex");
        Date startDate = new Date (startEndIndex[0] -1900 ,startEndIndex[1] - 1,startEndIndex[2],startEndIndex[3],startEndIndex[4]);
        Date endDate = new Date (startEndIndex[5]-1900 ,startEndIndex[6]-1,startEndIndex[7],startEndIndex[8],startEndIndex[9]);
        List<Records> adjustmentRecordsList = analyzer.getAdjustmentRecordsList();
        for (Records r: adjustmentRecordsList){
            if (!r.isHeader){
                NonHeaderRecords record = (NonHeaderRecords) r;
                if (record.compareTo(startDate) >= 0 && record.compareTo(endDate) <= 0){
                    String readableString = record.getReadableString();
                    stringList.add(readableString);
                }
            }
        }
        TextView adjustTextView = findViewById(R.id.adjust_num_text);
        adjustTextView.setText(String.valueOf(adjustmentRecordsList.size()));

        ListView adjustmentHistoryList = findViewById(R.id.adjust_history_list);
        ArrayAdapter<String> historyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringList);
        adjustmentHistoryList.setAdapter(historyAdapter);
    }


}
