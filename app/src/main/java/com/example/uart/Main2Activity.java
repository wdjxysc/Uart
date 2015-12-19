package com.example.uart;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.parse.Cmd;
import com.example.parse.Const;
import com.example.uartdemo.SerialPort;
import com.example.uartdemo.StringTool;
import com.example.uartdemo.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Main2Activity extends Activity implements View.OnClickListener {

    /**
     * UI  *
     */
    private EditText editRecv;
    private AutoCompleteTextView editSend;

    private Spinner cmdSpinner;
    private CheckBox checkRecv;
    private CheckBox checkSend;
    private Button buttonOpen;
    private Button buttonSend;
    private Button buttonClear;
    private Button refreshBtn;

    private ArrayList<HashMap> listCmdMap = new ArrayList<HashMap>();
    private List<String> listCmd = new ArrayList<String>();

    /**
     * serialport *
     */
    private SerialPort mSerialPort;
    private InputStream is;
    private OutputStream os;

    private int port = 13;
    private int buad = 9600;
    private String powerStr = "rfid power";

    /**
     * recv Thread *
     */
    private RecvThread recvThread;

    private boolean isHexRecv = false;
    private boolean isHexSend = false;
    private boolean isOpen = false;

    private Context context;


    private EditText meterIdEditText;
    private EditText meterValueEditText;
    private EditText meterStateEditText;
    private EditText meterNewAddressEditText;
    private EditText meterOldAddressEditText;
    private EditText jiesuanriEditText;
    private EditText chaobiaoriEditText;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        /** 初始化cmdMap**/
        listCmdMap = ConstData.getCmdList2();

        context = this;
        initView();
        listener();

        testFunc();
    }

    private void testFunc() {

    }


    private void initView() {
        editRecv = (EditText) findViewById(R.id.editTextInfo);
        editSend = (AutoCompleteTextView) findViewById(R.id.editTextSend);
        initHistory("history", editSend);
        cmdSpinner = (Spinner) findViewById(R.id.cmdSpinner);
        checkRecv = (CheckBox) findViewById(R.id.checkBoxHexRecv);
        checkSend = (CheckBox) findViewById(R.id.checkBoxHexSend);
        buttonOpen = (Button) findViewById(R.id.buttonOpen);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonClear = (Button) findViewById(R.id.buttonClear);
        meterIdEditText = (EditText) findViewById(R.id.meterIdEditText);
        meterStateEditText = (EditText) findViewById(R.id.meterStateEditText);
        meterValueEditText = (EditText) findViewById(R.id.meterValueEditText);
        meterNewAddressEditText = (EditText) findViewById(R.id.meterNewAddressEditText);
        meterOldAddressEditText = (EditText) findViewById(R.id.meterOldAddressEditText);
        jiesuanriEditText = (EditText) findViewById(R.id.jieauanriEditText);
        chaobiaoriEditText = (EditText) findViewById(R.id.chaobiaoriEditText);

        refreshBtn = (Button) findViewById(R.id.refreshBtn);

        for (HashMap cmdmap : listCmdMap) {
            listCmd.add((String) cmdmap.get("cmdname"));
        }

        cmdSpinner.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item,
                listCmd));
        cmdSpinner.setPrompt("命令");//标题
    }

    /**
     * listen componet
     */
    private void listener() {

        cmdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String str = ((String) ((HashMap) listCmdMap.toArray()[i]).get("cmddata")).replaceAll(" ", "");
                String strname = ((String) ((HashMap) listCmdMap.toArray()[i]).get("cmdname")).replaceAll(" ", "");

                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(Cmd.KEY_METER_ID, meterOldAddressEditText.getText().toString());
                map.put(Cmd.KEY_RELAY_ID, Const.rf_relay_id);
                map.put(Cmd.KEY_NODE_ID, Long.parseLong(meterIdEditText.getText().toString(), 16));
                if (strname.equals("出厂启用")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_START_USE);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写结算日")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_JIESUANRI);
                    int jiesuanri = 1;
                    try {
                        jiesuanri = Integer.valueOf(jiesuanriEditText.getText().toString());
                        if (jiesuanri > 28) throw new Exception();
                    } catch (Exception ex) {
                        Toast.makeText(getApplication(), "数据错误", Toast.LENGTH_SHORT).show();
                    }
                    map.put(Cmd.KEY_VALUE, jiesuanri);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写抄表日")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_CHAOBIAORI);
                    int chaobiaori = 1;
                    try {
                        chaobiaori = Integer.valueOf(chaobiaoriEditText.getText().toString());
                        if (chaobiaori > 28) throw new Exception();
                    } catch (Exception ex) {
                        Toast.makeText(getApplication(), "数据错误", Toast.LENGTH_SHORT).show();
                    }
                    map.put(Cmd.KEY_VALUE, chaobiaori);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("读结算日")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_JIESUANRI);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("读抄表日")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_CHAOBIAORI);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写表状态")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_METER_STATE);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("开阀")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_VALVE);
                    map.put(Cmd.KEY_VALUE, "1");
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("关阀")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_VALVE);
                    map.put(Cmd.KEY_VALUE, "0");
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("抄表")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_DATA);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写表地址")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_ADDRESS);
                    map.put(Cmd.KEY_VALUE, meterNewAddressEditText.getText().toString());
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写表底数")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_DATA);
                    String datastr = meterValueEditText.getText() + "";
                    map.put(Cmd.KEY_VALUE, Double.parseDouble(datastr) + "");
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("读标准时间")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_TIME);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写标准时间")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_TIME);
                    map.put(Cmd.KEY_VALUE, new Date());
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else {
                    String lastStr = meterIdEditText.getText() + str.substring(4, str.length());
                    editSend.setText(lastStr);

                    //表地址
                    String oldaddress = meterOldAddressEditText.getText() + "";
                    String oldaddr = "";
                    for (int k = 6; k > -1; k--) {
                        oldaddr += oldaddress.substring(k * 2, (k + 1) * 2);
                    }

                    //若不是广播命令 则指明表地址
                    if (!strname.equals("广播读表地址")) {
                        lastStr = lastStr.substring(0, 14) + oldaddr + lastStr.substring(28, lastStr.length());
                    }

                    //计算和校验
                    String datastr = lastStr.substring(10, lastStr.length() - 4);
                    byte cs = Tools.CheckSum(Tools.HexString2Bytes(datastr));
                    int csint = cs & 0xff;
                    lastStr = lastStr.substring(0, lastStr.length() - 4) + StringTool.padLeft(Integer.toHexString(csint), 2, '0').toUpperCase() + lastStr.substring(lastStr.length() - 2, lastStr.length());

                    lastStr = "AA" + lastStr;
                    editSend.setText(lastStr);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        checkRecv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHexRecv = isChecked;

            }
        });
        checkSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHexSend = isChecked;

            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String str = ((String) ((HashMap) listCmdMap.toArray()[cmdSpinner.getSelectedItemPosition()]).get("cmddata")).replaceAll(" ", "");
                String strname = ((String) ((HashMap) listCmdMap.toArray()[cmdSpinner.getSelectedItemPosition()]).get("cmdname")).replaceAll(" ", "");

                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(Cmd.KEY_METER_ID, meterOldAddressEditText.getText().toString());
                map.put(Cmd.KEY_RELAY_ID, Const.rf_relay_id);
                map.put(Cmd.KEY_NODE_ID, Long.parseLong(meterIdEditText.getText().toString(), 16));

                if (strname.equals("出厂启用")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_START_USE);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写结算日")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_JIESUANRI);
                    int jiesuanri = 1;
                    try {
                        jiesuanri = Integer.valueOf(jiesuanriEditText.getText().toString());
                        if (jiesuanri > 28) throw new Exception();
                    } catch (Exception ex) {
                        Toast.makeText(getApplication(), "数据错误", Toast.LENGTH_SHORT).show();
                    }
                    map.put(Cmd.KEY_VALUE, jiesuanri);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写抄表日")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_CHAOBIAORI);
                    int chaobiaori = 1;
                    try {
                        chaobiaori = Integer.valueOf(chaobiaoriEditText.getText().toString());
                        if (chaobiaori > 28) throw new Exception();
                    } catch (Exception ex) {
                        Toast.makeText(getApplication(), "数据错误", Toast.LENGTH_SHORT).show();
                    }
                    map.put(Cmd.KEY_VALUE, chaobiaori);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("读结算日")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_JIESUANRI);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("读抄表日")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_CHAOBIAORI);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写表状态")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_METER_STATE);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("开阀")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_VALVE);
                    map.put(Cmd.KEY_VALUE, "1");
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("关阀")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_VALVE);
                    map.put(Cmd.KEY_VALUE, "0");
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("抄表")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_DATA);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写表地址")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_ADDRESS);
                    map.put(Cmd.KEY_VALUE, meterNewAddressEditText.getText().toString());
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写表底数")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_DATA);
                    String datastr = meterValueEditText.getText() + "";
                    map.put(Cmd.KEY_VALUE, Double.parseDouble(datastr) + "");
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("读标准时间")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_TIME);
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else if (strname.equals("写标准时间")) {
                    map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_TIME);
                    map.put(Cmd.KEY_VALUE, new Date());
                    byte[] senddata = Cmd.AssembleCmd(map);
                    if (senddata != null)
                        editSend.setText(Tools.Bytes2HexString(senddata, senddata.length));
                } else {
                    String lastStr = meterIdEditText.getText() + str.substring(4, str.length());
                    editSend.setText(lastStr);

                    //表地址
                    String oldaddress = meterOldAddressEditText.getText() + "";
                    String oldaddr = "";
                    for (int k = 6; k > -1; k--) {
                        oldaddr += oldaddress.substring(k * 2, (k + 1) * 2);
                    }

                    //若不是广播命令 则指明表地址
                    if (!strname.equals("广播读表地址")) {
                        lastStr = lastStr.substring(0, 14) + oldaddr + lastStr.substring(28, lastStr.length());
                    }

                    //计算和校验
                    String datastr = lastStr.substring(10, lastStr.length() - 4);
                    byte cs = Tools.CheckSum(Tools.HexString2Bytes(datastr));
                    int csint = cs & 0xff;
                    lastStr = lastStr.substring(0, lastStr.length() - 4) + StringTool.padLeft(Integer.toHexString(csint), 2, '0').toUpperCase() + lastStr.substring(lastStr.length() - 2, lastStr.length());

                    lastStr = "AA" + lastStr;
                    editSend.setText(lastStr);
                }
            }
        });


        buttonOpen.setOnClickListener(this);
        buttonSend.setOnClickListener(this);
        buttonClear.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        this.close();
        super.onDestroy();
    }

    //open serialport
    private void open() {
        //open
        try {
            mSerialPort = new SerialPort(port, buad, 0);
        } catch (Exception e) {

//			Toast.makeText(this, "SerialPort init fail!!", 0).show();
            return;
        }
        is = mSerialPort.getInputStream();
        os = mSerialPort.getOutputStream();
        if ("3.3V".equals(powerStr)) {
            mSerialPort.power3v3on();
        } else if ("5V".equals(powerStr)) {
            mSerialPort.power_5Von();
        } else if ("scan power".equals(powerStr)) {
            mSerialPort.scaner_poweron();
        } else if ("psam power".equals(powerStr)) {
            mSerialPort.psam_poweron();
        } else if ("rfid power".equals(powerStr)) {
            mSerialPort.rfid_poweron();
        }

        recvThread = new RecvThread();
        recvThread.start();
        isOpen = true;
        buttonOpen.setText(context.getResources().getString(R.string.close));
    }

    //close serialport
    private void close() {
        if (recvThread != null) {
            recvThread.interrupt();
        }
        if (mSerialPort != null) {

            if ("3.3V".equals(powerStr)) {
                mSerialPort.power3v3off();
            } else if ("5V".equals(powerStr)) {
                mSerialPort.power_5Voff();
            } else if ("scan power".equals(powerStr)) {
                mSerialPort.scaner_poweroff();
            } else if ("psam power".equals(powerStr)) {
                mSerialPort.psam_poweroff();
            } else if ("rfid power".equals(powerStr)) {
                mSerialPort.rfid_poweroff();
            }
            try {
                is.close();
                os.close();
            } catch (IOException e) {
//				e.printStackTrace();
            }
            mSerialPort.close(port);
            isOpen = false;
            buttonOpen.setText(context.getResources().getString(R.string.open));
        }
    }

    //send cmd
    private void send() {
        byte[] cmd = null;
        String commandStr = editSend.getText().toString();
        Log.i("send()", commandStr);
        if (commandStr == null) {
//			Toast.makeText(context, "cmd is null", 0).show();
        }
        if (isHexSend) {
            cmd = Tools.HexString2Bytes(commandStr);
        } else {
            cmd = commandStr.getBytes();
        }
        try {
            os.write(cmd);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonOpen:
                if (!isOpen) {
                    this.open();
                } else {
                    this.close();
                }

                break;
            case R.id.buttonSend:
                if (isOpen) {
                    saveHistory("history", editSend);
                    send();
                } else {
//				Toast.makeText(context, "please open serialport", 0).show();
                }
                break;
            case R.id.buttonClear:
                editRecv.setText("");
                break;

            default:
                break;
        }

    }

    /**
     * recv thread receive serialport data
     *
     * @author Administrator
     */
    private class RecvThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (!isInterrupted()) {
                    int size = 0;
                    byte[] buffer = new byte[1024];
                    if (is == null) {
                        return;
                    }

                    if (is.available() > 0) {
                        Thread.sleep(100);
                        size = is.read(buffer);
                        onDataReceived(buffer, size);
                    }
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * add recv data on UI
     *
     * @param buffer
     * @param size
     */
    private void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (isHexRecv) {
                    String recv = Tools.Bytes2HexString(buffer, size);
                    editRecv.append("[Recv(HEX)]:" + recv + "\n");

                    if (size < 10) return;


                    //去掉数据头获取数据主体部分
                    int rf_relay_id_length = 0;
                    switch (Const.rf_transmisson_type) {
                        case RF_TRANSMISSON_TYPE_NO_RELAY:
                            rf_relay_id_length = 0;
                            break;
                        case RF_TRANSMISSON_TYPE_RELAY:
                            rf_relay_id_length = Const.RF_RELAY_ID_LENGTH;
                            break;
                    }

                    int head_length = (Const.IN_HEAD_LENGTH_4_BYTES + rf_relay_id_length) * 2;
                    switch (Const.rf_type) {
                        case RF_TYPE_NODEID_2_BYTES:
                            head_length = (Const.IN_HEAD_LENGTH_2_BYTES + rf_relay_id_length) * 2;
                            break;
                        case RF_TYPE_NODEID_4_BYTES:
                            head_length = (Const.IN_HEAD_LENGTH_4_BYTES + rf_relay_id_length) * 2;
                            break;
                    }

                    String recvreal = recv.substring(head_length, recv.length());

                    if (size >= 10 && recvreal.substring(22, 26).equals("17A0")) {
                        String value = recvreal.substring(30, 32);
                        int valueint = Integer.parseInt(value, 16);

                        String state = value;

                        if ((valueint & 0x03) == 0x00) {
                            state += "开阀";
                        } else if ((valueint & 0x03) == 0x01) {
                            state += "关阀";
                        } else if ((valueint & 0x03) == 0x03) {
                            state += "异常";
                        }

                        if ((valueint & 0x08) == 0x08) {
                            state += "漏气";
                        }
                        if ((valueint & 0x10) == 0x10) {
                            state += "6V电压低";
                        }
                        if ((valueint & 0x20) == 0x20) {
                            state += "3.6V电压低";
                        }
                        if ((valueint & 0x40) == 0x40) {
                            state += "干扰";
                        }
                        if ((valueint & 0x80) == 0x80) {
                            state += "坏";
                        }
                        meterStateEditText.setText(state);
                    }

                    if (size >= 24 && recvreal.substring(22, 26).equals("1F90")) {
                        String value = recvreal.substring(28, 38);
                        String lastValue = "";
                        for (int i = 4; i > -1; i--) {
                            lastValue += value.substring(i * 2, (i + 1) * 2);
                        }

                        double data = Double.parseDouble(lastValue) / 10;
                        meterValueEditText.setText(data + "");

                        String statevalue = recvreal.substring(recvreal.length() - 8, recvreal.length() - 6);
                        int valueint = Integer.parseInt(statevalue, 16);

                        String state = statevalue;

                        if ((valueint & 0x03) == 0x00) {
                            state += "开阀";
                        } else if ((valueint & 0x03) == 0x01) {
                            state += "关阀";
                        } else if ((valueint & 0x03) == 0x03) {
                            state += "异常";
                        }

                        if ((valueint & 0x08) == 0x08) {
                            state += "漏气";
                        }
                        if ((valueint & 0x10) == 0x10) {
                            state += "6V电压低";
                        }
                        if ((valueint & 0x20) == 0x20) {
                            state += "3.6V电压低";
                        }
                        if ((valueint & 0x40) == 0x40) {
                            state += "干扰";
                        }
                        if ((valueint & 0x80) == 0x80) {
                            state += "坏";
                        }
                        meterStateEditText.setText(state);

                    }

                    if (size >= 16 && recvreal.substring(22, 26).equals("55FF")) {

                    }

                    if (size >= 10 && recvreal.substring(22, 26).equals("1380")) {
                        String value = recvreal.substring(28, 30);
                        int vauleint = Integer.parseInt(value);
                        jiesuanriEditText.setText(String.format("%02d", vauleint));
                    }
                    if (size >= 10 && recvreal.substring(22, 26).equals("1480")) {
                        String value = recvreal.substring(28, 30);
                        int vauleint = Integer.parseInt(value);
                        chaobiaoriEditText.setText(String.format("%02d", vauleint));
                    }


                    try {
                        String datacsstr = recvreal.substring(0, recv.length() - 4);
                        int csrev = Integer.parseInt(recvreal.substring(recvreal.length() - 4, recvreal.length() - 2), 16);
                        byte cs = Tools.CheckSum(Tools.HexString2Bytes(datacsstr));
                        if ((cs & 0xff) != csrev) {
//                        editRecv.append("校验错误");
                        }
                    } catch (Exception ex) {

                    }
                } else {
                    String ssss = new String(buffer, 0, size);
                    editRecv.append("[Recv]:" + ssss + "\n");
                }


            }
        });
    }

    /**
     * ??????????????????????????????
     *
     * @param field
     * @param auto
     */
    private void initHistory(String field, AutoCompleteTextView auto) {
        SharedPreferences sp = getSharedPreferences("commad", 0);
        String longhistory = sp.getString("history", "nothing");
        String[] hisArrays = longhistory.split(",");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, hisArrays);
        //?????????????????????????????50????????????????
        if (hisArrays.length > 50) {
            String[] newArrays = new String[50];
            System.arraycopy(hisArrays, 0, newArrays, 0, 50);
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, newArrays);
        }
        auto.setAdapter(adapter);
        auto.setDropDownHeight(350);
        auto.setThreshold(1);
//        auto.setCompletionHint("???????????????5??????????????");
        auto.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                AutoCompleteTextView view = (AutoCompleteTextView) v;
                if (hasFocus) {
                    view.showDropDown();
                }
            }
        });

    }

    /**
     * ????????????????????????????
     *
     * @param field
     * @param auto
     */
    private void saveHistory(String field, AutoCompleteTextView auto) {
        String text = auto.getText().toString();
        SharedPreferences sp = getSharedPreferences("commad", 0);
        String longhistory = sp.getString(field, "nothing");
        if (!longhistory.contains(text + ",")) {
            StringBuilder sb = new StringBuilder(longhistory);
            sb.insert(0, text + ",");
            sp.edit().putString("history", sb.toString()).commit();
        }
    }
}
