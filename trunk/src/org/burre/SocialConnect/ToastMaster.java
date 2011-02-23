package org.burre.SocialConnect;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

public class ToastMaster {
	public static void showToast(String message){
		final String myMessage = message;
		new AsyncTask<Void, Void, Void>(){
			
			@Override
			protected void onProgressUpdate(Void... values) {
				
			}

			@Override
			protected Void doInBackground(Void... params) {
				return null;
			}
			
			@Override
			protected void onPostExecute(Void voids) {
				Toast toast = Toast.makeText(SocialConnectApp.getContext(),
						myMessage,
						Toast.LENGTH_LONG);
				toast.show();
			}
			
		}.execute();
	}
}
