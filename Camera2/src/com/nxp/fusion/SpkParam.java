package com.nxp.fusion;

/**
 * Created by nxa24750 on 12/04/2016.
 */
public class SpkParam {
    public static float minWaitTime = 0.4f;
    public static int sampleRate = 1200;
    public static String slaveID = "0x34";
    public static String climaxDestination = "/dev/i2c-8";
    public static final int FIX_SCALE = (int) Math.pow(2,23);
    public static final int AMP_ADDRESS,GAPLENGTH_ADDRESS,GLITCH_ADDRESS,NOISETAIL_ADDRESS,
            NGAPS_ADDRESS,TAPMAX_ADDRESS,MINTIMETHRESHOLD_ADDRESS,FS_ADDRESS, NOISETHRESHOLD_ADDRESS; //statePR
    static { // initialization block: equivalent to constructor for non-static fields.
        AMP_ADDRESS = 280;
        GLITCH_ADDRESS = 288;
        GAPLENGTH_ADDRESS = 272;
        NOISETAIL_ADDRESS = 291;
        MINTIMETHRESHOLD_ADDRESS = 61;
        TAPMAX_ADDRESS = 65;
        NGAPS_ADDRESS = 289;
        FS_ADDRESS = 2304;
        NOISETHRESHOLD_ADDRESS = 62;
    }

    public static final int PATTERNVEC_ADDRESS;// unifParam

    static{
        PATTERNVEC_ADDRESS = 75;
    }

}

/*
unifParam->glitchControl: address 53 (0x0035), type __sint_XMEM, size 1
unifParam->fastAR: address 54 (0x0036), type fix_XMEM, size 1
unifParam->slowAR: address 55 (0x0037), type fix_XMEM, size 1
unifParam->fastMA: address 56 (0x0038), type __A2fix_XMEM, size 2
unifParam->slowMA: address 58 (0x003a), type __A2fix_XMEM, size 2
unifParam->minTimeThreshold: address 61 (0x003d), type __sint_XMEM, size 1
unifParam->noiseThreshold: address 62 (0x003e), type fix_XMEM, size 1
unifParam->numSig: address 63 (0x003f), type __A2fix_XMEM, size 2
unifParam->tapMax: address 65 (0x0041), type __sint_XMEM, size 1
unifParam->silenceThreshold: address 67 (0x0043), type __sint_XMEM, size 1
unifParam->maxTapsInPattern: address 68 (0x0044), type __sint_XMEM, size 1
unifParam->Dic->nPattern: address 69 (0x0045), type __sint_XMEM, size 1
unifParam->Dic->featureInterval: address 70 (0x0046), type __A4__sint_XMEM, size 4
unifParam->Dic->maxFeature: address 74 (0x004a), type __sint_XMEM, size 1
unifParam->Dic->patternVec: address 75 (0x004b), type __A14__sint_XMEM, size 14
unifParam->maxTimeThreshold: address 89 (0x0059), type __sint_XMEM, size 1
unifParam->firstInitVal: address 90 (0x005a), type fix_XMEM, size 1
unifParam->maxAmp: address 91 (0x005b), type fix_XMEM, size 1
unifParam->minAmp: address 92 (0x005c), type fix_XMEM, size 1
unifParam->recips: address 93 (0x005d), type __A3fix_XMEM, size 3
unifParam->relAmpLoLim: address 96 (0x0060), type fix_XMEM, size 1
unifParam->relAmpUpLim: address 97 (0x0061), type fix_XMEM, size 1
unifParam->relDelAmpLoLim: address 98 (0x0062), type fix_XMEM, size 1
unifParam->relDelAmpUpLim: address 99 (0x0063), type fix_XMEM, size 1
unifParam->noiseTailStart: address 100 (0x0064), type __sint_XMEM, size 1
unifParam->noiseTailStop: address 101 (0x0065), type __sint_XMEM, size 1
unifParam->noiseTailUpLim: address 102 (0x0066), type fix_XMEM, size 1
unifParam->deltaPeak: address 103 (0x0067), type __sint_XMEM, size 1
protoBlockMem: address 104 (0x0068), type __A2__A40fix_XMEM, size 80
statePR->prevIdTap: address 270 (0x010e), type __sint_XMEM, size 1
statePR->flagTapPattern: address 271 (0x010f), type __sint_XMEM, size 1
statePR->tapPatternTime: address 272 (0x0110), type __A8__sint_XMEM, size 8
statePR->tapPatternAmp: address 280 (0x0118), type __A8fix_XMEM, size 8
statePR->getGlitchs: address 288 (0x0120), type __sint_XMEM, size 1
statePR->countNumberGaps: address 289 (0x0121), type __sint_XMEM, size 1
statePR->newTapFlag: address 290 (0x0122), type __sint_XMEM, size 1
statePR->tapPatternNoiseTail: address 291 (0x0123), type __A8fix_XMEM, size 8
statePR->patternWord: address 299 (0x012b), type __sint_XMEM, size 1
statePR->flagRubustChk: address 300 (0x012c), type __sint_XMEM, size 1
statePR->currTapMax: address 301 (0x012d), type __sint_XMEM, size 1
stateFE->flagCounterSilence: address 2048 (0x0800), type __sint_XMEM, size 1
stateFE->maxPosition: address 2049 (0x0801), type __sint_XMEM, size 1
stateFE->counterGlitch: address 2050 (0x0802), type __sint_XMEM, size 1
stateFE->minValue: address 2051 (0x0803), type fix_XMEM, size 1
stateFE->idTapMax: address 2052 (0x0804), type __sint_XMEM, size 1
stateFE->counterSilence: address 2053 (0x0805), type __sint_XMEM, size 1
stateFE->counterTime: address 2054 (0x0806), type __sint_XMEM, size 1
stateFE->maxValue: address 2055 (0x0807), type fix_XMEM, size 1
stateFE->idTap: address 2056 (0x0808), type __sint_XMEM, size 1
stateFE->lookForMax: address 2057 (0x0809), type __sint_XMEM, size 1
stateFE->bufferPeaksTime: address 2058 (0x080a), type __A2__sint_XMEM, size 2
stateFE->bufferPeaksAmp: address 2060 (0x080c), type __A2fix_XMEM, size 2
stateFE->bufferTapsTime: address 2062 (0x080e), type __A8__sint_XMEM, size 8
stateFE->bufferTapsAmp: address 2070 (0x0816), type __A8fix_XMEM, size 8
stateFE->bufferTapsNoiseTail: address 2078 (0x081e), type __A8fix_XMEM, size 8
stateFE->bufferTapsDelAmp: address 2086 (0x0826), type __A8fix_XMEM, size 8
stateFE->numSig: address 2094 (0x082e), type __A2fix_XMEM, size 2
stateFE->denSigFast: address 2096 (0x0830), type lfix_XMEM, size 2
stateFE->denSigSlow: address 2098 (0x0832), type lfix_XMEM, size 2
stateFE->fastMem: address 2100 (0x0834), type fix_XMEM, size 1
stateFE->slowMem: address 2101 (0x0835), type fix_XMEM, size 1
stateFE->acc: address 2102 (0x0836), type acc_XMEM, size 3
proto->fs: address 2304 (0x0900), type __sint_XMEM, size 1
proto->downFactor: address 2305 (0x0901), type __sint_XMEM, size 1
proto->num: address 2306 (0x0902), type __A512fix_XMEM, size 512
proto->sigBuf: address 2818 (0x0b02), type __A512fix_XMEM, size 512
proto->xPtr: address 3330 (0x0d02), type __sint_XMEM, size 1
Stack_var: address 4320 (0x10e0), type __STACK__, size 800
 */

