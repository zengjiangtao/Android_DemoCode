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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class HomeListener {

    public KeyFun mKeyFun;
    public Context mContext;
    public IntentFilter mHomeBtnIntentFilter = null;
    public HomeBtnReceiver mHomeBtnReceiver = null;
    public static final String TAG = "[NXP] HomeListener";

    public HomeListener(Context context) {
        mContext = context;
        mHomeBtnIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mHomeBtnReceiver = new HomeBtnReceiver();
    }

    public void startListen() {
        if (mContext != null )
            mContext.registerReceiver(mHomeBtnReceiver, mHomeBtnIntentFilter);
        else
            Log.e(TAG, "mContext is null and startListen fail");

    }
    public void stopListen() {
        if (mContext != null )
            mContext.unregisterReceiver(mHomeBtnReceiver);
        else
            Log.e(TAG, "mContext is null and stopListen fail");
    }

    public void setInterface(KeyFun keyFun){
        mKeyFun = keyFun;

    }
    class HomeBtnReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                if (reason != null) {
                    if(null != mKeyFun ){
                        Log.d(TAG, "Reason =======" + reason);
                        if (reason.equals("homekey")) {
                            //press home key with short time.
                            mKeyFun.home();
                        } else if (reason.equals("recentapps")) {
                            //press menu key
                            mKeyFun.recent();
                        } else if (reason.equals("assist")) {
                            //press home key with long time.
                            mKeyFun.longHome();
                        }
                    }
                }
            }
        }
    }

    public interface KeyFun {
        public void home();
        public void recent();
        public void longHome();
    }
}
