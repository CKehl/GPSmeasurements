package de.ckehl.gpsmeasurements;

/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility methods used in this sample.
 */
public class IntentBasedGeoUtils {

    final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    final static String KEY_BEST_LOCATION_UPDATES_RESULT = "location_best-update-result";
    final static String KEY_BEST_LOCATION_EXTENDED_UPDATES_RESULT = "location_best_extended-update-result";

    static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    static boolean getRequestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    static void sendNotification(Context context, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, IntentBasedGeoUtils.class);
        //Intent notificationIntent = new Intent(context, IntentBasedGeoNotifyView.class);

        notificationIntent.putExtra("from_notification", true);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainGPSactivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_gps_status_notification)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle("Location update")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }


    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     *
     * @param context The {@link Context}.
     */
    static String getLocationResultTitle(Context context, List<Location> locations) {
        String numLocationsReported = "Found "+Integer.toString(locations.size())+" locations";
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    /**
     * Returns te text for reporting about a list of  {@link Location} objects.
     *
     * @param locations List of {@link Location}s.
     */
    private static String getLocationResultText(Context context, List<Location> locations) {
        if (locations.isEmpty()) {
            return "Location unknown.";
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns te text for reporting about a {@link Location} object.
     *
     * @param location List of {@link Location}s.
     */
    public static String getBestLocationResultText(Context context, Location location) {
        if (location==null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(location.getLongitude());
        sb.append(", ");
        sb.append(location.getLatitude());
        sb.append(", ");
        sb.append(location.getAltitude());
        sb.append(")");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Returns te text for reporting about a {@link Location} object.
     *
     * @param location List of {@link Location}s.
     */
    public static String getBestLocationExtendedResultText(Context context, Location location) {
        if (location==null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(location.getLongitude());
        sb.append(", ");
        sb.append(location.getLatitude());
        sb.append(", ");
        sb.append(location.getAltitude());
        sb.append(", ");
        sb.append(location.getAccuracy());
        sb.append(", ");
        sb.append(location.getProvider());
        sb.append(", ");
        sb.append(location.getTime());
        sb.append(", ");
        sb.append(location.getElapsedRealtimeNanos());
        sb.append(")");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Returns the location values compressed in a location string
     * @param locationString comma-separated location string
     * @return array of position values, 3-dimensional, order: lon-lat-alt (x-y-z)
     */
    public static double[] parseLocationResultText(String locationString) {
        String noParantheses = locationString.substring(1, locationString.length()-2);
        Log.d("IBGeoUtils", "non-paran substring: '"+noParantheses+"'");
        StringTokenizer tokens = new StringTokenizer(noParantheses, ",");
        if(tokens.countTokens()<3) {
            Log.d("IBGeoUtils", "No valid location string");
        }
        double pos[] = new double[3];
        pos[0] = Double.parseDouble(tokens.nextToken().trim());
        pos[1] = Double.parseDouble(tokens.nextToken().trim());
        pos[2] = Double.parseDouble(tokens.nextToken().trim());
        return pos;
    }

    /**
     * Returns the location values compressed in a location string
     * @param locationString comma-separated location string
     * @return array of position values, 3-dimensional, order: lon-lat-alt (x-y-z)
     */
    public static Location parseLocationExtendedResultText(String locationString, Location containerInput) {
        String noParantheses = locationString.substring(1, locationString.length()-2);
        //Log.d("IBGeoUtils", "non-paran substring: '"+noParantheses+"'");
        StringTokenizer tokens = new StringTokenizer(noParantheses, ",");
        if(tokens.countTokens()<3) {
            Log.d("IBGeoUtils", "No valid location string");
        }

        if(containerInput!=null) {
            containerInput.setLongitude(Double.parseDouble(tokens.nextToken().trim()));
            containerInput.setLatitude(Double.parseDouble(tokens.nextToken().trim()));
            containerInput.setAltitude(Double.parseDouble(tokens.nextToken().trim()));
            containerInput.setAccuracy(Float.parseFloat(tokens.nextToken().trim()));
            containerInput.setProvider(tokens.nextToken().trim());
            containerInput.setTime(Long.parseLong(tokens.nextToken().trim()));
            containerInput.setElapsedRealtimeNanos(Long.parseLong(tokens.nextToken().trim()));
        }
        return containerInput;
    }

    static void setLocationUpdatesResult(Context context, List<Location> locations) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
                        + "\n" + getLocationResultText(context, locations))
                .apply();
    }

    static void setBestLocationUpdatesResult(Context context, Location location) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_BEST_LOCATION_UPDATES_RESULT, getBestLocationResultText(context, location))
                .apply();
    }

    static void setBestLocationExtendedUpdatesResult(Context context, Location location) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_BEST_LOCATION_EXTENDED_UPDATES_RESULT, getBestLocationExtendedResultText(context, location))
                .apply();
    }

    static String getLocationUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    public static String getBestLocationUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_BEST_LOCATION_UPDATES_RESULT, "");
    }

    public static String getBestLocationExtendedUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_BEST_LOCATION_EXTENDED_UPDATES_RESULT, "");
    }
}
