package de.ckehl.gpsmeasurements;

import android.*;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by christian on 06/12/17.
 */
public class GeoLocationFragment extends Fragment implements LocationInterface {
    protected LocationDataToView _uiInterface = null;
    protected GeoReceiver _geoReceiver = null;
    private LocationManager mLocationManager = null;

    private long startTime = -1;
    private long currentTime = 0;
    private boolean hasGPSpermissions = false;
    private View mHeadView = null;

    public static final int GPS_REQUEST_PERMISSION_CODE = 900;
    //private static final long SENSOR_MAXDELTA_TIME = 25000;
    private static final String TAG = "GeoLocationFragment";

    public GeoLocationFragment() {
        super();
    }

    public GeoLocationFragment(LocationDataToView uiInterface) {
        super();
        _uiInterface = uiInterface;
    }

    public void setUiInterface(LocationDataToView uiInterface) {
        _uiInterface = uiInterface;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public void onStop() {
        super.onStop();
        if( (_geoReceiver!=null) && (_geoReceiver.isRunning()==true) )
        {
            _geoReceiver.stopUsingGPS();
        }
        _geoReceiver=null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if( (_geoReceiver!=null) && (_geoReceiver.isRunning()==true) )
        {
            _geoReceiver.stopUsingGPS();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mHeadView = getActivity().findViewById(R.id.main_view_container);
        if(_geoReceiver == null) {
            _geoReceiver = new GeoReceiver(getActivity(), this);
            requestLocationPermission();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHeadView = getActivity().findViewById(R.id.main_view_container);
        if(_geoReceiver == null) {
            _geoReceiver = new GeoReceiver(getActivity(), this);
            requestLocationPermission();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void toggleReceiver() {
        if( (_geoReceiver!=null) && _geoReceiver.isRunning() )
        {
            deactivateGeoReceiver();
        } else if( (_geoReceiver!=null) && !_geoReceiver.isRunning() ) {
            activateGeoReceiver();
        }
    }

    private void activateGeoReceiver() {
        if(_geoReceiver == null) {
            _geoReceiver = new GeoReceiver(getActivity(), this);
            requestLocationPermission();
        }

        if(hasGPSpermissions==false)
            requestLocationPermission();

        if( hasGPSpermissions && !_geoReceiver.isRunning() )
        {
            Log.i(TAG, "Starting GPS measurement.");
            _geoReceiver.startUsingGPS();
            _uiInterface.isReceiving(true);
        }
    }

    private void deactivateGeoReceiver() {
        if( (_geoReceiver!=null) && (_geoReceiver.isRunning()==true) )
        {
            Log.i(TAG, "Stopping GPS measurement.");
            _geoReceiver.stopUsingGPS();
            _uiInterface.isReceiving(false);
        }
    }

    public void requestLocationPermission() {
        mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        // getting GPS status
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if ((isGPSEnabled==false) && (isNetworkEnabled==false)) {
            showSettingsAlert();
            //Intent permissionsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //startActivity(permissionsIntent);
        }

        // getting GPS status
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            return;
        }


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hasGPSpermissions = ((ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));
            if (!hasGPSpermissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(mHeadView, "Request GPS access", Snackbar.LENGTH_INDEFINITE).setAction("Granted", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQUEST_PERMISSION_CODE);
                        }
                    }).show();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQUEST_PERMISSION_CODE);
                }
            }
        }
    }

    @Override
    public void onLocationAvailable(Location location) {
        if(startTime<0)
            startTime = location.getTime();
        if( (location != null) && (_geoReceiver!=null) ) {

            if( (_geoReceiver.isRunning()==true) && (_geoReceiver.canGetLocation()==true) ) {
                Log.i(TAG, "New location available.");

                Location mLoc = _geoReceiver.GetCurrentLocation();

                if(_geoReceiver.getAccuracy() > 50)
                {

                }
                else if(_geoReceiver.getAccuracy() > 4)
                {

                }
                else if(_geoReceiver.getAccuracy() < 4)
                {

                }
                _uiInterface.updateLongitude((float) (mLoc.getLongitude()));
                _uiInterface.updateLatitude((float) (mLoc.getLatitude()));
                _uiInterface.updateAltitude((float) (mLoc.getAltitude()));

                currentTime = location.getTime();
                //long deltaT = currentTime-startTime;
                //if(deltaT>=SENSOR_MAXDELTA_TIME) {
                //    haveGPSlocation = true;
                //}
            }
        }
    }

    public void handleGPSpermissionRequest(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case GPS_REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty
                if((grantResults.length >0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //permission granted
                    Toast.makeText(getActivity(), "Permission was granted!", Toast.LENGTH_LONG).show();
                    hasGPSpermissions = true;
                } else {
                    //permission denied
                    hasGPSpermissions = false;
                }
                return;
            }
        }
    }

    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialogBuilder.setTitle("GPS is settings");
        alertDialogBuilder.setCancelable(false);
        // Setting Dialog Message
        alertDialogBuilder.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialogBuilder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();

        // Showing Alert Message
        alertDialog.show();
    }
}
