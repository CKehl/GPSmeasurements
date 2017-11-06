package de.ckehl.gpsmeasurements;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

/**
 * Created by christian on 27-10-17.
 */
public class IntentBasedGeoBroadcastService extends BroadcastReceiver {
    private static final String TAG = "IBGeoBroadcastService";
    //private static final double TIMEFRAME = 1000.0 * 1; // 1 second
    private static final int TIMEFRAME = 1000 * 1; // 1 second

    static final String ACTION_PROCESS_UPDATES =
            "christian.fragmentexample.action.PROCESS_UPDATES";

    private Location mLocation = null;
    private boolean mBestLocationChanged = false;
    private float mAccuracy = 0.0f;
    private float mAccuracyDelta = 0.0f;

    @Override
    public void onReceive(Context context, Intent intent) {
        mBestLocationChanged = false;
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                Log.i(TAG, "receiving process update from broadcaster");
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    for(Location locEntry : locations) {
                        //if((locEntry.getProvider() == LocationManager.GPS_PROVIDER) || (locEntry.getProvider() == LocationManager.PASSIVE_PROVIDER)) {
                        //if(true) {
                            if(mLocation==null) {
                                Log.d(TAG, "Entry location is null");
                                mLocation = locEntry;
                                mAccuracy = mLocation.getAccuracy();
                                mAccuracyDelta = -1.0f;
                                mBestLocationChanged = true;
                            } else {
                                if (isBetterLocation(locEntry, mLocation)) {
                                    mLocation = locEntry;
                                    mBestLocationChanged = true;
                                }
                            }
                        //}
                    }
                    //IntentBasedGeoUtils.setLocationUpdatesResult(context, locations);
                    if(mBestLocationChanged) {
                        Log.d(TAG, "Got new best location!");
                        //IntentBasedGeoUtils.setBestLocationUpdatesResult(context, mLocation);
                        IntentBasedGeoUtils.setBestLocationExtendedUpdatesResult(context, mLocation);
                    }
                    IntentBasedGeoUtils.sendNotification(context, IntentBasedGeoUtils.getLocationResultTitle(context, locations));
                    Log.i(TAG, IntentBasedGeoUtils.getBestLocationExtendedUpdatesResult(context));
                }
            }
        }
    }

    public Location getLocation() {
        return mLocation;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        Log.i(TAG, "got a new location");
        if (currentBestLocation == null) {
            // A new location is always better than no location
            if(location!=null)
                mAccuracy = location.getAccuracy();
            else
                mAccuracy = -1.0f;
            Log.d(TAG, "no location to compare to, use: "+Double.toString(location.getLongitude())+", "+Double.toString(location.getLatitude())+", "+Double.toString(location.getAltitude())+".");
            return true;
        } else {
            Log.d(TAG, "Old location: "+Double.toString(currentBestLocation.getLongitude())+", "+Double.toString(currentBestLocation.getLatitude())+", "+Double.toString(currentBestLocation.getAltitude())+".");
        }

        Log.i(TAG, "New location: "+Double.toString(location.getLongitude())+", "+Double.toString(location.getLatitude())+", "+Double.toString(location.getAltitude())+".");

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
}
