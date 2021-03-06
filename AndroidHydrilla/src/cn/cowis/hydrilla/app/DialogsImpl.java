package cn.cowis.hydrilla.app;



import cn.cowis.hydrilla.app.R;
import cn.cowis.hydrilla.app.entity.Constants;
import cn.cowis.hydrilla.app.entity.SensorManager;
import cn.cowis.hydrilla.app.service.BackEvents.BackEventsType;
import cn.cowis.hydrilla.app.service.BackEvents.OnBackListener;
import cn.cowis.hydrilla.app.service.SensorService;
import cn.cowis.hydrilla.app.service.SensorService.ReadNoCacheParamCycle;
import cn.cowis.hydrilla.util.ToolUtil;
import cn.cowis.hydrilla.view.CustomTextViewButton;
import cn.cowis.modbus.exception.ErrorCodeException;
import cn.cowis.modbus.exception.TransferException;

import com.google.inject.Inject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DialogsImpl implements Dialogs{

	
	@Inject
	Context context;
	
	//private  ReaderThread readThread = null;
	
	public abstract class CommunionService implements OnBackListener{
		
		private SensorService sensorService=null;
		private SensorManager sensorManager=null;
		
		private boolean isAnalog=true;
		
		private int retainedNum=-1;//结果的保留位数
		
		public CommunionService(SensorManager sensorManager,boolean isAnalog){
			 sensorService= ((MyRoboApplication)context.getApplicationContext()).sensorClient.sensorService;
			 this.sensorManager=sensorManager;
			 this.isAnalog=isAnalog;
			 
		}
		
		
		public abstract void updateData(String value);
		
		/**
		 * 开始读取数据
		 */
		public void begion() {
			
			sensorService.backEvents.addBackListener(this);
			
			if(isAnalog){
				
				 sensorService.startCycle(Constants.NO_CACHE_ANALOG, sensorManager, 2000, -1);//已经开启了线程了不停读数据了
				retainedNum=sensorManager.scp.analogNum;
			}
			else {
				sensorService.startCycle(Constants.NO_CACHE_RESULT1, sensorManager, 2000, -1);//已经开启了线程了不停读数据了
				retainedNum=sensorManager.scp.getCurrentResultNum();
			}
			
		}
		
		/**
		 * 销毁线程
		 */
		public void destory(){
			
			sensorService.destoryCycle();
			
			sensorService.backEvents.removeBackListener(this);
				
				
				
			}
			
		

		@Override
		public void onDroneEvent(BackEventsType event, Object object) {
			
			String value="";
			
			System.out.println(event.toString());
			//if(isAnalog)
			
			switch(event){
			
			case NO_CACHE_VALUE:
				
				
				if(object==null){
					Toast.makeText(context, "通信出错!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				
				value=ToolUtil.roundTOString((Float)object, retainedNum);
				
				updateData(value);
			}
			
		}
	
		
	}
	
	
	
	
	
	

	@Override
	public void inputDialog(String title, final Handler handler) {
		
		View  view= LayoutInflater.from(context).inflate(R.layout.dialog_only_number_edit, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setView(view);
		

		final EditText text = (EditText) view.findViewById(R.id.only_number_edit_text);
		
	
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//closeKeyBoad(text);
				
				Message msg= new Message();
				msg.getData().putString("back", text.getText().toString());
				handler.sendMessage(msg);

			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//closeKeyBoad(text);
			}
		});

		AlertDialog dialog= builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	
		//displayKeyboard(text);
		
	}
	
	@Override
	public void inputDialogPlus(String title, final Handler handler,final int slaveId) {

		View  view = LayoutInflater.from(context).inflate(R.layout.dialog_number_handle, null);
		
		final CustomTextViewButton value = (CustomTextViewButton) view.findViewById(R.id.custom_slave_id);

		value.setCurrentValue(slaveId);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setView(view);

		builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
				String str = value.getCurrentValue();
				  try{
					 int data = Integer.parseInt(str);

					 Message msg= new Message();
					 msg.getData().putInt("back", data);
					 handler.sendMessage(msg);
					
				 }catch(Exception e){
					 
				 }
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//closeKeyBoad(text);
			}
		});

		AlertDialog dialog= builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		//displayKeyboard(text);
		  
	}
	
	@Override
	public void inputDialogMinMax(String title, final Handler handler,float min,float max) {
		

		View view= LayoutInflater.from(context).inflate(R.layout.dialog_min_max, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setView(view);

		final EditText textMin = (EditText) view.findViewById(R.id.rangevalue_1);
		final EditText textMax = (EditText) view.findViewById(R.id.rangevalue_2);
		textMin.setText(String.valueOf(min));
		textMax.setText(String.valueOf(max));
	
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//closeKeyBoad(text);
				
				Message msg= new Message();
				msg.getData().putString("min", textMin.getText().toString());
				msg.getData().putString("max", textMax.getText().toString());
//				msg.getData().putInt("key",2);
				handler.sendMessage(msg);
				
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//closeKeyBoad(text);
//				
//				Message m= new Message();
//				m.getData().putInt("key",1);
//				handler.sendMessage(m);
			}
		});

		builder.create().show();
		//displayKeyboard(text);
	}

	@Override
	public void listInputDialog(String title,
			final String[] contextNames, final Handler handler) {

		final int[] selectIndex = new int[1];

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setSingleChoiceItems(contextNames,
				android.R.layout.select_dialog_singlechoice,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						selectIndex[0] = which;

					}

				});

		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
				Message msg= new Message();
				msg.getData().putInt("back",selectIndex[0]);
				handler.sendMessage(msg);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {

			}
		});

		AlertDialog dialog= builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

	}

	@Override
	public void dynamicInputDialog(String title, final Handler handler,
			final SensorManager sensorManager, final boolean isAnalog) {
		
	
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_dynamic_reader, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		final EditText text = (EditText) view
				.findViewById(R.id.dynamic_edit_text);
		
		final CommunionService cs=new CommunionService(sensorManager,isAnalog){

			@Override
			public void updateData(String value) {
				
				System.out.println("updateData"+value);
				text.setText(value);
				
			}
			
		};
		
		final ToggleButton readStop = (ToggleButton) view.findViewById(R.id.dema_read_button); // 读取和暂停按钮

		readStop.setChecked(true);
		
		readStop.setOnClickListener(new OnClickListener() {// 添加监听
			@Override
			public void onClick(View view) {
				ToggleButton button=(ToggleButton)view;
				if(!button.isChecked()){//打开线程	
					
					cs.begion();
				}else{
					cs.destory();
				}

			}

		});

		Button clear = (Button)view
				.findViewById(R.id.dema_clear_button); // 清除按钮

		clear.setOnClickListener(new OnClickListener() {// 为清除添加监听

			@Override
			public void onClick(View v) {
				text.setText("");// 0代表清除

			}

		});
		
	
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//closeKeyBoad(text);

				Message msg= new Message();
				msg.getData().putString("back",text.getText().toString());
				handler.sendMessage(msg);
				
				cs.destory();

			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				cs.destory();
			}
		});
		
		builder.setTitle(title);
		builder.setView(view);

		AlertDialog dialog= builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	

	/**
	 * 点击NH4+工作曲线标定进入下一界面时，弹出的对话框
	 */
	@Override
	public void setNH4ConfigDialog(String title, final Handler handler) {
		
		View  view = LayoutInflater.from(context).inflate(R.layout.dialog_nh4_workchartdema, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setView(view);

		final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
		final RadioButton densityRadio = (RadioButton) view.findViewById(R.id.densityRadio);
		final RadioButton diluteRadio = (RadioButton) view.findViewById(R.id.diluteRadio);
		final RadioButton ionicRadio = (RadioButton) view.findViewById(R.id.ionic);
		final RadioButton noIonicRadio = (RadioButton) view.findViewById(R.id.noIonic);
		ionicRadio.setChecked(true);//默认选"添加离子强度剂法"
		densityRadio.setChecked(true);//默认选"浓度值输入"
		
		densityRadio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				densityRadio.setChecked(true);
				diluteRadio.setChecked(false);
			}
		});
	
		diluteRadio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				densityRadio.setChecked(false);
				diluteRadio.setChecked(true);
			}
		});
		
		ionicRadio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ionicRadio.setChecked(true);
				noIonicRadio.setChecked(false);
			}
		});
	
		noIonicRadio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ionicRadio.setChecked(false);
				noIonicRadio.setChecked(true);
			}
		});
		
		ArrayAdapter<CharSequence> adapterData = ArrayAdapter.createFromResource(context
													, R.array.densityUnit
													, android.R.layout.simple_spinner_item);
		adapterData.setDropDownViewResource(R.layout.spinner_item_stypelayout);

		spinner.setAdapter(adapterData);
		spinner.setSelection(0);
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
										int position, long id) {

//			unit = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
		}

		});
		
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
				int method =0 ,ionic=0;
				if(densityRadio.isChecked())
					method=0;
				else if(diluteRadio.isChecked())
					method=1;
		
				if(ionicRadio.isChecked())
					ionic=1;
				else if(noIonicRadio.isChecked())
					ionic=0;
				
				Message msg= new Message();
				Bundle bun = msg.getData();
				bun.putInt("ionic",ionic); //校准方法
				bun.putInt("method",method); //输入数据类型
//				bun.putInt("unit", unit);//单位
				handler.sendMessage(msg);

			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Activity) context).finish();
			}
		});

		AlertDialog dialog= builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	
	}

	/**
	 * 动态读取模拟量 和温度值的线程
	 * 
	 * @author Administrator
	 * 
	 */
//	public class ReaderThread extends Thread {
//
//		private SensorManager sensorManager;
//		private boolean isAnalog;
//
//		private boolean flag = true;// 控制线程结束
//
//		private Handler handler = null;
//
//		public ReaderThread(SensorManager sensorManager, boolean isAnalog,
//				Handler handler) {
//			this.sensorManager = sensorManager;
//			this.isAnalog = isAnalog;
//			this.handler = handler;
//		}
//
//		public void run() {// 每隔3秒读一次
//			while (flag) {
//				Message msg = new Message();
//				msg.what = 1;
//				Bundle bundle = new Bundle();
//				try {
//					if (isAnalog) {
//						System.out.println("isAnalog");
//						bundle.putString("value",String.valueOf(sensorManager.readAnalogValue()));
//						msg.setData(bundle);
//					} else {
//						System.out.println("isResult");
//						bundle.putString("value",String.valueOf(sensorManager.readResultValue()) );
//						msg.setData(bundle);
//					}
//				} catch (Exception e) {
//					System.out.println("读取模拟量或结果值发生错误");
//					if(e instanceof TransferException){
//						System.out.println("111");
//						bundle.putString("value", "NaN" );
//						msg.setData(bundle);
//					}
//					if(e instanceof ErrorCodeException){
//						System.out.println("222");
//						if(e.getMessage().equals("9")){
//							bundle.putString("value", "NaN" );
//							msg.setData(bundle);
//							
//						}else{
//							bundle.putString("value", "NaN" );
//							msg.setData(bundle);
//						}
//					
//					}
//				}
//				
//				handler.sendMessage(msg);
//
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {}
//	
//			}
//		}
//
//		public void setFlag(boolean flag) {
//			this.flag = flag;
//		}
//
//	}
//	
	/**
	 * 动态读取NH4标定数据的线程
	 * @author Administrator
	 * 
//	 */
//	public class ReaderThread_NH4 extends Thread {
//
//		private SensorManager sensorManagerMain;
//		private SensorManager sensorManagerTem;
//		private int tem_interval;
//		private boolean flag = true;// 控制线程结束
//		private float temReference = -100f;
//
//		private Handler handler = null;
//
//		public ReaderThread_NH4(SensorManager sensorManager
//									, Handler handler
//									, int tem_interval) {
//			this.sensorManagerMain = sensorManager;
//			//this.sensorManagerTem = sensorManager.getSlaveTemSensorManager();
//			this.handler = handler;
//			this.tem_interval= tem_interval;
//			
//		}
//
//		public void run() {
//			while (flag) {
//				Message msg = new Message();
//				msg.what = 1;
//				Bundle bundle = new Bundle();
//				try {
//					float t = sensorManagerTem.getSensor().readSensorResultR1(); //读取温度
//					if(t-temReference >= tem_interval){
//						bundle.putString("value_tem",String.valueOf(t)); 
//						bundle.putString("value_emf",String.valueOf(sensorManagerMain.readAnalogValue())); //读取电动势
//						temReference = t ;
//					}
//					msg.setData(bundle);
//					
//				} catch (Exception e) {
//					System.out.println("读取模拟量或结果值发生错误");
//					if(e instanceof TransferException){
//						System.out.println("111");
//						bundle.putString("value", "NaN" );
//						msg.setData(bundle);
//					}
//					if(e instanceof ErrorCodeException){
//						System.out.println("222");
//						if(e.getMessage().equals("9")){
//							bundle.putString("value", "NaN" );
//							msg.setData(bundle);
//							
//						}else{
//							bundle.putString("value", "NaN" );
//							msg.setData(bundle);
//						}
//					
//					}
//				}
//				
//				handler.sendMessage(msg);
//
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {}
//	
//			}
//		}
//
//		public void setFlag(boolean flag) {
//			this.flag = flag;
//		}
//
//	}



//	@Override
//	public void onSensorChanged(SensorEvent event) {
//
//		if(event.sensor.getType()==Sensor.TYPE_PRESSURE){
////			pressure =Tools.roundValue((event.values[0])/10, 2);
////			text.setText(String.valueOf(pressure));
//		}
//		
//	}
//
//	@Override
//	public void onAccuracyChanged(Sensor sensor, int accuracy) {
//		// TODO Auto-generated method stub
//		
//	}
	
	
//	/**
//	 * 获得焦点弹出软键盘
//	 */
//	public void displayKeyboard(final EditText text){
//		
//		(new Handler()).postDelayed(new Runnable() {
//            public void run() {
//                InputMethodManager imm = (InputMethodManager)text	
//                    							.getContext()
//                    							.getSystemService(Context.INPUT_METHOD_SERVICE);
//                  
//                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//            }
//        }, 300);
//	}
//
//	/**
//	 * 关闭键盘
//	 * @param text
//	 */
//	public void closeKeyBoad(){
//		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);     
//	    imm.hideSoftInputFromWindow(text.getWindowToken(), 0);     
//	    imm.hideSoftInputFromWindow(windowToken, flags)
//	}

	
}
