package com.example.administrator.wifihotdemoserver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.name_num)
    EditText nameEt;
    @BindView(R.id.pwd_num)
    EditText pwdEt;
    private WifiManager wifiManager;
    private boolean flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        requestAllPower();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ButterKnife.bind(this);
    }
    @OnClick({R.id.setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.setting:
                String name = nameEt.getText().toString();
                String pwd = pwdEt.getText().toString();
                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)){
                    Toast.makeText(this,"请输入名称密码",Toast.LENGTH_SHORT).show();
                }else {
                    popConfrim(name,pwd);
                }
                break;
        }
    }

    private void popConfrim(final String name,final String pwd) {
        AlertDialog.Builder builder = null;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("开启热点将关闭您的WIFI!");
        builder.setCancelable(false); // 将对话框设置为不可取消
        // 给按钮添加注册监听
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击按钮所调用的方法
                flag=!flag;
                if(setWifiApEnabled(flag,name,pwd)){
                    Toast.makeText(MainActivity.this,"开启成功",Toast.LENGTH_SHORT).show();
                    String msg = "ConnectWifi:"+"{ \"name\":"+name+"\" , \"pwd\":\""+pwd+"\" }";
                    Intent intent = new Intent(MainActivity.this,CodeActivity.class);
                    intent.putExtra("code",msg);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(MainActivity.this,"开启失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }
    // wifi热点开关
    public boolean setWifiApEnabled(boolean enabled,String name,String pwd) {
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        Method method = null;
        try {
            method = wifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            method.setAccessible(true);
            WifiConfiguration netConfig = new WifiConfiguration();
            netConfig.SSID = name;
            netConfig.preSharedKey =  pwd;
            netConfig.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            netConfig.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            netConfig.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            netConfig.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            netConfig.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            netConfig.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);

            method.invoke(wifiManager, netConfig,true);

        } catch (Exception e) {
            Log.e("error", "startWifiAp: "+e.getMessage());
            return  false;
        }
        return true;
    }
    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_SETTINGS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_SETTINGS,
                                Manifest.permission.INTERNET,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CHANGE_CONFIGURATION,Manifest.permission.CHANGE_NETWORK_STATE,Manifest.permission.CHANGE_WIFI_STATE,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.ACCESS_WIFI_STATE}, 1);
            }
        }
    }
}
