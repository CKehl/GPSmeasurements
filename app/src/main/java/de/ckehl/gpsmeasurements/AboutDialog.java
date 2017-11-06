package de.ckehl.gpsmeasurements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by christian on 10.07.16.
 */
public class AboutDialog extends DialogFragment {
    TextView mVersionText = null;
    Button mExitButton = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.about_layout, null));
        builder.setTitle("About");
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        mVersionText = (TextView) (getDialog().findViewById(R.id.aboutDlg_version_txt));
        String versionText = getString(R.string.version)+"."+getString(R.string.release)+":r"+getString(R.string.revision);
        mVersionText.setText("Version: "+versionText);
        mExitButton = (Button) (getDialog().findViewById(R.id.about_btn_exit));
        mExitButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
