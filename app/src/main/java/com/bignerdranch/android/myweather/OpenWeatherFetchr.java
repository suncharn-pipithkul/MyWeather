package com.bignerdranch.android.myweather;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

public class OpenWeatherFetchr
{
    private static final String TAG = "OpenWeatherFetchr";
    private static final String TAG_FORECAST = "Forecast";

    private static final String API_KEY = "ec1d6173cd3d271d2a98547464fb46ea";

    public byte[] getUrlBytes(String urlSpec) throws IOException
    {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally
        {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException
    {
        return new String(getUrlBytes(urlSpec));
    }

    public Weather fetchItems(String city)
    {
        Weather currentWeather = new Weather();

        try
        {
            String url = Uri.parse("http://api.openweathermap.org/data/2.5/weather")
                    .buildUpon()
                    .appendQueryParameter("q", city)
                    .appendQueryParameter("APPID", API_KEY)
                    .appendQueryParameter("units", "imperial") // fahrenheit
                    //.appendQueryParameter("units", "metric") // uncomment for celsius
                    .appendQueryParameter("type", "like")
                    .build().toString();
            String responseString = getUrlString(url);
            Log.i(TAG, "Received response: " + responseString);
            JSONObject jsonBody = new JSONObject(responseString);
            currentWeather = parseJSONCurrentResult(jsonBody);

            return currentWeather;
        }
        catch (IOException ioe)
        {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        catch (JSONException je)
        {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return currentWeather;
    }

    public Weather[] fetchItemsArray(String city)
    {
        Weather[] tmrWeather = new Weather[7];
        for(int i = 0; i < 7; i++)
            tmrWeather[i] = new Weather();

        try
        {
            String url = Uri.parse("http://api.openweathermap.org/data/2.5/forecast")
                    .buildUpon()
                    .appendQueryParameter("q", city)
                    .appendQueryParameter("APPID", API_KEY)
                    .appendQueryParameter("units", "imperial") // fahrenheit
                    //.appendQueryParameter("units", "metric") // uncomment for celsius
                    .appendQueryParameter("type", "like")
                    .build().toString();
            String responseString = getUrlString(url);
            Log.i(TAG_FORECAST, "Received response: " + responseString);
            JSONObject jsonBody = new JSONObject(responseString);

            tmrWeather = parseJSONForecastResult(jsonBody);

            return tmrWeather;
        }
        catch (IOException ioe)
        {
            Log.e(TAG_FORECAST, "Failed to fetch items", ioe);
        }
        catch (JSONException je)
        {
            Log.e(TAG_FORECAST, "Failed to parse JSON", je);
        }
        return tmrWeather;
    }

    public Weather parseJSONCurrentResult(JSONObject jsonBody) throws JSONException
    {
        JSONArray weatherJsonArray = jsonBody.getJSONArray("weather");
        Weather weather = new Weather();

        // Get data from weather array
        for(int i = 0; i < weatherJsonArray.length(); i++)
        {
            // Get weather description
            weather.weatherDescription += weatherJsonArray.getJSONObject(i)
                                                            .getString("description") + "\n";
            // Get weather icon
            if(i == 0)
                weather.icon = weatherJsonArray.getJSONObject(i).getString("icon");
        }

        // Get the current temp from main
        JSONObject mainJsonObj = jsonBody.getJSONObject("main");
        weather.temp = mainJsonObj.getDouble("temp");

        // Get query time from current time
        //long queryTime = jsonBody.getLong("dt");
        //weather.epochTime = new Date(queryTime);
        weather.epochTime = Calendar.getInstance().getTime();

        weather.isDay = weather.icon.contains("d");

        return weather;
    }

    public Weather[] parseJSONForecastResult(JSONObject jsonBody) throws JSONException
    {
        JSONArray podsJsonArray = jsonBody.getJSONArray("list");
        Weather[] weatherArr = new Weather[7];
        for(int i = 0; i < 7; i++)
            weatherArr[i] = new Weather();

        // Set tmr date
        Calendar calendar = Calendar.getInstance(); // get current date
        calendar.add(Calendar.DAY_OF_YEAR, 1); // add 1 day to current date
        Date tomorrow = calendar.getTime();
        DateFormat dfDate = new SimpleDateFormat("MMMM dd");

        //dfDate.setTimeZone(ZonedDateTime.now());
        String tmrString = dfDate.format(tomorrow);
        int weatherIndex = 0;
        int midnight = 0;

        for(int i = 0; i < podsJsonArray.length(); i++)
        {
            JSONObject listJsonObj =  podsJsonArray.getJSONObject(i);
            JSONObject mainJsonObj = listJsonObj.getJSONObject("main");

            //String forecastTime = mainJsonObj.getString("dt_txt");
            long forecastEpoch = listJsonObj.getLong("dt");
            Date forecastDate = new Date(forecastEpoch * 1000L);
            String forecastDateString = dfDate.format(forecastDate);

            if (tmrString.equalsIgnoreCase(forecastDateString)) // forecast date == tmr
            {
                // skip the first one (midnight)
                if(midnight == 0)
                {
                    midnight++;
                    continue;
                }

                weatherArr[weatherIndex].temp = mainJsonObj.getDouble("temp");

                JSONArray weatherJsonArray = listJsonObj.getJSONArray("weather");
                weatherArr[weatherIndex].icon = weatherJsonArray.getJSONObject(0).getString("icon");
                weatherIndex++;
            }

            //Log.i(TAG_FORECAST, forecastTime + " is " + weatherArr[i].temp + " F");
        }
        return weatherArr;
    }
}
