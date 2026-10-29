package com.osoza.tahsilat;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends Activity {
  WebView web;
  @Override public void onCreate(Bundle b){super.onCreate(b);web=new WebView(this);setContentView(web);
    web.getSettings().setJavaScriptEnabled(true);
    web.getSettings().setDomStorageEnabled(true);
    web.setWebViewClient(new WebViewClient());web.setWebChromeClient(new WebChromeClient());
    web.addJavascriptInterface(new Bridge(),"Android");
    web.loadUrl("file:///android_asset/index.html");
  }
  class Bridge {
    File folder(){File f=new File(getFilesDir(),"Kayitlar");if(!f.exists())f.mkdirs();return f;}
    @JavascriptInterface public String listRecords(){JSONArray a=new JSONArray();File[] files=folder().listFiles((d,n)->n.endsWith(".json")&&n.startsWith("Kayit_"));if(files!=null){Arrays.sort(files);for(File f:files){try{byte[] data=new byte[(int)f.length()];try(FileInputStream in=new FileInputStream(f)){int off=0,n;while(off<data.length&&(n=in.read(data,off,data.length-off))>0)off+=n;}a.put(new JSONObject(new String(data,StandardCharsets.UTF_8)));}catch(Exception ignored){}}}return a.toString();}
    @JavascriptInterface public void saveRecord(String json){try{JSONObject r=new JSONObject(json);String name=String.format(java.util.Locale.US,"Kayit_%05d_%s.json",r.getInt("number"),r.getString("date"));try(FileOutputStream out=new FileOutputStream(new File(folder(),name))){out.write(json.getBytes(StandardCharsets.UTF_8));}}catch(Exception e){throw new RuntimeException(e);}}
    @JavascriptInterface public void saveJpeg(String name,String base64){try{byte[] data=Base64.decode(base64,Base64.DEFAULT);if(Build.VERSION.SDK_INT>=29){ContentValues values=new ContentValues();values.put(MediaStore.Downloads.DISPLAY_NAME,name);values.put(MediaStore.Downloads.MIME_TYPE,"image/jpeg");values.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/OSOZA");Uri uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values);if(uri==null)throw new IOException("Dosya açılamadı");try(OutputStream out=getContentResolver().openOutputStream(uri)){out.write(data);}}else{File dir=new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"OSOZA");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name))){out.write(data);}}}catch(Exception e){throw new RuntimeException(e);}}
  }
  @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}
