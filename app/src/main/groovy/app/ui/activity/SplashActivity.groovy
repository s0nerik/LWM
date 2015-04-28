package app.ui.activity
import android.content.Intent
import android.content.IntentSender
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.OnClick
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.plus.Plus
import com.lwm.app.R
import groovy.transform.CompileStatic
import ru.noties.debug.Debug

@CompileStatic
public class SplashActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SwissKnife.inject(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... emails) {
                Bundle appActivities = new Bundle();
                appActivities.putString(GoogleAuthUtil.KEY_REQUEST_VISIBLE_ACTIVITIES,
//                        "<APP-ACTIVITY1> <APP-ACTIVITY2>"
                        ""
                );
                String scopes = "oauth2:server:client_id:"\
                         +"608412075734-g1rs52is4nku7k5s7g3va1fnv74inidi.apps.googleusercontent.com:"\
                         +"api_scope:${Plus.SCOPE_PLUS_LOGIN}"
                String code = null;
                try {
                    code = GoogleAuthUtil.getToken(
                            SplashActivity.this,                                              // Context context
                            Plus.AccountApi.getAccountName(mGoogleApiClient),  // String accountName
                            scopes,                                            // String scope
                            appActivities                                      // Bundle bundle
                    );

                } catch (IOException transientEx) {
                    // network or server error, the call is expected to succeed if you try again later.
                    // Don't attempt to call again immediately - the request is likely to
                    // fail, you'll hit quotas or back-off.

                    return null;
                } catch (final UserRecoverableAuthException e) {
                    // Requesting an authorization code will always throw
                    // UserRecoverableAuthException on the first call to GoogleAuthUtil.getToken
                    // because the user must consent to offline access to their data.  After
                    // consent is granted control is returned to your activity in onActivityResult
                    // and the second call to GoogleAuthUtil.getToken will succeed.

                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!mIntentInProgress) {
                                mIntentInProgress = true;
                                FragmentActivity.startActivityForResult(e.getIntent(), RC_SIGN_IN);
                            }
                        }
                    });
                    return null;
                } catch (GoogleAuthException authEx) {
                    // Failure. The call is not expected to ever succeed so it should not be
                    // retried.
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return code;
            }

            @Override
            protected void onPostExecute(String authToken) {
                Debug.d("Token: " + authToken);
//                if (authToken != null) {
//                    saveTokenAndGetCalendars(email, authToken);
//                }
            }
        }.execute();

    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }

//        if (requestCode == RC_SIGN_IN && responseCode == RESULT_OK) {
//            Bundle extra = intent.getExtras();
//            String oneTimeToken = extra.getString("authtoken");
//        }
    }

    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @OnClick(R.id.sign_in_button)
    public void onSignInClicked() {
        mGoogleApiClient.disconnect();
        mGoogleApiClient.connect();
    }

}
