package com.vetrack.vetrack;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.vetrack.vetrack.Service.Network;

public class AuthoritiesActivity extends AppCompatActivity {
    Button confirmBtn, cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Network network = Network.getInstance();
        setContentView(R.layout.activity_authorities);
        confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                network.setPrivacyState(0);
                Intent localIntent = new Intent(AuthoritiesActivity.this, OutsideMap.class);//你要转向的Activity
                startActivity(localIntent);
                AuthoritiesActivity.this.finish();


            }
        });

        cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                network.setPrivacyState(1);
                Intent localIntent = new Intent(AuthoritiesActivity.this, OutsideMap.class);//你要转向的Activity
                startActivity(localIntent);
                AuthoritiesActivity.this.finish();

            }
        });
    }
}