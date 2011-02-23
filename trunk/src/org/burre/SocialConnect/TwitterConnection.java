package org.burre.SocialConnect;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.widget.Toast;

public class TwitterConnection {
	private static TwitterConnection m_instance = new TwitterConnection();
	RequestToken m_requestToken = null;
	AccessToken m_token = null;
	Twitter m_twitter = new TwitterFactory().getInstance();
	User user = null;
	boolean m_keyIsLoaded = false;
	private final static String OAUTH_KEY = "oauthkey";
	private final static String OAUTH_SECRET = "oauthsecret";
	private final static String ERROR_PREF = "[ERROR]";
	private final static String OAUTH_AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize?oauth_token=";

	Vector<TwitterStatusListener> m_statusListeners = new Vector<TwitterStatusListener>();

	public static TwitterConnection getInstance() {
		return m_instance;
	}

	public TwitterConnection() {
		Resources resources = SocialConnectApp.getContext().getResources();
		AssetManager assetManager = resources.getAssets();

		// Read from the /assets directory
		try {
			// Place a file named twitter4j.properties in the assets-folder with the format (replace the stars):
			// debug=true
			// oauth.consumerKey=*******
			// oauth.consumerSecret=*******			
			InputStream inputStream = assetManager.open("twitter4j.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			System.out.println("The properties are now loaded");
			System.out.println("properties: " + properties);

			String key = properties.getProperty("oauth.consumerKey", ERROR_PREF);
			String secret = properties.getProperty("oauth.consumerSecret", ERROR_PREF);
			if(key.equals(ERROR_PREF) || secret.equals(ERROR_PREF)){
				throw new IOException("Loading key or secret failed!");
			}
			
			m_twitter.setOAuthConsumer(key, secret);
		} catch (IOException e) {
			System.err.println("Failed to open 'assets/twitter4j.properties' property file");
			e.printStackTrace();
			Toast toast = Toast.makeText(SocialConnectApp.getContext(),
					"Failed to open 'assets/twitter4j.properties' property file: " + e.getMessage(),
					Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public interface TwitterStatusListener {
		public void onStatusUpdate(List<Status> statuses);
	}

	public void registerStatusListener(TwitterStatusListener listener) {
		if (m_statusListeners.contains(listener) == false) {
			m_statusListeners.add(listener);
		}
	}

	public void unregisterStatusListener(TwitterStatusListener listener) {
		m_statusListeners.remove(listener);
	}

	public void getPIN(Context context) {
		try {
			m_requestToken = m_twitter.getOAuthRequestToken();
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(OAUTH_AUTHORIZE_URL + m_requestToken.getToken()));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (TwitterException te) {
			if (te.getStatusCode() == TwitterException.UNAUTHORIZED) {
				System.out.println("UNAUTHORIZED: " + te.getMessage());
			} else {
				te.printStackTrace();
				System.out.println("Failed to get timeline: " + te.getMessage());
			}
		}
	}

	public boolean login(Context context, String pin) {
		try {
			// gets Twitter instance with default credentials
			if (m_twitter == null || m_token == null || m_keyIsLoaded == false) {
				m_token = m_twitter.getOAuthAccessToken(m_requestToken.getToken(),
						m_requestToken.getTokenSecret(),
						pin);
				user = m_twitter.verifyCredentials();

				if (user != null) {
					m_keyIsLoaded = true;
					saveOAuthKey(context);
				}
			}
			refreshTimeLine(context);
			
			return true;
		} catch (TwitterException te) {
			if (te.getStatusCode() == TwitterException.UNAUTHORIZED) {
				System.out.println("UNAUTHORIZED: " + te.getMessage());
			} else {
				te.printStackTrace();
				Toast toast = Toast.makeText(SocialConnectApp.getContext(),
						"Failed to get timeline: " + te.getMessage(),
						Toast.LENGTH_LONG);
				toast.show();
				System.out.println("Failed to get timeline: " + te.getMessage());
			}
		}

		return false;
	}

	public List<twitter4j.Status> refreshTimeLine(Context context) {
		final Context myContext = context;
		try {
			if (m_twitter == null || m_token == null || m_keyIsLoaded == false) {
				loadOAuthKey(myContext);
				if (m_twitter == null || m_token == null || m_keyIsLoaded == false) {

					Toast toast = Toast.makeText(SocialConnectApp.getContext(),
							"Failed to get timeline: Not logged in",
							Toast.LENGTH_LONG);
					toast.show();
					getPIN(context);
					return null;
				}
			}

			if (user == null) {
				loadOAuthKey(myContext);
				user = m_twitter.verifyCredentials();
			}
			if (user == null || m_token == null || m_keyIsLoaded == false) {
				throw new TwitterException("User not logged in!");
			}

			List<Status> statuses = m_twitter.getHomeTimeline();
			System.out.println("Recieved @" + user.getScreenName()
					+ "'s home timeline.");

			return statuses;

			//					for(TwitterStatusListener listener: m_statusListeners){
			//						listener.onStatusUpdate(statuses);
			//					}
		} catch (TwitterException te) {
			te.printStackTrace();
			Toast toast = Toast.makeText(SocialConnectApp.getContext(),
					"Failed to get timeline: " + te.getMessage(),
					Toast.LENGTH_LONG);
			toast.show();
			System.out.println("Failed to get timeline: " + te.getMessage());
		} catch (IllegalStateException ise) {
			System.out.println("IllegalStateException: " + ise.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			Toast toast = Toast.makeText(SocialConnectApp.getContext(),
					"Failed to get timeline: " + e.getMessage(),
					Toast.LENGTH_LONG);
			toast.show();
			System.out.println("Failed to get timeline: " + e.getMessage());
		}
		return null;
	}

	private void loadOAuthKey(Context context) {
		try {
			SharedPreferences settings = context.getSharedPreferences(SocialConnect.APP_PREFERENCES,
					Context.MODE_PRIVATE);

			String tok = settings.getString(OAUTH_KEY, ERROR_PREF);
			String sec = settings.getString(OAUTH_SECRET, ERROR_PREF);

			if (tok.contentEquals(ERROR_PREF) == false
					&& sec.contentEquals(ERROR_PREF) == false) {
				AccessToken token = new AccessToken(tok, sec);
				m_token = token;
				m_twitter.setOAuthAccessToken(m_token);
				m_keyIsLoaded = true;
			} else {
				m_keyIsLoaded = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast toast = Toast.makeText(SocialConnectApp.getContext(),
					"Failed to load OAuth key: " + e.getMessage(),
					Toast.LENGTH_LONG);
			toast.show();
			System.out.println("Failed to load OAuth key: " + e.getMessage());
		}
	}

	private void saveOAuthKey(Context context) {
		if (m_token == null || m_keyIsLoaded == false) {
			return;
		}

		// Restore preferences
		SharedPreferences settings = context.getSharedPreferences(SocialConnect.APP_PREFERENCES,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(OAUTH_KEY, m_token.getToken());
		editor.putString(OAUTH_SECRET, m_token.getTokenSecret());
		editor.commit();
	}
}
