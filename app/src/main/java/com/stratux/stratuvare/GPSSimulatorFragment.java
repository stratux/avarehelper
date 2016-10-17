/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.apps4av.avarehelper;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.apps4av.avarehelper.connections.Connection;
import com.apps4av.avarehelper.connections.GPSSimulatorConnection;
import com.apps4av.avarehelper.storage.Preferences;
import com.apps4av.avarehelper.storage.SavedEditText;

/**
 *
 * @author rasii, zkhan
 *
 */
public class GPSSimulatorFragment extends Fragment {

    private Connection mGPSSim;
    private CheckBox mLandAtCb;
    private CheckBox mFlyToCb;
    private SavedEditText mTextLat;
    private SavedEditText mTextLon;
    private SavedEditText mTextHeading;
    private SavedEditText mTextSpeed;
    private SavedEditText mTextAltitude;
    private Button mButtonStart;

    private Context mContext;

    private double getValidValue(String val) {
        double ret = 0;
        if(val.length() > 0) {
            try {
                ret = Double.parseDouble(val);
            }
            catch (Exception e) {
                ret = 0;
            }
        }

        return ret;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = container.getContext();

        View view = inflater.inflate(R.layout.layout_gpssim, container, false);

        mLandAtCb = (CheckBox)view.findViewById(R.id.main_button_gpssim_land_at);
        mFlyToCb = (CheckBox)view.findViewById(R.id.main_button_gpssim_fly_to);
        mTextLon = (SavedEditText)view.findViewById(R.id.main_gpssim_lon);
        mTextLat = (SavedEditText)view.findViewById(R.id.main_gpssim_lat);
        mTextAltitude = (SavedEditText)view.findViewById(R.id.main_gpssim_altitude);
        mTextSpeed = (SavedEditText)view.findViewById(R.id.main_gpssim_speed);
        mTextHeading = (SavedEditText)view.findViewById(R.id.main_gpssim_heading);
        mButtonStart = (Button)view.findViewById(R.id.main_button_gpssim_start);

        mButtonStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGPSSim.isConnected()) {
                    mGPSSim.stop();
                    mGPSSim.disconnect();
                }
                else {
                    mGPSSim.connect(
                            getValidValue(mTextLat.getText().toString()) + "," +
                                    getValidValue(mTextLon.getText().toString()) + "," +
                                    getValidValue(mTextHeading.getText().toString()) + "," +
                                    getValidValue(mTextSpeed.getText().toString()) + "," +
                                    getValidValue(mTextAltitude.getText().toString()) + "," +
                                    mFlyToCb.isChecked() + "," +
                                    mLandAtCb.isChecked(),
                            false);
                    mGPSSim.start(new Preferences(mContext));
                }

                setStates();
            }
        });

        /*
         * Get Connection
         */
        mGPSSim = GPSSimulatorConnection.getInstance(mContext);

        setStates();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setStates() {
        if(mGPSSim.isConnected()) {
            mButtonStart.setText(mContext.getString(R.string.Stop));
        }
        else {
            mButtonStart.setText(mContext.getString(R.string.Start));
        }

        // lat, lon, alt, bearing, speed, landatdest, flytodest
        String params[] = mGPSSim.getParam().split(",");


        mTextLat.setText(String.format("%.4f", getValidValue(params[0])));
        mTextLon.setText(String.format("%.4f", getValidValue(params[1])));
        mTextHeading.setText(String.format("%.0f", getValidValue(params[2])));
        mTextSpeed.setText(String.format("%.0f", getValidValue(params[3])));
        mTextAltitude.setText(String.format("%.0f", getValidValue(params[4])));
        mFlyToCb.setChecked(Boolean.parseBoolean(params[5]));
        mLandAtCb.setChecked(Boolean.parseBoolean(params[6]));
    }

}
