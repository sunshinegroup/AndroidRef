package com.example.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.logindemo.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class UpdateService extends Service {
	// ����
	private int titleId = 0;
	private final static int DOWNLOAD_COMPLETE = 0;
	private final static int DOWNLOAD_FAIL = 1;
	// �ļ��洢
	private File updateDir = null;
	private File updateFile = null;

	// ֪ͨ��
	private NotificationManager updateNotificationManager = null;
	private Notification updateNotification = null;
	// ֪ͨ����תIntent
	private Intent updateIntent = null;
	private PendingIntent updatePendingIntent = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {

		super.onCreate();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// ��ȡ��ֵ
		titleId = intent.getIntExtra("titleId", 0);
		// �����ļ�
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			updateDir = new File(Environment.getExternalStorageDirectory(),
					"download/");
			updateFile = new File(updateDir.getPath(), getResources()
					.getString(titleId) + ".apk");
		}

		this.updateNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		this.updateNotification = new Notification();

		// �������ع����У����֪ͨ�����ص�������
//		updateIntent = new Intent(this, main.class);
//		updatePendingIntent = PendingIntent.getActivity(this, 0, updateIntent,0);
		// ����֪ͨ����ʾ����
		updateNotification.icon = R.drawable.ic_launcher;
		updateNotification.tickerText = "��ʼ����";
		updateNotification.setLatestEventInfo(this, "Group", "0%",
				updatePendingIntent);
		// ����֪ͨ
		updateNotificationManager.notify(0, updateNotification);

		// ����һ���µ��߳����أ����ʹ��Serviceͬ�����أ��ᵼ��ANR���⣬Service����Ҳ������
		new Thread(new updateRunnable()).start();// ��������ص��ص㣬�����صĹ���

		return super.onStartCommand(intent, flags, startId);
	}

	private Handler updateHandler = new Handler() {
		@Override
		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD_COMPLETE:
				updateNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				// �����װPendingIntent
				Uri uri = Uri.fromFile(updateFile);
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setDataAndType(uri,
						"application/vnd.android.package-archive");
				updatePendingIntent = PendingIntent.getActivity(
						UpdateService.this, 0, installIntent, 0);

				updateNotification.defaults = Notification.DEFAULT_SOUND;// ��������
				updateNotification.setLatestEventInfo(UpdateService.this,
						"Group", "������ɣ������װ��", updatePendingIntent); //
				updateNotificationManager.notify(0, updateNotification);
				
				
				// ֹͣ����
//				stopService(updateIntent);
			case DOWNLOAD_FAIL:
				// ����ʧ��
				updateNotification.setLatestEventInfo(UpdateService.this,
						"Group", "����ʧ�ܡ�", updatePendingIntent);
				stopService(updateIntent);
			default:
				stopService(updateIntent);
			}
		}
	};

	class updateRunnable implements Runnable {
		Message message = updateHandler.obtainMessage();

		@Override
		public void run() {
			message.what = DOWNLOAD_COMPLETE;
			try {
				// ����Ȩ��<uses-permission
				// android:name="android.permission.WRITE_EXTERNAL_STORAGE">;
				if (!updateDir.exists()) {
					updateDir.mkdirs();
				}
				if (!updateFile.exists()) {
					updateFile.createNewFile();
				}
				// ����Ȩ��<uses-permission
				// android:name="android.permission.INTERNET">;
				long downloadSize = downloadUpdateFile(
						"http://www.mofriend.net/dev/LoginDemo.apk", updateFile);
				if (downloadSize > 0) {
					// ���سɹ�
					updateHandler.sendMessage(message);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				message.what = DOWNLOAD_FAIL;
				// ����ʧ��
				updateHandler.sendMessage(message);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public long downloadUpdateFile(String downloadUrl, File saveFile)
			throws Exception {
		// ����
		int downloadCount = 0;
		int currentSize = 0;
		long totalSize = 0;
		int updateTotalSize = 0;

		HttpURLConnection httpConnection = null;
		InputStream is = null;
		FileOutputStream fos = null;

		try {
			URL url = new URL(downloadUrl);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection
					.setRequestProperty("User-Agent", "PacificHttpClient");
			if (currentSize > 0) {
				httpConnection.setRequestProperty("RANGE", "bytes="
						+ currentSize + "-");
			}
			httpConnection.setConnectTimeout(10000);
			httpConnection.setReadTimeout(20000);
			updateTotalSize = httpConnection.getContentLength();
			if (httpConnection.getResponseCode() == 404) {
				throw new Exception("fail!");
			}
			is = httpConnection.getInputStream();
			fos = new FileOutputStream(saveFile, false);
			byte buffer[] = new byte[4096];
			int readsize = 0;
			while ((readsize = is.read(buffer)) > 0) {
				fos.write(buffer, 0, readsize);
				totalSize += readsize;
				// Ϊ�˷�ֹƵ����֪ͨ����Ӧ�óԽ����ٷֱ�����10��֪ͨһ��
				if ((downloadCount == 0)
						|| (int) (totalSize * 100 / updateTotalSize) - 10 > downloadCount) {
					downloadCount += 10;
					updateNotification.setLatestEventInfo(UpdateService.this,
							"��������", (int) totalSize * 100 / updateTotalSize
									+ "%", updatePendingIntent);
					updateNotificationManager.notify(0, updateNotification);
				}
			}
		} finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
			if (is != null) {
				is.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
		return totalSize;
	}

}
