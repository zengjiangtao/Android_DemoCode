package com.nxp.fusion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nxa24750 on 26/04/2016.
 * Helper class to store subresults in the sensor fusing.
 * Its function can be extended.
 */
public class FusionResult {
    // Aggregated result:
    public boolean result;
    // subresults
    public boolean isCloseTo;
    public boolean sameNumTaps;
    public boolean sameGapLength;
    public boolean isHarderThanY;
    public boolean isHarderThanX;
    public boolean nothingInProximity;
    public boolean ZGlitches;
    public boolean waitPeriodFinished;
    public List<PhoneContext> contextInfo;
    public enum PhoneContext{
        TABLE,
        SCREEN_TAP,
    }

    public FusionResult(){ // initializing constructor.
        this(true, true, true, true, true, true, true, true, new ArrayList<PhoneContext>());
    }

    public FusionResult(boolean isCloseTo,
                        boolean sameNumTaps,
                        boolean sameGapLength,
                        boolean isHarderThanX,
                        boolean isHarderThanY,
                        boolean nothingInProximity,
                        boolean ZGlitches,
                        boolean waitPeriodFinished,
                        List<PhoneContext> contextInfo){
        this.isCloseTo = isCloseTo;
        this.sameNumTaps = sameNumTaps;
        this.sameGapLength = sameGapLength;
        this.isHarderThanX = isHarderThanX;
        this.isHarderThanY = isHarderThanY;
        this.nothingInProximity = nothingInProximity;
        this.ZGlitches = ZGlitches;
        this.waitPeriodFinished = waitPeriodFinished;
        this.contextInfo = contextInfo;
        fuseToResult();

    }

    private void fuseToResult(){
        this.result = isCloseTo
         && sameNumTaps
         && sameGapLength
         && isHarderThanX
         && isHarderThanY
         && nothingInProximity
         && ZGlitches
         && waitPeriodFinished;
    }

    public String toString(){
        return "  |spk-Z|: " + (isCloseTo?"1":"0") + "\t" +
                "  =#Taps: " + (sameNumTaps?"1":"0") + "\t" +
                "  =GapLen: " + (sameGapLength?"1":"0") + "\n" +

                "  Z>X: " + (isHarderThanX?"1":"0") + "\t" +
                "  Z>Y: " + (isHarderThanY?"1":"0") + "\t" +
                "  prox: " + (nothingInProximity?"1":"0") + "\t" +
                "  ^: " + (ZGlitches?"1":"0") + "\n" +

                "FuseResult: " + result  + "\n" +
                "ContextInfo: " + contextInfoToString();

    }

    public String contextInfoToString(){
        String str = "";
        for (PhoneContext s : contextInfo){
            str+= s.toString() + ", ";
        }
        if (contextInfo.isEmpty())
            str+= "NONE";

        return str;
    }


}
