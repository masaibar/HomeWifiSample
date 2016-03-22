package com.masaibar.homewifisample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //半径
    private final static float FENCE_RADIUS_METERS = 200.0f;

    //ジオフェンスID
    private final static String FENCE_ID = "test";

    //設置、削除を示す定数
    private final static int ADD_FENCE = 0;
    private final static int REMOVE_FENCE = 1;

    private boolean mInProgress;
    private int mRequestType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapActivity.start(getApplicationContext());
            }
        });

        findViewById(R.id.button_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        MainActivity.this,
                        MapActivity.getLatLng(getApplicationContext()).toString(),
                        Toast.LENGTH_SHORT).show();
                Toast.makeText(
                        MainActivity.this,
                        LocationUtil.getAddressFromLatLng(getApplicationContext(), MapActivity.getLatLng(getApplicationContext())),
                        Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
