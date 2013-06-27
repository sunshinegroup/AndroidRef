package com.byff.easystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import com.byff.easystem.R;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceActivity.Header;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button login;
	Button reset;
	EditText username;
	EditText password;
	CheckBox saveInfo;
	static Handler myHandler;
	final static int PROGRESS_DIALOG = 1;
	final static int Flag = 2;
	public static boolean loginFlag = false;
	// flag for Internet connection status
	Boolean isInternetPresent = false;
	// Connection detector class
	ConnectionDetector cd;
	protected List<Cookie> mCookies;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // ����ʹ���Զ������
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);// �Զ��岼�ָ�ֵ

		login = (Button) findViewById(R.id.button2);
		reset = (Button) findViewById(R.id.ok);
		username = (EditText) findViewById(R.id.editText1);
		password = (EditText) findViewById(R.id.editText2);
		saveInfo = (CheckBox) findViewById(R.id.checkBox1);
		cd = new ConnectionDetector(getApplicationContext());
		username.setSelectAllOnFocus(true);
		password.setSelectAllOnFocus(true);// ����ʱȫѡ

		checkInternetState();// ��ȡ����״̬
		if (fileExists()) {// �жϱ����Ƿ�����û���Ϣ

			getUserInfo();// ��ȡ�û���Ϣ
		}
		reset.setOnClickListener(resetButtonClick);
		login.setOnClickListener(loginButtonClick);
		setTitle("�û���¼---����ϵͳ");
		myHandler = new Handler() {
			public void handleMessage(Message msg) {
				String errString = msg.getData().getString("exception");
				int res = msg.getData().getInt("res");
				int userinfo = msg.getData().getInt("userinfo");
				int state = msg.getData().getInt("state");/*
														 * 0:δ֪���� 1��������Ӧ 2������ʱ
														 * 3���ȴ����ݳ�ʱ
														 * 4������ǰ�޷������������������
														 */
				switch (state) {
				case 0:
					dismissDialog(PROGRESS_DIALOG);
					Toast.makeText(getApplicationContext(),
							"δ֪����\n" + errString, Toast.LENGTH_SHORT).show();
					break;
				case 1:
					dismissDialog(PROGRESS_DIALOG);
					switch (res) {
					case 0:

						Toast.makeText(getApplicationContext(), "�û��������ڣ���ȷ�ϣ�",
								Toast.LENGTH_SHORT).show();
						break;
					case 1:
						Toast.makeText(getApplicationContext(), "���벻��ȷ�����������룡",
								Toast.LENGTH_SHORT).show();
						break;
					case 2:
						getUserInfo.getInfo(mCookies, getApplicationContext());
						Toast.makeText(getApplicationContext(), "��¼�ɹ���",
								Toast.LENGTH_SHORT).show();

						Intent intent = new Intent(MainActivity.this,
								UserMain.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();// ���ٵ�ǰactivity����
						break;
					default:
						break;
					}
					break;
				case 2:
					dismissDialog(PROGRESS_DIALOG);
					Toast.makeText(getApplicationContext(), "����ʱ��������",
							Toast.LENGTH_SHORT).show();
					break;
				case 3:
					dismissDialog(PROGRESS_DIALOG);
					Toast.makeText(getApplicationContext(), "�ȴ��������ݳ�ʱ��������",
							Toast.LENGTH_SHORT).show();
					break;
				case 4:
					dismissDialog(PROGRESS_DIALOG);
					Toast.makeText(getApplicationContext(), "�������\n����������������",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					dismissDialog(PROGRESS_DIALOG);
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	// �˳���ʾ
	@Override
	public void onBackPressed() {

		AlertDialog.Builder alertbBuilder = new AlertDialog.Builder(this);
		alertbBuilder.setTitle("�˳�ȷ��").setMessage("��ȷ��Ҫ�˳���")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// �������Activity
						finish();
						System.exit(0);
					}
				})
				.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();

					}
				}).create();
		alertbBuilder.show();

	}

	// �˵���
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "����");

		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "����");

		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "�˳�");

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case Menu.FIRST + 1:

			Intent intent2 = new Intent(getApplicationContext(), Help.class);
			startActivity(intent2);
			break;

		case Menu.FIRST + 2:

			Intent about = new Intent(getApplicationContext(), about.class);
			startActivity(about);
			break;

		case Menu.FIRST + 3:

			System.exit(0);
			break;

		}
		return false;

	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			return ProgressDialog.show(this, "���Ժ�", "���ڵ�¼...");
		case Flag:
			return ProgressDialog.show(this, "���Ժ�", "��������...");
		default:
			return null;
		}

	}

	// ���ð�ť�¼�
	private Button.OnClickListener resetButtonClick = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO �Զ����ɵķ������
			username.setText("");
			password.setText("");
		}

	};
	// ��¼��ť�¼�
	private Button.OnClickListener loginButtonClick = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO �Զ����ɵķ������
			final String usernameString = username.getText().toString().trim();
			final String pwdString = password.getText().toString();
			UserMain.userID = usernameString;
			Log.v("��ʾ��",usernameString + "@" + pwdString);
			if (!(usernameString.equals("") || pwdString.equals(""))) {

				if (saveInfo.isChecked()) {
					try {
						saveUserInfo(usernameString, pwdString);
					} catch (Exception e) {
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					}
				} else {
					if (fileExists()) {
						try {
							saveUserInfo("", "");
						} catch (Exception e) {
							// TODO �Զ����ɵ� catch ��
							e.printStackTrace();
						}
					}
				}
				showDialog(PROGRESS_DIALOG); // �ȴ�Dialog
				Login(usernameString, pwdString);

			}

			else {
				Toast.makeText(getApplicationContext(), "�û��������벻��Ϊ�գ�",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	// ����AlertDialog
	public void showAlertDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setIcon(android.R.drawable.alert_dark_frame);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("��APN",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// ������ӵ��ȷ������߼�
						Intent intent = new Intent("/");
						ComponentName cm = new ComponentName(
								"com.android.settings",
								"com.android.settings.ApnSettings");
						intent.setComponent(cm);
						intent.setAction("android.intent.action.VIEW");
						startActivityForResult(intent, 0);

					}
				});
		builder.setNeutralButton("��WLAN",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// ������ӵ��ȷ������߼�
						Intent intent = new Intent("/");
						ComponentName cm = new ComponentName(
								"com.android.settings",
								"com.android.settings.WirelessSettings");
						intent.setComponent(cm);
						intent.setAction("android.intent.action.VIEW");
						startActivityForResult(intent, 0);

					}
				});

		builder.setNegativeButton("�˳�", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// ������ӵ��ȷ������߼�
				System.exit(0);
			}
		});
		builder.create().show();

	}

	// �鿴����״̬
	public void checkInternetState() {
		// get Internet status
		isInternetPresent = cd.isConnectingToInternet();

		// check for Internet status
		if (!isInternetPresent) {
			// Internet Connection is Present
			// make HTTP requests
			showAlertDialog("���������", "����ǰû�л����������\n�Ƿ���������ã�");

		}
	}

	// �����û���Ϣ
	public void saveUserInfo(String username, String pwd) throws Exception {
		String contentString = username + "@" + pwd;
		contentString = encryptionANDdecryption.encrypt(
				"!@#$%^&*()_+asdfghjkl;", contentString);// ����

		OutputStream os = null;
		try {
			os = MainActivity.this.openFileOutput("userinfo",
					Context.MODE_PRIVATE);// ����Դ�ļ�
			os.write(contentString.getBytes());
			os.close();
			Log.v("��ʾ��","�û���Ϣ����ɹ�");
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("��ʾ��","�û���Ϣ����ʧ��");
		}
	}

	// ��ȡ�û���Ϣ
	public void getUserInfo() {
		InputStream is = null;
		try {
			is = MainActivity.this.openFileInput("userinfo");
			ByteArrayOutputStream boStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[102];
			int len = -1;
			while ((len = is.read(buffer)) != -1) {
				boStream.write(buffer, 0, len);
			}
			String ss = encryptionANDdecryption.decrypt(
					"!@#$%^&*()_+asdfghjkl;", boStream.toString());
			String[] info = ss.split("@");
			username.setText(info[0]);
			password.setText(info[1]);
			Log.v("��ʾ��","�ɹ���ȡ�����û���Ϣ");
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("��ʾ��","�޷���ȡ�����û���Ϣ");
		}
	}

	// �ж��Ƿ�����û���Ϣ
	public boolean fileExists() {
		Context context = MainActivity.this;// ���ȣ���Activity���ȡcontext
		File f = context.getFilesDir();
		String path = f.getAbsolutePath();
		Log.v("��ʾ��","�û������ļ����ǣ�" + path);
		File file = new File(path + "//userinfo");
		if (file.exists()) { // �ж��ļ��Ƿ����

			return true;
		} else {

			return false;
		}
	}

	// �û���¼
	public void Login(final String username, final String pwd) {
		loginFlag = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO �Զ����ɵķ������
				while (loginFlag) {
					Looper.prepare();// /Ĭ������£��߳���û����Ϣѭ���ģ�����Ҫ����
										// Looper.prepare()�����̴߳�����Ϣѭ��
					// ����Bundle��Message֪ͨui�̸߳���
					Message msg = myHandler.obtainMessage();
					Bundle bundle = new Bundle();
					try {
						BasicHttpParams httpParams = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(httpParams,
								8 * 1000); // ����ʱ
						HttpConnectionParams
								.setSoTimeout(httpParams, 10 * 1000); // �ȴ����ݳ�ʱ
						HttpPost httppost = new HttpPost(
								"http://jw.fdygxy.cn:8081/default3.aspx");
						// ����µ�HttpClient
						HttpClient httpclient = new DefaultHttpClient(
								httpParams);
						// �����Ϣ
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs
								.add(new BasicNameValuePair(
										"__VIEWSTATE",
										"dDwtNjg3Njk1NzQ3O3Q8O2w8aTwxPjs+O2w8dDw7bDxpPDg+O2k8MTM+O2k8MTU+Oz47bDx0PHA8O3A8bDxvbmNsaWNrOz47bDx3aW5kb3cuY2xvc2UoKVw7Oz4+Pjs7Pjt0PHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Ozs+O3Q8cDxsPFZpc2libGU7PjtsPG88Zj47Pj47Oz47Pj47Pj47bDxpbWdETDtpbWdUQzs+PnC25DtPgOlDjO3PfyGQBEDH78tF"));
						nameValuePairs
								.add(new BasicNameValuePair("ddlSF", "ѧ��"));
						nameValuePairs.add(new BasicNameValuePair("imgDL.x",
								"33"));
						nameValuePairs.add(new BasicNameValuePair("imgDL.y",
								"6"));
						nameValuePairs.add(new BasicNameValuePair("tbYHM",
								username));
						nameValuePairs
								.add(new BasicNameValuePair("tbPSW", pwd));
						httppost.setEntity(new UrlEncodedFormEntity(
								nameValuePairs, "gb2312"));

						// ���� HTTP Post ����
						HttpResponse response = httpclient.execute(httppost);

						// ���������ص�����
						String responseString = null;
						if (response.getEntity() != null) {
							responseString = EntityUtils.toString(response
									.getEntity());
							// ���ط�������Ӧ��HTML����

						}
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
								&& responseString
										.contains("<title>�ۺϽ������ϵͳ��WEB�������ϵͳ</title>")) {
							bundle.putInt("res", 2);
							bundle.putInt("state", 1);
							mCookies = ((AbstractHttpClient) httpclient)
									.getCookieStore().getCookies();
							UserMain.mCookies = mCookies;

						} else if (responseString.contains("���벻��ȷ")) {
							Log.v("��ʾ��","���벻��ȷ");
							bundle.putInt("res", 1);
							bundle.putInt("state", 1);

						} else if (responseString.contains("�û�������")) {
							Log.v("��ʾ��","�û�������");
							bundle.putInt("res", 0);
							bundle.putInt("state", 1);

						}

					} catch (ConnectTimeoutException e) {// ����ʱ
						e.printStackTrace();
						bundle.putInt("state", 2);

					} catch (SocketTimeoutException e) {// �������ݳ�ʱ
						e.printStackTrace();
						bundle.putInt("state", 3);

					} catch (UnknownHostException e) {// δ֪��ַ����ʾ����ǰ�޷������������������
						e.printStackTrace();
						bundle.putInt("state", 4);

					} catch (Exception e) {
						e.printStackTrace();
						bundle.putInt("state", 0);
						bundle.putString("exception", e.toString());

					} finally {
						msg.setData(bundle);
						myHandler.sendMessage(msg);
						loginFlag = false;
					}
				}
			}
		}).start();
	}

}
