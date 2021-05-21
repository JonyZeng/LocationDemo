package com.jymeditech.locationdemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.permissionx.guolindev.PermissionX;

import org.w3c.dom.Text;

import java.text.DateFormat;


public class BlankFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "BlankFragment";

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;
    private LocationManagerService mHuaWeiService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    // Monitors the state of the connection to the service.
    private final ServiceConnection mHuaWeiServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationManagerService.LocalBinder binder = (LocationManagerService.LocalBinder) service;
            Log.i(TAG, "onServiceConnected: huawei");
            mHuaWeiService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mHuaWeiService = null;
            mBound = false;
        }
    };
    private View mView;
    private Button mBIndService;
    private Button mUnbindService;
    private TextView mTime;
    private TextView mDistance;
    private TextView mSendTime;
    private TextView mLocation;
    private StringBuilder append = new StringBuilder();

    public BlankFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myReceiver = new MyReceiver();
        mView = inflater.inflate(R.layout.fragment_blank, container, false);

        // Check that the user hasn't revoked permissions by going to Settings.
        Log.i(TAG, "onCreateView: " + Utils.requestingLocationUpdates(getContext()));
        return mView;
    }

    /**
     * 华为
     **/
    public static boolean isHuawei() {
        if (Build.BRAND == null) {
            return false;
        } else {
            return Build.BRAND.toLowerCase().equals("huawei") || Build.BRAND.toLowerCase().equals("honor");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText(getContext(), "是否是华为手机：" + isHuawei(), Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onStart: ");
        //注册sp变化的监听者
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);

        mRequestLocationUpdatesButton = (Button) mView.findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) mView.findViewById(R.id.remove_location_updates_button);
        mBIndService = mView.findViewById(R.id.bind_service);
        mUnbindService = mView.findViewById(R.id.unbind_service);
        mTime = mView.findViewById(R.id.time);
        mSendTime = mView.findViewById(R.id.sendTime);
        mDistance = mView.findViewById(R.id.distance);
        mLocation = mView.findViewById(R.id.lo);

        mBIndService.setOnClickListener(v -> {
            Log.i(TAG, "onStart: ==" + mTime.getText().length());
            long time = mTime.getText().length() <= 0 ? 5000 : Long.parseLong(mTime.getText().toString());
            float dis = mDistance.getText().length() <= 0 ? 10f : Float.parseFloat(mDistance.getText().toString());
            Utils.setIntervalTime(getContext(), time);
            Utils.setDis(getContext(), dis);
            // Bind to the service. If the service is in foreground mode, this signals to the service
            // that since this activity is in the foreground, the service can exit foreground mode.
            PermissionX.init(this)
                    .permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
                    .onExplainRequestReason((scope, deniedList) -> {
                        Log.i(TAG, "onStart: ");
                    }).request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    if (isHuawei()) {
                        Log.i(TAG, "onStart: ");
                        getActivity().bindService(new Intent(getContext(), LocationManagerService.class), mHuaWeiServiceConnection,
                                Context.BIND_AUTO_CREATE);
                    } else {
                        Log.i(TAG, "onStart: no");
                        getActivity().bindService(new Intent(getContext(), LocationUpdatesService.class), mServiceConnection,
                                Context.BIND_AUTO_CREATE);
                    }
                }
            });


        });

        LocationUpdatesService.setListListener(new LocationUpdatesService.ListListener() {
            @Override
            public void setListListener(String list) {
                append.append(list + "\n");
                append.append("=========" + "\n");
                mLocation.setText(append);
            }
        });
        mUnbindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    // Unbind from the service. This signals to the service that this activity is no longer
                    // in the foreground, and the service can respond by promoting itself to a foreground
                    // service.
                    if (isHuawei()) {
                        getActivity().unbindService(mHuaWeiServiceConnection);

                    } else {
                        getActivity().unbindService(mServiceConnection);

                    }
                    mBound = false;
                }
            }
        });
        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: ");
                PermissionX.init(BlankFragment.this)
                        .permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
                        .onExplainRequestReason((scope, deniedList) -> {

                        }).request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        if (isHuawei()) {
                            mHuaWeiService.requestLocationUpdates();
                        } else {
                            long time = mSendTime.getText().length() <= 0 ? 5000 : Long.parseLong(mSendTime.getText().toString());
                            mService.requestLocationUpdates();
//                            mService.senMsg();

                        }
                    }
                });
            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHuawei()) {
                    mHuaWeiService.removeLocationUpdates();
                } else {
                    mService.removeLocationUpdates();

                }
            }
        });

        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(getContext()));

    }

    @Override
    public void onResume() {
        super.onResume();
        //注册本地广播
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    public void onPause() {
        //去夏普注册本地广
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            if (isHuawei()) {
                getActivity().unbindService(mHuaWeiServiceConnection);

            } else {
                getActivity().unbindService(mServiceConnection);

            }
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    mView.findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                    },
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                if (isHuawei()) {
                    mHuaWeiService.requestLocationUpdates();
                } else {
                    mService.requestLocationUpdates();
                }

            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        mView.findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        "com.jymeditech.locationdemo", null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(getContext(), Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }
}