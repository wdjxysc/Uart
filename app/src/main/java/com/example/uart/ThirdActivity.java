package com.example.uart;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.MeterInfoAdapter;
import com.example.parse.Cmd;
import com.example.parse.Const;
import com.example.uartdemo.SerialPort;
import com.example.uartdemo.StringTool;
import com.example.uartdemo.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/7/21 0021.
 */
public class ThirdActivity extends Activity {
    /**
     * serialport
     */
    private SerialPort mSerialPort;
    private InputStream is;
    private OutputStream os;

    private int port = 13;
    //    private int buad = 57600;
    private int buad = 9600;
    private String powerStr = "rfid power";
    private boolean isOpen = false;



    private Handler handler = new Handler();

    private Button scanBtn;
    private ProgressBar indicatorProgressBar;
    private ProgressBar percentProgressBar;
    private TextView percentTextView;
    private EditText maxnumEditText;

    /**
     * 扫描最大数目
     */
    int maxnum = 0;

    /**
     * 已经扫描个数
     */
    int scanednum = 0;

    /**
     * ListView
     */
    private ListView meterInfoListView;

    /**
     * 数据列表
     */
    ArrayList<HashMap<String, String>> meterlist = new ArrayList<HashMap<String, String>>();

    /**
     * ListView adapter
     */
    MeterInfoAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        open();

        initView();

        testfunc();
    }

    private void testfunc(){
        String str = StringTool.reversalHexString("3344");
    }

    private void initView(){

        scanBtn = (Button)findViewById(R.id.scanBtn);
        indicatorProgressBar = (ProgressBar)findViewById(R.id.indicatorProgressBar);
        percentProgressBar = (ProgressBar)findViewById(R.id.percentProgressBar);
        percentTextView = (TextView)findViewById(R.id.percentTextView);
        maxnumEditText = (EditText)findViewById(R.id.maxnumEditText);

        indicatorProgressBar.setVisibility(View.INVISIBLE);


        meterInfoListView = (ListView) findViewById(R.id.meterInfoListView);

        //设置adapter
        adapter = new MeterInfoAdapter(meterlist,this);
        meterInfoListView.setAdapter(adapter);


        findViewById(R.id.addBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("nodeid", "0002");
                map.put("addressid", "00000000000002");
                meterlist.add(map);
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.deleteBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (meterlist.size() > 0) {
                    meterlist.remove(0);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        findViewById(R.id.scanBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (os != null && is != null) {
                    scanMeters();
                }
            }
        });



    }

    /**
     * 扫描已在网内的表
     */
    private void scanMeters() {

        try {
            maxnum = Integer.parseInt(maxnumEditText.getText().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "请输入正确的正整数", Toast.LENGTH_SHORT).show();
            return;
        }


        scanBtn.setVisibility(View.INVISIBLE);
        indicatorProgressBar.setVisibility(View.VISIBLE);
        percentTextView.setText("0%");
        percentProgressBar.setProgress(0);


        new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 1; i <= maxnum; i++) {

                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("netid", i);
                    map.put("datatype", Const.DATAID_SCAN_ALL_METER_ID);

                    byte[] cmd = Cmd.AssembleCmd(map);
                    if(cmd == null){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "获取命令失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                    int writerepeattime = 3;
                    int readrepeattime = 100;
                    int sleeptime = 100;

                    for (int k = 0;k<writerepeattime;k++){
                        try {
                            os.write(cmd);
                            for (int h = 0;h<readrepeattime;h++){
                                Thread.sleep(sleeptime);
                                byte[] buffer = new byte[1024];
                                if(is.available()>=16){
                                    int readylength = is.available();
                                    int readbytes = is.read(buffer,0,readylength);
                                    if(readbytes == readylength){
                                        if(buffer[0] == 0x68 && buffer[9] == (byte)Const.CTR_4_SCAN_ALL_METER_ID){

                                            byte[] meteraddressid = Arrays.copyOfRange(buffer,2,9);
                                            String meteraddressstr = StringTool.reversalHexString(Tools.Bytes2HexString(meteraddressid,meteraddressid.length));

                                            HashMap<String,String> meterinfo = new HashMap<String, String>();
                                            meterinfo.put("netid", Tools.Bytes2HexString(Tools.intToByte(i),Tools.intToByte(i).length) );
                                            meterinfo.put("addressid", meteraddressstr);
                                            meterlist.add(meterinfo);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //已经扫描的个数，更新界面
                    scanednum = i;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            percentProgressBar.setProgress((int) (((float) scanednum / maxnum) * percentProgressBar.getMax()));
                            percentTextView.setText((int) (((float) scanednum / maxnum) * 100)+"%");
                        }
                    });
                }


            }
        }).start();
    }


    //open serialport
    private void open() {
        //open
        try {
            mSerialPort = new SerialPort(port, buad, 0);
        } catch (Exception e) {

            Toast.makeText(this, "SerialPort init fail!!", Toast.LENGTH_SHORT).show();

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

        isOpen = true;
    }

    //close serialport
    private void close() {

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
                e.printStackTrace();
            }
            mSerialPort.close(port);
            isOpen = false;
        }
    }

    //send cmd
    private void send(String commandStr) {
        byte[] cmd = null;
        Log.i("send()", commandStr);
        if (commandStr == null) {
            Toast.makeText(this, "cmd is null", Toast.LENGTH_SHORT).show();
        }

        cmd = Tools.HexString2Bytes(commandStr);
        try {
            os.write(cmd);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
