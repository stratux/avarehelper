/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.apps4av.avarehelper.connections;

import android.content.Context;

import com.apps4av.avarehelper.R;

/**
 * Created by zkhan on 9/22/16.
 */
public class ConnectionFactory {

    public static Connection getConnection(String type, Context ctx) {
        if(type.equals("BlueToothConnectionIn")) {
            return BlueToothConnectionIn.getInstance(ctx);
        }
        if(type.equals("BlueToothConnectionOut")) {
            return BlueToothConnectionOut.getInstance(ctx);
        }
        if(type.equals("FileConnectionIn")) {
            return FileConnectionIn.getInstance(ctx);
        }
        if(type.equals("GPSSimulatorConnection")) {
            return GPSSimulatorConnection.getInstance(ctx);
        }
        if(type.equals("MsfsConnection")) {
            return MsfsConnection.getInstance(ctx);
        }
        if(type.equals("USBConnectionIn")) {
            return USBConnectionIn.getInstance(ctx);
        }
        if(type.equals("WifiConnection")) {
            return WifiConnection.getInstance(ctx);
        }
        if(type.equals("XplaneConnection")) {
            return XplaneConnection.getInstance(ctx);
        }
        return null;
    }


    /*
 * Find names of all running connections.
 */
    public static String getActiveConnections(Context ctx) {
        String s = "";
        s += getConnection("BlueToothConnectionIn", ctx).isConnected() ? "," + ctx.getString(R.string.Bluetooth) : "";
        s += getConnection("WifiConnection", ctx).isConnected() ?  "," + ctx.getString(R.string.WIFI) : "";
        s += getConnection("XplaneConnection", ctx).isConnected() ? "," + ctx.getString(R.string.XPlane) : "";
        s += getConnection("MsfsConnection", ctx).isConnected() ? "," + ctx.getString(R.string.MSFS) : "";
        s += getConnection("BlueToothConnectionOut", ctx).isConnected() ? "," + ctx.getString(R.string.AP) : "";
        s += getConnection("FileConnectionIn", ctx).isConnected() ? "," + ctx.getString(R.string.Play) : "";
        s += getConnection("GPSSimulatorConnection", ctx).isConnected() ? "," + ctx.getString(R.string.GPSSIM) : "";
        s += getConnection("USBConnectionIn", ctx).isConnected() ? "," + ctx.getString(R.string.USBIN) : "";
        if(s.startsWith(",")) {
            s = s.substring(1);
        }
        return "(" + s + ")";
    }

}
