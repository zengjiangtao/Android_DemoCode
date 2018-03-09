package com.nxp.fusion;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nxp.taptrigger.*;
import org.jtransforms.fft.FloatFFT_1D;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by nxa24750 on 6/04/2016.
 */
public class SensorFuser implements SensorEventListener, View.OnTouchListener {
    // CONSTANTS
    private static final String TAG = "FUSE";

    // Android context
    Context context;
    //Activity activity;

    //Phone Context
    public boolean flat = false;
    public float angle = 0; // angle of the phone
    public float maxStillStd = 0.15f; //Max value for std

    public boolean still = false;
    public float std = 0; // stddev of acc
    public float maxFlatAngle = 4; // in degrees;
//    public float meanX = 0.0f;
//    public float meanY = -0.04f;
//    public float meanZ = 10.09f;
//    public float flatMargin = 0.1f; // Margin on the mean
    public int screenTouchOffset;

    // fusion related
    public int maxPtrnDelay; // max timestampdiff
    public int maxGapLengthDiff;
    public int minTimeBetweenPtrns;
    public int lastPositiveFusionResult = Integer.MIN_VALUE/2;
    public FusionResult fusionResult;
    public int maxNGlitches;

    // acc related
    public int accSampleRate;
    public TTParam param;
    public State stateZ,stateY,stateX;
    public TapPtrn accTapX,accTapY,accTapZ;
    public int accTapZCount = 0;


    // spk related
    public int spkTapOffset;
    public TapPtrn spkTap;
    public int spkTapCount = 0;


    // other sensors:
    public float distance = 0;
    public int lastTouchEvent = 0;

    // gyro processing: Experimental work in progress.
    public float[] gyroscope = new float[3];
    private int fftlen = 32;
    private float[] gyroBuffer = new float[fftlen];
    private int gyroPointer = 0;
    private FloatFFT_1D fft = new FloatFFT_1D(fftlen);
    float pitchFreqEntropy = 0;

    /**
     * @param acc_sampling_period in milliseconds
     * @param context the activity that is constructing this sensorfuser (needed to access sensors and possible callbacks)
     */



    public SensorFuser(int acc_sampling_period, Context context, int maxNumTaps){
        // advised sampling period = 5 ms -- > 200 Hz
        // TT Params
        Log.i(TAG, "SensorFuser()  acc_sampling_period="  + acc_sampling_period + "  maxNumTaps=" + maxNumTaps);
        accSampleRate = Math.round(1000.0f/acc_sampling_period);
        param = new TTParam(accSampleRate);
        param.minAmp = 0.6f; // tune sensitivity of accelerometer
        param.setHpFilterSelect(TTParam.Filters.HP25PCT); //25 very sensitive but handling false alarms. 40 few false alarms but very soft taps not in.
        param.maxNumTaps = maxNumTaps;
        minTimeBetweenPtrns = Math.round(0.3f*accSampleRate);
        param.longCircBufferLen = Math.round(1.0f*accSampleRate); // 1 second buffer.
        param.stabilityOffset = Math.round(0.8f*accSampleRate);


        screenTouchOffset = Math.round(0.2f*accSampleRate);

        // Fusion Params
        spkTapOffset = Math.round(accSampleRate *(SpkParam.minWaitTime-param.minInterval)) + 8; //tuned ;)
        maxPtrnDelay = Math.round(accSampleRate *0.1f); // 100 ms 'jitter' allowed between acc and spk tap
        maxGapLengthDiff = 4; // 4 samples gap length difference allowed.
        maxNGlitches = 5; // for Z accelerometer.

        // Initializations
        stateX = new State(param);
        stateY = new State(param);
        stateZ = new State(param);

        accTapX = new TapPtrn(0,new int[]{0}, new float[]{0,0},0,new boolean[]{true,true},0);
        accTapY = new TapPtrn(0,new int[]{0}, new float[]{0,0},0,new boolean[]{true,true},0);
        accTapZ = new TapPtrn(0,new int[]{0}, new float[]{0,0},0,new boolean[]{true,true},0);
        spkTap = new TapPtrn(0,new int[]{0}, new float[]{0,0},0,new boolean[]{true,true},0);

        this.context = context;
        //this.activity = activity;
        // sensor manager
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //View mainView = activity.findViewById(R.layout.activity_main);
        //mainView.setOnTouchListener(this);

        // TODO: uncomment the next line to make SensorFuser more standalone.
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 1000* acc_sampling_period);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_UI);


        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 1000* acc_sampling_period);
        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_UI);
        fusionResult = new FusionResult();
    }

    public boolean onTouch(View view, MotionEvent event){
		Log.i(TAG,"[NXP] onTouch()");
        lastTouchEvent = stateZ.sampleCounter;
//        Log.d(TAG,"onTouch Event received: (" + lastTouchEvent + "): " +  MotionEvent.actionToString(event.getAction()));
        return true;
    }

    public boolean fuseSensors(){

        List<FusionResult.PhoneContext> contextInfo = new ArrayList<>();
        if (onTable())
            contextInfo.add(FusionResult.PhoneContext.TABLE);
        //if(screenTouched())
        //    contextInfo.add(FusionResult.PhoneContext.SCREEN_TAP);

        Log.d(TAG,"" + contextInfo.size());


        fusionResult  = new FusionResult(
                isCloseTo(spkTap, accTapZ), // remove acoustical false positives
                sameNumTaps(spkTap, accTapZ),
                sameGapLengthAs(spkTap, accTapZ),// remove drops on table
                isHarderThan(accTapZ, accTapX) , //remove cable plugins.
                isHarderThan(accTapZ, accTapY),
                nothingInProximity(),  // remove triggers if phone in pocket or face down.
                fewGlitches(accTapZ), // remove drops on table
                waitPeriodFinished(), // useful when two TFA's active.
                contextInfo);

        if(fusionResult.result){
            lastPositiveFusionResult = stateZ.sampleCounter;
        }
        Log.i(TAG, "fuseSensors()  result="  + fusionResult.result);

        return fusionResult.result; //|| (still && flat)
    }

    public boolean screenTouched(){
        return (stateZ.sampleCounter - spkTapOffset - lastTouchEvent < screenTouchOffset ); //TODO: minTimeBetweenPtrns is not the right parameter.
    }

    public boolean onTable(){
        stateX = TapTrigger.updateStability(stateX, param);
        stateY = TapTrigger.updateStability(stateY, param);
        stateZ = TapTrigger.updateStability(stateZ, param);

        angle = (float) (Math.toDegrees(Math.atan2(Math.hypot(stateX.mean,stateY.mean),stateZ.mean)));
        flat = angle < maxFlatAngle;
//        flat = Math.abs(stateX.mean - meanX) < 2*flatMargin && Math.abs(stateY.mean - meanY ) < flatMargin && Math.abs(stateZ.mean - meanZ) < flatMargin;
//        still = stateX.std < maxStillStd &&  stateY.std < maxStillStd &&  stateZ.std < maxStillStd;
        std = (float) Math.hypot(Math.hypot(stateX.std,stateY.std),stateZ.std);
        still =  std < maxStillStd;
        return (flat && still);
    }

    // returns true if Z has less or equal number of glitches as allowed
    public boolean fewGlitches(TapPtrn tapPtrn){
        if (tapPtrn.numTaps == 2)
            return tapPtrn.nGlitches <= maxNGlitches;
        else
            return tapPtrn.nGlitches <= Math.round(maxNGlitches * 3/2);

    }

    // returns true if previous positive fusion result was less than 0.5s ago
    public boolean waitPeriodFinished(){
        return stateZ.sampleCounter - lastPositiveFusionResult > minTimeBetweenPtrns;
    }

    // returns true if Z found a tap, false otherwise.
    public boolean processAcc(SensorEvent event){
        boolean result = false;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];// - SensorManager.STANDARD_GRAVITY;

        stateZ = TapTrigger.processSample(z, stateZ, param);
        stateY = TapTrigger.processSample( y, stateY, param);
        stateX = TapTrigger.processSample( x, stateX, param);
        if (stateZ.tapDetectFlag){
            addAccTap(stateZ,SensorFuser.SensorType.AccZ);
            Log.d("SENSORFUSER",accTapZ.toString());
            result = true;

        }
        if (stateY.tapDetectFlag){
            addAccTap(stateY, SensorFuser.SensorType.AccY);
        }
        if (stateX.tapDetectFlag){
            addAccTap(stateX, SensorFuser.SensorType.AccX);
        }
        return result;
    }

    public boolean sameNumTaps(TapPtrn a, TapPtrn b){
        return a.numTaps == b.numTaps;
    }

    private boolean nothingInProximity() {
        return distance > 0;
    }

    private boolean sameGapLengthAs(TapPtrn a, TapPtrn b) {
        boolean condition = true;
        int nGaps = Math.min(b.numTaps -1,a.numTaps -1);
        for(int idx = 0; idx< nGaps; idx ++){
            condition = condition && (a.gapLengths[idx]  - b.gapLengths[idx]) <= maxGapLengthDiff;
        }
        return condition;
    }

    public boolean isCloseTo(TapPtrn a, TapPtrn b){
        return Math.abs(a.timeStamp - b.timeStamp) < maxPtrnDelay;
    }

    public boolean isHarderThan(TapPtrn a, TapPtrn b){
        float meanAmpA = meanAmp(a);
        float meanAmpB = meanAmp(b);
        return !isCloseTo(a,b) || (meanAmpA > meanAmpB);
    }

    public float meanAmp(TapPtrn a){
        float acc = 0;
        for(int ii=0;ii<a.numTaps;ii++)
            acc += a.tapAmps[ii];
        acc = acc/a.numTaps;
        return acc;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                processAcc(event);
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscope = event.values;
                gyroBuffer[gyroPointer] = event.values[1];
                gyroPointer = (gyroPointer + 1)%fftlen;
                if (gyroPointer == 0||gyroPointer == fftlen/2){
                    float[] fftOut = gyroBuffer.clone();
                    fft.realForward(fftOut);
                    float[] absfftout = computeAbs(fftOut);
                    pitchFreqEntropy = computeEntropy(absfftout);
                }

                break;
            case Sensor.TYPE_PROXIMITY:
                distance = event.values[0];
                break;
        }
    }

    // Returns entropy in bits of the input sequence p.
    private float computeEntropy(float[] in){
        float entropy = 0;
        float[] p = normalizeSum(in);
        for(int ii = 1; ii<p.length; ii++){
            if(p[ii]>0) {
                entropy -= p[ii] * Math.log(p[ii]) / Math.log(2);
            }else{
                Log.d("entropy","" + p[ii]);
            }
        }
        return entropy;
    }

    // Normalizes such that the sum will be one
    private float[] normalizeSum(float[] in){
        float[] out = new float[in.length];
        float sum = 0;
        for(int ii = 0; ii<in.length; ii++){
            sum += in[ii];
        }
        for(int ii = 0; ii<in.length; ii++){
            out[ii] = (in[ii]/sum);
        }
        return out;
    }

    // Compute a
    private float[] computeAbs(float[] in){
        float[] out = new float[in.length/2 + 1];
        out[0] = Math.abs(in[0]); // DC
        for(int ii = 1; ii< out.length - 1; ii++){
            out[ii] = (float) Math.sqrt(Math.pow(in[2*ii],2)+ Math.pow(in[2*ii+1],2));
        }
        out[out.length - 1] = Math.abs(in[1]); // fs / 2
        return out;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void release(){
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    public enum SensorType {
        AccX,
        AccY,
        AccZ,
        Spk,
    }

    public void addAccTap(State state,SensorType sensor){
        int numTaps = state.numTap;
        int[] gapLengths = Arrays.copyOfRange(state.gapLengths,0,numTaps-1); // halfopen interval specification
        float[] tapAmps = Arrays.copyOfRange(state.tapAmps,0,numTaps);
        boolean[] tapPositiveness = Arrays.copyOfRange(state.tapPositiveness,0,numTaps);
        TapPtrn accTap = new TapPtrn(state.sampleCounter,
                gapLengths,
                tapAmps,
                state.nGlitches,
                tapPositiveness,
                state.rms);

        switch (sensor) {
            case AccX:
                accTapX = accTap;
                break;
            case AccY:
                accTapY = accTap;
                break;
            case AccZ:
                accTapZ = accTap;
                accTapZCount++;
                break;
        }
    }

    private int[] concatenate(int[]a, int[] b){
        int aLen = a.length;
        int bLen = b.length;
        int[] c = new int[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private int[] getAddresses(int startAddress, int len){
        int[] addresses = new int[len];
        for(int idx = 0; idx < len; idx++){
            addresses[idx] = startAddress + idx;
        }
        return addresses;
    }

    /*
     * Wrapper that gathers the needed information for constructing a spk TapPtrn and constructs it.
     */
    public boolean addSpkTap(int numTaps){
        spkTapCount++;
        Log.i(TAG, "addSpkTap()  numTaps="  + numTaps);

        // construct address array
        int[] ampAddresses = getAddresses(SpkParam.AMP_ADDRESS,numTaps);
        int[] glitchAddress = new int[]{SpkParam.GLITCH_ADDRESS};
        int[] gapLengthAddresses = getAddresses(SpkParam.GAPLENGTH_ADDRESS,numTaps-1);
        int[] addresses = concatenate(ampAddresses,glitchAddress);
        addresses = concatenate(addresses,gapLengthAddresses);

        // read addresses and get values
        int[] values = ShellExecuter.readDSPValues(addresses);

        // use returned values to construct a tap.
        int spkTapTime = stateZ.sampleCounter - spkTapOffset;
        float[] spkAmps = new float[numTaps];
        for(int idx = 0; idx< numTaps; idx++){
            spkAmps[idx] = (float)values[idx]/SpkParam.FIX_SCALE;
			Log.i(TAG, "addSpkTap()>>>>>  spkAmps="	+ values[idx] + "    " + spkAmps[idx]);
        }
        int spkNGlitches = values[numTaps];
        int[] spkGapLengths = new int[numTaps - 1];
        for(int idx = 0; idx < numTaps - 1; idx++){
            spkGapLengths[idx] = Math.round((float)values[numTaps + 1 + idx]/SpkParam.sampleRate* accSampleRate);
        }
        boolean[] tapPositiveness = new boolean[numTaps];
        for(int idx = 0; idx< numTaps; idx++){
            tapPositiveness[idx] = true;
        }
        float rms = 0;
        spkTap = new TapPtrn(spkTapTime,spkGapLengths,spkAmps,spkNGlitches,tapPositiveness,rms);

        return fuseSensors();
    }

    public String toString(){
        return  "TapPtrn States: \n" +
                "spk: " + spkTap.toString()  + "\n" +
                "Z  : " + accTapZ.toString() + "\n" +
                "Y  : " + accTapY.toString() + "\n" +
                "X  : " + accTapX.toString() + "\n" +
                "spk-Z: " + (spkTap.timeStamp - accTapZ.timeStamp) + " <> " + maxPtrnDelay + "\n" +
                "Fusion subresults: \n" +
                fusionResult.toString() + " \n" +
                tmpToString();

                //"rmsZ: " + accTapZ.rms + " rmsY: " + accTapY.rms + " rmsX: " + accTapX.rms + "\n" +
                //"Proximity: " + (int) distance +  "\n" +
                //"Gyroscope XYZ: " + Arrays.toString(gyroscope) + "\n" +
                //"pitchFreqEntropy: " + pitchFreqEntropy;
    }

    public String tmpToString(){
        DecimalFormat oneTwo = new DecimalFormat("#.##");
        DecimalFormat threeOne = new DecimalFormat("###.#");
        return
                "Std : " +  oneTwo.format(std) + " <> " + maxStillStd + " --> still = " + still + "\n" +
                "Angle: " + threeOne.format(angle) + "\u00b0 <> " + maxFlatAngle + " , --> flat = " + flat;
    }

}
