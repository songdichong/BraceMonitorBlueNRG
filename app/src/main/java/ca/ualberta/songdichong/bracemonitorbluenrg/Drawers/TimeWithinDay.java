package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import java.util.Objects;
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

File Name          : TimeWithinDay.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description   : This is the TimeWithinDay object. There should be 24*60 = 1440 objects created.
                    It is used to draw graph.
*/
public class TimeWithinDay {
    int hour;
    int min;

    public int getHour() {
        return hour;
    }

    public int getMin() {
        return min;
    }

    public TimeWithinDay(int[] array){
        this.hour = array[0];
        this.min = array[1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeWithinDay days = (TimeWithinDay) o;
        return hour == days.hour &&
                min == days.min;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, min);
    }
}
