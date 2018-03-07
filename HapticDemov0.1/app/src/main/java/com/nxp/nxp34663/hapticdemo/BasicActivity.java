/*
 *   Copyright (c) 2018. NXP. All rights are reserved.
 *   Reproduction in whole or in part is prohibited without the prior
 *   written consent of the copyright owner.
 *
 *   This software and any compilation or derivative thereof is and
 *   shall remain the proprietary information of NXP and is highly
 *   confidential in nature. Any and all use hereof is restricted and
 *   is subject to the terms and conditions set forth in the software
 *   license agreement concluded with NXP.
 *
 *   Under no circumstances is this software or any derivative thereof
 *   to be combined with any Open Source Software in any way or
 *   licensed under any Open License Terms without the express prior
 *   written  permission of NXP.
 *
 *   For the purpose of this clause, the term Open Source Software means
 *   any software that is licensed under Open License Terms. Open
 *   License Terms means terms in any license that require as a
 *   condition of use, modification and/or distribution of a work
 *
 *            1. the making available of source code or other materials
 *               preferred for modification, or
 *
 *            2. the granting of permission for creating derivative
 *               works, or
 *
 *            3. the reproduction of certain notices or license terms
 *               in derivative works or accompanying documentation, or
 *
 *            4. the granting of a royalty-free license to any party
 *               under Intellectual Property Rights
 *
 *   regarding the work and/or any work that contains, is combined with,
 *   requires or otherwise is based on the work.
 *
 *   This software is provided for ease of recompilation only.
 *   Modification and reverse engineering of this software are strictly
 *   prohibited.
 *
 */

package com.nxp.nxp34663.hapticdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class BasicActivity extends AppCompatActivity {
    private static final String TAG = "[NXP][haptic basic]";
    private static final int DEFAULT_VIBRATING_TIME = 50;
    private boolean IsEnableKeyUpVibrating;
    private int KeyDown_VibrationTime = 0;
    private int KeyUp_VibrationTime = 0;
    private Vibrator mVibrator = null;
    private HomeListener mHomeListen = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        this.setTitle(R.string.strTitle);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        assert mVibrator != null;
		
        ExitApplication.getInstance().addActivity(this);

        /* load application settings. */
        LoadApplicationParameters();
		
		/* update component view. */
        SetupComponentListener();
        SetupComponentView(TRUE);
        UpdateInputComponentFontColor(IsEnableKeyUpVibrating);
        Log.d(TAG, "The basic UI creating is done. SDK_INT="+ Build.VERSION.SDK_INT);
    }

    private void LoadApplicationParameters(){
        SharedPreferences sharedPreferences = getSharedPreferences("haptic_data", MODE_PRIVATE);

        KeyDown_VibrationTime = sharedPreferences.getInt("KeyDownTime", DEFAULT_VIBRATING_TIME);
        KeyUp_VibrationTime = sharedPreferences.getInt("KeyUpTime", DEFAULT_VIBRATING_TIME);
        IsEnableKeyUpVibrating = sharedPreferences.getBoolean("EnableKeyup", FALSE);
        Log.d(TAG, "LoadApplicationParameters() KeyDownTime=" + KeyDown_VibrationTime + " KeyUpTime=" + KeyUp_VibrationTime + " EnableKeyup=" + IsEnableKeyUpVibrating);
    }

    private void SaveApplicationParameters(){
        SharedPreferences sharedPreferences = getSharedPreferences("haptic_data", MODE_PRIVATE);
        ReadKeyDownVibratingTime();
        ReadKeyUpVibratingTime();
        Log.d(TAG, "SaveApplicationParameters() KeyDownTime=" + KeyDown_VibrationTime + " KeyUpTime=" + KeyUp_VibrationTime + " EnableKeyup=" + IsEnableKeyUpVibrating);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putInt("KeyDownTime", KeyDown_VibrationTime);
        ed.putInt("KeyUpTime", KeyUp_VibrationTime);
        ed.putBoolean("EnableKeyup", IsEnableKeyUpVibrating);
        ed.commit();
    }

    private void SetupComponentView(Boolean show)
    {
        Log.d(TAG, "SetupComponentView() " + show);

        CheckBox cb = (CheckBox)findViewById(R.id.checkBoxKeyupVibrating);
        if (cb != null)
            cb.setChecked(IsEnableKeyUpVibrating);

        int visible = View.VISIBLE;
        if (!show)
            visible = View.INVISIBLE;

        TextView tv = (TextView)findViewById(R.id.keydown_tv1);
        if (tv != null)
            tv.setVisibility(visible);

        tv = (TextView)findViewById(R.id.keydown_tv2);
        if (tv != null)
            tv.setVisibility(visible);

        EditText et = (EditText)findViewById(R.id.KeyDownInput);
        if (et != null) {
            et.setVisibility(visible);
            if (show) {
                et.setText(Integer.toString(KeyDown_VibrationTime));
                et.setSelection((Integer.toString(KeyDown_VibrationTime)).length());
            }
        }

        tv = (TextView)findViewById(R.id.keyup_tv1);
        if (tv != null)
            tv.setVisibility(visible);

        tv = (TextView)findViewById(R.id.keyup_tv2);
        if (tv != null)
            tv.setVisibility(visible);

        et = (EditText)findViewById(R.id.KeyUpInput);
        if (et != null) {
            et.setVisibility(visible);
            if (show) {
                et.setText(Integer.toString(KeyUp_VibrationTime));
                et.setSelection((Integer.toString(KeyUp_VibrationTime)).length());
                if (cb.isChecked())
                    et.setEnabled(TRUE);
                else
                    et.setEnabled(FALSE);
            }
        }
    }

    private void SetupComponentListener()
    {
        ButtonListener b = new ButtonListener();

        /* we have 4 buttons in this Activity.*/
        Button[] btn = new Button[4];
        for (int i = 0; i < btn.length; i++) {
            btn[i] = (Button) findViewById(R.id.btn0 + i);
            if (btn[i] == null) {
                Log.d(TAG, "We didn't find Button ID(" + (R.id.btn0 + i) + ")");
            } else {
                //btn[i].setOnClickListener(b);
                btn[i].setOnTouchListener(b);
            }
        }

        /* Listen image click for advance mode. */
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        imgView.setOnClickListener(b);
        //imgView.setOnTouchListener(b);

        CheckBox checkbox = (CheckBox)findViewById(R.id.checkBoxKeyupVibrating);
        checkbox.setOnCheckedChangeListener(myCheckChangelistener);

        mHomeListen = new HomeListener(this);
        mHomeListen.setInterface(new HomeListener.KeyFun() {
            @Override
            public void recent() {
                Log.d(TAG, "menu");
                ReadKeyDownVibratingTime();
                PlayPattern(1, KeyDown_VibrationTime);
            }
            @Override
            public void home() {
                Log.d(TAG, "home");
                ReadKeyDownVibratingTime();
                PlayPattern(2, KeyDown_VibrationTime);
            }
            @Override
            public void longHome() {
                Log.d(TAG, "longHome");
                ReadKeyDownVibratingTime();
                PlayPattern(2, KeyDown_VibrationTime);
            }
        });
    }

    /* update key down vibrating duration time. */
    private void ReadKeyDownVibratingTime(){
        EditText et = (EditText)findViewById(R.id.KeyDownInput);
        if (et != null && (et.getVisibility() == View.VISIBLE)) {
            String StrValue = et.getText().toString();
            Log.d(TAG, "ReadKeyDownVibratingTime()  StrValue=" + StrValue);
            if (StrValue.length() > 0) {
                KeyDown_VibrationTime = Integer.parseInt(StrValue);
                /* if(KeyDown_VibrationTime < 10) {
                    Log.d(TAG, "The input vibrating time is too short, so we will be using default value 50ms.");
                    KeyDown_VibrationTime = DEFAULT_VIBRATING_TIME;
                    et.setText(Integer.toString(KeyDown_VibrationTime));
                    et.setSelection((Integer.toString(KeyDown_VibrationTime)).length());
                } */
            } else {
                Log.d(TAG, "we will be using default value 50ms once the input box is empty.");
                KeyDown_VibrationTime = DEFAULT_VIBRATING_TIME;
                et.setText(Integer.toString(KeyDown_VibrationTime));
                et.setSelection((Integer.toString(KeyDown_VibrationTime)).length());
            }
        }
    }

    /* update key up vibrating duration time. */
    private void ReadKeyUpVibratingTime(){
        EditText et = (EditText)findViewById(R.id.KeyUpInput);
        if (et != null && (et.getVisibility() == View.VISIBLE)) {
            String StrValue = et.getText().toString();
            Log.d(TAG, "ReadKeyUpVibratingTime()  StrValue=" + StrValue);
            if (StrValue.length() > 0) {
                KeyUp_VibrationTime = Integer.parseInt(StrValue);
                /* if(KeyUp_VibrationTime < 10) {
                    Log.d(TAG, "The input vibrating time is too short, so we will be using default value 50ms.");
                    KeyUp_VibrationTime = DEFAULT_VIBRATING_TIME;
                    et.setText(Integer.toString(KeyUp_VibrationTime));
                    et.setSelection((Integer.toString(KeyUp_VibrationTime)).length());
                } */
            } else {
                Log.d(TAG, "we will be using default value 50ms once the input box is empty.");
                KeyUp_VibrationTime = DEFAULT_VIBRATING_TIME;
                et.setText(Integer.toString(KeyUp_VibrationTime));
                et.setSelection((Integer.toString(KeyUp_VibrationTime)).length());
            }
        }
    }
    @Override
    protected void onResume( ) {
        super.onResume();
        if (mHomeListen != null)
            mHomeListen.startListen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHomeListen != null)
            mHomeListen.stopListen();
    }
    /* playing the specified pattern. */
    private void PlayPattern(int index, int duration) {
        /* if the duration is less than 5, I will be using it as pattern index in driver code, 
           otherwise the value is vibrating duration. */
        Log.d(TAG, "PlayPattern()  index=" + index + "  VibrationTime=" + duration);
        /* if(Build.VERSION.SDK_INT >=26 ) {
            if (index <= 10)
                mVibrator.vibrate(VibrationEffect.createOneShot(index, VibrationEffect.DEFAULT_AMPLITUDE));;    //set pattern index.

            mVibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));

        } else */
        {
            if (index <= 10)
                mVibrator.vibrate(index+1);    //set pattern index.

            mVibrator.vibrate(duration);
        }
    }

    class ButtonListener implements View.OnClickListener, View.OnTouchListener {

        public void onClick(View v) {
            /* Log.d(TAG, "onClick    ID=" + v.getId());*/
        }

        public boolean onTouch(View v, MotionEvent event) {
            //Log.d(TAG, "onTouch    ID:" + v.getId() + "   Event:" + event.getAction());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ReadKeyDownVibratingTime();

                switch (v.getId()) {
                    case R.id.btn0:
                        PlayPattern(0, KeyDown_VibrationTime);
                        break;
                    case R.id.btn1:
                        PlayPattern(1, KeyDown_VibrationTime);
                        break;
                    case R.id.btn2:
                        PlayPattern(2, KeyDown_VibrationTime);
                        break;
                    case R.id.btn3:
                        PlayPattern(3, KeyDown_VibrationTime);
                        break;
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                if (IsEnableKeyUpVibrating) {
                    ReadKeyUpVibratingTime();
                    switch (v.getId()) {
                        case R.id.btn0:
                            PlayPattern(0, KeyUp_VibrationTime);
                            break;
                        case R.id.btn1:
                            PlayPattern(1, KeyUp_VibrationTime);
                            break;
                        case R.id.btn2:
                            PlayPattern(2, KeyUp_VibrationTime);
                            break;
                        case R.id.btn3:
                            PlayPattern(3, KeyUp_VibrationTime);
                            break;
                    }
                }
            }

            return false;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onKeyDown: keycode=" + keyCode);
        //if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        //    return true;
        //}

        if (event.getRepeatCount() == 0) {
            if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_BACK))
                ReadKeyDownVibratingTime();

            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    //Log.d(TAG, "onKeyDown: KEYCODE_VOLUME_UP");
                    PlayPattern(1, KeyDown_VibrationTime);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    //Log.d(TAG, "onKeyDown: KEYCODE_VOLUME_DOWN");
                    PlayPattern(2, KeyDown_VibrationTime);
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    PlayPattern(3, KeyDown_VibrationTime);
                    break;
                default:
                    PlayPattern(0, KeyDown_VibrationTime);
                    break;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onKeyUp: keycode=" + keyCode);

        //if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        //    return true;
        //}

        if (IsEnableKeyUpVibrating) {
            if (event.getRepeatCount() == 0) {
                if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_BACK))
                     ReadKeyUpVibratingTime();

                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_UP:
                        //Log.d(TAG, "onKeyDown: KEYCODE_VOLUME_UP");
                        PlayPattern(1, KeyUp_VibrationTime);
                        return true;
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        //Log.d(TAG, "onKeyDown: KEYCODE_VOLUME_DOWN");
                        PlayPattern(2, KeyUp_VibrationTime);
                        return true;
                    case KeyEvent.KEYCODE_BACK:
                        PlayPattern(3, KeyUp_VibrationTime);
                        break;
                    default:
                        PlayPattern(0, KeyUp_VibrationTime);
                        break;
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.question)
                .setTitle(R.string.strTitle)
                .setMessage(R.string.strPromptExit)

                .setNegativeButton(R.string.strNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.strYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SaveApplicationParameters();
                        ExitApplication.getInstance().exit();
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private void UpdateInputComponentFontColor(boolean status)
    {
        TextView tv = (TextView)findViewById(R.id.keyup_tv1);
        if (tv != null)
            if (status)
                tv.setTextColor(getColor(R.color.black));
            else
                tv.setTextColor(getColor(R.color.gray));

        tv = (TextView)findViewById(R.id.keyup_tv2);
        if (tv != null)
            if (status)
                tv.setTextColor(getColor(R.color.black));
            else
                tv.setTextColor(getColor(R.color.gray));

        EditText et = (EditText)findViewById(R.id.KeyUpInput);
        if (et != null) {
            if (status)
                et.setTextColor(getColor(R.color.black));
            else
                et.setTextColor(getColor(R.color.gray));
        }
    }

    CompoundButton.OnCheckedChangeListener myCheckChangelistener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onCheckedChanged   isChecked=" + isChecked);
            if (buttonView.getId() == R.id.checkBoxKeyupVibrating) {
                IsEnableKeyUpVibrating = buttonView.isChecked();
                EditText et = (EditText) findViewById(R.id.KeyUpInput);
                if ((et != null) && (et.getVisibility() == View.VISIBLE)) {
                    et.setEnabled(isChecked);
                }
                UpdateInputComponentFontColor(IsEnableKeyUpVibrating);
            }
        }
    };
}
