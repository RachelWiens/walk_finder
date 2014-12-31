package aurora.walkfinder;

import android.app.FragmentManager;
import android.content.IntentSender;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_RESOLVE_ERROR = 1001;     // Request code to use when launching the resolution activity

    private static final String DIALOG_ERROR = "dialog_error";      // Unique tag for the error dialog fragment

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private boolean mResolvingError = false;       // Bool to track whether the app is already resolving an error

    private EditText startLocEditTxt;
    private EditText endLocEditTxt;
    private EditText durationEditTxt;
    private CheckBox endLocChk;
    private Button goBtn;
    

    /**
     * Upon app start.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mResolvingError = (savedInstanceState != null) && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);  // load mResolvingError if it exists
        buildGoogleApiClient();
        
        startLocEditTxt = (EditText) findViewById(R.id.start_loc_txt);
        endLocEditTxt = (EditText) findViewById(R.id.end_loc_txt);
        durationEditTxt = (EditText) findViewById(R.id.duration_txt);
        endLocChk = (CheckBox) findViewById(R.id.end_loc_chk);
        goBtn = (Button) findViewById(R.id.go_btn);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    // TODO
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // TODO
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Connect to Google Play services. 
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if( mLastLocation != null ) {
            startLocEditTxt.setText(mLastLocation.toString());
        }
    }

    /**
     * Connection to Google Play services has been interrupted. 
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // TODO
    }

    /**
     * Failed to connect to Google Play services.
     * @param result
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // already attempting to resolve error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // there was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    /**
     * Create a dialog for an error message. 
     * @param errorCode
     */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }
    
    public void onDialogDismissed() {
        mResolvingError = false;
    }


    /** 
     * Using the start and end location, plot one or more walks of length duration.
     * 
     * @param view
     */
    public void plotWalks(View view) {
        String startLoc = startLocEditTxt.getText().toString();
        String endLoc = startLoc;
        if( ! endLocChk.isChecked() ) {                 // if not checked, use a location different from the start for the end
            endLoc = endLocEditTxt.getText().toString();
        }
        String duration = durationEditTxt.getText().toString();
        TextView outputTxt = (TextView) findViewById(R.id.output_lbl);
        outputTxt.setText(startLoc + " " + endLoc);
        
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if( !mResolvingError ) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    
    @Override
    protected void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }
}