/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.ui;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.camera.debug.Log;
import com.android.camera2.R;

/**
 * This class manages the looks of the countdown.
 */
public class TextTipsView extends FrameLayout {

    private static final Log.Tag TAG = new Log.Tag("TextTipsView");
    private TextView mTextTips;
    private final RectF mPreviewArea = new RectF();

    public TextTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Responds to preview area change by centering th countdown UI in the new
     * preview area.
     */
    public void onPreviewAreaChanged(RectF previewArea) {
        mPreviewArea.set(previewArea);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTextTips = (TextView) findViewById(R.id.text_tips);
    }

    /**
     * Starts showing countdown in the UI.
     *
     * @param sec duration of the countdown, in seconds
     */
	public void ShowTips(String text) {
		setVisibility(View.VISIBLE);
		if (mTextTips != null){
            mTextTips.setText(text);
		}		
	}
	
	public void HideTips(){
	    setVisibility(View.INVISIBLE);
	}
}