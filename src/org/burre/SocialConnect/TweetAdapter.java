package org.burre.SocialConnect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.burre.SocialConnect.SocialConnect.TweetStatusData;

import twitter4j.Status;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TweetAdapter extends BaseAdapter {
	private LayoutInflater m_inflater;

	TweetStatusData m_statuses;
	HashMap<URL, Bitmap> m_imageCache = new HashMap<URL, Bitmap>();

	public TweetAdapter(Context context, ListView viewGroup,
			TweetStatusData statuses) {
		m_inflater = LayoutInflater.from(context);

		m_statuses = statuses;
		m_inflater.inflate(R.layout.tweet, viewGroup);
	}

	@Override
	public int getCount() {
		return m_statuses.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.tweet, null);
			holder = new ViewHolder();
			holder.tweetNmae = (TextView) convertView.findViewById(R.id.tweetNameLabel);
			holder.tweetTxt = (TextView) convertView.findViewById(R.id.tweetTxt);
			holder.profilePic = (ImageView) convertView.findViewById(R.id.tweetIcon);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		List<Status> statusList = m_statuses.getData();
		if (statusList.size() > 0) {
			Status currStatus = statusList.get(position);

			CharSequence strA = holder.tweetNmae.getText();

			holder.tweetNmae.setText(currStatus.getUser().getName() + " (@" + currStatus.getUser().getScreenName() + ")");
			holder.tweetTxt.setText(currStatus.getText());

			CharSequence strB = holder.tweetNmae.getText();

			if (strA.equals(strB) == false) {
				try {
					final URL url = currStatus.getUser().getProfileImageURL();
					final ViewHolder myHolder = holder;
					new AsyncTask<Void, Void, Bitmap>(){
						Bitmap bm = m_imageCache.get(url);
						
						@Override
						protected void onProgressUpdate(Void... values) {
							
						};

						@Override
						protected Bitmap doInBackground(Void... params) {
							if (bm == null) {
								bm = LoadImageFromWebOperations(url);

								if (m_imageCache.size() > 100) {
									m_imageCache.keySet().iterator().remove();
								}
								m_imageCache.put(url, bm);
							}
							return bm;
						}
						
						@Override
						protected void onPostExecute(Bitmap img) {
							if (bm != null) {
								myHolder.profilePic.setImageBitmap(bm);
								myHolder.profilePic.invalidate();
							}
						};
						
					}.execute();
				} catch (Exception e) {

				}
			}
		}
		return convertView;
	}

	public void updateData() {

	}

	private Bitmap LoadImageFromWebOperations(URL url) {
		try {
			Bitmap bm;
			HttpGet httpRequest = null;
			try {
				httpRequest = new HttpGet(url.toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			InputStream instream = bufHttpEntity.getContent();
			BufferedInputStream bis = new BufferedInputStream(instream);
			bm = BitmapFactory.decodeStream(bis);

			return bm;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static class ViewHolder {
		TextView tweetNmae;
		TextView tweetTxt;
		ImageView profilePic;
	}
}
