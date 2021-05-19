/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jymeditech.locationdemo;


import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    static final String KEY_INERVAL = "interval";
    static final String KEY_FASTESTINERVAL = "fastestinerval";
    static final String KEY_DIS = "dis";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     *
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    static void setIntervalTime(Context context, long time) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(KEY_INERVAL, time)
                .apply();
    }

    static long getIntervalTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(KEY_INERVAL, 5000);
    }

    static void setFastIntervalTime(Context context, String time) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_FASTESTINERVAL, time)
                .apply();
    }

    static String getFastIntervalTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_FASTESTINERVAL, "");
    }

    static void setDis(Context context, float dis) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putFloat(KEY_DIS, dis)
                .apply();
    }

    static Float getDis(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getFloat(KEY_DIS, 10);
    }

    /**
     * Returns the {@code location} object as a human readable string.
     *
     * @param location The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }
}
