/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.yepstudio.android.doodle;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

import com.yepstudio.android.library.doodle.Doodle;

/**
 * 涂鸦
 * Created by J.O.B on 2015-05-27.
 */
public class DoodleActivity extends AppCompatActivity {

    private Doodle doodle;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_doodle);

        handler = new Handler(Looper.getMainLooper());
        doodle = (Doodle) findViewById(R.id.doodle);

        doodle.setOnDoodleListener(new Doodle.OnDoodleListener() {
            @Override
            public void onTogglePaintAndColor(boolean hasAnimation, boolean isShow) {

            }

            @Override
            public void onDrawPaint(boolean isClear) {

            }

            @Override
            public void onSaveCanvas(Bitmap mBitmap) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 600);
            }
        });
    }
}
