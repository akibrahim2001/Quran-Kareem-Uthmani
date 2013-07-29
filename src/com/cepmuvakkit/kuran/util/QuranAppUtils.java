package com.cepmuvakkit.kuran.util;

import android.text.TextUtils;
import android.util.Log;

import com.cepmuvakkit.kuran.data.Constants;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QuranAppUtils {
   private static final String TAG = "QuranAppUtils";

   public static String getQuranAppUrl(int sura, Integer startAyah,
                                       Integer endAyah){
      String url = null;
      String fallbackUrl = null;
      try {
         Map<String, String> params = new HashMap<String, String>();
         params.put("surah", sura + "");
         fallbackUrl = Constants.QURAN_APP_BASE + sura;
         if (startAyah != null){
            params.put("start_ayah", startAyah.toString());
            fallbackUrl += "/" + startAyah;
            if (endAyah != null){
               params.put("end_ayah", endAyah.toString());
               fallbackUrl += "-" + endAyah;
            }
            else { params.put("end_ayah", startAyah.toString()); }
         }
         params.put("key", ApiKeys.QURAN_APP_KEY);
         String result = getQuranAppUrl(params);
         if (!TextUtils.isEmpty(result)){
            JSONObject json = new JSONObject(result);
            url = json.getString("url");
         }
      }
      catch (Exception e){
         Log.d(TAG, "error getting QuranApp url", e);
      }

      Log.d(TAG, "got back " + url + " and fallback " + fallbackUrl);
      return TextUtils.isEmpty(url)? fallbackUrl : url;
   }

   private static String getQuranAppUrl(Map<String, String> params)
           throws IOException {
      URL url = null;
      try { url = new URL(Constants.QURAN_APP_ENDPOINT); }
      catch (MalformedURLException me){}

      StringBuilder builder = new StringBuilder();
      Iterator<Map.Entry<String, String>> iterator =
              params.entrySet().iterator();
      while (iterator.hasNext()){
         Map.Entry<String, String> item = iterator.next();
         builder.append(item.getKey()).append("=").append(item.getValue());
         if (iterator.hasNext()){
            builder.append('&');
         }
      }

      String result = "";
      String body = builder.toString();
      byte[] bytes = body.getBytes();
      HttpURLConnection conn = null;
      try {
         conn = (HttpURLConnection)url.openConnection();
         conn.setReadTimeout(10000);
         conn.setConnectTimeout(15000);
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setUseCaches(false);
         conn.setFixedLengthStreamingMode(bytes.length);
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Content-Type",
                 "application/x-www-form-urlencoded;charset=UTF-8");

         // post the request
         OutputStream out = conn.getOutputStream();
         out.write(bytes);
         out.close();

         // handle the response
         BufferedReader reader =
                 new BufferedReader(new InputStreamReader(
                         conn.getInputStream(), "UTF-8"));

         String line;
         while ((line = reader.readLine()) != null){
            result += line;
         }

         try { reader.close(); }
         catch (Exception e){}
      }
      finally {
         if (conn != null){ conn.disconnect(); }
      }

      return result;
   }
}
