package com.example.uart;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parse.Cmd;
import com.example.parse.Const;
import com.example.uartdemo.CRC16;
import com.example.uartdemo.SerialPort;
import com.example.uartdemo.Tools;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UpdateActivity extends Activity {
    //用于日志
    private int cmdidnow = 0;
    private byte[] readbufferrrrr =new byte[1024];
    private int readbufferrrrrlength = 0;
    //正在发送的命令
    private byte[] cmd;

    //UI
    private Button updateBtn;
    private TextView percentTextView;
    private Button getStateBtn;
    private TextView meterStateTextView;

    private Button resetBtn;

    //表状态
    private String stateStr;

    //数据头部长度
    private int HEADLENGTH = Const.IN_HEAD_LENGTH_4_BYTES;

    /**
     * serialport *
     */
    private SerialPort mSerialPort;
    private InputStream is;
    private OutputStream os;

    private int port = 13;
//    private int buad = 57600;
    private int buad = 9600;
    private String powerStr = "rfid power";

    private boolean isOpen = false;

    private EditText nodeidEditText;
    private EditText addressidEditText;
    private ProgressBar progressBar;
    private ProgressBar indicatorProgressBar;
    private int sended;//已发送数据包数

    private  EditText editTextInfo;

    private ArrayList<byte[]> cmdlist;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        System.out.println("onCreate");

        //打开串口
        open();

        //初始化界面
        initView();

        switch (Const.rf_type){
            case RF_TYPE_NODEID_2_BYTES:
                HEADLENGTH = Const.IN_HEAD_LENGTH_2_BYTES;
                break;
            case RF_TYPE_NODEID_4_BYTES:
                HEADLENGTH = Const.IN_HEAD_LENGTH_4_BYTES;
                break;
            default:
                break;
        }
    }

    /**
     * 初始化界面
     */
    private void initView()
    {
        nodeidEditText = (EditText)findViewById(R.id.nodeidEditText);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        indicatorProgressBar = (ProgressBar)findViewById(R.id.indicatorProgressBar);
        editTextInfo = (EditText)findViewById(R.id.editTextInfo);
        percentTextView = (TextView)findViewById(R.id.percentTextView);
        getStateBtn = (Button)findViewById(R.id.getStateBtn);
        meterStateTextView = (TextView)findViewById(R.id.meterStateTextView);
//        resetBtn = (Button)findViewById(R.id.resetBtn);
        addressidEditText = (EditText)findViewById(R.id.addressidEditText);

        editTextInfo.setKeyListener(null);//使用户无法编辑


        updateBtn = (Button)findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFirmware1();
            }
        });


        getStateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMeterState();
            }
        });

//        resetBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                resetMeter();
//            }
//        });


    }

    /**
     * 在正式升级之前，询问表状态
     */
    private void getMeterState() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("nodeid", Long.parseLong(nodeidEditText.getText().toString(),16));
                map.put("datatype", Const.DATAID_UPDATE_FIRMWARE);
                String addressidstr = addressidEditText.getText().toString();
                if (addressidstr.length() != 14) return;
                map.put("addressid", addressidstr);

                cmd = Cmd.AssembleCmd(map);
                if (cmd == null) return;

                try {
                    is.read(readbufferrrrr);
                    os.write(cmd);

                    stateStr = "正在查询...";
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            meterStateTextView.setText(stateStr);
                            editTextInfo.append("\r\n" + "SEND:----" + Tools.Bytes2HexString(cmd, cmd.length));
                            //滚动到最后一行
                            editTextInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
                            editTextInfo.setSelection(editTextInfo.getText().length(), editTextInfo.getText().length());
                        }
                    });
                    for (int i = 0; i < 100; i++) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (is.available() >= 17) {
                            byte[] buffer = new byte[1024];
                            int readed = is.read(buffer);


                            if (buffer[HEADLENGTH] == 0x68 && buffer[HEADLENGTH + 14] == 0x00) {
                                stateStr = "0x00  就绪，请执行复位操作";
                                //执行复位
                                resetMeter();
                            } else if (buffer[HEADLENGTH] == 0x68 && buffer[HEADLENGTH + 14] == (byte) 0x81) {
                                stateStr = "0x81  表忙";
                            } else if (buffer[HEADLENGTH] == 0x68 && buffer[HEADLENGTH + 14] == (byte) 0x82) {
                                stateStr = "0x82  低电压";
                            } else if (buffer[HEADLENGTH] == 0x68 && buffer[HEADLENGTH + 14] == (byte) 0x83) {
                                stateStr = "0x83  阀门异常";
                            } else {
                                stateStr = "请先查询表状态";
                            }

                            readbufferrrrr = buffer;
                            readbufferrrrrlength = readed;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    meterStateTextView.setText(stateStr);
                                    editTextInfo.append("\r\n" + "RECV:----" + Tools.Bytes2HexString(readbufferrrrr, readbufferrrrrlength));
                                    //滚动到最后一行
                                    editTextInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
                                    editTextInfo.setSelection(editTextInfo.getText().length(), editTextInfo.getText().length());
                                }
                            });

                            return;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                stateStr = "请先查询表状态";
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        meterStateTextView.setText(stateStr);
                    }
                });
            }
        }).start();
    }

    /**
     * 复位表
     */
    private void resetMeter(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String,Object> map = new HashMap<String, Object>();
                map.put("nodeid",Long.parseLong(nodeidEditText.getText().toString(),16));
                map.put("datatype", Const.DATAID_UPDATE_FIRMWARE_RESET);
                String addressidstr = addressidEditText.getText().toString();
                if(addressidstr.length() != 14) return;
                map.put("addressid", addressidstr);

                cmd = Cmd.AssembleCmd(map);
                if(cmd == null)return;

                try {
                    is.read(readbufferrrrr);
                    os.write(cmd);

                    stateStr = "正在复位...";
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            meterStateTextView.setText(stateStr);
                            editTextInfo.append("\r\n" + "SEND:----" + Tools.Bytes2HexString(cmd, cmd.length));
                            //滚动到最后一行
                            editTextInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
                            editTextInfo.setSelection(editTextInfo.getText().length(), editTextInfo.getText().length());
                        }
                    });
                    for (int i = 0;i<100;i++){
                        Thread.sleep(100);
                        if(is.available()>=17) {
                            byte[] buffer = new byte[1024];
                            int readed = is.read(buffer);

                            if(buffer[HEADLENGTH] == 0x68 && buffer[HEADLENGTH + 14] == 0x00){
                                stateStr = "0x00  复位发送成功，若表的显示屏显示LOAD，请连接SET脚至GND，然后点击升级按钮开始升级";
                            }else {
                                stateStr = "复位失败";
                            }

                            readbufferrrrr = buffer;
                            readbufferrrrrlength = readed;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    meterStateTextView.setText(stateStr);
                                    editTextInfo.append("\r\n" + "RECV:----" + Tools.Bytes2HexString(readbufferrrrr, readbufferrrrrlength));
                                    //滚动到最后一行
                                    editTextInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
                                    editTextInfo.setSelection(editTextInfo.getText().length(), editTextInfo.getText().length());
                                }
                            });

                            return;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                stateStr = "";
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        meterStateTextView.setText(stateStr);
                    }
                });
            }
        }).start();
    }

    /**
     * 升级程序1
     */
    private void updateFirmware1() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateBtn.setVisibility(View.INVISIBLE);
                indicatorProgressBar.setVisibility(View.VISIBLE);
                percentTextView.setText("0%");
                progressBar.setProgress(0);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap map = new HashMap();

                String nodeidstr = nodeidEditText.getText().toString();
                int nodeid = 0;
                try {
                    nodeid = Integer.parseInt(nodeidstr,16);
//                    if (netidstr.length() != 8) throw new Exception();
                } catch (Exception ex) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "请输入正确的nodeid", Toast.LENGTH_SHORT).show();
                            updateBtn.setVisibility(View.VISIBLE);
                            indicatorProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    return;
                }

                map.put("nodeid", nodeidstr);
                if(cmdlist == null) {
                    cmdlist = Cmd.GetUpdateCmdListFromTxt(map);
                }

                if (cmdlist.size() == 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "升级文件不存在", Toast.LENGTH_SHORT).show();
                            updateBtn.setVisibility(View.VISIBLE);
                            indicatorProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    return;
                } else {

                    for (int i = 0; i < cmdlist.size(); i++) {
                        try {
                            //最多发三次
                            for (int k = 0; k < 3; k++) {
                                is.read(readbufferrrrr);
                                //升级操作....
                                os.write(cmdlist.get(i));
                                Log.i("ccccccccc", "send:" + Tools.Bytes2HexString(cmdlist.get(i), cmdlist.get(i).length));

                                cmdidnow = i;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (editTextInfo.getText().toString().length() > 10000) {
                                            editTextInfo.setText(editTextInfo.getText().toString().substring(5000));
                                        }
                                        editTextInfo.append("\r\n" + "SEND:----" + Tools.Bytes2HexString(cmdlist.get(cmdidnow), cmdlist.get(cmdidnow).length));

                                        editTextInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
                                        editTextInfo.setSelection(editTextInfo.getText().length(), editTextInfo.getText().length());
                                    }
                                });

//                                Thread.sleep(2000);
                                int repeatTime = 100;
                                int sleepTime = 100;

                                //不断收repeatTime次若还是没有回应 则升级失败
                                for (int h = 0; h < repeatTime; h++) {
                                    Thread.sleep(sleepTime);
                                    byte[] buffer = new byte[1024];
                                    if (is.available() > 0) {
                                        int length = is.read(buffer);
                                        Log.i("ccccccccc", "recv:" + Tools.Bytes2HexString(buffer, length));
                                        readbufferrrrr = buffer;
                                        readbufferrrrrlength = length;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                editTextInfo.append("\r\n" + "RECV:----" + Tools.Bytes2HexString(readbufferrrrr, readbufferrrrrlength));

                                                editTextInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
                                                editTextInfo.setSelection(editTextInfo.getText().length(), editTextInfo.getText().length());
                                            }
                                        });

                                        if (length >= HEADLENGTH + 7) {
                                            byte headbyte = buffer[HEADLENGTH];
                                            byte resultbyte = buffer[HEADLENGTH + 4];
                                            int result = (int)resultbyte;
                                            if (headbyte == 0x68 && resultbyte == 0x00) {

                                                //返回成功
                                                //计算校验码
                                                byte[] dataforcrc = Arrays.copyOfRange(buffer, HEADLENGTH, HEADLENGTH + 7);
                                                int crc16 = CRC16.calcCrc16(dataforcrc);
                                                if (crc16 == 0) {
                                                    //校验成功 ,k设置为3，发送下一条命令，否则继续发送当前命令
                                                    k = 3;
                                                } else {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getApplication(), "升级失败，校验错误", Toast.LENGTH_SHORT).show();
                                                            editTextInfo.append("\r\n 升级失败，校验错误");
                                                            updateBtn.setVisibility(View.VISIBLE);
                                                            indicatorProgressBar.setVisibility(View.INVISIBLE);
                                                            progressBar.setProgress(0);
                                                            percentTextView.setText("0%");
                                                        }
                                                    });
                                                }
                                                break;

                                            }else if(headbyte == 0x68 &&(resultbyte == (byte)0x83 || resultbyte == (byte)0x86)){
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplication(), "升级失败，返回错误", Toast.LENGTH_SHORT).show();
                                                        editTextInfo.append("\r\n升级失败，返回错误");
                                                        updateBtn.setVisibility(View.VISIBLE);
                                                        indicatorProgressBar.setVisibility(View.INVISIBLE);
                                                        progressBar.setProgress(0);
                                                        percentTextView.setText("0%");
                                                    }
                                                });
                                                return;
                                            }else if (headbyte == 0x68 && ( resultbyte > (byte)0x80)) {
                                                //接受失败，重复发送命令
                                                if(k == 2){
                                                    i = cmdlist.size();
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getApplication(), "升级失败，返回错误", Toast.LENGTH_SHORT).show();
                                                            editTextInfo.append("\r\n升级失败，返回错误");
                                                            updateBtn.setVisibility(View.VISIBLE);
                                                            indicatorProgressBar.setVisibility(View.INVISIBLE);
                                                            progressBar.setProgress(0);
                                                            percentTextView.setText("0%");
                                                        }
                                                    });
                                                    return;
                                                }
                                                break;
                                            } else if (headbyte != 0x68 && k == 2) {
                                                i = cmdlist.size();
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplication(), "升级失败，返回错误", Toast.LENGTH_SHORT).show();
                                                        editTextInfo.append("\r\n升级失败，返回错误");
                                                        updateBtn.setVisibility(View.VISIBLE);
                                                        indicatorProgressBar.setVisibility(View.INVISIBLE);
                                                        progressBar.setProgress(0);
                                                        percentTextView.setText("0%");
                                                    }
                                                });
                                                return;
                                            }
                                        }

                                        break;
                                    } else if (h == repeatTime - 1 && k == 2) {

                                        i = cmdlist.size();
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplication(), "升级失败，超时", Toast.LENGTH_SHORT).show();
                                                editTextInfo.append("\r\n升级失败，超时");
                                                updateBtn.setVisibility(View.VISIBLE);
                                                indicatorProgressBar.setVisibility(View.INVISIBLE);
                                                progressBar.setProgress(0);
                                                percentTextView.setText("0%");
                                            }
                                        });
                                        return;
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        sended = i;
                        //更新主界面progress
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress((int) (((float) sended / cmdlist.size()) * progressBar.getMax()));
                                percentTextView.setText((int) (((float) sended / cmdlist.size()) * 100) + "%");
                            }
                        });
                    }

                    //更新主界面progress
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressBar.getMax());
                        }
                    });
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateBtn.setVisibility(View.VISIBLE);
                        indicatorProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "固件升级成功", Toast.LENGTH_SHORT).show();
                        editTextInfo.append("\r\n固件升级成功");
                        percentTextView.setText("100%");
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
        close();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
