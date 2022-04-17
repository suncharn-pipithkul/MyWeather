package com.bignerdranch.android.myweather;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MyWeatherActivity extends AppCompatActivity
{
    private static final String TAG = "MyWeatherActivity";

    private ConstraintLayout mainLayout;
    private EditText mCitySearchEditText;
    private String mSearchString;

    // Current weather views
    private TextView mCurrentTempTextView;
    private TextView mWeatherDescriptionTextView;
    private TextView mQueryTimeTextView;
    //private Button mButton;
    private ImageButton mUpdateButton;
    private ImageView mCurrentWeatherIcon;
    private Weather mCurrentWeather;

    // Tomorrow weather views
    private TextView mTomorrowDateTextView;
    private TextView m3AMtempTextView;
    private ImageView m3AMIcon;
    private TextView m6AMtempTextView;
    private ImageView m6AMIcon;
    private TextView m9AMtempTextView;
    private ImageView m9AMIcon;
    private TextView m12PMtempTextView;
    private ImageView m12PMIcon;
    private TextView m3PMtempTextView;
    private ImageView m3PMIcon;
    private TextView m6PMtempTextView;
    private ImageView m6PMIcon;
    private TextView m9PMtempTextView;
    private ImageView m9PMIcon;
    private Weather[] mTmrWeather;
    private final int NUMFORECAST = 7;
    private ImageView[] tmrIconArr;
    private TextView[] tmrTempTextArr;

    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_weather);

        // Initialize the weather classes
        mCurrentWeather = new Weather();
        mTmrWeather = new Weather[NUMFORECAST];
        for(int i = 0; i < NUMFORECAST; i++)
            mTmrWeather[i] = new Weather();

        mainLayout = (ConstraintLayout) findViewById(R.id.main_constraint_layout);

        mSearchString = "Boston";
        mCitySearchEditText = (EditText) findViewById(R.id.search_edit_text);
        mCitySearchEditText.setText(mSearchString);
        mCitySearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if(actionId==EditorInfo.IME_ACTION_DONE)
                {
                    // Send query to openWeatherMap
                    mSearchString = mCitySearchEditText.getText().toString();
                    //Log.i(TAG, "mQuery : " + mQuery);
                    new FetchItemTask().execute();
                }
                return false;
            }
        });

        // Current weather views
        mCurrentTempTextView = (TextView) findViewById(R.id.current_temp_text_view);
        mWeatherDescriptionTextView = (TextView) findViewById(R.id.weather_description_text_view);
        mQueryTimeTextView = (TextView) findViewById(R.id.query_time_text_view);
        mCurrentWeatherIcon = (ImageView) findViewById(R.id.current_weather_icon_image_view);

        // Tomorrow weather views
        mTomorrowDateTextView = (TextView) findViewById(R.id.tmr_date_text_view);
        m3AMtempTextView = (TextView) findViewById(R.id.tmr_3am_temp_text_view);
        m6AMtempTextView = (TextView) findViewById(R.id.tmr_6am_temp_text_view);
        m9AMtempTextView = (TextView) findViewById(R.id.tmr_9am_temp_text_view);
        m12PMtempTextView = (TextView) findViewById(R.id.tmr_12pm_temp_text_view);
        m3PMtempTextView = (TextView) findViewById(R.id.tmr_3pm_temp_text_view);
        m6PMtempTextView = (TextView) findViewById(R.id.tmr_6pm_temp_text_view);
        m9PMtempTextView = (TextView) findViewById(R.id.tmr_9pm_temp_text_view);
        tmrTempTextArr = new TextView[NUMFORECAST];
        tmrTempTextArr[0] = m3AMtempTextView;
        tmrTempTextArr[1] = m6AMtempTextView;
        tmrTempTextArr[2] = m9AMtempTextView;
        tmrTempTextArr[3] = m12PMtempTextView;
        tmrTempTextArr[4] = m3PMtempTextView;
        tmrTempTextArr[5] = m6PMtempTextView;
        tmrTempTextArr[6] = m9PMtempTextView;

        m3AMIcon = (ImageView) findViewById(R.id.tmr_3am_icon_image_view);
        m6AMIcon = (ImageView) findViewById(R.id.tmr_6am_icon_image_view);
        m9AMIcon = (ImageView) findViewById(R.id.tmr_9am_icon_image_view);
        m12PMIcon = (ImageView) findViewById(R.id.tmr_12pm_icon_image_view);
        m3PMIcon = (ImageView) findViewById(R.id.tmr_3pm_icon_image_view);
        m6PMIcon = (ImageView) findViewById(R.id.tmr_6pm_icon_image_view);
        m9PMIcon = (ImageView) findViewById(R.id.tmr_9pm_icon_image_view);
        tmrIconArr = new ImageView[NUMFORECAST];
        tmrIconArr[0] = m3AMIcon;
        tmrIconArr[1] = m6AMIcon;
        tmrIconArr[2] = m9AMIcon;
        tmrIconArr[3] = m12PMIcon;
        tmrIconArr[4] = m3PMIcon;
        tmrIconArr[5] = m6PMIcon;
        tmrIconArr[6] = m9PMIcon;

        /*mButton = (Button) findViewById(R.id.update_button);
        mButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new FetchItemTask().execute();
            }
        });*/

        mUpdateButton = (ImageButton)  findViewById(R.id.update_image_button);
        mUpdateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new FetchItemTask().execute();
                Toast.makeText(getApplicationContext(), "Data Updated", Toast.LENGTH_SHORT).show();
            }
        });
        //update_image_button

        // Perform data update
        //new FetchItemTask().execute();
        setRepeatingAsyncTask();
    }

    @Override
    protected void onDestroy()
    {
        mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }

    private class FetchItemTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            mCurrentWeather = new OpenWeatherFetchr().fetchItems(mSearchString);
            mTmrWeather = new OpenWeatherFetchr().fetchItemsArray(mSearchString);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            // Update UI
            mCurrentTempTextView.setText(Double.toString(mCurrentWeather.temp));
            mWeatherDescriptionTextView.setText(mCurrentWeather.weatherDescription);

            DateFormat df = new SimpleDateFormat("MMMM dd, KK:mm a");
            mQueryTimeTextView.setText(df.format(mCurrentWeather.epochTime));

            // Set the background color
            if(mCurrentWeather.isDay)
                mainLayout.setBackgroundColor(getResources().getColor(R.color.colorBackgroundDay));
            else
                mainLayout.setBackgroundColor(getResources().getColor(R.color.colorBackgroundNight));

            // Set the appropriate icon
            iconSelector(mCurrentWeatherIcon, mCurrentWeather.icon);

            // Set tmr date
            Calendar calendar = Calendar.getInstance(); // get current date
            calendar.add(Calendar.DAY_OF_YEAR, 1); // add 1 day to current date
            Date tomorrow = calendar.getTime();
            DateFormat dfDate = new SimpleDateFormat("MMMM dd");
            mTomorrowDateTextView.setText(dfDate.format(tomorrow));

            // Set tmr weather
            for(int i = 0; i < NUMFORECAST; i++)
            {
                tmrTempTextArr[i].setText(String.format("%.0f", mTmrWeather[i].temp) + "°");
                iconSelector(tmrIconArr[i], mTmrWeather[i].icon);
            }
        }
    }

    private void setRepeatingAsyncTask()
    {
        mHandler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                mRunnable = new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            new FetchItemTask().execute();
                        }
                        catch (Exception e)
                        {
                            Log.e(TAG, "Failed to fetch items", e);
                        }
                    }
                };
                mHandler.post(mRunnable);
            }
        };

        timer.schedule(task, 0, 60*1000* 10);  // interval of 10 minute
    }

    private void iconSelector(ImageView icon, String iconID)
    {
        if(iconID.equals("01d"))
            icon.setImageResource(R.drawable.sunny);
        else if (iconID.equals("01n"))
            icon.setImageResource(R.drawable.night);
        else if (iconID.equals("02d"))
            icon.setImageResource(R.drawable.partlycloudyday);
        else if (iconID.equals("02n"))
            icon.setImageResource(R.drawable.partlycloudynight);
        else if (iconID.equals("03d"))
            icon.setImageResource(R.drawable.cloudy);
        else if (iconID.equals("03n"))
            icon.setImageResource(R.drawable.cloudy);
        else if (iconID.equals("04d"))
            icon.setImageResource(R.drawable.overcast);
        else if (iconID.equals("04n"))
            icon.setImageResource(R.drawable.overcast);
        else if (iconID.equals("09d"))
            icon.setImageResource(R.drawable.lightrainday);
        else if (iconID.equals("09n"))
            icon.setImageResource(R.drawable.lightrainnight);
        else if (iconID.equals("10d"))
            icon.setImageResource(R.drawable.rainday);
        else if (iconID.equals("10n"))
            icon.setImageResource(R.drawable.rainnight);
        else if (iconID.equals("11d"))
            icon.setImageResource(R.drawable.thunderday);
        else if (iconID.equals("11n"))
            icon.setImageResource(R.drawable.thundernight);
        else if (iconID.equals("13d"))
            icon.setImageResource(R.drawable.snowday);
        else if (iconID.equals("13n"))
            icon.setImageResource(R.drawable.snownight);
        else if (iconID.equals("50d"))
            icon.setImageResource(R.drawable.fog);
        else if (iconID.equals("50n"))
            icon.setImageResource(R.drawable.fog);
        else
            icon.setImageResource(R.drawable.default_weather);
    }


}