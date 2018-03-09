package com.nxp.fusion;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by nxa24750 on 13/04/2016.
 * TODO: best is to create TapPtrn which is extended by accTapPtrn
 */
public class TapPtrn {
    public int timeStamp; // sample number at which tapPtrn was reported, acc Clock value
    public int[] gapLengths; // in units of acc clock samples.
    public float[] tapAmps;
    public int nGlitches;
    public int numTaps;
    public boolean[] positiveness;
    public float rms; // rms, e.g. 250-50ms before start of TapPtrn.

    public TapPtrn(int timeStamp,int[] gapLengths, float[] tapAmps,
                   int nGlitches,boolean[] positiveness, float rms){
        this.timeStamp = timeStamp;
        this.tapAmps = tapAmps;
        this.nGlitches = nGlitches;
        this.numTaps = tapAmps.length;
        this.gapLengths = gapLengths;
        if ((gapLengths.length + 1) != tapAmps.length)
            Log.d("TapPtrn","(gapLengths.length + 1) != tapAmps.length");
        this.positiveness = positiveness; // only relevant for acc Tap.
        this.rms = rms;
    }

    public String toString(){
        DecimalFormat oneTwo = new DecimalFormat("#.##");
        DecimalFormat oneThree = new DecimalFormat("#.###");
        String string =  "T: " + timeStamp +
                " gap: " + Arrays.toString(gapLengths) +
                " amp: ";
        for(int idx = 0; idx<this.numTaps;idx++){
            string +=  oneThree.format(tapAmps[idx]) + " ";
        }
        for(int idx = 0; idx<this.numTaps;idx++) {
            string += (positiveness[idx] ? "+" : "-");
        }
            string +=" "  + nGlitches + "^ ";

        return string;
    }
}
