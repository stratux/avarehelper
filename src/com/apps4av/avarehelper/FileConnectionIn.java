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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.apps4av.avarehelper.gdl90.Constants;
import com.apps4av.avarehelper.gdl90.Id413Product;
import com.apps4av.avarehelper.gdl90.Id6364Product;
import com.apps4av.avarehelper.gdl90.OwnshipMessage;
import com.apps4av.avarehelper.gdl90.Product;
import com.apps4av.avarehelper.gdl90.UplinkMessage;
import com.apps4av.avarehelper.nmea.Ownship;
import com.ds.avare.IHelper;

/**
 * 
 * @author zkhan
 *
 */
public class FileConnectionIn {

    private static InputStream mStream = null;
    private static boolean mRunning = false;
    
    private static FileConnectionIn mConnection;
    
    private static ConnectionStatus mConnectionStatus;
    private static IHelper mHelper;
    
    private Thread mThread;
    private String mFileName;

    /**
     * 
     */
    private FileConnectionIn() {
    }

    
    /**
     * 
     * @return
     */
    public static FileConnectionIn getInstance() {

        if(null == mConnection) {
            mConnection = new FileConnectionIn();
            mConnectionStatus = new ConnectionStatus();
            mConnectionStatus.setState(ConnectionStatus.DISCONNECTED);
        }
        return mConnection;
    }

    /**
     * 
     */
    public void stop() {
        Logger.Logit("Stopping File Reader");
        if(mConnectionStatus.getState() != ConnectionStatus.CONNECTED) {
            Logger.Logit("Stop failed because already stopped");
            return;
        }
        mRunning = false;
        if(null != mThread) {
            mThread.interrupt();
        }
    }

    /**
     * 
     */
    public void start() {
        
        Logger.Logit("Starting File Reader");
        if(mConnectionStatus.getState() != ConnectionStatus.CONNECTED) {
            Logger.Logit("Starting failed because already started");
            return;
        }
        
        mRunning = true;
        
        /*
         * Thread that reads BT
         */
        mThread = new Thread() {
            @Override
            public void run() {
                
                Logger.Logit("Reading file data");

                byte[] buffer = new byte[8192];
                com.apps4av.avarehelper.gdl90.DataBuffer dbuffer = 
                        new com.apps4av.avarehelper.gdl90.DataBuffer(16384);
                com.apps4av.avarehelper.nmea.DataBuffer nbuffer = 
                        new com.apps4av.avarehelper.nmea.DataBuffer(16384);
                com.apps4av.avarehelper.gdl90.Decode decode = 
                        new com.apps4av.avarehelper.gdl90.Decode();
                com.apps4av.avarehelper.nmea.Decode ndecode = 
                        new com.apps4av.avarehelper.nmea.Decode();
                Ownship nmeaOwnship = new Ownship();
                
                
                /*
                 * This state machine will keep trying to connect to 
                 * ADBS/GPS receiver
                 */
                while(mRunning) {
                    
                    int red = 0;
                    
                    /*
                     * Read.
                     */
                    red = read(buffer);
                    if(red <= 0) {
                        if(!mRunning) {
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
                    nbuffer.put(buffer, red);                     
                    dbuffer.put(buffer, red);
                 
                    byte[] buf;

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        
                    }

                    while(null != (buf = nbuffer.get())) {
                        com.apps4av.avarehelper.nmea.Message m = ndecode.decode(buf);
                        if(nmeaOwnship.addMessage(m)) {
                                
                            /*
                             * Make a GPS locaiton message from ADSB ownship message.
                             */
                            JSONObject object = new JSONObject();
                            Ownship om = nmeaOwnship;
                            try {
                                object.put("type", "ownship");
                                object.put("longitude", (double)om.mLon);
                                object.put("latitude", (double)om.mLat);
                                object.put("speed", (double)(om.mHorizontalVelocity));
                                object.put("bearing", (double)om.mDirection);
                                object.put("altitude", (double)((double)om.mAltitude));
                                object.put("time", (long)om.getTime());
                            } catch (JSONException e1) {
                                continue;
                            }
                            
                            if(mHelper != null) {
                                try {
                                    mHelper.sendDataText(object.toString());
                                } catch (Exception e) {
                                }
                            }

                        }
                        continue;
                    }

                    while(null != (buf = dbuffer.get())) {

                        /*
                         * Get packets, decode
                         */
                        com.apps4av.avarehelper.gdl90.Message m = decode.decode(buf);
                        /*
                         * Post on UI thread.
                         */
                        
                        if(m instanceof UplinkMessage) {
                            /*
                             * Send an uplink nexrad message
                             */
                            LinkedList<Product> pds = ((UplinkMessage) m).getFis().getProducts();
                            for(Product p : pds) {
                                if(p instanceof Id6364Product) {
                                    Id6364Product pn = (Id6364Product)p;
                                    JSONObject object = new JSONObject();
                                    
                                    JSONArray arrayEmpty = new JSONArray();
                                    JSONArray arrayData = new JSONArray();
                                    
                                    int[] data = pn.getData();
                                    if(null != data) {
                                        for(int i = 0; i < data.length; i++) {
                                            arrayData.put(data[i]);
                                        }
                                    }
                                    LinkedList<Integer> empty = pn.getEmpty();
                                    if(null != empty) {
                                        for(int e : empty) {
                                            arrayEmpty.put(e);
                                        }
                                    }
                                
                                    try {
                                        object.put("type", "nexrad");
                                        object.put("time", (long)pn.getTime().getTimeInMillis());
                                        object.put("conus", pn.isConus());
                                        object.put("blocknumber", (long)pn.getBlockNumber());
                                        object.put("x", Constants.COLS_PER_BIN);
                                        object.put("y", Constants.ROWS_PER_BIN);
                                        object.put("empty", arrayEmpty);
                                        object.put("data", arrayData);
                                    } catch (JSONException e1) {
                                        continue;
                                    }
                                    
                                    if(mHelper != null) {
                                        try {
                                            String tosend = object.toString();
                                            mHelper.sendDataText(tosend);
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                                /*
                                 * Text product
                                 */
                                else if(p instanceof Id413Product) {
                                    Id413Product pn = (Id413Product)p;
                                    JSONObject object = new JSONObject();
                                    
                                    String data = pn.getData();
                                    String type = pn.getHeader();
                                    long time = (long)pn.getTime().getTimeInMillis();
                                    
                                    /*
                                     * Clear garbage spaces etc. Convert to Avare format
                                     */
                                    try {
                                        if(type.equals("WINDS")) {
                                            
                                            String tokens[] = data.split("\n");
                                            if(tokens.length < 2) {
                                                /*
                                                 * Must have line like
                                                 * MSY 230000Z  FT 3000 6000    F9000   C12000  G18000  C24000  C30000  D34000  39000   Y 
                                                 * and second line like
                                                 * 1410 2508+10 2521+07 2620+01 3037-12 3041-26 304843 295251 29765
                                                 */
                                                continue;
                                            }
                                            
                                            tokens[0] = tokens[0].replaceAll("\\s+", " ");
                                            tokens[1] = tokens[1].replaceAll("\\s+", " ");
                                            String winds[] = tokens[1].split(" ");
                                            String alts[] = tokens[0].split(" ");
                                                                                    
                                            /*
                                             * Start from 3rd entry - alts
                                             */
                                            data = "";
                                            boolean found = false;
                                            for(int i = 2; i < alts.length; i++) {
                                                if(alts[i].contains("3000") && !alts[i].contains("30000")) {
                                                    data += winds[i - 2] + ",";
                                                    found = true;
                                                }
                                            }
                                            if(!found) {
                                                data += ",";
                                            }
                                            found = false;
                                            for(int i = 2; i < alts.length; i++) {
                                                if(alts[i].contains("6000")) {
                                                    data += winds[i - 2] + ",";
                                                    found = true;
                                                }
                                            }
                                            if(!found) {
                                                data += ",";
                                            }
                                            found = false;
                                            for(int i = 2; i < alts.length; i++) {
                                                if(alts[i].contains("9000") && !alts[i].contains("39000")) {
                                                    data += winds[i - 2] + ",";
                                                    found = true;
                                                }
                                            }
                                            if(!found) {
                                                data += ",";
                                            }
                                            found = false;
                                            for(int i = 2; i < alts.length; i++) {
                                                if(alts[i].contains("12000")) {
                                                    data += winds[i - 2] + ",";
                                                    found = true;
                                                }
                                            }
                                            if(!found) {
                                                data += ",";
                                            }
                                            found = false;
                                            for(int i = 2; i < alts.length; i++) {
                                                if(alts[i].contains("18000")) {
                                                    data += winds[i - 2] + ",";
                                                    found = true;
                                                }
                                            }
                                            if(!found) {
                                                data += ",";
                                            }
                                            found = false;
                                            for(int i = 2; i < alts.length; i++) {
                                                if(alts[i].contains("24000")) {
                                                    data += winds[i - 2] + ",";
                                                    found = true;
                                                }
                                            }
                                            if(!found) {
                                                data += ",";
                                            }
                                            found = false;
                                            for(int i = 2; i < alts.length; i++) {
                                                if(alts[i].contains("30000")) {
                                                    data += winds[i - 2] + ",";
                                                    found = true;
                                                }
                                            }
                                            if(!found) {
                                                data += ",";
                                            }
                                            found = false;
                                            for(int i = 2; i < alts.length; i++) {
                                                if(alts[i].contains("34000")) {
                                                    data += winds[i - 2] + ",";
                                                    found = true;
                                                }
                                            }
                                            if(!found) {
                                                data += ",";
                                            }
                                            found = false;
                                            for(int i = 2; i < alts.length; i++) {
                                                if(alts[i].contains("39000")) {
                                                    data += winds[i - 2] + ",";
                                                    found = true;
                                                }
                                            }
                                            if(!found) {
                                                data += ",";
                                            }
                                        }
                                    }
                                    catch (Exception e) {
                                        continue;
                                    }
                                    
                                    try {
                                        object.put("type", pn.getHeader());
                                        object.put("time", time);
                                        object.put("location", pn.getLocation());
                                        object.put("data", data);
                                    } catch (JSONException e1) {
                                        continue;
                                    }
                                    
                                    if(mHelper != null) {
                                        try {
                                            String tosend = object.toString();
                                            mHelper.sendDataText(tosend);
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                            }
                        }
                        else if(m instanceof OwnshipMessage) {
                            
                            /*
                             * Make a GPS locaiton message from ADSB ownship message.
                             */
                            JSONObject object = new JSONObject();
                            OwnshipMessage om = (OwnshipMessage)m;
                            try {
                                object.put("type", "ownship");
                                object.put("longitude", (double)om.mLon);
                                object.put("latitude", (double)om.mLat);
                                object.put("speed", (double)(om.mHorizontalVelocity));
                                object.put("bearing", (double)om.mDirection);
                                object.put("altitude", (double)((double)om.mAltitude));
                                object.put("time", (long)om.getTime());
                            } catch (JSONException e1) {
                                continue;
                            }
                            
                            if(mHelper != null) {
                                try {
                                    mHelper.sendDataText(object.toString());
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                
            }
        };
        mThread.start();
    }
    
    /**
     * 
     * @param state
     */
    private void setState(int state) {
        mConnectionStatus.setState(state);
    }
    
    
    /**
     * 
     * A device name devNameMatch, will connect to first device whose
     * name matched this string.
     * @return
     */
    public boolean connect(String fileName) {
        
        Logger.Logit("Opening file " + fileName);

        if(fileName == null) {
            return false;
        }
        
        mFileName = fileName;
        
        /*
         * Only when not connected, connect
         */
        if(mConnectionStatus.getState() != ConnectionStatus.DISCONNECTED) {
            Logger.Logit("Failed! Already reading?");

            return false;
        }
        setState(ConnectionStatus.CONNECTING);

        Logger.Logit("Getting input stream");

        try {
            mStream = new BufferedInputStream(new FileInputStream(mFileName));
        } 
        catch (Exception e) {
            Logger.Logit("Failed! Input stream error");

            setState(ConnectionStatus.DISCONNECTED);
        } 

        setState(ConnectionStatus.CONNECTED);

        Logger.Logit("Success!");

        return true;
    }
    
    /**
     * 
     */
    public void disconnect() {
        
        Logger.Logit("Closing file");

        /*
         * Exit
         */
        try {
            mStream.close();
        } 
        catch(Exception e2) {
            Logger.Logit("Error stream close");
        }
        
        setState(ConnectionStatus.DISCONNECTED);
        Logger.Logit("Disconnected");
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
    public boolean isConnected() {
        return mConnectionStatus.getState() == ConnectionStatus.CONNECTED;
    }

    /**
     * 
     * @return
     */
    public boolean isConnectedOrConnecting() {
        return mConnectionStatus.getState() == ConnectionStatus.CONNECTED ||
                mConnectionStatus.getState() == ConnectionStatus.CONNECTING;
    }

    /**
     * 
     * @param helper
     */
    public void setHelper(IHelper helper) {
        mHelper = helper;
    }

}