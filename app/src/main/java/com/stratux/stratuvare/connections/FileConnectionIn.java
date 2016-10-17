/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.stratux.stratuvare.connections;

import android.content.Context;

import com.stratux.stratuvare.storage.Preferences;
import com.stratux.stratuvare.utils.GenericCallback;
import com.stratux.stratuvare.utils.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author zkhan
 *
 */
public class FileConnectionIn extends Connection {
    private static FileConnectionIn mConnection;

    private InputStream mStream = null;
    private String mFileName = null;

    /**
     * 
     */
    private FileConnectionIn() {
        super("File Input");
                /*
         * Thread that reads File
         */
        setCallback(new GenericCallback() {
            @Override
            public Object callback(Object o, Object o1) {
                BufferProcessor bp = new BufferProcessor();


                byte[] buffer = new byte[8192];

                /*
                 * This state machine will keep trying to connect to
                 * ADBS/GPS receiver
                 */
                while(isRunning()) {

                    int red = 0;

                    /*
                     * Read.
                     */
                    red = read(buffer);
                    if(red <= 0) {
                        if(isStopped()) {
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {

                        }

                        continue;
                    }

                    /*
                     * Put both in Decode and ADBS buffers
                     */
                    bp.put(buffer, red);
                    LinkedList<String> objs = bp.decode((Preferences)o);
                    for(String s : objs) {
                        sendDataToHelper(s);
                    }
                }
                return null;
            }
        });
    }

    
    /**
     * 
     * @return
     * @param ctx
     */
    public static FileConnectionIn getInstance(Context ctx) {

        if(null == mConnection) {
            mConnection = new FileConnectionIn();
        }
        return mConnection;
    }

    /**
     * 
     * A device name devNameMatch, will connect to first device whose
     * name matched this string.
     * @return
     */
    @Override
    public boolean connect(String to, boolean securely) {
        
        if(to == null) {
            return false;
        }
        
        mFileName = to;
        
        /*
         * Only when not connected, connect
         */
        if(getState() != Connection.DISCONNECTED) {
            Logger.Logit("Failed! Already reading?");

            return false;
        }
        setState(Connection.CONNECTING);

        Logger.Logit("Getting input stream");

        try {
            mStream = new BufferedInputStream(new FileInputStream(mFileName));
        } 
        catch (Exception e) {
            Logger.Logit("Failed! Input stream error");

            setState(Connection.DISCONNECTED);
        } 

        return connectConnection();
    }

    @Override
    public String getParam() {
        return mFileName;
    }

    /**
     * 
     */
    @Override
    public void disconnect() {

        /*
         * Exit
         */
        try {
            mStream.close();
        } 
        catch(Exception e2) {
            Logger.Logit("Error stream close");
        }

        disconnectConnection();
    }

    @Override
    public List<String> getDevices() {
        return new ArrayList<String>();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getConnDevice() {
        return "";
    }

    /**
     * 
     * @return
     */
    private int read(byte[] buffer) {
        int red = -1;
        try {
            red = mStream.read(buffer, 0, buffer.length);
        } 
        catch(Exception e) {
            red = -1;
        }
        return red;
    }

    /**
     * 
     * @return
     */
    public String getFileName() {
        return mFileName;
    }

}
