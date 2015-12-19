package com.example.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.parse.Cmd;
import com.example.parse.Const;
import com.example.uart.R;
import com.example.uartdemo.SerialPortTool;
import com.example.uartdemo.Tools;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MeterTestActivity extends Activity {

    EditText meterIdEditText;
    EditText logEditText;
    Button button;

    Spinner timeSpinner;

    SerialPortTool serialPortTool;

    ArrayList<String> timeList = new ArrayList<String>();

    int time = 5;

    private android.os.Handler handler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_test);

        initMyView();
    }

    private void initMyView(){
        meterIdEditText = (EditText)findViewById(R.id.meterIdEditText);
        logEditText = (EditText)findViewById(R.id.logEditText);
        button = (Button)findViewById(R.id.button);
        timeSpinner = (Spinner)findViewById(R.id.timeSpinner);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startTimer();
            }
        });


        for (int i = 1;i<10;i++){
            timeList.add(i*5 + "");
        }

        timeSpinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                timeList));
        timeSpinner.setPrompt("间隔时间");//标题

        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str = timeList.get(i);
                time = Integer.valueOf(str);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void startTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                HashMap map = new HashMap<String, Object>();
                String addressidstr = meterIdEditText.getText().toString();
                if (addressidstr.length() != 14) return;
                map.put("addressid", addressidstr);
                map.put("netid", Integer.parseInt(addressidstr.substring(10, 14), 16));
                map.put("datatype", Const.DATAID_READ_DATA);

                final byte[] cmd = Cmd.AssembleCmd(map);

                if (cmd != null) {
                    if (cmd.length == 0) return;
                    try {
                        serialPortTool = new SerialPortTool(SerialPortTool.HANDLE_COMM_PORT, SerialPortTool.HANDLE_COMM_PORT_BR);
                        serialPortTool.openPort(SerialPortTool.PowerLevel.POWER_RFID);
                        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String datastr1 = df1.format(new Date());
                        final String sendstr = Tools.Bytes2HexString(cmd, cmd.length) + "    at:" + datastr1;
                        if (serialPortTool.isOpen) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    logEditText.append("\r\n" + "SEND:" + sendstr);
                                }
                            });

                            byte[] revdata = serialPortTool.sendAndRevData(cmd,15000);
                            if (revdata != null) {
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String datastr = df.format(new Date());
                                final String revstr = Tools.Bytes2HexString(revdata, revdata.length) + "    at:" + datastr;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        logEditText.append("\r\n" + "RECV:" + revstr);
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 0,  1000);
    }
}
