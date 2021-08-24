package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import java.util.Objects;

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
