package com.example.uartdemo;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import com.example.parse.Const;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2015/9/4.
 */
public class SerialPortTool {

    /**
     * 掌机串口端口号
     */
    public static final int HANDLE_COMM_PORT = 13;

    /**
     * 掌机串口波特率
     */
    public static final int HANDLE_COMM_PORT_BR = 9600;


    /**
     * 串口
     */
    SerialPort comm;

    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private int port;
    private int baud;

    public boolean isOpen;

    public enum PowerLevel{
        POWER_3_3V,
        POWER_5V,
        POWER_SCAN,
        POWER_PSAM,
        POWER_RFID
    }


    public SerialPortTool(int port,int baud) throws IOException {
        this.port = port;
        this.baud = baud;
    }

    /**
     * 发开串口
     * @param powerLevel 电压
     * @throws IOException
     */
    public void openPort(PowerLevel powerLevel) throws IOException {
        comm = new SerialPort(this.port,this.baud,0);

        inputStream = (FileInputStream) comm.getInputStream();
        outputStream = (FileOutputStream) comm.getOutputStream();

        if (powerLevel == PowerLevel.POWER_3_3V) {
            comm.power3v3on();
        } else if (powerLevel == PowerLevel.POWER_5V) {
            comm.power_5Von();
        } else if (powerLevel == PowerLevel.POWER_SCAN) {
            comm.scaner_poweron();
        } else if (powerLevel == PowerLevel.POWER_PSAM) {
            comm.psam_poweron();
        } else if (powerLevel == PowerLevel.POWER_RFID) {
            comm.rfid_poweron();
        }

        isOpen = true;
    }

    /**
     * 关闭串口
     * @param powerLevel 电压
     */
    public void closePort(PowerLevel powerLevel){
        if (comm != null) {

            if (powerLevel == PowerLevel.POWER_3_3V) {
                comm.power3v3off();
            } else if (powerLevel == PowerLevel.POWER_5V) {
                comm.power_5Voff();
            } else if (powerLevel == PowerLevel.POWER_SCAN) {
                comm.scaner_poweroff();
            } else if (powerLevel == PowerLevel.POWER_PSAM) {
                comm.psam_poweroff();
            } else if (powerLevel == PowerLevel.POWER_RFID) {
                comm.rfid_poweroff();
            }
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            comm.close(port);
            isOpen = false;
        }
    }


    public byte[] sendAndRevData(byte[] senddata,int timeout,Const.RfModuleType rfModuleType) throws IOException, InterruptedException{
        byte[] revdata = null;

        switch (rfModuleType){
            case JIEXUN:
                revdata = sendAndRevData(senddata,timeout);
                break;
            case SKY_SHOOT:

                break;
            case LIERDA:
                revdata = sendAndRevDataLierda(senddata,timeout);
                break;
        }

        return revdata;
    }

    /**
     * 捷迅表模块
     * 发送数据并接受数据 同步接收
     * @param senddata 发送的数据
     * @param timeout 超时时间
     * @return 接收到的数据
     * @throws IOException
     * @throws InterruptedException
     */
    public byte[] sendAndRevData(byte[] senddata,int timeout) throws IOException, InterruptedException {
        switch (Const.rf_transmisson_type){
            case RF_TRANSMISSON_TYPE_NO_RELAY:
                timeout = 15000;
                break;
            case RF_TRANSMISSON_TYPE_RELAY:
                timeout = 26000;  //skyshoot 有中继模式 传输时间比较长 设置为26s
                break;
        }

        byte[] revdata = null;
        byte[] buffer1 = new byte[inputStream.available()];
        int readed1 = inputStream.read(buffer1);

        outputStream.write(senddata);

        Log.i("wdj", "send:" + Tools.Bytes2HexString(senddata, senddata.length));

        //RF射频模块可能回应比较慢
        for (int i = 0; i < timeout/1000; i++) {
            Thread.sleep(1000);
            if (inputStream.available() >= 5) {
                byte[] buffer = new byte[inputStream.available()];
                int readed = inputStream.read(buffer);
                revdata = buffer;
                break;
            }
        }
        if(revdata!=null) {
            Log.i("wdj", "rev:" + Tools.Bytes2HexString(revdata, revdata.length));
        }else {
            Log.i("wdj", "rev:null");
        }

        return revdata;
    }


    /**
     * 利尔达表模块
     * 发送数据并接受数据 同步接收
     * @param senddata
     * @param timeout
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public byte[] sendAndRevDataLierda(byte[] senddata,int timeout) throws IOException, InterruptedException {
        byte[] revdata = null;
        byte[] buffer1 = new byte[inputStream.available()];
        int readed1 = inputStream.read(buffer1);

        outputStream.write(senddata);

        Log.i("wdj-lierda", "send:" + Tools.Bytes2HexString(senddata, senddata.length));

        //RF射频模块可能回应比较慢
        for (int i = 0; i < timeout/1000; i++) {
            Thread.sleep(1000);
            if (inputStream.available() >= 5) {
                byte[] buffer = new byte[inputStream.available()];

                int readed = inputStream.read(buffer);
                if(buffer.length>10) {
                    revdata = buffer;
                    break;
                }
            }
        }
        if(revdata!=null) {
            Log.i("wdj-lierda", "rev:" + Tools.Bytes2HexString(revdata, revdata.length));
        }else {
            Log.i("wdj-lierda", "rev:null");
        }

        return revdata;
    }
}
