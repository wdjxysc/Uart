package com.example.uart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.example.parse.Cmd;
import com.example.parse.Const;
import com.example.uartdemo.SerialPort;
import com.example.uartdemo.Tools;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends Activity implements OnClickListener {

	/**
	 * UI  *
	 */
	private EditText editRecv;
	private AutoCompleteTextView editSend;
	//	private Spinner spinnerBuadrate ;
//	private Spinner spinnerSerialport ;
//	private Spinner spinnerPower ;
	private Spinner cmdSpinner;
	private CheckBox checkRecv;
	private CheckBox checkSend;
	private Button buttonOpen;
	private Button buttonSend;
	private Button buttonClear;

//	private String[] buadrateStrs ;
//	private String[] serialportStrs ;
//	private String[] powerStrs ;

	//	private List<String> listBuadRate = new ArrayList<String>();
//	private List<String> listSerialPort = new ArrayList<String>();
//	private List<String> listPower = new ArrayList<String>();
	private ArrayList<HashMap> listCmdMap = new ArrayList<HashMap>();
	private List<String> listCmd = new ArrayList<String>();

	/**
	 * serialport *
	 */
	private SerialPort mSerialPort;
	private InputStream is;
	private OutputStream os;

	private int port = 13;
	private int buad = 57600;
//	private int buad = 9600;
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
	private EditText timeEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		/** 初始化cmdMap**/
		listCmdMap = ConstData.getCmdList();

		context = this;
		initView();
		listener();

		testFunc();
	}

	private void testFunc()
	{

	}


	private void initView() {
		editRecv = (EditText) findViewById(R.id.editTextInfo);
		editSend = (AutoCompleteTextView) findViewById(R.id.editTextSend);
		initHistory("history", editSend);
//		spinnerBuadrate = (Spinner) findViewById(R.id.spinnerBuadrate);
//		spinnerPower = (Spinner) findViewById(R.id.spinnerPower);
//		spinnerSerialport = (Spinner) findViewById(R.id.spinnerSerialport);
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

//		timeEditText = (EditText)findViewById(R.id.timeEditText);
//
//		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//		timeEditText.setText(format.format(new Date()));

//
//		buadrateStrs = context.getResources().getStringArray(R.array.buadrateArray);
//		serialportStrs = context.getResources().getStringArray(R.array.serialportArray);
//		powerStrs = context.getResources().getStringArray(R.array.powerArray);

//		for(String buad:buadrateStrs){
//			listBuadRate.add(buad);
//		}
//		for(String serial:serialportStrs){
//			listSerialPort.add(serial);
//		}
//		for(String power:powerStrs){
//			listPower.add(power);
//		}

		for (HashMap cmdmap : listCmdMap) {
			listCmd.add((String) cmdmap.get("cmdname"));

		}

//		spinnerBuadrate.setAdapter(new ArrayAdapter<String>(context,
//				android.R.layout.simple_spinner_dropdown_item,
//				listBuadRate));
//		spinnerSerialport.setAdapter(new ArrayAdapter<String>(context,
//				android.R.layout.simple_spinner_dropdown_item,
//				listSerialPort));
//		spinnerPower.setAdapter(new ArrayAdapter<String>(context,
//				android.R.layout.simple_spinner_dropdown_item,
//				listPower));
		cmdSpinner.setAdapter(new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_dropdown_item,
				listCmd));
		cmdSpinner.setPrompt("命令");//标题


	}

	/**
	 * listen componet
	 */
	private void listener() {
//		spinnerSerialport.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> adapter, View view,
//					int position, long id) {
//				port = position;
//
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> arg0) {
//
//			}
//		});
//		spinnerBuadrate.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> adapter, View view,
//					int position, long id) {
//				String buadrate = buadrateStrs[position];
//				buad = Integer.valueOf(buadrate);
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> arg0) {
//
//			}
//		});
//		spinnerPower.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> adapter, View view,
//					int position, long id) {
//				powerStr = powerStrs[position];
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> arg0) {
//
//			}
//		});

		cmdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

				String str = ((String) ((HashMap) listCmdMap.toArray()[i]).get("cmddata")).replaceAll(" ", "");
				String strname = ((String) ((HashMap) listCmdMap.toArray()[i]).get("cmdname")).replaceAll(" ", "");
				String lastStr = str.substring(0, 10) + meterIdEditText.getText() + str.substring(18, str.length());
				editSend.setText(lastStr);

				//表地址
				String oldaddress  = meterOldAddressEditText.getText()+"";
				String oldaddr = "";
				for (int k = 6; k > -1; k--) {
					oldaddr += oldaddress.substring(k * 2, (k + 1) * 2);
				}

				if (strname.equals("1.开阀")) {
					lastStr = lastStr.substring(0, 28) + oldaddr + lastStr.substring(42,lastStr.length());
				}
				if (strname.equals("2.关阀")) {
					lastStr = lastStr.substring(0, 28) + oldaddr + lastStr.substring(42,lastStr.length());
				}
				if (strname.equals("5.抄表")) {
					lastStr = lastStr.substring(0, 28) + oldaddr + lastStr.substring(42,lastStr.length());
				}

				if (strname.equals("3.写表地址")) {

					String newaddr = lastStr.substring(52, 66);
					if (meterNewAddressEditText.getText().length() == 14) {
						String newtext = meterNewAddressEditText.getText() + "";
						newaddr = "";
						for (int k = 6; k > -1; k--) {
							newaddr += newtext.substring(k * 2, (k + 1) * 2);
						}
					}

					lastStr = lastStr.substring(0, 28) + oldaddr + lastStr.substring(42,52) + newaddr + lastStr.substring(66, 70);
				}

				if (strname.equals("4.写表底数")) {
					try {
						String datastr = meterValueEditText.getText() + "";
						float f = Float.parseFloat(datastr) * 10;
						int datai = (int) f;
						String datastr1 = String.format("%010d", datai);

						String llldata = "";
						for (int k = 4; k > -1; k--) {
							llldata += datastr1.substring(k * 2, (k + 1) * 2);
						}
						lastStr = lastStr.substring(0, 28)+oldaddr+lastStr.substring(42,52) + llldata + lastStr.substring(62, 66);
					} catch (Exception ex) {

					}
				}

				if(strname.equals("6.写标准时间"))
				{
					lastStr = lastStr.substring(0, 28) + oldaddr + lastStr.substring(42,lastStr.length());
				}

				if(strname.equals("7.升级固件"))
				{
					byte[] datalist1;
					HashMap map = new HashMap();
					map.put("datatype",Const.DATAID_UPDATE_FIRMWARE);
					datalist1 = Cmd.AssembleCmd(map);

				}

				if(strname.equals("8.读标准时间"))
				{
					try{
						SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
						String datastr = df.format(new Date());

						String timeStr = "";

						for (int k = 6; k > -1; k--) {
							timeStr += datastr.substring(k * 2, (k + 1) * 2);
						}
						lastStr = lastStr.substring(0, 28)+oldaddr+lastStr.substring(42,52) + timeStr + lastStr.substring(66, lastStr.length());
					}catch (Exception ex)
					{

					}
				}

				editSend.setText(lastStr);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {


			}
		});


		checkRecv.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isHexRecv = isChecked;

			}
		});
		checkSend.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isHexSend = isChecked;

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
//		spinnerBuadrate.setClickable(false);
//		spinnerPower.setClickable(false);
//		spinnerSerialport.setClickable(false);
		buttonOpen.setText(context.getResources().getString(R.string.close));
//		Toast.makeText(this, "SerialPort open success", 0).show();

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
//			spinnerBuadrate.setClickable(true);
//			spinnerPower.setClickable(true);
//			spinnerSerialport.setClickable(true);
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

					if (size >= 24 && recv.substring(32, 36).equals("17A0")) {
						String value = recv.substring(38, 40);
						if (value.equals("00")) {
							meterStateEditText.setText("00:开阀");
						} else if (value.equals("01")) {
							meterStateEditText.setText("01:关阀");
						} else if (value.equals("03")) {
							meterStateEditText.setText("03:异常");
						}
					}

					if (size >= 24 && recv.substring(32, 36).equals("1F90")) {
						String value = recv.substring(38, 48);
						String lastValue = "";
						for (int i = 4; i > -1; i--) {
							lastValue += value.substring(i * 2, (i + 1) * 2);
						}

						float data = Float.parseFloat(lastValue) / 10;
						meterValueEditText.setText(data + "m³");
					}

					editRecv.append("[Recv(HEX)]:" + recv + "\n");
				} else {
					String recv = new String(buffer, 0, size);
					editRecv.append("[Recv]:" + recv + "\n");
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
		auto.setOnFocusChangeListener(new OnFocusChangeListener() {
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
