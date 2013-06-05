/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package org.selfkleptomaniac.mod.titwilio;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.proxy.IntentProxy;
import org.appcelerator.kroll.common.Log;

import ti.modules.titanium.android.PendingIntentProxy;

import com.twilio.client.Connection;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;

@Kroll.module(name="Twilioandroid", id="org.selfkleptomaniac.mod.titwilio")
public class TwilioandroidModule extends KrollModule implements DeviceListener
{

	// Standard Debugging variables
	private static final String TAG = "TwilioandroidModule";
	private static TwilioPhone phone = null;
	private String url = null;
	private KrollDict args;

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	public class TwilioPhoneLoader extends AsyncTaskLoader<TwilioPhone> {
		private String url;
		private PendingIntent pendingIntent;
		public TwilioPhoneLoader(Context context) {
			super(context);
		}

		public void setUrl(String url){
			this.url = url;
		}
		
		public void setPendingIntent(PendingIntent intent){
			this.pendingIntent = intent;
		}
		
		@Override
		public TwilioPhone loadInBackground() {
			TwilioPhone phone = new TwilioPhone(TiApplication.getInstance(), this.url, this.pendingIntent);
			return phone;
		}
	}
	
	public TwilioandroidModule()
	{
		super();
	}
	
	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}
	
	private void createPhone(KrollDict args){
		this.args = args;
		TwilioPhoneLoader loader = new TwilioPhoneLoader(TiApplication.getInstance());
		if(args.containsKey("url")){
			String url = args.get("url").toString();
			Log.d(TAG, url);
			loader.setUrl(url);
		}
		if(args.containsKey("pendingIntent")){
			PendingIntentProxy proxy = (PendingIntentProxy) args.get("pendingIntent");
			PendingIntent pendingIntent = proxy.getPendingIntent();
			loader.setPendingIntent(pendingIntent);
		}
		phone = loader.loadInBackground();
 	}
	
	// Methods
	@Kroll.method
	public String example()
	{
		return "hello world";
	}
	
	@Kroll.method
	public void connect(KrollDict args)
	{
		this.fireEvent("connecting", null);
		this.args = args;
		if(args.containsKey("url")){
			this.url = args.get("url").toString();
			//TODO
			phone = null;
			
			try{
				if(phone == null){
					createPhone(args);
				}
				phone.connect(args);
				this.fireEvent("connected", null);
			}catch(RuntimeException e){
				this.raiseError("connectionError", "Connection error");
			}
		}else{
			this.raiseError("connectionError", "url is null");
		}
	}
	
	@Kroll.method
	public void login(KrollDict args)
	{
		this.args = args;
		fireEvent("loginStart", null);
		if(args.containsKey("url")){
			this.url = args.get("url").toString();
			if(phone == null){
				createPhone(args);
				fireEvent("loggedIn", null);
			}else{
				try{
					phone.login(args);
				}catch(RuntimeException e){
					this.raiseError("loginError", "Connection error");
				}
			}
		}else{
			this.raiseError("loginError", "url is null");
		}
	}
	
	private void raiseError(String event, String message){
		HashMap<String, Object> error = new HashMap<String, Object>();
		error.put("message", message);
		fireEvent(event, error);
	}
	
	@Kroll.method
	public void disconnect()
	{
		if(phone != null){
			phone.disconnect();
		}
		fireEvent("disconnected", null);
	}
	
	@Kroll.method
	public void acceptIncomingCall(KrollDict args)
	{
		handleIncomingConnection((IntentProxy) args.get("intent"));
		phone.acceptIncomingCall();
	}
	
	@Kroll.method
	public void ignoreIncomingCall(KrollDict args)
	{
		handleIncomingConnection((IntentProxy) args.get("intent"));
		phone.ignoreIncomingCall();
	}
	
	@Kroll.method
	public void handleIncomingConnection(IntentProxy intentProxy){
		Intent intent = intentProxy.getIntent();
		Connection connection = intent.getParcelableExtra(Device.EXTRA_CONNECTION);
		Device device = intent.getParcelableExtra(Device.EXTRA_DEVICE);
		phone.handleIncomingConnection(device, connection);
	}
	
	@Override
	public void onResume(Activity activity){
		Log.d(TAG, "on Resume");
		if(this.args != null){
			createPhone(this.args);
		
			Intent intent = activity.getIntent();
			Device device = intent.getParcelableExtra(Device.EXTRA_DEVICE);
			Connection connection = intent.getParcelableExtra(Device.EXTRA_CONNECTION);
			if (connection != null) {
				Log.d(TAG, "resume & connection");
				intent.removeExtra(Device.EXTRA_DEVICE);
				intent.removeExtra(Device.EXTRA_CONNECTION);
				phone.handleIncomingConnection(device, connection);
			}else{
				Log.d(TAG, "we have no way to handle incoming connection");
			}
		}
		super.onResume(activity);
	}
	
//	public void handleIncomingConnection(Device inDevice, Connection inConnection) {
//		phone.setDevice(inDevice);
//		phone.setConnection(inConnection);
//		Log.d(TAG, "YES, we have handled in coming connection");
//	}
	
	@Override
	public void onPresenceChanged(Device inDevice, PresenceEvent inEvent) {
		// TODO Auto-generated method stub
		fireEvent("onPresenceChanged", null);
	}

	@Override
	public void onStartListening(Device inDevice) {
		// TODO Auto-generated method stub
		fireEvent("onStartListening", null);
		
	}

	@Override
	public void onStopListening(Device inDevice) {
		// TODO Auto-generated method stub
		fireEvent("onStopListening", null);
		
	}

	@Override
	public void onStopListening(Device inDevice, int inErrorCode, String inErrorMessage) {
		// TODO Auto-generated method stub
		fireEvent("onStopListening", inErrorMessage);
	}

	@Override
	public boolean receivePresenceEvents(Device inDevice) {
		// TODO Auto-generated method stub
		return false;
	}

}