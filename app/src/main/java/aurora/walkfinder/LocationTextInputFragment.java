package aurora.walkfinder;

import android.app.Fragment;
import android.content.Context;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by aurora on 13/01/15.
 */
public class LocationTextInputFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolvingError = (savedInstanceState != null) && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);  // load mResolvingError if it exists
        buildGoogleApiClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_input, container, false);
        
        startLocEditTxt = (EditText) view.findViewById(R.id.start_loc_txt);
        endLocEditTxt = (EditText) view.findViewById(R.id.end_loc_txt);
        durationEditTxt = (EditText) view.findViewById(R.id.duration_txt);
        endLocChk = (CheckBox) view.findViewById(R.id.end_loc_chk);
        goBtn = (Button) view.findViewById(R.id.go_btn);
        
        return view;
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }


    /**
     * Connect to Google Play services.
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            startLocEditTxt.setText(mLastLocation.toString());
            // get the postal address
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                (new GetAddressTask(getActivity())).execute(mLastLocation);
            }
        }
    }

    /**
     * Connection to Google Play services has been interrupted.
     *
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // TODO
    }

    /**
     * Failed to connect to Google Play services.
     *
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
                result.startResolutionForResult(getActivity(), REQUEST_RESOLVE_ERROR);
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
     *
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
        makeToast("creating dialog for an error message.");
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
        if (!endLocChk.isChecked()) {                 // if not checked, use a location different from the start for the end
            endLoc = endLocEditTxt.getText().toString();
        }
        String duration = durationEditTxt.getText().toString();
        TextView outputTxt = (TextView) getActivity().findViewById(R.id.output_lbl);
        outputTxt.setText(startLoc + " " + endLoc);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();   // TODO: Consider stopping this as soon as the call returns instead of in onStop()
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    /**
     * Get a location address. Task runs in the background to avoid delaying other tasks.
     */
    private class GetAddressTask extends AsyncTask<Location, Void, String> {
        Context mContext;

        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        /**
         * Look up address for a location based on its latitude and longitude.
         *
         * @param params : one or more location objects. Only returns address for the first.
         * @return string containing address of the first location, empty string if no address found, or error message.
         */
        @Override
        protected String doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            Location loc = params[0];

            // get result addresses
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity", "IO Exception in getFromLocation()");
                e1.printStackTrace();
                makeToast("IO Exception trying to get address");
                return ("IO Exception trying to get address");
            } catch (IllegalArgumentException e2) {
                String errorString = "Illegal arguments " + Double.toString(loc.getLatitude()) +
                        " , " + Double.toString(loc.getLongitude()) + " passed to address service.";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                makeToast(errorString);
                return errorString;
            }

            // if the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // get the first address
                Address address = addresses.get(0);
                // format the address (if available)
                String addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),          // city
                        address.getAdminArea()         // province/state
                );
                return addressText;
            } else {
                return "";
            }

        }

        /**
         * Called after doInBackground completes.
         * Update the text to show the postal address instead of latitude and longitude.
         */
        @Override
        protected void onPostExecute(String address) {
            startLocEditTxt.setText(address);
        }

    }

    private void makeToast(String msg) {
        Context context = getActivity().getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();

    }

}


