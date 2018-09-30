package com.example.android.timer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;

public class TimerActivity extends AppCompatActivity implements View.OnClickListener {

    private enum ActiveButton {
        STOP,
        PAUSE,
        START
    }

    private enum TimerState {
        RUNNING_W,
        RUNNING_SR,
        RUNNING_LR
    }

    private ActiveButton activeButton = ActiveButton.STOP;
    private TimerState timerState = TimerState.RUNNING_W;

    private ProgressBar progressBarCircle;
    private EditText editTextMinute;
    private TextView textViewTime;
    private TextView textViewState;
    private ImageView imageViewReset;
    private ImageView buttonStartResume;
    private ImageView buttonPause;
    private ImageView buttonStop;
    private CountDownTimer countDownTimer;

    private long timeCountInMilliSeconds = 0;
    private long timeLeftInMillis = 0;
    private long endTime = 0;
    private int pomodoroCounter = 0;
    private boolean bRestore = false;
    private boolean bStarted = false;
    public static boolean isOpen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // method call to initialize the views
        initViews();
        // method call to initialize the listeners
        initListeners();
    }

    /**
     * method to initialize the views
     */
    private void initViews() {
        progressBarCircle = findViewById(R.id.progressBarCircle);
        editTextMinute = findViewById(R.id.editTextMinute);
        textViewTime = findViewById(R.id.textViewTime);
        textViewState = findViewById(R.id.textViewState);
        imageViewReset = findViewById(R.id.imageViewReset);
        buttonStartResume = findViewById(R.id.imageViewStart);
        buttonPause = findViewById(R.id.imageViewPause);
        buttonStop = findViewById(R.id.imageViewStop);
    }

    /**
     * method to initialize the click listeners
     */
    private void initListeners() {
        imageViewReset.setOnClickListener(this);
        buttonStartResume.setOnClickListener(this);
        buttonPause.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
    }

    /**
     * method to listen clicks
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewReset:
                resetTimer();
                // changing the active button
                activeButton = ActiveButton.START;
                updateViews();
                break;
            case R.id.imageViewStart:
                if (editTextMinute.getText().toString().isEmpty()
                || Integer.parseInt(editTextMinute.getText().toString().trim()) == 0)
                    Toast.makeText(getApplicationContext(), getString(R.string.message_minutes), Toast.LENGTH_SHORT).show();

                else {
                    startResumeTimer();
                    updateViews();
                }
                break;
            case R.id.imageViewPause:
                pauseTimer();
                updateViews();
                break;
            case R.id.imageViewStop:
                stopTimer();
                // changing the active button
                activeButton = ActiveButton.STOP;
                updateViews();
                break;
        }
    }

    /**
     * method to update views
     */
    private void updateViews() {
        if (bRestore) {
            if (activeButton == ActiveButton.PAUSE)
                activeButton = ActiveButton.START;
            else if (activeButton == ActiveButton.START) {
                if (bStarted)
                    activeButton = ActiveButton.PAUSE;
                else
                    activeButton = ActiveButton.STOP;
            }
            else if (activeButton == ActiveButton.STOP)
                activeButton = ActiveButton.START;
        }

        if (activeButton == ActiveButton.START) {
            // change state text
            if (timerState == TimerState.RUNNING_W)
                textViewState.setText(R.string.state_work);
            else if (timerState == TimerState.RUNNING_SR)
                textViewState.setText(R.string.state_short_rest);
            else
                textViewState.setText(R.string.state_long_rest);
            // showing the resetTimer icon
            imageViewReset.setVisibility(View.VISIBLE);
            // changing play icon to pause icon
            buttonStartResume.setVisibility(View.GONE);
            buttonPause.setVisibility(View.VISIBLE);
            // making edit text not editable
            editTextMinute.setEnabled(false);
            // changing the active button
            activeButton = ActiveButton.PAUSE;

        } else if (activeButton == ActiveButton.PAUSE){
            // change state text
            textViewState.setText(R.string.state_paused);
            // showing the resetTimer icon
            imageViewReset.setVisibility(View.VISIBLE);
            // changing pause icon to start icon
            buttonStartResume.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.GONE);
            // making edit text not editable
            editTextMinute.setEnabled(false);
            // change time text
            textViewTime.setText(hmsTimeFormatter(timeLeftInMillis));
            // changing the active button
            activeButton = ActiveButton.START;

        } else if (activeButton == ActiveButton.STOP) {
            // change state text
            textViewState.setText(R.string.state_stopped);
            // hiding the resetTimer icon
            imageViewReset.setVisibility(View.GONE);
            // changing pause icon to start icon
            buttonStartResume.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.GONE);
            // making edit text editable
            editTextMinute.setEnabled(true);
            // change time text
            textViewTime.setText("00:00:00");
            // changing the active button
            activeButton = ActiveButton.START;
        }
        bRestore = false;
    }

    /**
     * method to resetTimer count down timer
     */
    private void resetTimer() {
        // call to stop the count down timer
        stopTimer();
        // call to start the count down timer
        startResumeTimer();
    }

    /**
     * method to start, pause and resume count down timer
     */
    private void startResumeTimer() {
         if (!bStarted) {
            // call to initialize the timer values
            setTimerValues();
            // call to initialize the progress bar values
            setProgressBarValues();
            // call to start the count down timer
            startTimer();
            bStarted = true;

         } else {
            // call to initialize the timer values
            setTimerValues();
            // call to start the count down timer
            startTimer();
        }

    }

    /**
     * method to pause count down timer
     */
    private void pauseTimer() {
        // call to stop the count down timer
        countDownTimer.cancel();
    }

    /**
     * method to stopTimer count down timer
     */
    public void stopTimer() {
        // call to stop the count down timer
        if (countDownTimer != null)
            countDownTimer.cancel();
        bStarted = false;
        progressBarCircle.setProgress(progressBarCircle.getMax());
    }

    /**
     * method to initialize the values for count down timer
     */
    private void setTimerValues() {
        double time;
        if (bStarted) {
            timeCountInMilliSeconds = timeLeftInMillis;

        } else if (timerState == TimerState.RUNNING_W) {
            // fetching value from edit text and type cast to integer
            time = Integer.parseInt(editTextMinute.getText().toString().trim());
            // assigning values after converting to milliseconds
            timeCountInMilliSeconds = (long) (time * 60 * 1000);

        } else if (timerState == TimerState.RUNNING_SR) {
            // fetching value from edit text and type cast to integer
            time = Integer.parseInt(editTextMinute.getText().toString().trim()) / 5;
            // assigning values after converting to milliseconds
            timeCountInMilliSeconds = (long) (time * 60 * 1000);

        } else if (timerState == TimerState.RUNNING_LR) {
            // fetching value from edit text and type cast to integer
            time = Integer.parseInt(editTextMinute.getText().toString().trim()) + Integer.parseInt(editTextMinute.getText().toString().trim()) / 5;
            // assigning values after converting to milliseconds
            timeCountInMilliSeconds = (long) (time * 60 * 1000);
        }
    }

    /**
     * method to control timer
     */
    private void startTimer() {
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                endTime = System.currentTimeMillis() + millisUntilFinished;
                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // call to stop the count down timer
                stopTimer();
                // changing the timer state
                activeButton = ActiveButton.STOP;
                updateViews();
                // changing the timer state
//                if (timerState == TimerState.RUNNING_W && pomodoroCounter <= 3){
//                    pomodoroCounter = pomodoroCounter++;
//                    timerState = TimerState.RUNNING_SR;
//
//                } else if (timerState == TimerState.RUNNING_W && pomodoroCounter == 4) {
//                    pomodoroCounter = 0;
//                    timerState = TimerState.RUNNING_LR;
//
//                } else if (timerState == TimerState.RUNNING_SR){
//                    timerState = TimerState.RUNNING_W;
//
//                }
            }
        }.start();
    }

    /**
     * method to set circular progress bar values
     */
    private void setProgressBarValues() {
        progressBarCircle.setMax(Integer.parseInt(editTextMinute.getText().toString().trim()) * 60);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);

    }

    /**
     * method to convert millisecond to time format
     */
    private String hmsTimeFormatter(long milliSeconds) {

        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

    }

    /**
     * method to save timer values
     */

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        bRestore = true;
        editor.putLong("timeCountInMilliSeconds", timeCountInMilliSeconds);
        editor.putLong("timeLeft", timeLeftInMillis);
        editor.putLong("endTime", endTime);
        editor.putString("editText", editTextMinute.getText().toString().trim());
        editor.putString("activeButton", String.valueOf(activeButton));
        editor.putString("timerState", String.valueOf(timerState));
        editor.putBoolean("bRestore", bRestore);
        editor.putBoolean("bStarted", bStarted);
        editor.putInt("pomodoroCounter", pomodoroCounter);
        editor.commit();

        isOpen = false;
        startService(new Intent(this, NotificationService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editTextMinute.setText(preferences.getString("editText", "25"));
        endTime = preferences.getLong("endTime", endTime);
        activeButton = ActiveButton.valueOf(preferences.getString("activeButton", String.valueOf(activeButton)));
        timerState = TimerState.valueOf(preferences.getString("timerState", String.valueOf(timerState)));
        bRestore = preferences.getBoolean("bRestore", bRestore);
        bStarted = preferences.getBoolean("bStarted", bStarted);
        pomodoroCounter = preferences.getInt("pomodoroCounter", pomodoroCounter);

        if (bStarted) {
            timeLeftInMillis = endTime - System.currentTimeMillis();
            if (timeLeftInMillis <= 0) {
                stopTimer();
                activeButton = ActiveButton.START;
            }
            else {
                if (activeButton == ActiveButton.START) {
                    timeLeftInMillis = preferences.getLong("timeLeft", timeLeftInMillis);
                    setTimerValues();
                    setProgressBarValues();
                    startTimer();
                    pauseTimer();
                }
                else {
                    if (countDownTimer != null)
                        pauseTimer();
                    setTimerValues();
                    setProgressBarValues();
                    startTimer();
                    activeButton = ActiveButton.STOP;
                }
            }
        } updateViews();

        isOpen = true;
        stopService(new Intent(this, NotificationService.class));
    }
}

