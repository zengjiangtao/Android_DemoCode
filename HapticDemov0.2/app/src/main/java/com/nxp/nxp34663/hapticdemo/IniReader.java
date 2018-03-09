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
 *   Date:    02/08/2018
 */

package com.nxp.nxp34663.hapticdemo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class IniReader {
    private static final String TAG = "[NXP][IniReader]";
    protected HashMap<String, Properties> sections = new HashMap<String, Properties>();
    private transient String currentSecion;
    private transient Properties current;

    public IniReader(String filename) throws IOException {
        Log.d(TAG, "Loading " + filename);
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        read(reader);
        reader.close();
    }

    protected void read(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            parseLine(line);
        }
    }

    protected void parseLine(String line) {
        line = line.trim();
        if (line.matches("\\[.*\\]")) {
            currentSecion = line.replaceFirst("\\[(.*)\\]", "$1");
            current = new Properties();
            sections.put(currentSecion, current);
        } else if (line.matches(".*=.*")) {
            if (current != null) {
                int i = line.indexOf('=');
                String name = line.substring(0, i);
                String value = line.substring(i + 1);
                current.setProperty(name, value);
                Log.d(TAG, "parseLine() section" +  currentSecion + " name=" + name + "  value=" + value);
            }
        }
    }

    public String getValue(String section, String name) {
        Properties p = (Properties) sections.get(section);
        if (p == null) {
            Log.e(TAG, "             getValue() not found session (" + section + ")");
            return null;
        }

        //Log.d(TAG, "getValue() section=" + section + "  name=" + name + "  value=" + p.getProperty(name));
        return p.getProperty(name);
    }

}