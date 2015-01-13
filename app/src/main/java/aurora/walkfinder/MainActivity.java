package aurora.walkfinder;

import android.app.FragmentManager;
import android.content.Context;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity{

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

        goBtn = (Button) findViewById(R.id.go_btn);
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
     * Using the start and end location, plot one or more walks of length duration.
     * 
     * @param view
     */
    public void plotWalks(View view) {
        /*String startLoc = startLocEditTxt.getText().toString();
        String endLoc = startLoc;
        if( ! endLocChk.isChecked() ) {                 // if not checked, use a location different from the start for the end
            endLoc = endLocEditTxt.getText().toString();
        }
        String duration = durationEditTxt.getText().toString();
        TextView outputTxt = (TextView) findViewById(R.id.output_lbl);
        outputTxt.setText(startLoc + " " + endLoc);*/
        
        LocationTextInputFragment inputFrag = (LocationTextInputFragment) getFragmentManager().findFragmentById(R.id.text_input_frag);
        inputFrag.plotWalks(view);
    }

}
