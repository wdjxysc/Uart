package com.example.uart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class LoginActivity extends Activity {
    private Button cmdBtn;
    private Button cmdBtn2;
    private Button secondBtn;
    private Button thirdBtn;
    private Button updateAtyBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        cmdBtn = (Button)findViewById(R.id.cmdBtn);
        cmdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClassName(getApplicationContext(), "com.example.uart.MainActivity");
                startActivity(intent);
            }
        });

        cmdBtn2 = (Button)findViewById(R.id.cmdBtn2);
        cmdBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClassName(getApplicationContext(), "com.example.uart.Main2Activity");
                startActivity(intent);
            }
        });

        secondBtn = (Button)findViewById(R.id.secondBtn);
        secondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClassName(getApplicationContext(),"com.example.uart.SecondActivity");
                startActivity(intent);
            }
        });

        thirdBtn = (Button)findViewById(R.id.thirdBtn);
        thirdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClassName(getApplicationContext(), "com.example.uart.ThirdActivity");
                startActivity(intent);
            }
        });

        updateAtyBtn = (Button)findViewById(R.id.updateAtyBtn);
        updateAtyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, UpdateActivity.class);
                startActivity(intent);
            }
        });


        test();
    }

    private void test()
    {
        String value = "0000000000";
        String lastValue = "";
        for (int i = 4; i > -1; i--) {
            lastValue += value.substring(i * 2, (i + 1) * 2);
        }

        double data = Double.parseDouble(lastValue) / 10;

//        int crc = CRC16.calcCrc16(new byte[]{0x45,0x78,(byte)0xf7,(byte)0xd8,0x56});
//        System.out.println(String.format("0x%04x", crc));
//        String str1 = String.format("0x%04x", crc);

//        HashMap<String,String> map = new HashMap<String, String>();
//        map.put("netid","07000005");
//        GetUpdateTxtCmdList(map);
    }
}
