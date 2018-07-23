package com.song.alan.mypomodorotimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int COUNT_DOWN_INTERVAL = 500;

    private View mViewDummyOutside;
    private EditText mEditTextTask;

    private TextView mTextViewCountDown;

    private Button mButtonStartPause;
    private Button mButtonReset;
    private NumberPicker mNumberPickerTime;
    private ViewSwitcher mViewSwitcherTime;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;
    private boolean mTimerPaused;

    private long mStartTimeInMillis;
    private long mTimeLeftinMillis;
    private long mEndTime;

    private String[] time_values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initialize();
    }

    private void initialize() {
        findViews();
        setOnClickListeners();

        initializeViewSwitcher();
    }


    private void initializeAfterLoading() {
        initializeNumberPicker();
    }

    private void findViews() {
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);

        mEditTextTask = findViewById(R.id.edit_text_task);

        mViewSwitcherTime = findViewById(R.id.view_switcher_countdown);
        mNumberPickerTime = findViewById(R.id.number_picker_countdown);

        mViewDummyOutside = findViewById(R.id.view_dummy_outside);
    }

    private void setOnClickListeners() {
        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        mTextViewCountDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTimerRunning && !mTimerPaused) {
                    mViewSwitcherTime.showNext();
                }
            }
        });

        mViewDummyOutside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewSwitcherTime.getCurrentView() == mNumberPickerTime) {
                    long time = Long.parseLong(time_values[mNumberPickerTime.getValue()]) * 60000;
                    setTime(time);

                    mViewSwitcherTime.showNext();
                }
            }
        });
    }

    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        resetTimer();
    }

    private void startTimer() {
        if (!mTimerPaused) {
            long time = Long.parseLong(time_values[mNumberPickerTime.getValue()]) * 60000;
            setTime(time);
        }

        if (mViewSwitcherTime.getCurrentView() == mNumberPickerTime) {
            mViewSwitcherTime.showNext();
        }

        mEndTime = System.currentTimeMillis() + mTimeLeftinMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftinMillis, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftinMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                vibrate(1500);

                mTimerRunning = false;
                mTimerPaused = false;
                updateWatchInterface();
            }
        }.start();

        mTimerRunning = true;
        updateWatchInterface();
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        mTimerPaused = true;
        updateWatchInterface();
    }

    private void resetTimer() {
        mTimeLeftinMillis = mStartTimeInMillis;
        mTimerPaused = false;
        updateCountDownText();
        updateWatchInterface();
    }

    private void updateCountDownText() {
        int hours = (int) mTimeLeftinMillis / 1000 / 3600;
        int minutes = (int) ((mTimeLeftinMillis / 1000) % 3600) / 60;
        int seconds = (int) mTimeLeftinMillis / 1000 % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }

        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void updateWatchInterface() {
        if (mTimerRunning) {
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        } else {
            mButtonStartPause.setText("Start");

            if (mTimeLeftinMillis < COUNT_DOWN_INTERVAL) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftinMillis < mStartTimeInMillis) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void vibrate(int milliseconds) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            vibrator.vibrate(milliseconds);
        }
    }

    private void initializeNumberPicker() {
        time_values = getResources().getStringArray(R.array.countdown_times);
        long minutes = mStartTimeInMillis / 60000;
        String stringMinutes = String.valueOf((minutes));
        int default_index = Arrays.asList(time_values).indexOf(stringMinutes);

        mNumberPickerTime.setMinValue(0);
        mNumberPickerTime.setMaxValue(time_values.length-1);
        mNumberPickerTime.setValue(default_index);
        mNumberPickerTime.setDisplayedValues(time_values);
    }

    private void initializeViewSwitcher() {

    }

    @Override
    protected void onStop() {
        super.onStop();

        saveVariables();
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadVariables();
        initializeAfterLoading();
    }

    private void loadVariables() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mStartTimeInMillis = prefs.getLong("start_time_in_millis", 1500000);
        mTimeLeftinMillis = prefs.getLong("millis_left", mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timer_running", false);
        mTimerPaused = prefs.getBoolean("timer_paused", false);

        updateCountDownText();
        updateWatchInterface();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("end_time", 0);
            mTimeLeftinMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftinMillis < 0) {
                mTimeLeftinMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
    }

    private void saveVariables() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("start_time_in_millis", mStartTimeInMillis);
        editor.putLong("millis_left", mTimeLeftinMillis);
        editor.putBoolean("timer_running", mTimerRunning);
        editor.putBoolean("timer_paused", mTimerPaused);
        editor.putLong("end_time", mEndTime);
        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}
