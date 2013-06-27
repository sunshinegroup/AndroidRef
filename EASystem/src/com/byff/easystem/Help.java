package com.byff.easystem;

import com.byff.easystem.R;

import android.R.color;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.widget.TextView;

public class Help extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);

		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		String version = packInfo.versionName;

		String text=getResources().getString(R.string.help);
		SpannableString ss = new SpannableString(text.replace("#", "").replace("*", ""));  
		  
	    //设置指定位置字符颜色   
	    ss.setSpan(new ForegroundColorSpan(Color.RED), text.indexOf("#"), text.indexOf("*"),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
	    TextView tView = (TextView) findViewById(R.id.textView2);
		tView.setMovementMethod(ScrollingMovementMethod.getInstance());
		tView.setText(ss);
		
		TextView tView2 = (TextView) findViewById(R.id.textView3);
		tView2.setText("当前程序版本：" + version + "("
				+ version.replace("1.", "2013.") + " 更新)");
		tView2.setTextColor(Color.RED);
		
	}

}
