package com.xuqiqiang.uikit.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Random;
import static com.xuqiqiang.uikit.utils.StringUtils.getFormatSize;

/**
 * Created by xuqiqiang on 2019/08/21.
 */
public class DeviceUtils {

    private static final String TAG = "DeviceInfoModule";

    private static final String marshmallowMacAddress = "02:00:00:00:00:00";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    public static String getMacAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMan == null)
            return marshmallowMacAddress;
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        if (wifiInf != null) {
            String mac = wifiInf.getMacAddress();
            if (marshmallowMacAddress.equals(mac)) {
                return getAndroid6MacAddress();
            } else if (!TextUtils.isEmpty(mac)) {
                return mac;
            }
        }
        return marshmallowMacAddress;
    }

    public static String getAndroid6MacAddress() {
        String str = "";
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if ("".equals(macSerial)) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(macSerial)) {
                Log.i(TAG, "For android7.0");
                try {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface iF = interfaces.nextElement();
                        byte[] addr = iF.getHardwareAddress();
                        if (addr == null || addr.length == 0) {
                            continue;
                        }
                        StringBuilder buf = new StringBuilder();
                        for (byte b : addr) {
                            buf.append(String.format("%02X:", b));
                        }
                        if (buf.length() > 0) {
                            buf.deleteCharAt(buf.length() - 1);
                        }
                        String mac = buf.toString();
                        Log.d(TAG, "interfaceName=" + iF.getName() + ", mac=" + mac);
                        if (iF.getName().equalsIgnoreCase("wlan0")) {
                            macSerial = mac;
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "SocketException e=" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return macSerial;
    }

    private static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }

    public static String getIMEI(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (TelephonyMgr == null) return null;
        return TelephonyMgr.getDeviceId();
    }

    public static String getSimSerialNumber(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (TelephonyMgr == null) return null;
        return TelephonyMgr.getSimSerialNumber();
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getSerialNumber() {
        return android.os.Build.SERIAL;
    }

    /**
     * 获取当前的网络状态 ：没有网络-0：WIFI网络1：4G网络-4：3G网络-3：2G网络-2
     *
     * @param context
     * @return
     */
    public static int getAPNType(Context context) {
        //结果返回值
        int netType = 0;
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return netType;
        }
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = 1;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService
                    (Context.TELEPHONY_SERVICE);
            //3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 4;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 3;
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 2;
            } else {
                netType = 2;
            }
        }
        return netType;
    }
//    public String getDeviceInfo() {
//
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("mac", getPhoneMacAddress(mReactContext));
//            jsonObject.put("imei", getIMEI(mReactContext));
//            jsonObject.put("SimSerialNumber", getSimSerialNumber(mReactContext));
//            jsonObject.put("androidId", getAndroidId(mReactContext));
//            jsonObject.put("serialNumber", getSerialNumber());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jsonObject.toString();
//    }

    public static String get_phone_model() {
        return Build.MODEL;
    }

    public static String get_system_edition() {
        return Build.VERSION.RELEASE;
    }

    public static String get_system_edition_number() {
        return Build.DISPLAY;
    }

    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }

        double result_double = 0;
        try {
            result_double = Double.valueOf(result.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }

        NumberFormat f = NumberFormat.getInstance(); // 创建一个格式化类f
        f.setMaximumFractionDigits(2); // 设置小数位的格式
        String str_result = f.format(result_double / 1000); // 格式化数据a,将a格式化为f

        str_result = remove(str_result, ',');
        str_result += "MHz";
        return str_result;
    }

    public static String remove(String d, char c) {
        String s1, s2;

        int id = d.indexOf(c);
        while (id != -1) {
            s1 = d.substring(0, id);
            s2 = d.substring(id + 1, d.length());
            d = s1 + s2;
            id = d.indexOf(c);
        }

        return d;
    }

    public static long getTotalRomMemory() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
//        return (totalBlocks * blockSize / 1024 / 1024) + "MB";
    }

    public static long getAvailableRomMemory() {
        long romInfo;
        // Total rom memory

        // Available rom memory
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        romInfo = blockSize * availableBlocks;
        // getVersion();
        return romInfo;
//        return (romInfo / 1024 / 1024) + "MB";
    }

    public static long[] getSDCardMemory() {
        long[] sdCardInfo = new long[2];
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSizeLong();
            long bCount = sf.getBlockCountLong();
            long availBlocks = sf.getAvailableBlocksLong();

            sdCardInfo[0] = bSize * bCount;//(bSize * bCount / 1024 / 1024) + "MB";// 总大小
            sdCardInfo[1] = bSize * availBlocks;//(bSize * availBlocks / 1024 / 1024) + "MB";// 可用大小
        }
        return sdCardInfo;
    }

    public static long[] getMemoryInfo() {
        long[] memoryInfo = new long[2];
        long[] sdCardMemory = getSDCardMemory();
        if (sdCardMemory[0] > 0) memoryInfo[0] = sdCardMemory[0];
        else memoryInfo[0] = getTotalRomMemory();
        if (sdCardMemory[1] > 0) memoryInfo[1] = sdCardMemory[1];
        else memoryInfo[1] = getAvailableRomMemory();
        return memoryInfo;
    }

    public static String get_rest_Memory(Context context) {
        long all = getTotalMemory();
        long use = all - getAvailMemory(context);

        return String.valueOf(use) + "MB" + "("
                + String.valueOf((int) ((double) use / (double) all * 100))
                + "%)";
    }

    public static String get_total_Memory() {
        return String.valueOf(getTotalMemory()) + "MB";
    }

    public static long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        // return Formatter.formatFileSize(context, mi.availMem);
        return mi.availMem / (1024 * 1024);
    }

    public static long getTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException e) {
        }
        // return Formatter.formatFileSize(context, initial_memory);//
        // Byte转换为KB或者MB，内存大小规格化
        return initial_memory / (1024 * 1024);
    }

    public static String getCPUABI() {
        return android.os.Build.CPU_ABI;
    }


    public static String getAllInfo(Context context) {

        String channel = "未知";
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            channel = appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            long[] memoryInfo = getMemoryInfo();
            String str = "设备型号: " + get_phone_model() + "\n"
                    + "操作系统版本: " + get_system_edition() + "\n"
                    + "操作系统版本号: " + get_system_edition_number() + "\n"
                    + "CPU型号: " + getCpuName() + "\n"
                    + "CPU频率: " + getMaxCpuFreq() + "\n"
                    + "CPU架构: " + getCPUABI() + "\n"
//                    + "内部存储器容量: " + getFormatSize(getTotalRomMemory()) + "MB\n"
//                    + "内部存储器剩余: " + getFormatSize(getAvailableRomMemory()) + "MB\n"
                    + "存储容量: " + getFormatSize(memoryInfo[0]) + "\n"
                    + "存储剩余: " + getFormatSize(memoryInfo[1]) + "\n"
                    + "全部内存: " + get_total_Memory() + "\n"
                    + "已用内存: " + get_rest_Memory(context) + "\n"
                    + "屏幕分辨率: " + ScreenUtils.getSize() + "\n"
                    + "当前版本: " + ApplicationUtils.getVersionName(context) + "\n"
                    + "渠道: " + channel + "\n\n";

            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return "未知设备";
        }

    }

    public static String getRandomString(int length){
        //1.  定义一个字符串（A-Z，a-z，0-9）即62个数字字母；
        String str="zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        //2.  由Random生成随机数
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        //3.  长度为几就循环几次
        for(int i=0; i<length; ++i){
            //从62个的数字或字母中选择
            int number=random.nextInt(62);
            //将产生的数字通过length次承载到sb中
            sb.append(str.charAt(number));
        }
        //将承载的字符转换成字符串
        return sb.toString();
    }

    @SuppressLint("MissingPermission")
    public static String getPhoneInfo(Context context) {
        TelephonyManager tm = null;
        try {
            tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (Exception e) {
            return "";
        }
    }
    public static String getIMEINew(Context context) {
        //we make this look like a valid IMEI
        String m_szDevIDShort = "35" +
                Build.BOARD.length()%10 +
                Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 +
                Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 +
                Build.HOST.length()%10 +
                Build.ID.length()%10 +
                Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 +
                Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 +
                Build.TYPE.length()%10 +
                Build.USER.length()%10 ; //13 digits
        return m_szDevIDShort;
    }

}
