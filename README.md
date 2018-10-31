# WifiHotDemoServer
安卓打开热点demo，并生成二维码</br>
项目截图</br>
![image](https://github.com/TankSao/WifiHotDemoServer/blob/master/image/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20181031111823.png)</br>
![image](https://github.com/TankSao/WifiHotDemoServer/blob/master/image/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20181031111848.png)</br>
![image](https://github.com/TankSao/WifiHotDemoServer/blob/master/image/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20181031111853.png)</br>
![image](https://github.com/TankSao/WifiHotDemoServer/blob/master/image/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20181031111857.png)</br>
关键代码</br>
``` Android
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
    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            Log.e("codeErr",e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
