package com.example.lierda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
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
import com.example.parse.ParseData;
import com.example.uart.R;
import com.example.uartdemo.SerialPort;
import com.example.uartdemo.StringTool;
import com.example.uartdemo.Tools;

import org.apache.http.cookie.CookieIdentityComparator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

public class LierdaRfActivity extends Activity implements View.OnClickListener {

    /**
     * UI
     **/
    private EditText editRecv;
    private Spinner spinnerSerialport;
    private Button buttonOpen;
    private Button buttonClear;

    private EditText meterIdEditText;
    private EditText meterValueEditText;
    private EditText meterValveStateEditText;

    private Button readValueBtn;
    private Button openValveBtn;
    private Button closeValveBtn;

    private String[] serialportStrs;

    private List<String> listSerialPort = new ArrayList<String>();

    /**
     * serialport
     **/
    private SerialPort mSerialPort;
    private InputStream is;
    private OutputStream os;

    private int port;

    /**
     * recv Thread
     **/
    private RecvThread recvThread;

    private boolean isOpen = false;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_lierda_rf);

        context = this;
        initView();
        listener();
    }


    private void initView() {
        editRecv = (EditText) findViewById(R.id.editTextInfo);
        spinnerSerialport = (Spinner) findViewById(R.id.spinnerSerialport);
        buttonOpen = (Button) findViewById(R.id.buttonOpen);
        buttonClear = (Button) findViewById(R.id.buttonClear);

        meterIdEditText = (EditText) findViewById(R.id.meterIdEditText);
        meterValueEditText = (EditText) findViewById(R.id.meterValueEditText);
        meterValveStateEditText = (EditText) findViewById(R.id.meterValveStateEditText);

        readValueBtn = (Button) findViewById(R.id.readValueBtn);
        openValveBtn = (Button) findViewById(R.id.openValveBtn);
        closeValveBtn = (Button) findViewById(R.id.closeValveBtn);

        serialportStrs = context.getResources().getStringArray(R.array.serialportArray);

        for (String serial : serialportStrs) {
            listSerialPort.add(serial);
        }
        spinnerSerialport.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item,
                listSerialPort));
    }

    /**
     * listen componet
     */
    private void listener() {
        spinnerSerialport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                port = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        buttonOpen.setOnClickListener(this);
        buttonClear.setOnClickListener(this);
        readValueBtn.setOnClickListener(this);
        openValveBtn.setOnClickListener(this);
        closeValveBtn.setOnClickListener(this);
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
            mSerialPort = new SerialPort(port, 9600, 0);
        } catch (Exception e) {

            Toast.makeText(this, "SerialPort init fail!!", Toast.LENGTH_LONG).show();
            return;
        }
        is = mSerialPort.getInputStream();
        os = mSerialPort.getOutputStream();
        mSerialPort.rfid_poweron();

        recvThread = new RecvThread();
        recvThread.start();
        isOpen = true;
        spinnerSerialport.setClickable(false);
        buttonOpen.setText(context.getResources().getString(R.string.close));
        Toast.makeText(this, "SerialPort open success", Toast.LENGTH_SHORT).show();

    }

    //close serialport
    private void close() {
        if (recvThread != null) {
            recvThread.interrupt();
        }
        if (mSerialPort != null) {

            mSerialPort.rfid_poweroff();
            try {
                is.close();
                os.close();
            } catch (IOException e) {
//				e.printStackTrace();
            }

            mSerialPort.close(port);
            isOpen = false;
            spinnerSerialport.setClickable(true);
            buttonOpen.setText(context.getResources().getString(R.string.open));
        }
    }

    //send cmd
    private void send(byte[] data) {
        editRecv.append("[Send(HEX)]:" + Tools.Bytes2HexString(data, data.length) + "\n");
        Log.i("wdj","[Send(HEX)]:" + Tools.Bytes2HexString(data, data.length));
        try {
            os.write(data);
        } catch (IOException e) {
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
            case R.id.buttonClear:
                editRecv.setText("");
                meterValueEditText.setText("");
                meterValveStateEditText.setText("");
                break;
            case R.id.readValueBtn:
                this.readValue();
                break;
            case R.id.openValveBtn:
                this.openValve();
                break;
            case R.id.closeValveBtn:
                this.closeValve();
                break;
            default:
                break;
        }

    }

    private void readValue() {
        HashMap<String, Object> map = new HashMap<String, Object>();

        if (meterIdEditText.getText().length() != 8) {
            Toast.makeText(getApplication(), "表ID长度错误", Toast.LENGTH_SHORT).show();
            return;
        }

        map.put(Cmd.KEY_METER_ID, meterIdEditText.getText().toString());
        map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_READ_DATA);

        byte[] cmd = Cmd.AssembleCmdLierda(map);

        send(cmd);
    }


    private void openValve() {
        HashMap<String, Object> map = new HashMap<String, Object>();

        if (meterIdEditText.getText().length() != 8) {
            Toast.makeText(getApplication(), "表ID长度错误", Toast.LENGTH_SHORT).show();
            return;
        }

        map.put(Cmd.KEY_METER_ID, meterIdEditText.getText().toString());
        map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_VALVE);
        map.put(Cmd.KEY_VALUE,"1");

        byte[] cmd = Cmd.AssembleCmdLierda(map);

        send(cmd);
    }

    private void closeValve() {
        HashMap<String, Object> map = new HashMap<String, Object>();

        if (meterIdEditText.getText().length() != 8) {
            Toast.makeText(getApplication(), "表ID长度错误", Toast.LENGTH_SHORT).show();
            return;
        }

        map.put(Cmd.KEY_METER_ID, meterIdEditText.getText().toString());
        map.put(Cmd.KEY_DATA_TYPE, Const.DATAID_WRITE_VALVE);
        map.put(Cmd.KEY_VALUE,"0");

        byte[] cmd = Cmd.AssembleCmdLierda(map);

        send(cmd);
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
                    size = is.available();
                    if (size > 0) {
                        Thread.sleep(100);

                        size = is.read(buffer);
                        byte[] buffer1 = new byte[size];
                        System.arraycopy(buffer, 0, buffer1, 0, size);
                        onDataReceived(buffer1, size);
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
                String recv = Tools.Bytes2HexString(buffer, size);
                editRecv.append("[Recv(HEX)]:" + recv + "\n");
                Log.i("wdj","[Recv(HEX)]:" + recv);

                HashMap<String, Object> resultmap = ParseData.ParseDataToMapLierda(buffer);
                if (resultmap.containsKey(Cmd.KEY_SUCCESS)) {
                    if (resultmap.get(Cmd.KEY_SUCCESS).toString().equals("1")) {

                        Log.i("wdj","成功");
                        Toast.makeText(context,"成功",Toast.LENGTH_SHORT).show();
                        if (resultmap.containsKey(Cmd.KEY_VALUE_NOW))

                            meterValueEditText.setText(resultmap.get(Cmd.KEY_VALUE_NOW).toString());
                        if(resultmap.containsKey(Cmd.KEY_VALVE_STATE))
                        meterValveStateEditText.setText(resultmap.get(Cmd.KEY_VALVE_STATE).toString());
                    }else {
                        if(resultmap.containsKey(Cmd.KEY_ERR_MESSAGE)){

                            Log.i("wdj", "失败");
                            if(resultmap.get(Cmd.KEY_ERR_MESSAGE).equals("ACK")){

                            }else {
                                Toast.makeText(context, "失败:" + resultmap.get(Cmd.KEY_ERR_MESSAGE), Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }
            }
        });
    }

}
