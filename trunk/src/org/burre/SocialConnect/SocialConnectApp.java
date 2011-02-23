package org.burre.SocialConnect;

import android.app.Application;
import android.content.Context;

public class SocialConnectApp extends Application {
	private static SocialConnectApp m_instance;
	
	public static Application getApplication(){
		return m_instance;
	}
	
	public static Context getContext(){
		return m_instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		m_instance = this;
	}
}
