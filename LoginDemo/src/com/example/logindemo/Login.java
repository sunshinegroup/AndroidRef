package com.example.logindemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity {
	Button btnLogin;
	Button btnClear;
	EditText name;
	EditText password;
	TextView textView;
	Handler myHandler;
	String user;
	String pwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnLogin = (Button) findViewById(R.id.button1);
		btnClear = (Button) findViewById(R.id.button2);
		name = (EditText) findViewById(R.id.editText1);
		password = (EditText) findViewById(R.id.editText2);
		textView = (TextView) findViewById(R.id.textView1);
		textView.setMovementMethod(ScrollingMovementMethod.getInstance());
		btnLogin.setOnClickListener(Login);
		btnClear.setOnClickListener(ClearText);
		myHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int status = msg.getData().getInt("status");
				switch (status) {
				case 0:
					Toast.makeText(getApplicationContext(), "请检查用户名密码是否正确！",
							Toast.LENGTH_SHORT).show();
					textView.setText(msg.getData().getString("text"));
					break;
				case 1:
					Toast.makeText(getApplicationContext(), "登录成功！保存cookie！",
							Toast.LENGTH_SHORT).show();
					textView.setText(msg.getData().getString("text"));

					Intent intent = new Intent(Login.this, MyWebView.class);
					finish();
					startActivity(intent);// 跳转到webview
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	// 登录按钮事件
	private Button.OnClickListener Login = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			user = name.getText().toString();
			pwd = password.getText().toString();
			if (user == "" || pwd == "") {
				Toast.makeText(getApplicationContext(), "用户名或密码不能为空",
						Toast.LENGTH_LONG);
			} else {
				Login(user, pwd);
			}
		}
	};
	// 清空按钮事件
	private Button.OnClickListener ClearText = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			name.setText("");
			password.setText("");
			textView.setText("");

		}
	};

	// HttpClient 进行登录验证（post方式）
	public void Login(final String user, final String pwd) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO 自动生成的方法存根
				Looper.prepare();//
				try {
					HttpPost httppost = new HttpPost(
							"http://www.mofriend.net/index.php?app=public&mod=Passport&act=doLogin");
					// 添加新的HttpClient
					HttpClient httpclient = new DefaultHttpClient();
					// 添加信息
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							3);
					nameValuePairs.add(new BasicNameValuePair("login_email",
							user));
					nameValuePairs.add(new BasicNameValuePair("login_password",
							pwd));
					nameValuePairs.add(new BasicNameValuePair("login_remember",
							"1"));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
							HTTP.UTF_8));

					httppost.setHeader("Referer",
							"http://www.mofriend.net/index.php?app=public&mod=Passport&act=login");
					// 发送 HTTP Post 请求
					HttpResponse response = httpclient.execute(httppost);
					String responseString = null;
					if (response.getEntity() != null) {
						responseString = EntityUtils.toString(response
								.getEntity());
						// 返回服务器响应的HTML代码
						System.out.println(responseString);

						JSONObject jsonObject = new JSONObject(responseString);
						System.out.println("登录状态！statue："
								+ jsonObject.getString("status"));

						// 利用Bundle和Message通知ui线程更新
						Message msg = myHandler.obtainMessage();
						Bundle bundle = new Bundle();

						if (jsonObject.getString("status").equals("1")) {
							System.out.println("登录成功！");
							MyWebView.url = jsonObject.getString("data");
							List<Cookie> mCookies;
							String sessionIDString = "";
							mCookies = ((AbstractHttpClient) httpclient)
									.getCookieStore().getCookies();// 保存cookie
							// MyWebView.mCookies=mCookies;
							for (int i = 0; i < mCookies.size(); i++) {

								sessionIDString += mCookies.get(i).getName()
										+ "=" + mCookies.get(i).getValue()
										+ ";";
							}// cookie转成字符串
							Log.v("cookieInLogin", sessionIDString);

							// SharedPreferences储存Cookie
							SharedPreferences share = getSharedPreferences(
									"info", Context.MODE_PRIVATE);// 新建一个SharedPreferences对象
							Editor edit = share.edit();// 获取编辑器
							edit.putString("cookie", sessionIDString);// 添加内容
							edit.commit();// 提交

							bundle.putInt("status", 1);
							bundle.putString("text", "登录成功！");
						} else {

							System.out.println("登录失败！");
							bundle.putInt("status", 0);
							bundle.putString("text", "登录失败！");
						}

						msg.setData(bundle);
						myHandler.sendMessage(msg);
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}
}
