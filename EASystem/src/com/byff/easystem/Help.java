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

		// ��ȡpackagemanager��ʵ��
		PackageManager packageManager = getPackageManager();
		// getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		String version = packInfo.versionName;

		String text=getResources().getString(R.string.help);
		SpannableString ss = new SpannableString(text.replace("#", "").replace("*", ""));  
		  
	    //����ָ��λ���ַ���ɫ   
	    ss.setSpan(new ForegroundColorSpan(Color.RED), text.indexOf("#"), text.indexOf("*"),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
	    TextView tView = (TextView) findViewById(R.id.textView2);
		tView.setMovementMethod(ScrollingMovementMethod.getInstance());
		tView.setText(ss);
		
		TextView tView2 = (TextView) findViewById(R.id.textView3);
		tView2.setText("��ǰ����汾��" + version + "("
				+ version.replace("1.", "2013.") + " ����)");
		tView2.setTextColor(Color.RED);
		
	}

}
