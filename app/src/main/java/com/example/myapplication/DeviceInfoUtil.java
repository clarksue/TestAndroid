package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class DeviceInfoUtil {

    private Activity activity;


    public DeviceInfoUtil(Activity activity) {

        this.activity = activity;
    }


    private String getIP() {
        try {

            Context context = activity.getApplicationContext();

            NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                    return ipAddress;
                }
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }


    private String getAppversion() {
        PackageManager pm = activity.getApplicationContext().getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(activity.getPackageName(), 0);
            String appVersion = pi.versionName;
            return appVersion;
        } catch (Exception e) {
            return "1.0";
        }
    }


    private String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    private String getCurrentUserAgent() {
        try {
            String userAgent = System.getProperty("http.agent");
            return userAgent;
        } catch (Exception e) {
            return "";
        }
    }


    private String getPhoneIMEI() {
        try {
            TelephonyManager mTm = (TelephonyManager) activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("MissingPermission") String imei = mTm.getDeviceId();
            return imei;
        } catch (Exception e) {
            return "";
        }
    }


    private String getMacAddress() {
        try {
            String macAddress = null;
            StringBuffer buf = new StringBuffer();
            NetworkInterface networkInterface = null;
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "02:00:00:00:00:02";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
            return macAddress;
        } catch (Exception e) {
            return "";
        }
    }


    private Location getLocation() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider;
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            return null;
        }

        //获取Location
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            //不为空,显示地理位置经纬度
            return location;
        } else {
            return null;
        }
    }


    private String getAndroidid() {
        try {
            return Settings.System.getString(activity.getContentResolver(), Settings.System.ANDROID_ID);
        } catch (Exception e) {
            return "";
        }
    }


    private String getCarrier() {
        try {
            TelephonyManager telManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            String operator = telManager.getSimOperator();
            return operator;
        } catch (Exception e) {
            return "";
        }
    }


    private Integer getNetwork() {
        try {
            int network = 0;
            ConnectivityManager connectivity = (ConnectivityManager) activity.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return network;
            }
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    network = 1;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    String _strSubTypeName = networkInfo.getSubtypeName();
                    // TD-SCDMA   networkType is 17
                    int networkType = networkInfo.getSubtype();
                    switch (networkType) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                            network = 2;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            network = 3;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            network = 4;
                            break;
                        default:
                            //中国移动 联通 电信 三种3G制式
                            if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                network = 3;
                            } else {
                                network = 0;
                            }
                            break;
                    }
                }
            }
            return network;
        } catch (Exception e) {
            return 0;
        }
    }

    private String getMD5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getPackageList() {
        ArrayList<String> al = new ArrayList<String>();
        PackageManager packageManager = activity.getPackageManager();
        List<PackageInfo> list = packageManager.getInstalledPackages(0);
        for (PackageInfo p : list) {
            int flags = p.applicationInfo.flags;
            if ((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                al.add(p.applicationInfo.packageName);
            }
        }
        StringBuilder sb = new StringBuilder("");
        for (String s : al) {
            if (sb.toString().equals("")) {
                sb.append(s);
                continue;
            }
            sb.append(",").append(s);
        }
        return sb.toString();
    }

    public String getGaid() {
        String advertisingId = "";
        try {
            AdvertisingIdClient.Info adInfo = AdvertisingIdClient
                    .getAdvertisingIdInfo(activity);
            advertisingId = adInfo.getId();
            return advertisingId;
        } catch (Exception e) {
            return advertisingId;
        }
    }

    private boolean isGooglePlayAvaiable() {
        PackageManager pm = activity.getPackageManager();
        boolean app_installed = false;
        try {
            PackageInfo info = pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
            String label = (String) info.applicationInfo.loadLabel(pm);
            app_installed = (!TextUtils.isEmpty(label) && label.startsWith("GooglePlay"));
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

}