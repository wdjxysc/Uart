package com.example.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.parse.Cmd;
import com.example.parse.Const;
import com.example.parse.ParseData;
import com.example.parse.RfCmd;
import com.example.thirdclass.svprogresshud.SVProgressHUD;
import com.example.uart.R;
import com.example.uartdemo.SerialPortTool;
import com.example.uartdemo.StringTool;

import java.io.IOException;
import java.util.HashMap;

public class SetMeterRfParamActivity extends Activity {

    EditText oldAddressIdEditText;
    EditText oldNodeIdEditText;

    Button writeBtn;
    EditText netidEditText;
    Spinner brSpinner;
    EditText frequencyEditText;
    Spinner rffactorSpinner;
    Spinner bwSpinner;
    EditText nodeidEditText;
    Spinner powerSpinner;
    Spinner breathSpinner;

    SerialPortTool serialPortTool;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_meter_rf_param);

        openPort();

        init();
    }

    private void openPort() {
        try {
            serialPortTool = new SerialPortTool(SerialPortTool.HANDLE_COMM_PORT,SerialPortTool.HANDLE_COMM_PORT_BR);
            serialPortTool.openPort(SerialPortTool.PowerLevel.POWER_RFID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        oldAddressIdEditText = (EditText) findViewById(R.id.oldAddressIdEditText);
//        oldNodeIdEditText = (EditText)findViewById(R.id.oldNodeIdEditText);

        writeBtn = (Button) findViewById(R.id.writeBtn);
        netidEditText = (EditText) findViewById(R.id.netidEditText);
        brSpinner = (Spinner) findViewById(R.id.brSpinner);
        frequencyEditText = (EditText) findViewById(R.id.frequencyEditText);
        rffactorSpinner = (Spinner) findViewById(R.id.rffactorSpinner);
        bwSpinner = (Spinner) findViewById(R.id.bwSpinner);
//        nodeidEditText = (EditText)findViewById(R.id.nodeidEditText);
        powerSpinner = (Spinner) findViewById(R.id.powerSpinner);
        breathSpinner = (Spinner) findViewById(R.id.breathSpinner);

        brSpinner.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                this.getResources().getStringArray(R.array.rfCommBaudRateArray)));
        brSpinner.setSelection(3);

        rffactorSpinner.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                this.getResources().getStringArray(R.array.rfFactorArray)));
        rffactorSpinner.setSelection(4);
        bwSpinner.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                this.getResources().getStringArray(R.array.rfBwArray)));
        bwSpinner.setSelection(1);
        powerSpinner.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                this.getResources().getStringArray(R.array.rfPowerArray)));
        powerSpinner.setSelection(6);
        breathSpinner.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                this.getResources().getStringArray(R.array.rfBreathArray)));
        breathSpinner.setSelection(2);
        writeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                writeBtn.setEnabled(false);
                                showProgress();
                            }
                        });


                        HashMap map = getParams();

                        byte[] cmd = Cmd.AssembleCmd(map);

                        byte[] revdata;

                        boolean b = false;
                        try {
                            revdata = serialPortTool.sendAndRevData(cmd, 15000);
                            if (revdata != null) {
                                HashMap<String, Object> resultmap = ParseData.ParseDataToMap(revdata);
                                if (resultmap.containsKey(Cmd.KEY_SUCCESS)) {
                                    if ((Integer) resultmap.get(Cmd.KEY_SUCCESS) == 1) {
                                        b = true;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        final boolean finalB = b;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplication(), finalB?"设置成功":"设置失败", Toast.LENGTH_SHORT).show();
                                dismissWithStatus(finalB);
                                writeBtn.setEnabled(true);
                            }
                        });
                    }
                }).start();

            }
        });
    }


    private HashMap<String,Object> getParams() {
        HashMap<String, Object> map = new HashMap<String, Object>();

        String addressidstr = oldAddressIdEditText.getText().toString();
        map.put(Cmd.KEY_METER_ID, addressidstr);
        map.put(Cmd.KEY_RELAY_ID,Const.rf_relay_id);
        map.put(Cmd.KEY_NODE_ID, Long.parseLong("FFFFFFFF", 16));
        map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_SET_METER_RF_PARAM);


        int baudrate = brSpinner.getSelectedItemPosition() + 1; //等级
        map.put(Cmd.key_meter_rf_baudrate, baudrate);

        double frequency = Double.valueOf(frequencyEditText.getText().toString());
        int frequencyint = (int) (frequency * 1000000 / 61.035);
        map.put(Cmd.key_meter_rf_frequency, frequencyint);

        int rffactor = rffactorSpinner.getSelectedItemPosition() + 7;
        map.put(Cmd.key_meter_rf_factor, rffactor);

        int bw = bwSpinner.getSelectedItemPosition() + 6;
        map.put(Cmd.key_meter_rf_bw, bw);

        map.put(Cmd.key_meter_rf_new_nodeid, "00" + addressidstr.substring(addressidstr.length() - 6, addressidstr.length()));

        int netid = Integer.parseInt(netidEditText.getText().toString(), 16);
        map.put(Cmd.key_meter_rf_new_netid, netid);

        int power = powerSpinner.getSelectedItemPosition() + 1;
        map.put(Cmd.key_meter_rf_power, power);

        int breath = breathSpinner.getSelectedItemPosition();
        map.put(Cmd.key_meter_rf_breath, breath);

        return map;
    }

    @Override
    protected void onDestroy() {
        if(serialPortTool!= null){
            serialPortTool.closePort(SerialPortTool.PowerLevel.POWER_RFID);
            serialPortTool = null;
        }
        super.onDestroy();
    }


    private void showProgress(){
        SVProgressHUD.show(this);
    }

    private void dismissWithStatus(boolean b){
        if(b){
            SVProgressHUD.showSuccessWithStatus(this, "设置成功");
        }else {
            SVProgressHUD.showErrorWithStatus(this, "设置失败");
        }
    }
}
