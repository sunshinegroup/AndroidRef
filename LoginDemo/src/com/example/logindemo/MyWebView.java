package com.example.logindemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebView extends Activity {
	WebView webView;
	public static String url = "http://www.mofriend.net/index.php?app=public&mod=Index&act=index";

	// public static List<Cookie> mCookies = null;
	@Override
	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		webView = (WebView) findViewById(R.id.webView1);

		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);// ����֧��javascript
		webSettings.setBuiltInZoomControls(true);// ����ҳ��֧������

		// ʹwebview����ʹ�ñ������������ҳ�������ǵ����ⲿ�����
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		
		String cookieString = getCookie();
		Log.v("Cookie", cookieString);
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setCookie("http://www.mofriend.net", cookieString);
		CookieSyncManager.getInstance().sync();

		webView.loadUrl(url);
	}

	// �����ҳʱ�����ϵͳ��Back����������Browser�����finish()�������������ϣ���������
	// ҳ���˶������Ƴ����������Ҫ�ڵ�ǰActivity�д������ѵ���Back�¼���
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// �˵���
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "�л��û�");
		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "ע����¼");
		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "�˳�");

		return true;

	}

	// �˵���
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case Menu.FIRST + 1:
			finish();
			Intent i = new Intent(getApplicationContext(), Login.class);
			startActivity(i);

			break;
		case Menu.FIRST + 2:
			clearCache();
			finish();
			Intent i1 = new Intent(getApplicationContext(), Login.class);
			startActivity(i1);

			break;
		case Menu.FIRST + 3:
			finish();
			System.exit(0);

			break;

		}

		return false;

	}

	public String getCookie() {

		SharedPreferences myShare = getApplicationContext()
				.getSharedPreferences("info", Context.MODE_PRIVATE);
		String str = myShare.getString("cookie", "");
		Log.v("CookieFromXML", str);
		return str;
	}

	public void clearCache() {

		SharedPreferences share = getSharedPreferences("info",
				Context.MODE_PRIVATE);// �½�һ��SharedPreferences����
		Editor edit = share.edit();// ��ȡ�༭��
		edit.clear();// ���
		edit.commit();// �ύ
	}
}
