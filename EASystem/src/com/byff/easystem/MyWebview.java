package com.byff.easystem;

import com.byff.easystem.R;

import android.R.color;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class MyWebview extends Activity {
	public static String content;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		setTitle("�ɼ���ѯ�������������ϵͳ");
		setTitleColor(color.black);
		WebView wv = (WebView) findViewById(R.id.webView1);
		wv.getSettings().setDefaultTextEncodingName("gb2312");
		wv.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
	}

	// �˵���
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "�л��û�");

		menu.add(Menu.NONE, Menu.FIRST + 2, 2, "����");

		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "����");

		menu.add(Menu.NONE, Menu.FIRST + 4, 4, "�˳�");

		return true;

	}

	// �˵���
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case Menu.FIRST + 1:
			
		Intent i = new Intent(getApplicationContext(),MainActivity.class);
		startActivity(i);

			break;

		case Menu.FIRST + 2:

			Intent intent2 = new Intent(getApplicationContext(), Help.class);
			startActivity(intent2);

			break;

		case Menu.FIRST + 3:

			Intent about = new Intent(getApplicationContext(), about.class);
			startActivity(about);

			break;

		case Menu.FIRST + 4:
			
			finish();
			System.exit(0);

			break;

		}

		return false;

	}
}
