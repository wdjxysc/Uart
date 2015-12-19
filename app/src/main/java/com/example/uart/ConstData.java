package com.example.uart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2015/7/13 0013.
 */
public class ConstData {

    /**
     * 爱美通模块命令
     * @return
     */
    public static ArrayList<HashMap> getCmdList() {
        ArrayList<HashMap> listCmd = new ArrayList<HashMap>();
        HashMap map = new HashMap();
        map.put("cmdname","1.开阀");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 03 FE FE FE 68 30 03 00 00 00 00 00 00 04 04 17 A0 01 55 B0 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname", "2.关阀");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 03 FE FE FE 68 30 03 00 00 00 00 00 00 04 04 17 A0 01 99 F4 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","3.写表地址");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 03 FE FE FE 68 30 03 00 00 00 00 00 00 15 0A 18 A0 FF 05 00 00 00 00 00 00 76 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","4.写表底数");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 03 FE FE FE 68 30 03 00 00 00 00 00 00 16 08 16 A0 FF 78 56 34 12 00 82 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","5.抄表");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 03 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 1F 90 FF 4D 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","6.写标准时间");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 03 FE FE FE 68 30 03 00 00 00 00 00 00 04 0A 15 A0 FF 40 50 12 16 07 15 20 00 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","7.升级固件");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 2A 03 AA 55 00 00 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","8.读标准时间");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 03 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 15 85 00 00 16");
        listCmd.add(map);


        map = new HashMap();
        map.put("cmdname","9.写表状态");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 04 03 80 A5 00 00 00 00 16");
        listCmd.add(map);


        map = new HashMap();
        map.put("cmdname","10.读表状态");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 80 85 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","11.读上1月-上6月数据");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 3F 90 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","12.读上7月-上12月数据");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 4F 90 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","13.读上1日-5日数据");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 3F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","14.读上6日-10日数据");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 4F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","15.读上11日-15日数据");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 5F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","16.读上16日-20日数据");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 6F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","17.读上21日-25日数据");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 7F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","18.读上26日-30日数据");
        map.put("cmddata", "FF 07 8D 98 10 00 00 00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 8F 91 00 00 16");
        listCmd.add(map);

        return listCmd;
    }

    /**
     * 捷迅模块命令
     * @return
     */
    public static ArrayList<HashMap> getCmdList2() {
        ArrayList<HashMap> listCmd = new ArrayList<HashMap>();
        HashMap map = new HashMap();
        map.put("cmdname", "开阀");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 04 04 17 A0 01 55 B0 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname", "关阀");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 04 04 17 A0 01 99 F4 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","写表地址");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 15 0A 18 A0 FF 05 00 00 00 00 00 00 76 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","写表底数");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 16 08 16 A0 FF 78 56 34 12 00 82 16");

        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","抄表");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 1F 90 FF 4D 16");
        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","写标准时间");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 04 0A 15 A0 FF 40 50 12 16 07 15 20 00 16");
        listCmd.add(map);
//        map = new HashMap();
//        map.put("cmdname","升级固件");
//        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 2A 03 AA 55 00 00 16");
//        listCmd.add(map);
        map = new HashMap();
        map.put("cmdname","读标准时间");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 15 81 00 00 16");
        listCmd.add(map);


        map = new HashMap();
        map.put("cmdname","写表状态");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 04 05 80 FF 00 00 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读表状态");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 00 FF 00 00 16");
        listCmd.add(map);


        map = new HashMap();
        map.put("cmdname","出厂启用");
        map.put("cmddata", "");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","写结算日");
        map.put("cmddata", "");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","写抄表日");
        map.put("cmddata", "");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读结算日");
        map.put("cmddata", "");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读抄表日");
        map.put("cmddata", "");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读上1月-上6月数据");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 3F 90 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读上7月-上12月数据");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 4F 90 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读上1日-5日数据");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 3F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读上6日-10日数据");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 4F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读上11日-15日数据");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 5F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读上16日-20日数据");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 6F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读上21日-25日数据");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 7F 91 00 00 16");
        listCmd.add(map);

        map = new HashMap();
        map.put("cmdname","读上26日-30日数据");
        map.put("cmddata", "00 05 FE FE FE 68 30 03 00 00 00 00 00 00 01 03 8F 91 00 00 16");
        listCmd.add(map);


        map = new HashMap();
        map.put("cmdname","广播读表地址");
        map.put("cmddata", "FF FF FE FE FE 68 30 AA AA AA AA AA AA AA 22 03 55 FF 00 00 16");
        listCmd.add(map);

        return listCmd;
    }
}
