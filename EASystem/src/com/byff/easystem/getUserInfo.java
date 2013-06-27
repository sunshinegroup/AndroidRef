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
					final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
					final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
					final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
					String SessionIDString = null;
					String htmlStr;
					for (int i = 0; i < mCookies.size(); i++) {

						SessionIDString = mCookies.get(i).getName() + "="
								+ mCookies.get(i).getValue();
					}
					// httpget获取用户信息
					BasicHttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParams,
							5 * 1000); // 请求超时
					HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000); // 等待数据超时
					// 添加新的HttpClient
					HttpClient httpclient = new DefaultHttpClient(httpParams);
					HttpGet httpget = new HttpGet(
							"http://jw.fdygxy.cn:8081/xstop.aspx");
					httpget.setHeader("Cookie", SessionIDString);
					HttpResponse res;
					try {

						res = httpclient.execute(httpget);
						// 发送GET,并返回一个HttpResponse对象
						htmlStr = EntityUtils.toString(res.getEntity());// 获取返回结果

						Pattern p_script = Pattern.compile(regEx_script,
								Pattern.CASE_INSENSITIVE);
						Matcher m_script = p_script.matcher(htmlStr);
						htmlStr = m_script.replaceAll(""); // 过滤script标签

						Pattern p_style = Pattern.compile(regEx_style,
								Pattern.CASE_INSENSITIVE);
						Matcher m_style = p_style.matcher(htmlStr);
						htmlStr = m_style.replaceAll(""); // 过滤style标签

						Pattern p_html = Pattern.compile(regEx_html,
								Pattern.CASE_INSENSITIVE);
						Matcher m_html = p_html.matcher(htmlStr);
						htmlStr = m_html.replaceAll(""); // 过滤html标签
						htmlStr = htmlStr
								.replaceAll("\\s*|\t|\r|\n", "")
								.replaceAll("&nbsp;", "@")
								.replaceAll("@@", "@")
								.replace(
										"@·个人信息·信息查询·跨院系选修课程网上选课·一般课网上选课·网上评价·公告信息·帮助文件·学校网站",
										"").replace("无标题文档@", "姓名:")
								.replace(",您好!", "").replace("班级:", "@班级:")
								.replace("@学号", "班@学号").replace("班级:@", "班级:");// 去除字符串中的空格,回车,换行符,制表符，替换不必要信息
						String[] info = htmlStr.split("@");
						try {

							// 保存到本地
							File f = context.getFilesDir();
							String path = f.getAbsolutePath() + "//information";
							File file = new File(path);
							if (!file.exists()) {
								file.createNewFile();
							}
							FileWriter fw = null;
							BufferedWriter bw = null;
							fw = new FileWriter(path, false);// 第二个参数为是否覆盖 //
																// 创建FileWriter对象，用来写入字符流
							bw = new BufferedWriter(fw); // 将缓冲对文件的输出
							bw.write(htmlStr); // 写入文件
							bw.flush(); // 刷新该流的缓冲
							bw.close();
							fw.close();
							Log.v("提示：","用户资料保存成功");

						} catch (Exception e) {
							// TODO: handle exception
							Log.v("提示：","用户资料保存失败");
						}

					} catch (ClientProtocolException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
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