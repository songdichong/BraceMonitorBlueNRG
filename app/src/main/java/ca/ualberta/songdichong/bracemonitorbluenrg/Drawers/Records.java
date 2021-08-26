package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;

/**
 * @author songdichong
 *
 * @object Drawers.Records: record specific time, force and temperature information to draw graph
 *
 * @constructor  int year, int month, int date, int hour, int minute, double force, double temperature
 *
 * @return int year, int month, int date, int hour, int minute, double force, double temperature
 */
public abstract class Records {
    public boolean isHeader = false;

    public String getHeaderString(boolean isActive)
    {
        return null;
    }
}
