package com.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.example.parse.Cmd;
import com.example.parse.Const;
import com.example.parse.ParseData;
import com.example.thirdclass.svprogresshud.SVProgressHUD;
import com.example.uart.R;
import com.example.uartdemo.SerialPortTool;
import com.example.uartdemo.StringTool;

import java.io.IOException;
import java.util.HashMap;

public class RLMeterTestActivity extends Activity {

    EditText meterAddressEditText;
    EditText netAddressEditText;
    EditText meterValueEditText;
    EditText valveStateEditText;
    EditText batteryStateEditText;

    RadioButton valveStateOpenRadioBtn;
    RadioButton valveStateCloseRadioBtn;

    Button minusBtn;
    Button addBtn;

    Button readValueBtn;
    Button openValveBtn;
    Button closeValveBtn;
    Button clearBtn;

    /**
     * 按钮OnClickListener
     */
    View.OnClickListener listener;


    /**
     * 发送的命令
     */
    byte[] cmd;

    /**
     * 串口工具
     */
    SerialPortTool serialPortTool;

    Handler handler = new Handler();

    int timeout = 15000;//skyshoot模块 中继模式下时间比较长 设为26s

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rlmeter_test);

        initView();

        openCommPort();
    }

    /**
     * 打开串口
     */
    private void openCommPort() {
        try {
            serialPortTool = new SerialPortTool(SerialPortTool.HANDLE_COMM_PORT,SerialPortTool.HANDLE_COMM_PORT_BR);
            serialPortTool.openPort(SerialPortTool.PowerLevel.POWER_RFID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化界面 并进行绑定
     */
    private void initView() {
        meterAddressEditText = (EditText) findViewById(R.id.meterAddressEditText);
        netAddressEditText = (EditText) findViewById(R.id.netAddressEditText);
        meterValueEditText = (EditText) findViewById(R.id.meterValueEditText);
        valveStateEditText = (EditText) findViewById(R.id.valveStateEditText);
        batteryStateEditText = (EditText) findViewById(R.id.batteryStateEditText);

        minusBtn = (Button) findViewById(R.id.minusBtn);
        addBtn = (Button) findViewById(R.id.addBtn);

        readValueBtn = (Button) findViewById(R.id.readValueBtn);
        openValveBtn = (Button) findViewById(R.id.openValveBtn);
        closeValveBtn = (Button) findViewById(R.id.closeValveBtn);
        clearBtn = (Button)findViewById(R.id.clearBtn);

        listener = new ClickListener();

        readValueBtn.setOnClickListener(listener);
        openValveBtn.setOnClickListener(listener);
        closeValveBtn.setOnClickListener(listener);
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (meterAddressEditText.getText().toString().length() != 14) return;
                try {
                    long i = Long.parseLong(meterAddressEditText.getText().toString().substring(8));
                    if (i > 0) {
                        i--;
                        String str = meterAddressEditText.getText().toString().substring(0, 8)+ StringTool.padLeft(i + "", 6, '0');
                        meterAddressEditText.setText(str);
                        netAddressEditText.setText(StringTool.padLeft(i + "", 6, '0'));
                    }
                } catch (Exception ex) {
                    Toast.makeText(getApplication(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (meterAddressEditText.getText().toString().length() != 14) return;
                try {
                    long i = Long.parseLong(meterAddressEditText.getText().toString().substring(8));
                    if (i < 999999) {
                        i++;
                        String str = meterAddressEditText.getText().toString().substring(0, 8) + StringTool.padLeft(i + "", 6, '0');
                        meterAddressEditText.setText(str);
                        netAddressEditText.setText(StringTool.padLeft(i + "", 6, '0'));
                    }
                } catch (Exception ex) {
                    Toast.makeText(getApplication(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        meterAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (meterAddressEditText.getText().length() == 14) {
                    netAddressEditText.setText(meterAddressEditText.getText().toString().substring(8));
                }
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                meterValueEditText.setText("");
                valveStateEditText.setText("");
                batteryStateEditText.setText("");
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

    private void showSVProgressHUD(){
        SVProgressHUD.show(this);
    }
    private void dismissSVProgressHUD(){
        SVProgressHUD.dismiss(this);
    }


    public class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String meterAddressText = meterAddressEditText.getText().toString();
            if (meterAddressText.length() != 14) {
                Toast.makeText(getApplicationContext(), "气表地址应为 14 位BCD码", Toast.LENGTH_SHORT).show();
                return;
            }

            String netAddressText = netAddressEditText.getText().toString();

            HashMap<String, Object> map = new HashMap<String, Object>();
            try {
                map.put(Cmd.KEY_NODE_ID, Long.parseLong(netAddressText, 16));
                map.put(Cmd.KEY_METER_ID, meterAddressText);
                map.put(Cmd.KEY_RELAY_ID,Const.rf_relay_id);
            }catch (Exception ex){
                Toast.makeText(getApplication(),"输入错误",Toast.LENGTH_SHORT).show();
                return;
            }

            final int viewid = view.getId();
            if (viewid == readValueBtn.getId()) {
                Log.i("wdj", "readValueBtn Clicked!");
                map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_DATA);

            } else if (viewid == openValveBtn.getId()) {
                Log.i("wdj", "openValveBtn Clicked!");
                map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_VALVE);
                map.put(Cmd.KEY_VALUE, "1");
            } else if (viewid == closeValveBtn.getId()) {
                Log.i("wdj", "closeValveBtn Clicked!");
                map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_VALVE);
                map.put(Cmd.KEY_VALUE, "0");
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
                        switch (Const.rf_transmisson_type){
                            case RF_TRANSMISSON_TYPE_NO_RELAY:
                                timeout = 15000;
                                break;
                            case RF_TRANSMISSON_TYPE_RELAY:
                                timeout = 26000;
                                break;
                        }

                        byte[] revdata = serialPortTool.sendAndRevData(cmd, timeout);

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
                                    Log.i("wdj", "readValueBtn Clicked!");
                                    final String valuenowstr = resultmap.get(Cmd.KEY_VALUE_NOW).toString();
                                    final String valvestatestr = resultmap.get(Cmd.KEY_VALVE_STATE).toString();
                                    final String battarystatestr = "3.6V:" + resultmap.get(Cmd.KEY_BATTERY_3_6_STATE).toString() + "|6V:" + resultmap.get(Cmd.KEY_BATTERY_6_STATE).toString();

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            meterValueEditText.setText(valuenowstr);
                                            valveStateEditText.setText(valvestatestr);
                                            batteryStateEditText.setText(battarystatestr);
                                        }
                                    });
                                } else if (viewid == openValveBtn.getId()) {
                                    Log.i("wdj", "openValveBtn Clicked!");
                                    final String valvestatestr = resultmap.get(Cmd.KEY_VALVE_STATE).toString();
                                    final String battarystatestr = "3.6V:" + resultmap.get(Cmd.KEY_BATTERY_3_6_STATE).toString() + "|6V:" + resultmap.get(Cmd.KEY_BATTERY_6_STATE).toString();

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            valveStateEditText.setText(valvestatestr);
                                            batteryStateEditText.setText(battarystatestr);
                                        }
                                    });
                                } else if (viewid == closeValveBtn.getId()) {
                                    Log.i("wdj", "closeValveBtn Clicked!");
                                    final String valvestatestr = resultmap.get(Cmd.KEY_VALVE_STATE).toString();
                                    final String battarystatestr = "3.6V:" + resultmap.get(Cmd.KEY_BATTERY_3_6_STATE).toString() + "|6V:" + resultmap.get(Cmd.KEY_BATTERY_6_STATE).toString();

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            valveStateEditText.setText(valvestatestr);
                                            batteryStateEditText.setText(battarystatestr);
                                        }
                                    });
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String result = resultmap.get(Cmd.KEY_RESULT).toString();
                                        if (result.equals("1")) {
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
}

