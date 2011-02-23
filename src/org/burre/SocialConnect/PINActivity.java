package org.burre.SocialConnect;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PINActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pin);
		setupUIPIN();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	private void setupUIPIN(){
		Button getPIN = (Button) findViewById(R.id.getPINBtn);
		getPIN.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterConnection.getInstance().getPIN(getApplicationContext());
			}
		});

		Button sendPIN = (Button) findViewById(R.id.sendPIN);
		sendPIN.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView pinTxt = (TextView) findViewById(R.id.PINTxt);
				if(TwitterConnection.getInstance().login(getApplicationContext(), pinTxt.getText().toString())){
					finish();
				}
			}
		});
	}
}
