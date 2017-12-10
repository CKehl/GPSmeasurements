package de.ckehl.gpsmeasurements;

import android.app.DialogFragment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainGPSactivity extends AppCompatActivity implements LocationDataToView {

    private TextView _txt_longitude=null;
    private TextView _txt_latitude=null;
    private TextView _txt_altitude=null;
    private Button _btn_start_stop=null;
    private Button _btn_about=null;
    private GeoLocationFragment _localisationFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(_txt_longitude==null)
            _txt_longitude = (TextView)findViewById(R.id.txt_longitude);
        if(_txt_latitude==null)
            _txt_latitude = (TextView)findViewById(R.id.txt_latitude);
        if(_txt_altitude==null)
            _txt_altitude = (TextView)findViewById(R.id.txt_altitude);
        if(_btn_start_stop==null) {
            _btn_start_stop = (Button) findViewById(R.id.btn_start_stop);
            _btn_start_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchLocalisationActivity();
                }
            });
        }
        if(_btn_about==null) {
            _btn_about = (Button)findViewById(R.id.btn_about);
            _btn_about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dialog = new AboutDialog();
                    dialog.show(getFragmentManager(), "AboutDialog");
                }
            });
        }
        if(_localisationFragment==null) {
            _localisationFragment = new GeoLocationFragment(this);
            //getFragmentManager().beginTransaction().attach(_localisationFragment).commit();
            getFragmentManager().beginTransaction().add(_localisationFragment, "GeoLocalisation").commit();
        }
    }

    @Override
    protected void onDestroy() {
        if(_localisationFragment!=null) {
            getFragmentManager().beginTransaction().remove(_localisationFragment).commit();
            _localisationFragment=null;
        }
        super.onDestroy();
    }

    @Override
    public void isReceiving(boolean indicator) {
        if(indicator)
            _btn_start_stop.setText("Stop");
        else
            _btn_start_stop.setText("Start");
    }

    @Override
    public void updateLongitude(float longitude) {
        _txt_longitude.setText(Float.toString(longitude));
    }

    @Override
    public void updateLatitude(float latitude) {
        _txt_latitude.setText(Float.toString(latitude));
    }

    @Override
    public void updateAltitude(float altitude) {
        _txt_altitude.setText(Float.toString(altitude));
    }

    private void switchLocalisationActivity() {
        _localisationFragment.toggleReceiver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == GeoLocationFragment.GPS_REQUEST_PERMISSION_CODE) {
            _localisationFragment.handleGPSpermissionRequest(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
