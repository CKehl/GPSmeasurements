package de.ckehl.gpsmeasurements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import android.view.View;


/**
 * Created by christian on 10.07.16.
 */
public class AboutWindow extends PopupWindow {

    Button mExitButton = null;

    public AboutWindow(View v)
    {
        super(v, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        mExitButton = (Button)getContentView().findViewById(R.id.about_btn_exit);
        mExitButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
