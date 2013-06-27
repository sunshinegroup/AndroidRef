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
			HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000); // 请求超时
			HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000); // 等待数据超时
			// 添加新的HttpClient
			HttpClient httpclient = new DefaultHttpClient(httpParams);

			// httpget获取VIEWSTATE值
			HttpGet httpget = new HttpGet(
					"http://www.mofriend.net/dev/update.json");
			HttpResponse res;
			res = httpclient.execute(httpget);
			// 发送GET,并返回一个HttpResponse对象
			String result = EntityUtils.toString(res.getEntity());// 获取返回结果
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
		// 获取packagemanager的实例
		PackageManager packageManager = mContext.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(mContext.getPackageName(),
					0);
		} catch (NameNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		int versionCode = packInfo.versionCode;
		return versionCode;
	}

	// 判断是否为最新版
	public boolean isUpdate() {
		int serverVersionCode = Integer.parseInt(version.get("versionCode"));
		int localVersionCode = getVersionCode();
		if (serverVersionCode > localVersionCode) {
			System.out.println("检测到新版本");
			return true;
		} else {
			System.out.println("当前已经是最新版本");
			return false;
		}
	}

}
