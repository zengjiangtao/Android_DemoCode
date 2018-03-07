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

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class ExitApplication extends Application {
    private static final String TAG = "[NXP] haptic ExitApplication";
    private List<Activity> mList = new LinkedList<Activity>();
    private static ExitApplication instance;

    private ExitApplication() {
        Log.d(TAG, "ExitApplication constructor()");
    }

    public synchronized static ExitApplication getInstance() {
        if (null == instance) {
            Log.d(TAG, "getInstance() create new instance........");
            instance = new ExitApplication();
        }
        return instance;
    }

    // add Activity into list
    public void addActivity(Activity activity) {
        Log.d(TAG, "addActivity() " + activity);
        mList.add(activity);
    }

    // close all Activity.
    public void exit() {
        Log.d(TAG, "exit() entry");
        try {
            /* close all activity. */
            for (Activity activity : mList) {
                if (activity != null) {
                    //Log.d(TAG, "exit() >>>>>>>");
                    activity.finish();
                }
            }
            /* remove all item from list. */
            mList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }
}
