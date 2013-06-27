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
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);// 自定义布局赋值

		login = (Button) findViewById(R.id.button2);
		reset = (Button) findViewById(R.id.ok);
		username = (EditText) findViewById(R.id.editText1);
		password = (EditText) findViewById(R.id.editText2);
		saveInfo = (CheckBox) findViewById(R.id.checkBox1);
		cd = new ConnectionDetector(getApplicationContext());
		username.setSelectAllOnFocus(true);
		password.setSelectAllOnFocus(true);// 焦点时全选

		checkInternetState();// 获取网络状态
		if (fileExists()) {// 判断本地是否存有用户信息

			getUserInfo();// 获取用户信息
		}
		reset.setOnClickListener(resetButtonClick);
		login.setOnClickListener(loginButtonClick);
		setTitle("用户登录---教务系统");
		myHandler = new Handler() {
			public void handleMessage(Message msg) {
				String errString = msg.getData().getString("exception");
				int res = msg.getData().getInt("res");
				int userinfo = msg.getData().getInt("userinfo");
				int state = msg.getData().getInt("state");/*
														 * 0:未知错误 1：正常响应 2：请求超时
														 * 3：等待数据超时
														 * 4：程序当前无法访问域名，网络故障
														 */
				switch (state) {
				case 0:
					dismissDialog(PROGRESS_DIALOG);
					Toast.makeText(getApplicationContext(),
							"未知错误！\n" + errString, Toast.LENGTH_SHORT).show();
					break;
				case 1:
					dismissDialog(PROGRESS_DIALOG);
					switch (res) {
					case 0:

						Toast.makeText(getApplicationContext(), "用户名不存在，请确认！",
								Toast.LENGTH_SHORT).show();
						break;
					case 1:
						Toast.makeText(getApplicationContext(), "密码不正确，请重新输入！",
								Toast.LENGTH_SHORT).show();
						break;
					case 2:
						getUserInfo.getInfo(mCookies, getApplicationContext());
						Toast.makeText(getApplicationContext(), "登录成功！",
								Toast.LENGTH_SHORT).show();

						Intent intent = new Intent(MainActivity.this,
								UserMain.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();// 销毁当前activity对象
						break;
					default:
						break;
					}
					break;
				case 2:
					dismissDialog(PROGRESS_DIALOG);
					Toast.makeText(getApplicationContext(), "请求超时，请重试",
							Toast.LENGTH_SHORT).show();
					break;
				case 3:
					dismissDialog(PROGRESS_DIALOG);
					Toast.makeText(getApplicationContext(), "等待返回数据超时，请重试",
							Toast.LENGTH_SHORT).show();
					break;
				case 4:
					dismissDialog(PROGRESS_DIALOG);
					Toast.makeText(getApplicationContext(), "网络错误\n请检查您的网络连接",
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

	// 退出提示
	@Override
	public void onBackPressed() {

		AlertDialog.Builder alertbBuilder = new AlertDialog.Builder(this);
		alertbBuilder.setTitle("退出确认").setMessage("你确定要退出？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 结束这个Activity
						finish();
						System.exit(0);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();

					}
				}).create();
		alertbBuilder.show();

	}

	// 菜单项
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "帮助");

		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "关于");

		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "退出");

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
			return ProgressDialog.show(this, "请稍后", "正在登录...");
		case Flag:
			return ProgressDialog.show(this, "请稍后", "正在载入...");
		default:
			return null;
		}

	}

	// 重置按钮事件
	private Button.OnClickListener resetButtonClick = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			username.setText("");
			password.setText("");
		}

	};
	// 登录按钮事件
	private Button.OnClickListener loginButtonClick = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			final String usernameString = username.getText().toString().trim();
			final String pwdString = password.getText().toString();
			UserMain.userID = usernameString;
			Log.v("提示：",usernameString + "@" + pwdString);
			if (!(usernameString.equals("") || pwdString.equals(""))) {

				if (saveInfo.isChecked()) {
					try {
						saveUserInfo(usernameString, pwdString);
					} catch (Exception e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				} else {
					if (fileExists()) {
						try {
							saveUserInfo("", "");
						} catch (Exception e) {
							// TODO 自动生成的 catch 块
							e.printStackTrace();
						}
					}
				}
				showDialog(PROGRESS_DIALOG); // 等待Dialog
				Login(usernameString, pwdString);

			}

			else {
				Toast.makeText(getApplicationContext(), "用户名或密码不能为空！",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	// 设置AlertDialog
	public void showAlertDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setIcon(android.R.drawable.alert_dark_frame);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("打开APN",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// 这里添加点击确定后的逻辑
						Intent intent = new Intent("/");
						ComponentName cm = new ComponentName(
								"com.android.settings",
								"com.android.settings.ApnSettings");
						intent.setComponent(cm);
						intent.setAction("android.intent.action.VIEW");
						startActivityForResult(intent, 0);

					}
				});
		builder.setNeutralButton("打开WLAN",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// 这里添加点击确定后的逻辑
						Intent intent = new Intent("/");
						ComponentName cm = new ComponentName(
								"com.android.settings",
								"com.android.settings.WirelessSettings");
						intent.setComponent(cm);
						intent.setAction("android.intent.action.VIEW");
						startActivityForResult(intent, 0);

					}
				});

		builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// 这里添加点击确定后的逻辑
				System.exit(0);
			}
		});
		builder.create().show();

	}

	// 查看网络状态
	public void checkInternetState() {
		// get Internet status
		isInternetPresent = cd.isConnectingToInternet();

		// check for Internet status
		if (!isInternetPresent) {
			// Internet Connection is Present
			// make HTTP requests
			showAlertDialog("无网络可用", "您当前没有活动的网络连接\n是否打开网络设置？");

		}
	}

	// 保存用户信息
	public void saveUserInfo(String username, String pwd) throws Exception {
		String contentString = username + "@" + pwd;
		contentString = encryptionANDdecryption.encrypt(
				"!@#$%^&*()_+asdfghjkl;", contentString);// 加密

		OutputStream os = null;
		try {
			os = MainActivity.this.openFileOutput("userinfo",
					Context.MODE_PRIVATE);// 覆盖源文件
			os.write(contentString.getBytes());
			os.close();
			Log.v("提示：","用户信息保存成功");
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("提示：","用户信息保存失败");
		}
	}

	// 获取用户信息
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
			Log.v("提示：","成功读取本地用户信息");
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("提示：","无法读取本地用户信息");
		}
	}

	// 判断是否存在用户信息
	public boolean fileExists() {
		Context context = MainActivity.this;// 首先，在Activity里获取context
		File f = context.getFilesDir();
		String path = f.getAbsolutePath();
		Log.v("提示：","用户数据文件夹是：" + path);
		File file = new File(path + "//userinfo");
		if (file.exists()) { // 判断文件是否存在

			return true;
		} else {

			return false;
		}
	}

	// 用户登录
	public void Login(final String username, final String pwd) {
		loginFlag = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO 自动生成的方法存根
				while (loginFlag) {
					Looper.prepare();// /默认情况下，线程是没有消息循环的，所以要调用
										// Looper.prepare()来给线程创建消息循环
					// 利用Bundle和Message通知ui线程更新
					Message msg = myHandler.obtainMessage();
					Bundle bundle = new Bundle();
					try {
						BasicHttpParams httpParams = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(httpParams,
								8 * 1000); // 请求超时
						HttpConnectionParams
								.setSoTimeout(httpParams, 10 * 1000); // 等待数据超时
						HttpPost httppost = new HttpPost(
								"http://jw.fdygxy.cn:8081/default3.aspx");
						// 添加新的HttpClient
						HttpClient httpclient = new DefaultHttpClient(
								httpParams);
						// 添加信息
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs
								.add(new BasicNameValuePair(
										"__VIEWSTATE",
										"dDwtNjg3Njk1NzQ3O3Q8O2w8aTwxPjs+O2w8dDw7bDxpPDg+O2k8MTM+O2k8MTU+Oz47bDx0PHA8O3A8bDxvbmNsaWNrOz47bDx3aW5kb3cuY2xvc2UoKVw7Oz4+Pjs7Pjt0PHA8bDxWaXNpYmxlOz47bDxvPGY+Oz4+Ozs+O3Q8cDxsPFZpc2libGU7PjtsPG88Zj47Pj47Oz47Pj47Pj47bDxpbWdETDtpbWdUQzs+PnC25DtPgOlDjO3PfyGQBEDH78tF"));
						nameValuePairs
								.add(new BasicNameValuePair("ddlSF", "学生"));
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

						// 发送 HTTP Post 请求
						HttpResponse response = httpclient.execute(httppost);

						// 服务器返回的数据
						String responseString = null;
						if (response.getEntity() != null) {
							responseString = EntityUtils.toString(response
									.getEntity());
							// 返回服务器响应的HTML代码

						}
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
								&& responseString
										.contains("<title>综合教务管理系统－WEB服务管理系统</title>")) {
							bundle.putInt("res", 2);
							bundle.putInt("state", 1);
							mCookies = ((AbstractHttpClient) httpclient)
									.getCookieStore().getCookies();
							UserMain.mCookies = mCookies;

						} else if (responseString.contains("密码不正确")) {
							Log.v("提示：","密码不正确");
							bundle.putInt("res", 1);
							bundle.putInt("state", 1);

						} else if (responseString.contains("用户不存在")) {
							Log.v("提示：","用户不存在");
							bundle.putInt("res", 0);
							bundle.putInt("state", 1);

						}

					} catch (ConnectTimeoutException e) {// 请求超时
						e.printStackTrace();
						bundle.putInt("state", 2);

					} catch (SocketTimeoutException e) {// 返回数据超时
						e.printStackTrace();
						bundle.putInt("state", 3);

					} catch (UnknownHostException e) {// 未知地址，表示程序当前无法访问域名，网络故障
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
