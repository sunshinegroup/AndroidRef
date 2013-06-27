package com.example.update;

import java.io.IOException;
import java.util.HashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class CheckUpdate {
	Context mContext;
	HashMap<String, String> version;
	boolean flag = true;

	public CheckUpdate(Context content) {
		this.mContext = content;
		getVersionInfo();
	}

	public HashMap<String, String> getVersionInfo() {
		version=new HashMap<String, String>();
		try {
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000); // ����ʱ
			HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000); // �ȴ����ݳ�ʱ
			// ����µ�HttpClient
			HttpClient httpclient = new DefaultHttpClient(httpParams);

			// httpget��ȡVIEWSTATEֵ
			HttpGet httpget = new HttpGet(
					"http://www.mofriend.net/dev/update.json");
			HttpResponse res;
			res = httpclient.execute(httpget);
			// ����GET,������һ��HttpResponse����
			String result = EntityUtils.toString(res.getEntity());// ��ȡ���ؽ��
			System.out.println(result);
			JSONObject jsonObject = new JSONObject(result);			
			version.put("updateURL", jsonObject.getString("updateURL").toString());
			version.put("versionCode", jsonObject.getString("versionCode").toString());
			version.put("versionName", jsonObject.getString("versionName").toString());
			flag = false;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;

	}

	public int getVersionCode() {
		// ��ȡpackagemanager��ʵ��
		PackageManager packageManager = mContext.getPackageManager();
		// getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(mContext.getPackageName(),
					0);
		} catch (NameNotFoundException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		int versionCode = packInfo.versionCode;
		return versionCode;
	}

	// �ж��Ƿ�Ϊ���°�
	public boolean isUpdate() {
		int serverVersionCode = Integer.parseInt(version.get("versionCode"));
		int localVersionCode = getVersionCode();
		if (serverVersionCode > localVersionCode) {
			System.out.println("��⵽�°汾");
			return true;
		} else {
			System.out.println("��ǰ�Ѿ������°汾");
			return false;
		}
	}

}
