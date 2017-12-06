package de.ckehl.gpsmeasurements;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainGPSactivity extends AppCompatActivity implements LocationDataToView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public void isReceiving(boolean indicator) {

    }

    @Override
    public void updateLongitude(float longitude) {

    }

    @Override
    public void updateLatitude(float latitude) {

    }

    @Override
    public void updateAltitude(float altitude) {

    }
}
