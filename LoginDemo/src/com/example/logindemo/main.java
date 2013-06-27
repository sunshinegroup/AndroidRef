package com.example.logindemo;

import java.util.HashMap;

import com.example.update.CheckUpdate;
import com.example.update.UpdateService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class main extends Activity {
	boolean flag = true;
	HashMap<String, String> version;
	Handler myHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		myHandler = new Handler() {

			@Override
			@SuppressLint("HandlerLeak")
			public void handleMessage(Message msg) {
				int state = msg.getData().getInt("state");
				switch (state) {
				case 0:
					showNoticeDialog();
					break;
				default:
					break;
				}
			}
		};

		new Thread(new Runnable() {

			Message msg = myHandler.obtainMessage();
			Bundle bundle = new Bundle();

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (flag) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					CheckUpdate update = new CheckUpdate(
							getApplicationContext());
					if (update.isUpdate()) {
						bundle.putInt("state", 0);
						msg.setData(bundle);
						myHandler.sendMessage(msg);
					} else {
						goToWhere();
					}
					flag = false;
				}
			}

		}).start();

	}

	public void goToWhere(){
		if (isLogin() == "false") {
			Intent intent = new Intent(main.this, Login.class);
			finish();
			startActivity(intent);// ��ת��webview
		} else {
			Intent intent = new Intent(main.this,
					MyWebView.class);
			finish();
			startActivity(intent);// ��ת��webview
		}
	}
	// �ж��Ƿ����cookie
	public String isLogin() {
		SharedPreferences myShare = getApplicationContext()
				.getSharedPreferences("info", Context.MODE_PRIVATE);
		String str = myShare.getString("cookie", "false");
		return str;
	}

	public void showNoticeDialog() {
		// ����Ի���
		AlertDialog.Builder builder = new Builder(main.this);
		builder.setTitle("�����°汾");
		builder.setMessage("�Ƿ��������£�");
		// ����
		builder.setPositiveButton("����", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// ��ʾ���ضԻ���
				Intent updateIntent =new Intent(main.this, UpdateService.class);  
		            updateIntent.putExtra("titleId",R.string.app_name);  
		            startService(updateIntent); 
				goToWhere();
				 
			}
		});
		// �Ժ����
		builder.setNegativeButton("ȡ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				goToWhere();
				dialog.dismiss();
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

}
