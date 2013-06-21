package com.phone.num;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

/**
 * @ 作者：yh_android
 * 
 * @ 功能：获取用户的手机号码、运营商，需要android.permission.READ_PHONE_STATE权限
 * 
 * @ 日期：2013-4-23
 */
public class GetPhoneInfo extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView tv_num1 = (TextView) findViewById(R.id.num1);
		TextView tv_num2 = (TextView) findViewById(R.id.num2);

		/*getPhoneNumAndProvider(tv_num1, tv_num2);
		getCallRecord();*/
		getSmsInfo();
	}

	/**
	 * @功能：获取用户的手机号码、运营商
	 */
	private void getPhoneNumAndProvider(TextView tv_num1, TextView tv_num2) {
		SIMCardInfo siminfo = new SIMCardInfo(GetPhoneInfo.this);
		System.out.println(siminfo.getProvidersName());
		System.out.println(siminfo.getNativePhoneNumber());
		tv_num1.setText(siminfo.getNativePhoneNumber());
		tv_num2.setText(siminfo.getProvidersName());
	}

	/**
	 * @功能：获取用户的通话记录
	 */
	private void getCallRecord() {
		String strNumber, strName = "";
		int type;
		long callTime;
		Date date;
		String time = "";
		ContentResolver resolver = getContentResolver();
		final Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, new String[] { CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME,
				CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION }, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
		for (int i = 0; i < cursor.getCount(); i++) {
			// 循环显示所有的通话记录
			cursor.moveToPosition(i);
			strNumber = cursor.getString(0); // 呼叫号码
			strName = cursor.getString(1); // 联系人姓名
			type = cursor.getInt(2); // 来电:1,拨出:2,未接:3

			/*public static final int INCOMING_TYPE = 1;
			public static final int OUTGOING_TYPE = 2;
			public static final int MISSED_TYPE = 3;*/

			long duration = cursor.getLong(4);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			date = new Date(Long.parseLong(cursor.getString(3)));
			time = format.format(date);

			System.out.println("number----" + strNumber);
			System.out.println("name----" + strName);
			String typeStr = null;
			switch (type) {
			case 1:
				typeStr = "来电";
				break;
			case 2:
				typeStr = "拨出";
				break;
			case 3:
				typeStr = "未接";
				break;
			default:
				break;
			}
			System.out.println("type---------------" + typeStr);
			System.out.println("time----------------" + time);
			String durationStr = "";
			int hour = (int) (duration / 3600);
			int minutes = (int) ((duration - 3600 * hour) / 60);
			int second = (int) (duration % 60);
			if (hour != 0) {
				durationStr = hour + "时";
				durationStr = durationStr + minutes + "分";
				durationStr = durationStr + second + "秒";
			} else {
				if (minutes != 0) {
					durationStr = durationStr + minutes + "分";
				}
				durationStr = durationStr + second + "秒";
			}
			System.out.println("duration--------------" + durationStr);
			System.out.println("======================================");
		}
	}

	/**
	 * @功能：获取短信信息
	 * @时间：2013-4-23 下午5:42:26
	 */
	private void getSmsInfo() {
		final String SMS_URI_ALL = "content://sms/";
		final String SMS_URI_INBOX = "content://sms/inbox";
		final String SMS_URI_SEND = "content://sms/sent";
		final String SMS_URI_DRAFT = "content://sms/draft";
		StringBuilder smsBuilder = new StringBuilder();
		try {
			ContentResolver resolver = getContentResolver();
			String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
			Uri uri = Uri.parse(SMS_URI_ALL);
			String[] selectStr = new String[] { "18500192785" };
			Cursor cursor = resolver.query(uri, projection, "address=?", selectStr, "date desc");
			if (cursor.moveToFirst()) {
				String name;
				String phoneNumber;
				String smsbody;
				String date;
				String type;
				int nameColumn = cursor.getColumnIndex("person");
				int phoneNumberColumn = cursor.getColumnIndex("address");
				int smsbodyColumn = cursor.getColumnIndex("body");
				int dateColumn = cursor.getColumnIndex("date");
				int typeColumn = cursor.getColumnIndex("type");
				do {
					name = cursor.getString(nameColumn);
					phoneNumber = cursor.getString(phoneNumberColumn);
					smsbody = cursor.getString(smsbodyColumn);
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date d = new Date(Long.parseLong(cursor.getString(dateColumn)));
					date = dateFormat.format(d);
					int typeId = cursor.getInt(typeColumn);
					if (typeId == 1) {
						type = "接收";
					} else if (typeId == 2) {
						type = "发送";
					} else {
						type = "其他";
					}
					smsBuilder.append("[");
					smsBuilder.append(name + ",");
					smsBuilder.append(phoneNumber + ",");
					smsBuilder.append(smsbody + ",");
					smsBuilder.append(date + ",");
					smsBuilder.append(type);
					smsBuilder.append("] ");

					if (smsbody == null)
						smsbody = "";
				} while (cursor.moveToNext());
			} else {
				smsBuilder.append("no result!");
			}
			smsBuilder.append("getSmsInPhone has executed!");
		} catch (SQLiteException ex) {
			Log.d("SQLiteException in getSmsInPhone", ex.getMessage());
		}
		System.out.println(smsBuilder.toString());
	}
}

class SIMCardInfo {
	private TelephonyManager telephonyManager;
	private String IMSI;

	public SIMCardInfo(Context context) {
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	/**
	 * @功能：获取用户手机号码
	 */
	public String getNativePhoneNumber() {
		String NativePhoneNumber = null;
		NativePhoneNumber = telephonyManager.getLine1Number();
		return NativePhoneNumber;
	}

	/**
	 * @功能：获取用户运营商名称
	 */
	public String getProvidersName() {
		String ProvidersName = null;
		// 返回唯一的用户ID;就是这张卡的IMSI编号
		IMSI = telephonyManager.getSubscriberId();
		// IMSI号前面3位460是国家，紧接着后面2位，00与02是中国移动，01是中国联通，03是中国电信。
		System.out.println(IMSI);
		if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
			ProvidersName = "中国移动";
		} else if (IMSI.startsWith("46001")) {
			ProvidersName = "中国联通";
		} else if (IMSI.startsWith("46003")) {
			ProvidersName = "中国电信";
		}
		return ProvidersName;
	}
}
