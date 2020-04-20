package com.example.billythepollingdisplay;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.altodypolly.PortalAlt;


import java.lang.ref.WeakReference;
import java.util.Set;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*display = (TextView) findViewById(R.id.textView1);
        editText = (EditText) findViewById(R.id.editText1);*/
        Button sendButton = (Button) findViewById(R.id.btnMain);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String data = editText.getText().toString();

            }
        });

        PortalAlt portal = new PortalAlt(this,"text",1659);
        portal.main();
    }
}