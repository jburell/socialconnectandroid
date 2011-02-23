package org.burre.SocialConnect;

import java.util.List;
import java.util.Vector;

import org.burre.SocialConnect.TwitterConnection.TwitterStatusListener;

import twitter4j.Status;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class SocialConnect extends Activity implements TwitterStatusListener {
	public static final String APP_PREFERENCES = "NFITwitterPref";
	PINActivity m_pinActivity;
	ProgressDialog m_progress;
	
	class TweetStatusData{
		List<Status> m_listData = new Vector<Status>();
		public void setData(List<Status> listData){
			synchronized (m_listData) {
				m_listData = listData;
			}
		}
		
		public List<Status> getData(){
			return m_listData;
		}
		
		public int size(){
			return m_listData.size();
		}
	}
	
	TweetStatusData m_tweetData = new TweetStatusData();
	TweetAdapter m_tweetDataAdapter;
	ListView m_tweetListView;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		m_progress = new ProgressDialog(getApplicationContext());
		m_progress.hide();
		
		m_tweetListView = (ListView)findViewById(R.id.tweetListView);
		m_tweetDataAdapter = new TweetAdapter(this, null, m_tweetData);
		m_tweetListView.setAdapter(m_tweetDataAdapter);
		
		setupUI();
	}

	private void setupUI() {
		Button refreshBtn = (Button) findViewById(R.id.RefreshBtn);
		final Activity myActivity = this;
		
		refreshBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Activity myNewActivity = myActivity;
				new AsyncTask<Void, Void, List<twitter4j.Status>>(){
					@Override
					protected void onProgressUpdate(Void... values) {
						m_progress = ProgressDialog.show(myNewActivity, "", 
		            "Loading. Please wait...", false);
					};

					@Override
					protected List<twitter4j.Status> doInBackground(Void... params) {
						publishProgress();
						return TwitterConnection.getInstance().refreshTimeLine(getApplicationContext());
					}
					
					@Override
					protected void onPostExecute(List<twitter4j.Status> statuses) {
						onStatusUpdate(statuses);
					};
					
				}.execute();
			}
		});
	}

	@Override
	public void onStatusUpdate(List<Status> statuses) {
		if(statuses != null){			
			m_tweetData.setData(statuses);
			m_tweetDataAdapter.notifyDataSetChanged();
			m_tweetListView.invalidate();
		}
		m_progress.hide();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.loginMenuItem: {
			Intent intent = new Intent(getApplicationContext(), PINActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(intent);

			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}	
}