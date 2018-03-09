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
 *   Aurthor: Jiangtao.zeng
 *   Date:    01/20/2018
 */

package com.nxp.nxp34663.hapticdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
//import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
//import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.io.File;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class AdvanceActivity extends AppCompatActivity {
    private static final String TAG = "[NXP][haptic Activity]";
    private Vibrator mVibrator = null;
    private HomeListener mHomeListen = null;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int DEFAULT_PATTERN_ID = 0;
    private static final int DEFAULT_VIBRATING_TIME = 50;

    private static final int NUMBER_OF_APP_BUTTONS = 15;    /* the number is for basic app buttons.*/
    private static final int NUMBER_OF_VIRTUAL_BUTTONS = 3; /* the number is for menu/home/back keys */
    private static final int NUMBER_OF_HARDKEY = 1;         /* the number is for 1 group hardkey (volume up/down) */
    /* total buttons is app buttons + virtual button + hardkey. */
    private static final int NUMBER_OF_ALL_BUTTONS = NUMBER_OF_APP_BUTTONS + NUMBER_OF_VIRTUAL_BUTTONS + NUMBER_OF_HARDKEY;
    
    private static final String INI_FILE_IN_SDCARD = "/sdcard/hapticdemo.ini";
    private static final String INI_FILE_IN_INTERNAL = "/data/hapticdemo.ini";

    private KeyConfiguration[] mKeyConfigurations;

    private void PlayPattern(int ObjID, int time, String msg) {
        if (msg == null)
            Log.d(TAG, "PlayPattern()    ObjID="+ ObjID + "  time=" + time);
        else
            Log.d(TAG, msg + "\t\tPlayPattern() ObjID="+ ObjID + "  time=" + time);
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            mVibrator.vibrate(VibrationEffect.createOneShot(ObjID, VibrationEffect.DEFAULT_AMPLITUDE));
            mVibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mVibrator.vibrate(ObjID, new AudioAttributes.Builder().build());
                mVibrator.vibrate(time, new AudioAttributes.Builder().build());
            }
            else
            {
                mVibrator.vibrate(ObjID);
                mVibrator.vibrate(time);
            }
        }
*/
        mVibrator.vibrate(ObjID);
        mVibrator.vibrate(time);
    }


    class ButtonListener implements View.OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            //Log.d(TAG, "onTouch    ID:" + v.getId() + "   Event:" + event.getAction());
            int ButtonIndex = v.getId()-R.id.btn10;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mKeyConfigurations[ButtonIndex].mKeyDonwObjectID >= 0)
                    PlayPattern(mKeyConfigurations[ButtonIndex].mKeyDonwObjectID, mKeyConfigurations[ButtonIndex].mKeyDownTime, "Button" + ButtonIndex + "  Down");
            } else if ((event.getAction() == MotionEvent.ACTION_UP) && (mKeyConfigurations[v.getId()-R.id.btn10].mIsEnableKeyUpVibrating)) {
                if (mKeyConfigurations[ButtonIndex].mKeyUpObjectID >= 0)
                    PlayPattern(mKeyConfigurations[ButtonIndex].mKeyUpObjectID, mKeyConfigurations[ButtonIndex].mKeyUpTime, "Button" + ButtonIndex + "  Up");
            }
            return false;
        }
    }

    private void SetupButtonListener(){
        /* create button listener object. */
        ButtonListener b = new ButtonListener();

        /* we have 15 app buttons in this Activity, the listener object is connected to each button. */
        Button[] btn = new Button[NUMBER_OF_APP_BUTTONS];
        for (int i = 0; i < NUMBER_OF_APP_BUTTONS; i++) {
            btn[i] = (Button) findViewById(R.id.btn10 + i);
            if (btn[i] == null) {
                Log.d(TAG, "We didn't find Button ID(" + (R.id.btn10 + i) + ") from resource list.");
            } else {
                btn[i].setOnTouchListener(b);
            }
        }
    }

    private void SetupVirtualKeyListener() {
        mHomeListen = new HomeListener(this);
        mHomeListen.setInterface(new HomeListener.KeyFun() {
            @Override
            public void recent() {
                int ButtonID = NUMBER_OF_APP_BUTTONS;
                PlayPattern(mKeyConfigurations[ButtonID].mKeyDonwObjectID, mKeyConfigurations[ButtonID].mKeyDownTime, "Press menu key");
            }
            @Override
            public void home() {
                int ButtonID = NUMBER_OF_APP_BUTTONS + 1;
                PlayPattern(mKeyConfigurations[ButtonID].mKeyDonwObjectID, mKeyConfigurations[ButtonID].mKeyDownTime, "Press home key");
            }
            @Override
            public void longHome() {
                int ButtonID = NUMBER_OF_APP_BUTTONS + 1;
                PlayPattern(mKeyConfigurations[ButtonID].mKeyDonwObjectID, mKeyConfigurations[ButtonID].mKeyDownTime, "Press longHome key");
            }
        });        
    }

    public void onBackPressed() {
        /* menu key offset is 0, home key offset is 1, back key offset is 2*/
        int ButtonID = NUMBER_OF_APP_BUTTONS + 2;
        PlayPattern(mKeyConfigurations[ButtonID].mKeyDonwObjectID, mKeyConfigurations[ButtonID].mKeyDownTime, "Press back key");
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
                        dialog.dismiss();
                        finish();
                    }
                }).create();
        dialog.show();
    }

    private boolean FileIsExist(String name)
    {
        if (name.isEmpty())
            return FALSE;

        Log.d(TAG, "FileIsExist() name=" + name);
        try {
            File f = new File(name);
            if (!f.exists()) {
                Log.d(TAG, "FileIsExist() return FALSE");
                return FALSE;
            }
        } catch (Exception e) {
            Log.d(TAG, "FileIsExist() " + e.toString());
            return FALSE;
        }
        Log.d(TAG, "FileIsExist() return TRUE");
        return  TRUE;
    }

    private void LoadConfigurationFileToMemory(int mode){
        /* check configuration file. */
        if ((mode != 0) && (FileIsExist(INI_FILE_IN_SDCARD) || FileIsExist(INI_FILE_IN_INTERNAL))) {
            IniReader iniReader;
            Log.d(TAG, "LoadConfigurationFileToMemory() parsing ini file");
            try {
                int ObjID1, Time1, ObjID2, Time2;
                if (FileIsExist(INI_FILE_IN_SDCARD))
                    iniReader = new IniReader(INI_FILE_IN_SDCARD);
                else
                    iniReader = new IniReader(INI_FILE_IN_INTERNAL);

                /* load button's configuration parameters. */
                for (int i = 0; i < NUMBER_OF_ALL_BUTTONS; i++) {
                    /* read parameter from memory. */
                    String StrKeyDownPattern = iniReader.getValue("button"+ i, "KeyDownPattern");
                    String StrKeyDownTime = iniReader.getValue("button"+ i, "KeyDownTime");
                    String StrKeyUpPattern = iniReader.getValue("button"+ i, "KeyUpPattern");
                    String StrKeyUpTime = iniReader.getValue("button"+ i, "KeyUpTime");

                    ObjID1 = -1; Time1  = 0;
                    ObjID2 = -1; Time2  = 0;

                    /* Parsing KeyDown params. */
                    if (StrKeyDownPattern != null) {
                        ObjID1 = Integer.parseInt(StrKeyDownPattern);
                        if (StrKeyDownTime == null) {
                            Time1 = DEFAULT_VIBRATING_TIME;
                        } else {
                            Time1 = Integer.parseInt(StrKeyDownTime);
                        }
                    }

                    /* Parsing KeyUp params. */
                    if (StrKeyUpPattern != null) {
                        ObjID2 = Integer.parseInt(StrKeyUpPattern);
                        if (StrKeyUpTime == null) {
                            Time2 = DEFAULT_VIBRATING_TIME;
                        } else {
                            Time2 = Integer.parseInt(StrKeyUpTime);
                        }
                    }

                    /* update button configuration table. */
                    mKeyConfigurations[i].UpdateKeyParameters(ObjID1, Time1, ObjID2, Time2);
                }
            }
            catch (Exception e){
                Log.e(TAG, e.toString());
            }
        } else {
            Log.d(TAG, "LoadConfigurationFileToMemory() we will be using default parameter for each button.");
            for (int i = 0; i < NUMBER_OF_ALL_BUTTONS; i++) {
                mKeyConfigurations[i].UpdateKeyParameters(DEFAULT_PATTERN_ID, DEFAULT_VIBRATING_TIME, -1, 0);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() SDK_INT=" + Build.VERSION.SDK_INT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance);
        this.setTitle(R.string.strTitle);

        /* create key configuration table. */
        mKeyConfigurations = new KeyConfiguration[NUMBER_OF_ALL_BUTTONS];
        for (int i = 0; i < NUMBER_OF_ALL_BUTTONS; i++) {
            mKeyConfigurations[i] = new KeyConfiguration();
        }

        if (checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE))
            LoadConfigurationFileToMemory(1);
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showRequestPermissionDialog(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);

            }else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
        /* get vibrator service. */
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        assert mVibrator != null;

        /* handle application key. */
        SetupButtonListener();

        /* handle home key and menu key. */
        SetupVirtualKeyListener();

        Log.d(TAG, "The advance UI creating is done.");
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    int ButtonID = NUMBER_OF_APP_BUTTONS + NUMBER_OF_VIRTUAL_BUTTONS;
                    PlayPattern(mKeyConfigurations[ButtonID].mKeyDonwObjectID, mKeyConfigurations[ButtonID].mKeyDownTime, "Volume KeyDown");
                    return true;
                default:
                    return super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    int ButtonID = NUMBER_OF_APP_BUTTONS + NUMBER_OF_VIRTUAL_BUTTONS;
                    PlayPattern(mKeyConfigurations[ButtonID].mKeyUpObjectID, mKeyConfigurations[ButtonID].mKeyUpTime, "Volume KeyDown");
                    return true;
                default:
                    return super.onKeyUp(keyCode, event);
            }
        }
        return true;
    }

    private void showRequestPermissionDialog(final String[] permissions, final int requestCode) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.question)
                //.setTitle(R.string.strTitle)
                .setMessage(R.string.strReadPermission)

                .setNegativeButton(R.string.strNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.strYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Log.d(TAG, "showRequestPermissionDialog() permissions=" + permissions[0]);
                        Log.d(TAG, "showRequestPermissionDialog() requestCode=" + requestCode);
                        ActivityCompat.requestPermissions(AdvanceActivity.this,permissions,requestCode);
                    }
                }).create();
        dialog.show();
    }

    private boolean checkPermission(Context context, String permission) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context,permission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult() requestCode=" + requestCode);
        Log.d(TAG, "onRequestPermissionsResult() permissions=" + permissions[0]);
        Log.d(TAG, "onRequestPermissionsResult() grantResults=" + grantResults[0]);
        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    /* we will be using customer configuraion when we got SDCARD access permission. */
                    LoadConfigurationFileToMemory(1);
                }else {
                    /* we will be using default configuration once we don't have permission. */
                    LoadConfigurationFileToMemory(0);
                }
                break;
            default:
                break;
        }
    }

    private static final class KeyConfiguration {
        private int mKeyDonwObjectID;
        private int mKeyDownTime;
        private int mKeyUpTime;
        private int mKeyUpObjectID;
        private boolean mIsEnableKeyUpVibrating;
        KeyConfiguration() {
            mKeyDonwObjectID = -1;
            mKeyDownTime = 0;
            mKeyUpObjectID = -1;
            mKeyUpTime = 0;
            mIsEnableKeyUpVibrating = FALSE;
        }

        void UpdateKeyParameters(int objID1, int keydown, int objID2, int keyup) {
            Log.d(TAG, "UpdateKeyParameters()\tKeyDonwObjectID=" + objID1 + "\tkeydown=" + keydown + "\t\tKeyUpObjectID=" + objID2 +  "\tkeyup=" + keyup);
            mKeyDonwObjectID = objID1;
            mKeyDownTime = keydown;

            mKeyUpObjectID = objID2;
            mKeyUpTime = keyup;
            if((mKeyUpObjectID >=0) && (mKeyUpTime != 0))
                mIsEnableKeyUpVibrating = TRUE;
        }
    }
}
