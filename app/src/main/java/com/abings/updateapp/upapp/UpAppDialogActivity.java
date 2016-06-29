package com.abings.updateapp.upapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.abings.updateapp.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by HaomingXu on 2016/6/29.
 */
public class UpAppDialogActivity extends AppCompatActivity {

    private TextView tvDesc;
    private String apkpath;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upapp_dialog);
        initId();
        String desc = getIntent().getStringExtra("desc");
        tvDesc.setText(desc);
    }

    private void initId() {
        tvDesc = (TextView) findViewById(R.id.upappdialog_tv_desc);
    }

    public void play(View v){
        switch (v.getId()){
            case R.id.upappdialog_btn_ok:
                if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                    String url = getIntent().getStringExtra("url");
                    String fileName = url.substring(url.lastIndexOf("/") + 1);
                    apkpath = fileName;
                    download(url,Const.apkDownPath,fileName);
                    finish();
                }else{
                    Toast.makeText(this, "SDCard不存在", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.upappdialog_btn_cancel:
                finish();
                break;
        }
    }

    public void showLoadingProgress(boolean show) {
        if (show) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.logo);
            builder.setTitle("提示");
            builder.setMessage("正在下载中...");
            builder.create().show();
        }
    }

    public void installApp(String path) {
        Log.i("TAG00",path);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(path);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    Handler handler = new Handler() {
       public void handleMessage(Message msg){
           switch (msg.what){
               case 0:
                   showLoadingProgress(false);
                   break;
           }
       }
    };

    public void download(final String loadurl, final String path, final String fileName){
        showLoadingProgress(true);
        Log.i("TAG00",loadurl);
        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setReadTimeout(3000, TimeUnit.MILLISECONDS);
        mOkHttpClient.setWriteTimeout(3000, TimeUnit.MILLISECONDS);
        mOkHttpClient.setConnectTimeout(3000, TimeUnit.MILLISECONDS);
        Request request = new Request.Builder().url(loadurl).tag(this).build();
        mOkHttpClient.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream is = response.body().byteStream();
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;
                    try {
                        is = response.body().byteStream();
                        final long total = response.body().contentLength();
                        long sum = 0;
                        File dir = new File(path);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File file = new File(dir, fileName);
                        fos = new FileOutputStream(file);
                        while ((len = is.read(buf)) != -1) {
                            sum += len;
                            fos.write(buf, 0, len);
                            final long finalSum = sum;
                        }
                        fos.flush();
                    } finally {
                        if (is != null) is.close();
                        if (fos != null) fos.close();
                        handler.sendEmptyMessage(0);
                        installApp(Const.apkDownPath + apkpath);
                    }

                } else {
                    Log.i("TAG00", "文件下载链接失败。。。");
                }
            }
        });
    }
}
