package com.nxp.fusion;

/**
 * Created by nxa24750 on 8/04/2016.
 * From https://www.learn2crack.com/2014/03/android-executing-shell-commands.html
 * Currently contains some climax specific commands at the bottom (could be moved in future)
 */
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ShellExecuter {
    private static final String TAG = "shell";

    public ShellExecuter() {

    }

    public static String execute(String command) {
        StringBuffer output = new StringBuffer();
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String response = output.toString();
        return response;
    }

    public static String[] executeSu(String[] command, int nOutputLines) {
        String[] output = new String[nOutputLines];
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            for(int ii = 0; ii<nOutputLines;ii++){
                output[ii] = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    // climax specific
    public static int[] readDSPValues(int[] addresses){
        int nAddresses = addresses.length;
        // construct commands
        String[] cmds = { "su", "-c",
                "climax -d" + SpkParam.climaxDestination + " --slave=" + SpkParam.slaveID + " --xmem=" + addresses[0] };
        for(int ii=1;ii<nAddresses;ii++){
            cmds[2] += ";" + "climax -d" + SpkParam.climaxDestination + " --slave=" + SpkParam.slaveID + " --xmem=" + addresses[ii];
        }

        // execute commands
        long startTime = System.currentTimeMillis();
        String[] lines = ShellExecuter.executeSu(cmds, nAddresses);
        Log.d(TAG, "Took " + (System.currentTimeMillis() - startTime) + "ms to read " + addresses.length + " addresses.");

        int[] values = ShellExecuter.parseClimaxOutput(lines);

        return values;
    }

    public static void setDSPValues(int[] addresses, int[] values){

        int nAddresses = addresses.length;
        Log.d(TAG,"addresses:" + Arrays.toString(addresses) + ", values: " + Arrays.toString(values));
        // construct commands
        String[] cmds = { "su", "-c",
                "climax -d" + SpkParam.climaxDestination + " --slave=" + SpkParam.slaveID + " --xmem=" + addresses[0] + " -w" + values[0]};
        for(int ii=1;ii<nAddresses;ii++){
            cmds[2] += ";" + "climax -d" + SpkParam.climaxDestination + " --slave=" + SpkParam.slaveID + " --xmem=" + addresses[ii] + " -w" + values[0];
        }

        // execute commands
        long startTime = System.currentTimeMillis();
        int nAttempts = 0;
        boolean written = false;
        while (!written){
            nAttempts++;
            executeSu(cmds, nAddresses);
            written = true;
            int[] actualValues = readDSPValues(addresses);
            Log.d(TAG,Arrays.toString(actualValues));
            if(actualValues == null) {
                Log.d(TAG,"failed at reading dsp values");
                break;
            }
            for(int ii =0; ii < nAddresses; ii++){
                if(actualValues[ii]!=values[ii])
                    written = false;
            }


        }
        Log.d(TAG,"Took " + (System.currentTimeMillis() - startTime) + "ms, and " + nAttempts + " attempts to write " + addresses.length + " addresses.");

        //climax -d/dev/i2c-8 --slave=0x34 --xmem=77 -w2097169
    }

    // parse climax output for values
    public static int[] parseClimaxOutput(String[] outputLines) {
        if (outputLines == null)
            return null;
        int nOutputLines = outputLines.length;
        int[] values = new int[nOutputLines];
        for (int ii = 0; ii < nOutputLines; ii++) {
            if (outputLines[ii] != null) {
                String[] parts = outputLines[ii].split("x");
                String value = parts[parts.length - 1].substring(0, 6);
                values[ii] = Integer.parseInt(value, 16);
            } else {
                Log.d(TAG,"'null' line encountered while parsing climax output.");
                values[ii] = 0;
            }
        }
        return values;
    }


    /* OLD VERSION of the function (slow):
    public String executeSu(String[] command) {
        StringBuffer output = new StringBuffer();
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String response = output.toString();
        return response;
    }
     */
}