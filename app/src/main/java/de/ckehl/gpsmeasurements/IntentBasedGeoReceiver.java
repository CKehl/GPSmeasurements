package de.ckehl.gpsmeasurements;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by christian on 26-10-17.
 */
public class IntentBasedGeoReceiver extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "IBGeoReceiver";
    public static String getFragmentTag() {
        return TAG;
    }
    public static final int PENDING_INTEND_GPS_REQUEST_PERMISSION_CODE = 959;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 200 * 1; // 0.2 seconds
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL = 1000; // Every 1 seconds.
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private static final long FASTEST_UPDATE_INTERVAL = 250; // Every 0.25 seconds
    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 30; // Every 30 seconds.

    private Context mContext;
    private Activity mActivity;
    private View mHeadView;
    private boolean _canGetLocation = false;
    private boolean _running = false;
    private boolean _hasGPSpermissions = false;
    private boolean _gacInitialised = false;

    private Location mlocation = null;
    private double latitude = 0.0, longitude = 0.0, altitude = 0.0;
    private float mAccuracy = 0.0f;

    //private LocationManager mLocationManager;
    private LocationInterface mLocationInterface;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest = null;
    //private FusedLocationProviderClient mFusedLocationClient;
    private FusedLocationProviderApi mFusedLocationClient;

    public Location GetCurrentLocation() {
        return mlocation;
    }
    /**
     * Function to get latitude
     * */
    public double getLatitude() {
        if(mlocation != null){
            latitude = mlocation.getLatitude();
        }
        // return latitude
        return latitude;
    }
    /**
     * Function to get longitude
     * */
    public double getLongitude() {
        if(mlocation != null){
            longitude = mlocation.getLongitude();
        }
        // return longitude
        return longitude;
    }
    /*
     * Function to get altitude
     */
    public double getAltitude() {
        if(mlocation != null)
        {
            altitude = mlocation.getAltitude();
        }
        return altitude;
    }
    public float getAccuracy()
    {
        return mAccuracy;
    }

    public boolean isRunning() {
        return _running;
    }

    public boolean HasGPSpermission() {
        return _hasGPSpermissions;
    }
    /**
     * Function to check if best network provider
     * @return boolean
     * */
    public boolean canGetLocation() {
        return _canGetLocation;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = mActivity.getBaseContext();
        mHeadView = mActivity.findViewById(R.id.main_view_container);
        _canGetLocation = false;
        _running = false;
        //mGoogleApiClient = new GoogleApiClient.Builder(mContext).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
        //mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

        //if (!checkPermissions()) {
        //    requestPermissions();
        //}

        mFusedLocationClient = LocationServices.FusedLocationApi;
    }

    /*@Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(this);
    }*/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(this);
    }

    /*@Override
    public void onResume() {
        super.onResume();
    }*/

    /*@Override
    public void onStop() {
        PreferenceManager.getDefaultSharedPreferences(mContext).unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }*/

    @Override
    public void onDetach() {
        PreferenceManager.getDefaultSharedPreferences(mContext).unregisterOnSharedPreferenceChangeListener(this);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        if(_running)
            stopUsingGPS();
        if(mGoogleApiClient!=null) {
            if(mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @NonNull
    public void explicitInitialise(LocationInterface locationInterface) {
        mLocationInterface = locationInterface;
        createLocationRequest();
    }

    public void startUsingGPS() {
        if((_gacInitialised) && (_hasGPSpermissions))
            requestLocationUpdates();
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        removeLocationUpdates();
        _running = false;
        _canGetLocation=false;

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(IntentBasedGeoUtils.KEY_LOCATION_UPDATES_RESULT)) {
            Log.d(TAG, "Location available");
        } else if(key.equals(IntentBasedGeoUtils.KEY_BEST_LOCATION_UPDATES_RESULT)) {
            Log.d(TAG, "Improved geo-location available");
            String gpsstring = IntentBasedGeoUtils.getBestLocationUpdatesResult(mContext);
            Log.d(TAG, "gps string: "+gpsstring);
            double pos [] = IntentBasedGeoUtils.parseLocationResultText(gpsstring);
            longitude = pos[0];
            latitude = pos[1];
            altitude = pos[2];
            Log.d(TAG, "GPS position: "+Double.toString(longitude)+", "+Double.toString(latitude)+", "+Double.toString(altitude)+".");
            //mlocation = location;
            mlocation.setLongitude(longitude);
            mlocation.setLatitude(latitude);
            mlocation.setAltitude(altitude);
            mlocation.setTime(System.currentTimeMillis());
            mlocation.setElapsedRealtimeNanos(System.nanoTime());
            _canGetLocation = true;
            Log.d(TAG, "last location received.");
            mLocationInterface.onLocationAvailable(mlocation);
        } else if(key.equals(IntentBasedGeoUtils.KEY_BEST_LOCATION_EXTENDED_UPDATES_RESULT)) {
            Log.d(TAG, "Improved geo-location (extended) available");
            String gpsstring = IntentBasedGeoUtils.getBestLocationExtendedUpdatesResult(mContext);
            Log.d(TAG, "gps string: "+gpsstring);
            if(mlocation==null)
                mlocation = new Location(LocationManager.PASSIVE_PROVIDER);
            Location loc = IntentBasedGeoUtils.parseLocationExtendedResultText(gpsstring, mlocation);
            if(loc != null) {
                mlocation = loc;
                longitude = mlocation.getLongitude();
                latitude = mlocation.getLatitude();
                altitude = mlocation.getAltitude();
                _canGetLocation = true;
                Log.d(TAG, "last location received.");
                mLocationInterface.onLocationAvailable(mlocation);
            }
        } else if (key.equals(IntentBasedGeoUtils.KEY_LOCATION_UPDATES_REQUESTED)) {
            if(IntentBasedGeoUtils.getRequestingLocationUpdates(mContext))
                Toast.makeText(mContext, "Location updates are requested.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(mContext, "This device is not supported.", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    private PendingIntent getPendingIntent() {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(mContext, IntentBasedGeoBroadcastService.class);
        intent.setAction(IntentBasedGeoBroadcastService.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION);
        _hasGPSpermissions = (permissionState == PackageManager.PERMISSION_GRANTED);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(mHeadView,
                    "Request GPS access",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Granted", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PENDING_INTEND_GPS_REQUEST_PERMISSION_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PENDING_INTEND_GPS_REQUEST_PERMISSION_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == PENDING_INTEND_GPS_REQUEST_PERMISSION_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //-------------------------
                // Permission was granted.
                //-------------------------
                // requestLocationUpdates();
                _hasGPSpermissions = true;
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(mHeadView,
                        "Permissions fimly denied.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Open Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    public void handleGPSpermissionRequest(int requestCode, String permissions[], int[] grantResults) {
        if(!_hasGPSpermissions)
            onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates");
            IntentBasedGeoUtils.setRequestingLocationUpdates(mContext, true);
            if(mLocationRequest==null)
                createLocationRequest();
            mFusedLocationClient.requestLocationUpdates(mGoogleApiClient,mLocationRequest, getPendingIntent());
            //mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
            _running=true;
        } catch (SecurityException e) {
            IntentBasedGeoUtils.setRequestingLocationUpdates(mContext, false);
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        IntentBasedGeoUtils.setRequestingLocationUpdates(mContext, false);
        mFusedLocationClient.removeLocationUpdates(mGoogleApiClient, getPendingIntent());
        // mFusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!checkPermissions()) {
            requestPermissions();
        }
        _gacInitialised=true;
        Log.i(TAG, "Google API connected");

        _canGetLocation=false;
        try {
            mlocation = mFusedLocationClient.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if(mlocation!=null) {
            Log.i("GeoReceiver", "last location received.");
            latitude = mlocation.getLatitude();
            longitude = mlocation.getLongitude();
            altitude = mlocation.getAltitude();
            _canGetLocation = true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        _running = false;
        Toast.makeText(mContext, "Failed to connect to Google API", Toast.LENGTH_LONG).show();
    }
}
