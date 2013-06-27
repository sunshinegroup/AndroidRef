package com.byff.easystem;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class getUserInfo extends Activity {
	public static boolean getUserInfoFlag = false;

	public static void getInfo(final List<Cookie> mCookies,
			final Context context) {
		getUserInfoFlag = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (getUserInfoFlag) {

					// Looper.prepare();//
					final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // ����script��������ʽ
					final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // ����style��������ʽ
					final String regEx_html = "<[^>]+>"; // ����HTML��ǩ��������ʽ
					String SessionIDString = null;
					String htmlStr;
					for (int i = 0; i < mCookies.size(); i++) {

						SessionIDString = mCookies.get(i).getName() + "="
								+ mCookies.get(i).getValue();
					}
					// httpget��ȡ�û���Ϣ
					BasicHttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParams,
							5 * 1000); // ����ʱ
					HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000); // �ȴ����ݳ�ʱ
					// ����µ�HttpClient
					HttpClient httpclient = new DefaultHttpClient(httpParams);
					HttpGet httpget = new HttpGet(
							"http://jw.fdygxy.cn:8081/xstop.aspx");
					httpget.setHeader("Cookie", SessionIDString);
					HttpResponse res;
					try {

						res = httpclient.execute(httpget);
						// ����GET,������һ��HttpResponse����
						htmlStr = EntityUtils.toString(res.getEntity());// ��ȡ���ؽ��

						Pattern p_script = Pattern.compile(regEx_script,
								Pattern.CASE_INSENSITIVE);
						Matcher m_script = p_script.matcher(htmlStr);
						htmlStr = m_script.replaceAll(""); // ����script��ǩ

						Pattern p_style = Pattern.compile(regEx_style,
								Pattern.CASE_INSENSITIVE);
						Matcher m_style = p_style.matcher(htmlStr);
						htmlStr = m_style.replaceAll(""); // ����style��ǩ

						Pattern p_html = Pattern.compile(regEx_html,
								Pattern.CASE_INSENSITIVE);
						Matcher m_html = p_html.matcher(htmlStr);
						htmlStr = m_html.replaceAll(""); // ����html��ǩ
						htmlStr = htmlStr
								.replaceAll("\\s*|\t|\r|\n", "")
								.replaceAll("&nbsp;", "@")
								.replaceAll("@@", "@")
								.replace(
										"@��������Ϣ����Ϣ��ѯ����Ժϵѡ�޿γ�����ѡ�Ρ�һ�������ѡ�Ρ��������ۡ�������Ϣ�������ļ���ѧУ��վ",
										"").replace("�ޱ����ĵ�@", "����:")
								.replace(",����!", "").replace("�༶:", "@�༶:")
								.replace("@ѧ��", "��@ѧ��").replace("�༶:@", "�༶:");// ȥ���ַ����еĿո�,�س�,���з�,�Ʊ�����滻����Ҫ��Ϣ
						String[] info = htmlStr.split("@");
						try {

							// ���浽����
							File f = context.getFilesDir();
							String path = f.getAbsolutePath() + "//information";
							File file = new File(path);
							if (!file.exists()) {
								file.createNewFile();
							}
							FileWriter fw = null;
							BufferedWriter bw = null;
							fw = new FileWriter(path, false);// �ڶ�������Ϊ�Ƿ񸲸� //
																// ����FileWriter��������д���ַ���
							bw = new BufferedWriter(fw); // ��������ļ������
							bw.write(htmlStr); // д���ļ�
							bw.flush(); // ˢ�¸����Ļ���
							bw.close();
							fw.close();
							Log.v("��ʾ��","�û����ϱ���ɹ�");

						} catch (Exception e) {
							// TODO: handle exception
							Log.v("��ʾ��","�û����ϱ���ʧ��");
						}

					} catch (ClientProtocolException e) {
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					} catch (IOException e) {
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					}finally
					{
						getUserInfoFlag=false;
					}
				}
			}
		}).start();
	}

}