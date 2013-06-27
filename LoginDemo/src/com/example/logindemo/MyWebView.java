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
		webSettings.setJavaScriptEnabled(true);// 设置支持javascript
		webSettings.setBuiltInZoomControls(true);// 设置页面支持缩放

		// 使webview继续使用本身浏览请求网页，而不是调用外部浏览器
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

	// 浏览网页时，点击系统“Back”键，整个Browser会调用finish()而结束自身，如果希望浏览的网
	// 页回退而不是推出浏览器，需要在当前Activity中处理并消费掉该Back事件。
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// 菜单项
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "切换用户");
		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "注销登录");
		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "退出");

		return true;

	}

	// 菜单项
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
				Context.MODE_PRIVATE);// 新建一个SharedPreferences对象
		Editor edit = share.edit();// 获取编辑器
		edit.clear();// 清空
		edit.commit();// 提交
	}
}
