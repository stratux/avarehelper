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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.webkit.WebView;
import android.support.v4.app.*;

/**
 * 
 * @author zkhan
 * 
 */
public class HelpFragment extends Fragment {

    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 

        View view = inflater.inflate(R.layout.layout_help, container, false);

        mWebView = (WebView) view.findViewById(R.id.help_mainpage);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.loadUrl("file:///android_asset/help.html");
        
        // This is need on some old phones to get focus back to webview.
        mWebView.setOnTouchListener(new View.OnTouchListener() {  
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				arg0.performClick();
				arg0.requestFocus();
				return false;
			}
        });

        mWebView.setOnLongClickListener(new OnLongClickListener() {
        	@Override
        	public boolean onLongClick(View v) {
        	    return true;
        	}
        });
        mWebView.setLongClickable(false);

        return view;
    }
    

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}