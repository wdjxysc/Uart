package com.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.parse.Cmd;
import com.example.parse.Const;
import com.example.parse.RfCmd;
import com.example.thirdclass.svprogresshud.SVProgressHUD;
import com.example.uart.R;
import com.example.uartdemo.SerialPortTool;

import java.io.IOException;
import java.util.HashMap;

public class SetHandleRfNetIDActivity extends Activity {

    Button writeBtn;
    EditText netidEditText;

    SerialPortTool serialPortTool;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_net_id);

        writeBtn = (Button)findViewById(R.id.writeBtn);
        netidEditText = (EditText)findViewById(R.id.netidEditText);

        try {
            serialPortTool = new SerialPortTool(SerialPortTool.HANDLE_COMM_PORT,SerialPortTool.HANDLE_COMM_PORT_BR);
            serialPortTool.openPort(SerialPortTool.PowerLevel.POWER_RFID);
        } catch (IOException e) {
            e.printStackTrace();
        }


        writeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showProgress();
                                writeBtn.setEnabled(false);
                            }
                        });


                        HashMap<String, Object> map = new HashMap<String, Object>();
                        try {
                            map.put(Cmd.KEY_NET_ID, Long.parseLong(netidEditText.getText().toString()));
                            map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_SET_HANDLE_RF_PARAM);
                            map.put(Cmd.key_handle_rf_baudrate, 4);
                            map.put(Cmd.key_handle_rf_factor, 11);
                            map.put(Cmd.key_handle_rf_bw, 7);
                            map.put(Cmd.key_handle_rf_new_nodeid, 0);
                            map.put(Cmd.key_handle_rf_new_netid, Long.parseLong(netidEditText.getText().toString()));
                            map.put(Cmd.key_handle_rf_power, 7);
                            map.put(Cmd.key_handle_rf_breath, 2);
                        } catch (Exception ex) {
                            Log.i("wdj", ex.getMessage());
                        }

                        byte[] cmd = RfCmd.AssembleRFCmd(map);
                        boolean b = false;
                        try {
                            byte[] revdata = serialPortTool.sendAndRevData(cmd, 5000);
                            if (revdata == null) {
                                b = false;
                            } else {
                                int sss = revdata[18] & 0xff;
                                int ddd = Integer.parseInt(netidEditText.getText().toString());

                                Log.i("wdj", "   " + sss + "   " + ddd);
                                if (sss == ddd) {
                                    b = true;
                                } else {
                                    b = false;
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
                                Toast.makeText(getApplication(), finalB ?"设置成功":"设置失败", Toast.LENGTH_SHORT).show();
                                dismissWithStatus(finalB);
                                writeBtn.setEnabled(true);
                            }
                        });

                    }
                }).start();
            }
        });
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
