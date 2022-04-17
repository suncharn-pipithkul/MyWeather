package com.bignerdranch.android.myweather;

import java.util.Date;

public class Weather
{
    Date epochTime;
    double temp;
    String weatherDescription;
    String icon;
    boolean isDay;

    public Weather()
    {
        epochTime = new Date();
        temp = 0.0;
        weatherDescription = "";
        icon = "";
        isDay = true;
    }

    public Weather(Date epochTime, double temp, String weatherDescription, String icon, boolean isDay)
    {
        this.epochTime = epochTime;
        this.temp = temp;
        this.weatherDescription = weatherDescription;
        this.icon = icon;
        this.isDay = isDay;
    }

    public Date getEpochTime()
    {
        return epochTime;
    }

    public void setEpochTime(Date epochTime)
    {
        this.epochTime = epochTime;
    }

    public double getTemp()
    {
        return temp;
    }

    public void setTemp(double temp)
    {
        this.temp = temp;
    }

    public String getWeatherDescription()
    {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription)
    {
        this.weatherDescription = weatherDescription;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public boolean isDay()
    {
        return isDay;
    }

    public void setDay(boolean day)
    {
        isDay = day;
    }
}
