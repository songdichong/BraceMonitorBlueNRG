package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;
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

File Name          : Records.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description   : This is the abstract class of Records. There are 2 types of records and they are identified by
                     the attribute isHeader

Data Structure:
                    Analyzer:   List<Days>: contains days information
                                List<Records>: contains all records information

                    Days:       List<Records>: contains records information for that specific day

                    Records(abstract)----!isHeader---->NonHeaderRecords(abstract)------->ActiveRecords
                       |                                                        |------->PassiveRecords
                      |----------------------isHeader---------------------------------->HeaderRecords*/
public abstract class Records {
    public boolean isHeader = false;

    public abstract String getString(boolean isActive);
}
