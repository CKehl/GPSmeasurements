package de.ckehl.gpsmeasurements;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by christian on 06/12/17.
 */
public class GeoLocationFragment extends Fragment {
    protected LocationDataToView _uiInterface;


    public GeoLocationFragment() {
        super();
    }

    public GeoLocationFragment(LocationDataToView uiInterface) {
        _uiInterface = uiInterface;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
