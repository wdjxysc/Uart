package com.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.parse.Cmd;
import com.example.parse.Const;
import com.example.parse.MeterStateConst;
import com.example.parse.ParseData;
import com.example.thirdclass.svprogresshud.SVProgressHUD;
import com.example.uart.R;
import com.example.uartdemo.SerialPortTool;

import java.io.IOException;
import java.util.HashMap;

public class RLMeterSettingsActivity extends Activity {

    EditText oldAddressIdEditText;
    EditText newAddressIdEditText;
    EditText nodeIdEditText;
    EditText meterValueEditText;
    EditText valveStateEditText;
    EditText batteryStateEditText;
    EditText netidEditText;

    RadioButton valveStateOpenRadioBtn;
    RadioButton valveStateCloseRadioBtn;
    RadioButton valveStateErrorRadioBtn;

    Button readValueBtn;
    Button writeMeterIdBtn;
    Button writeMeterValueBtn;
    Button readMeterStateBtn;
    Button writeMeterStateBtn;
    Button writeMeterNetIdBtn;

    BtnClickListener listener;

    Handler handler = new Handler();

    /**
     * 发送的命令
     */
    byte[] cmd;

    /**
     * 串口工具
     */
    SerialPortTool serialPortTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rlmeter_settings);

        initView();

        openCommPort();
    }

    private void initView() {

        oldAddressIdEditText = (EditText) findViewById(R.id.oldAddressIdEditText);
        newAddressIdEditText = (EditText) findViewById(R.id.newAddressIdEditText);
        nodeIdEditText = (EditText) findViewById(R.id.nodeIdEditText);
        meterValueEditText = (EditText) findViewById(R.id.meterValueEditText);
        valveStateEditText = (EditText) findViewById(R.id.valveStateEditText);
        batteryStateEditText = (EditText) findViewById(R.id.batteryStateEditText);
        netidEditText = (EditText) findViewById(R.id.netidEditText);


        valveStateCloseRadioBtn = (RadioButton) findViewById(R.id.valveStateCloseRadioBtn);
        valveStateOpenRadioBtn = (RadioButton) findViewById(R.id.valveStateOpenRadioBtn);
        valveStateErrorRadioBtn = (RadioButton) findViewById(R.id.valveStateErrorRadioBtn);

        readValueBtn = (Button) findViewById(R.id.readValueBtn);
        writeMeterIdBtn = (Button) findViewById(R.id.writeMeterIdBtn);
        writeMeterValueBtn = (Button) findViewById(R.id.writeMeterValueBtn);
        readMeterStateBtn = (Button) findViewById(R.id.readMeterStateBtn);
        writeMeterStateBtn = (Button) findViewById(R.id.writeMeterStateBtn);
        writeMeterNetIdBtn = (Button) findViewById(R.id.writeMeterNetIdBtn);

        listener = new BtnClickListener();

        readValueBtn.setOnClickListener(listener);
        writeMeterIdBtn.setOnClickListener(listener);
        writeMeterValueBtn.setOnClickListener(listener);
        readMeterStateBtn.setOnClickListener(listener);
        writeMeterStateBtn.setOnClickListener(listener);
        writeMeterNetIdBtn.setOnClickListener(listener);
    }

    private class BtnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String meterAddressText = oldAddressIdEditText.getText().toString();
            if (meterAddressText.length() != 14) {
                Toast.makeText(getApplicationContext(), "气表地址应为 14 位BCD码", Toast.LENGTH_SHORT).show();
                return;
            }

            String newAddressId = newAddressIdEditText.getText().toString();
            if (newAddressId.length() != 14) {
                Toast.makeText(getApplicationContext(), "气表地址应为 14 位BCD码", Toast.LENGTH_SHORT).show();
                return;
            }


            String nodeIdText = nodeIdEditText.getText().toString();

            HashMap<String, Object> map = new HashMap<String, Object>();
            try {
                map.put(Cmd.KEY_NODE_ID, Long.parseLong(nodeIdText, 16));
                map.put(Cmd.KEY_METER_ID, meterAddressText);
                map.put(Cmd.KEY_RELAY_ID, Const.rf_relay_id);
            } catch (Exception ex) {
                Toast.makeText(getApplication(), "输入错误", Toast.LENGTH_SHORT).show();
                return;
            }

            final int viewid = view.getId();
            if (viewid == readValueBtn.getId()) {
                map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_DATA);
            } else if (viewid == writeMeterIdBtn.getId()) {
                map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_ADDRESS);
                map.put(Cmd.KEY_VALUE, newAddressId);
            } else if (viewid == writeMeterValueBtn.getId()) {
                map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_DATA);
                double value = Double.parseDouble(meterValueEditText.getText().toString());
                map.put(Cmd.KEY_VALUE, value + "");
            } else if (viewid == readMeterStateBtn.getId()) {
                map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_METER_STATE);
            } else if (viewid == writeMeterStateBtn.getId()) {
                //默认为正常关阀
                map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_METER_STATE);
            } else if (viewid == writeMeterNetIdBtn.getId()) {
                map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_SET_METER_RF_PARAM);

                map.put(Cmd.key_meter_rf_baudrate, 4);
                map.put(Cmd.key_meter_rf_factor, 11);
                map.put(Cmd.key_meter_rf_bw, 7);
                map.put(Cmd.key_meter_rf_frequency, 0x7A8CE1);
                map.put(Cmd.key_meter_rf_new_nodeid, "00000000");//此处写nodeid无用  表总是以表ID最后6位为nodeid
                int netid = Integer.parseInt(netidEditText.getText().toString(), 16);
                map.put(Cmd.key_meter_rf_new_netid, netid);
                map.put(Cmd.key_meter_rf_power, 7);
                map.put(Cmd.key_meter_rf_breath, 2);
            }

            cmd = Cmd.AssembleCmd(map);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    showSVProgressHUD();
                }
            });


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] revdata = serialPortTool.sendAndRevData(cmd, 15000);

                        if (revdata == null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    dismissSVProgressHUD();
                                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }

                        final HashMap resultmap = ParseData.ParseDataToMap(revdata);

                        if (resultmap.size() != 0) {
                            if (resultmap.get(Cmd.KEY_ERR_MESSAGE) != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), resultmap.get(Cmd.KEY_ERR_MESSAGE).toString(), Toast.LENGTH_SHORT).show();
                                        dismissSVProgressHUD();
                                    }
                                });
                            } else {
                                if (viewid == readValueBtn.getId()) {
                                    final String valuenowstr = resultmap.get(Cmd.KEY_VALUE_NOW).toString();
                                    final MeterStateConst.STATE_VALVE valvestate = (MeterStateConst.STATE_VALVE) resultmap.get(Cmd.KEY_VALVE_STATE);
                                    final String battarystatestr = "3.6V:" + resultmap.get(Cmd.KEY_BATTERY_3_6_STATE).toString() + "|6V:" + resultmap.get(Cmd.KEY_BATTERY_6_STATE).toString();

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            meterValueEditText.setText(valuenowstr);
                                            switch (valvestate) {
                                                case OPEN:
                                                    valveStateOpenRadioBtn.setChecked(true);
                                                    break;
                                                case CLOSE:
                                                    valveStateCloseRadioBtn.setChecked(true);
                                                    break;
                                                case ERROR:
                                                    valveStateErrorRadioBtn.setChecked(true);
                                                    break;
                                            }

                                            batteryStateEditText.setText(battarystatestr);
                                            dismissSVProgressHUD();
                                        }
                                    });
                                } else if (viewid == writeMeterIdBtn.getId()) {

                                } else if (viewid == writeMeterValueBtn.getId()) {

                                } else if (viewid == readMeterStateBtn.getId()) {
                                    final MeterStateConst.STATE_VALVE valvestate = (MeterStateConst.STATE_VALVE) resultmap.get(Cmd.KEY_VALVE_STATE);
                                    final String battarystatestr = "3.6V:" + resultmap.get(Cmd.KEY_BATTERY_3_6_STATE).toString() + "|6V:" + resultmap.get(Cmd.KEY_BATTERY_6_STATE).toString();

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (valvestate) {
                                                case OPEN:
                                                    valveStateOpenRadioBtn.setChecked(true);
                                                    break;
                                                case CLOSE:
                                                    valveStateCloseRadioBtn.setChecked(true);
                                                    break;
                                                case ERROR:
                                                    valveStateErrorRadioBtn.setChecked(true);
                                                    break;
                                            }
                                            batteryStateEditText.setText(battarystatestr);
                                            dismissSVProgressHUD();
                                        }
                                    });
                                } else if (viewid == writeMeterStateBtn.getId()) {

                                } else if (viewid == writeMeterNetIdBtn.getId()) {

                                }


                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String result = resultmap.get(Cmd.KEY_RESULT).toString();
                                        if (resultmap.get(Cmd.KEY_RESULT).toString().equals("1")) {
                                            Toast.makeText(getApplication(), "成功", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplication(), "失败", Toast.LENGTH_SHORT).show();
                                        }
                                        dismissSVProgressHUD();
                                    }
                                });
                            }
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "map.size=0", Toast.LENGTH_SHORT).show();
                                    dismissSVProgressHUD();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissSVProgressHUD();
                        }
                    });
                }
            }).start();
        }
    }

    private void showSVProgressHUD() {
        SVProgressHUD.show(this);
    }

    private void dismissSVProgressHUD() {
        SVProgressHUD.dismiss(this);
    }

    /**
     * 打开串口
     */
    private void openCommPort() {
        try {
            serialPortTool = new SerialPortTool(SerialPortTool.HANDLE_COMM_PORT, SerialPortTool.HANDLE_COMM_PORT_BR);
            serialPortTool.openPort(SerialPortTool.PowerLevel.POWER_RFID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (serialPortTool != null) {
            serialPortTool.closePort(SerialPortTool.PowerLevel.POWER_RFID);
            serialPortTool = null;
        }
        super.onDestroy();
    }
}
