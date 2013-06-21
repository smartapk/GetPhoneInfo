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
 * @ ���ߣ�yh_android
 * 
 * @ ���ܣ���ȡ�û����ֻ����롢��Ӫ�̣���Ҫandroid.permission.READ_PHONE_STATEȨ��
 * 
 * @ ���ڣ�2013-4-23
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
	 * @���ܣ���ȡ�û����ֻ����롢��Ӫ��
	 */
	private void getPhoneNumAndProvider(TextView tv_num1, TextView tv_num2) {
		SIMCardInfo siminfo = new SIMCardInfo(GetPhoneInfo.this);
		System.out.println(siminfo.getProvidersName());
		System.out.println(siminfo.getNativePhoneNumber());
		tv_num1.setText(siminfo.getNativePhoneNumber());
		tv_num2.setText(siminfo.getProvidersName());
	}

	/**
	 * @���ܣ���ȡ�û���ͨ����¼
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
			// ѭ����ʾ���е�ͨ����¼
			cursor.moveToPosition(i);
			strNumber = cursor.getString(0); // ���к���
			strName = cursor.getString(1); // ��ϵ������
			type = cursor.getInt(2); // ����:1,����:2,δ��:3

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
				typeStr = "����";
				break;
			case 2:
				typeStr = "����";
				break;
			case 3:
				typeStr = "δ��";
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
				durationStr = hour + "ʱ";
				durationStr = durationStr + minutes + "��";
				durationStr = durationStr + second + "��";
			} else {
				if (minutes != 0) {
					durationStr = durationStr + minutes + "��";
				}
				durationStr = durationStr + second + "��";
			}
			System.out.println("duration--------------" + durationStr);
			System.out.println("======================================");
		}
	}

	/**
	 * @���ܣ���ȡ������Ϣ
	 * @ʱ�䣺2013-4-23 ����5:42:26
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
						type = "����";
					} else if (typeId == 2) {
						type = "����";
					} else {
						type = "����";
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
	 * @���ܣ���ȡ�û��ֻ�����
	 */
	public String getNativePhoneNumber() {
		String NativePhoneNumber = null;
		NativePhoneNumber = telephonyManager.getLine1Number();
		return NativePhoneNumber;
	}

	/**
	 * @���ܣ���ȡ�û���Ӫ������
	 */
	public String getProvidersName() {
		String ProvidersName = null;
		// ����Ψһ���û�ID;�������ſ���IMSI���
		IMSI = telephonyManager.getSubscriberId();
		// IMSI��ǰ��3λ460�ǹ��ң������ź���2λ��00��02���й��ƶ���01���й���ͨ��03���й����š�
		System.out.println(IMSI);
		if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
			ProvidersName = "�й��ƶ�";
		} else if (IMSI.startsWith("46001")) {
			ProvidersName = "�й���ͨ";
		} else if (IMSI.startsWith("46003")) {
			ProvidersName = "�й�����";
		}
		return ProvidersName;
	}
}
