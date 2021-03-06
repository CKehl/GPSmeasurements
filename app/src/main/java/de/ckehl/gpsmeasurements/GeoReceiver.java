package de.ckehl.gpsmeasurements;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

/**
 * Created by christian on 2-2-15.
 */
public class GeoReceiver extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,LocationListener {
    private Context mContext;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;

    private Location mlocation = null;
    private double latitude = 0.0, longitude = 0.0, altitude = 0.0;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 200 * 1; // 0.2 second
    //private static final double TIMEFRAME = 1000.0 * 1; // 1 second
    private static final int TIMEFRAME = 1000 * 1; // 1 second

    private LocationManager mLocationManager;
    private LocationInterface mLocationInterface;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest = null;

    private float mAccuracy = 0.0f;
    private float mAccuracyDelta = 0.0f;

    private boolean mRunning = false;

    private boolean hasGPSpermissions = false;

    //private EGM96 geoid_converter = null;
    //private ProgressDialog mProgressDialog = null;

    public GeoReceiver() {
    }

    public GeoReceiver(Context context)
    {
        mContext = context;
        getLocation();
    }

    public GeoReceiver(Context context, LocationInterface gpsInterface)
    {
        mContext = context;
        canGetLocation = false;
        mRunning = false;
        mLocationInterface = gpsInterface;
        mGoogleApiClient = new GoogleApiClient.Builder(mContext).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        createLocationRequest();
        mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    /*
    public GeoReceiver(Activity headActivity, LocationInterface gpsInterface) {
        mContext = headActivity.getBaseContext();
        canGetLocation = false;
        mRunning = false;
        mLocationInterface = gpsInterface;
        mGoogleApiClient = new GoogleApiClient.Builder(mContext).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        createLocationRequest();
        mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
    }
    */

    public synchronized void startUsingGPS()
    {
        //MODIFY HERE PRE-AND-POST ANDROID-6
        //if(false) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //mGoogleApiClient = new GoogleApiClient.Builder(mContext).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            //createLocationRequest();
            //mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
            if(mGoogleApiClient!=null) {
                mRunning = false;
                canGetLocation = false;
                mGoogleApiClient.connect();
            }
        } else {
            getLocation();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setFastestInterval(MIN_TIME_BW_UPDATES);
        //mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        // === crucial === //
        builder.setAlwaysShow(true);
    }

    public Location GetCurrentLocation()
    {
        return mlocation;
    }

    public void setLocationManagerRequest() {
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            Log.e("GeoReceiver", "Sensor access refused.");
        }
    }

    public Location getLocation()
    {
        try {
            mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    try {
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (mLocationManager != null) {
                            mlocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (mlocation != null) {
                                latitude = mlocation.getLatitude();
                                longitude = mlocation.getLongitude();
                                altitude = mlocation.getAltitude();
                            }
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    try {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mLocationManager != null) {
                            mlocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mlocation != null) {
                                latitude = mlocation.getLatitude();
                                longitude = mlocation.getLongitude();
                                altitude = mlocation.getAltitude();
                            }
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
            mRunning = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mlocation;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        Log.i("GeoReceiver", "got a new location");
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        Log.i("GeoReceiver", "New location: "+Double.toString(location.getLongitude())+", "+Double.toString(location.getLatitude())+", "+Double.toString(location.getAltitude())+".");

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        //double timeDelta = (location.getElapsedRealtimeNanos() - currentBestLocation.getElapsedRealtimeNanos())/1000000.0;
        boolean isSignificantlyNewer = timeDelta > TIMEFRAME;
        boolean isSignificantlyOlder = timeDelta < -TIMEFRAME;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta >= 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 40;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            mAccuracy = currentBestLocation.getAccuracy();
            mAccuracyDelta = (location.getAccuracy() - currentBestLocation.getAccuracy());
            return true;
        } else if (isNewer && !isLessAccurate) {
            mAccuracy = currentBestLocation.getAccuracy();
            mAccuracyDelta = (location.getAccuracy() - currentBestLocation.getAccuracy());
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            mAccuracy = currentBestLocation.getAccuracy();
            mAccuracyDelta = (location.getAccuracy() - currentBestLocation.getAccuracy());
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("GeoReceiver", "Received new coordinates: "+location.getProvider()+" - "+Double.toString(location.getLongitude())+", "+Double.toString(location.getLatitude())+", "+Double.toString(location.getAltitude()));
        if((isBetterLocation(location, mlocation) == true) || (canGetLocation==false))
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            mlocation = location;
            canGetLocation = true;
            Log.d("GeoReceiver", "last location received.");
            mLocationInterface.onLocationAvailable(mlocation);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(mlocation != null){
            latitude = mlocation.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(mlocation != null){
            longitude = mlocation.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /*
     * Function to get altitude
     */
    public double getAltitude()
    {
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

    public float getAccuracyDelta()
    {
        return mAccuracyDelta;
    }

    public boolean isRunning() {
         return mRunning;
    }

    public boolean HasGPSpermission() {
        return hasGPSpermissions;
    }

    /**
     * Function to check if best network provider
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(mGoogleApiClient!=null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            if(mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();
        }
        if(mLocationManager != null){
            try { mLocationManager.removeUpdates(GeoReceiver.this); }
            catch (SecurityException e) { e.printStackTrace(); }
        }
        mRunning = false;
        canGetLocation=false;
        isGPSEnabled = false;
        isNetworkEnabled = false;
        mlocation=null;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        if(provider == LocationManager.GPS_PROVIDER)
        {
            Toast.makeText(this, "GPS enabled.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if(provider == LocationManager.GPS_PROVIDER)
        {
            Toast.makeText(this, "GPS disabled.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            mRunning = true;
            canGetLocation = false;
            mlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mlocation != null) {
                Log.i("GeoReceiver", "last location received.");
                latitude = mlocation.getLatitude();
                longitude = mlocation.getLongitude();
                altitude = mlocation.getAltitude();
                canGetLocation = true;
            }
            setLocationManagerRequest();
            Log.i("GeoReceiver", "Location request is set up.");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        //mRunning = true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mRunning = false;
        Toast.makeText(mContext, "Failed to obtain location", Toast.LENGTH_LONG).show();
    }
}
