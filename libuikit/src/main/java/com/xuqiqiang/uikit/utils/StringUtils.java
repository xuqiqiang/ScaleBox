package com.xuqiqiang.uikit.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final String[] UNIT_SIZE = {"B", "KB", "MB", "GB", "TB"};

    public static String getFormatSize(long size) {

        int decimal = 0;
        int unit = 0;
        while (size >= 1024 && unit < UNIT_SIZE.length - 1) {
            decimal = (int) (size % 1024);
            size = size / 1024;
            unit++;
        }
        return (unit == 0 ? (int) size + "" :
                String.format(Locale.US, "%.2f",
                        (double) size + (double) decimal / 1024.f)) + UNIT_SIZE[unit];
    }

    public static boolean startsWithIgnoreCaseMaybe(String text, String prefix) {
        return text != null && (text.startsWith(prefix)
                || text.toLowerCase().startsWith(prefix.toLowerCase())
                || text.toUpperCase().startsWith(prefix.toUpperCase()));
    }

    public static boolean endsWithIgnoreCaseMaybe(String text, String suffix) {
        return text != null && (text.endsWith(suffix)
                || text.toLowerCase().endsWith(suffix.toLowerCase())
                || text.toUpperCase().endsWith(suffix.toUpperCase()));
    }

    public static boolean startsWithIgnoreCase(String text, String prefix) {
        return text != null && (text.startsWith(prefix)
                || text.startsWith(prefix.toLowerCase())
                || text.startsWith(prefix.toUpperCase()));
    }

    public static boolean endsWithIgnoreCase(String text, String suffix) {
        return text != null && (text.endsWith(suffix)
                || text.endsWith(suffix.toLowerCase())
                || text.endsWith(suffix.toUpperCase()));
    }

    public static String subStart(String text, int length) {
        String result = "";
        if (!TextUtils.isEmpty(text)) {
            if (length >= text.length()) result = text;
            else result = text.substring(0, length);
        }
        return result;
    }

    public static String subEnd(String text, int length) {
        String result = "";
        if (!TextUtils.isEmpty(text)) {
            if (length >= text.length()) result = text;
            else result = text.substring(text.length() - length);
        }
        return result;
    }

    public static boolean contains(String text, CharSequence s) {
        return text != null && text.contains(s);
    }

    public static String numToHex8(int b) {
        return String.format("%02x", b);//2表示需要两个16进制数
    }

    public static String numToHex16(int b) {
        return String.format("%04x", b);
    }

    public static String numToHex32(int b) {
        return String.format("%08x", b);
    }

    //region File
    public static String getShortName(String url) {
        if (TextUtils.isEmpty(url)) return url;
        int sIndex = url.lastIndexOf(File.separator);
        if (sIndex < 0) sIndex = 0;
        else {
            sIndex += 1;
            if (sIndex >= url.length()) return "";
        }
        int dotIndex = url.lastIndexOf(".");
        if (dotIndex < sIndex) dotIndex = url.length();
        if (dotIndex == sIndex) return "";
        return url.substring(sIndex, dotIndex);
    }

    public static String getSuffix(String url) {
        if (TextUtils.isEmpty(url)) return url;
        int dotIndex = url.lastIndexOf(".");
        if (dotIndex < 0)
            return url;
        return url.substring(dotIndex);
    }
    //endregion

    //region Str To Other

    /**
     * String转Int
     */
    public static int parseInt(String str, int need) {
        int i = need;
        if (!TextUtils.isEmpty(str)) {
            try {
                i = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    /**
     * String转Float
     */
    public static float parseFloat(String str, float need) {
        float f = need;
        if (!TextUtils.isEmpty(str)) {
            try {
                f = Float.parseFloat(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return f;
    }

    /**
     * String转Double
     */
    public static double parseDouble(String str, double need) {
        double d = need;
        if (!TextUtils.isEmpty(str)) {
            try {
                d = Double.parseDouble(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return d;
    }

    /**
     * String转Long
     */
    public static long parseLong(String str, long need) {
        long l = need;
        if (!TextUtils.isEmpty(str)) {
            try {
                l = Long.parseLong(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return l;
    }

    /**
     * String转Int 默认值0
     */
    public static int parseInt(String str) {
        return parseInt(str, 0);
    }

    /**
     * String转Float 默认值0f
     */
    public static float parseFloat(String str) {
        return parseFloat(str, 0f);
    }

    /**
     * String转Double 默认值0d
     */
    public static double parseDouble(String str) {
        return parseDouble(str, 0d);
    }

    /**
     * String转Long 默认值0l
     */
    public static long parseLong(String str) {
        return parseLong(str, 0L);
    }

    /**
     * String转Int
     */
    public static int double2Int(String str) {
        int i = 0;
        if (!TextUtils.isEmpty(str)) {
            try {
                double d = Double.parseDouble(str);
                i = (int) d;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    //endregion

    //region empty

    /**
     * 判空获取值
     */
    public static String getEmptyStr(Object obj) {
        return getEmptyStr(obj, "");
    }

    /**
     * 判空获取值（需要的）
     */
    public static String getEmptyStr(Object obj, String need) {
        String result = need;
        if (obj != null) {
            String str = obj.toString();
            if (!TextUtils.isEmpty(str)) {
                result = str;
            }
        }
        return result;
    }

    /**
     * 判空获取值
     */
    public static String getEmptyStr(String str) {
        return getEmptyStr(str, "");
    }

    /**
     * 判空获取值（需要的）
     */
    public static String getEmptyStr(String str, String need) {
        String result = need;
        if (!TextUtils.isEmpty(str)) {
            result = str;
        }
        return result;
    }

    /**
     * 截取值防报错
     */
    public static String getSubStr(String str, int start, int end) {
        String result = "";
        try {
            result = str.substring(start, end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 安全获取list
     */
    public static <T> List<T> getEmptyList(List<T> list) {
        List<T> result = new ArrayList<T>();
        if (list != null) {
            result = list;
        }
        return result;
    }

    //endregion

    //region other

    /**
     * 比较两个long字符串的大小
     */
    public static long compareTwoStringLong(String str1, String str2) {
        return parseLong(str1) - parseLong(str2);
    }

    /**
     * 比较两个long字符串的大小
     */
    public static long compareTwoStringLong2(String str1, long l) {
        return parseLong(str1) - l;
    }

    /**
     * 简单的隐藏一位小数.0
     */
    public static String formatMoney(String money) {
        String result = "0";
        if (!TextUtils.isEmpty(money)) {
            if (money.endsWith(".0")) {
                result = money.substring(0, money.length() - 2);
            }
        }
        return result;
    }

    /**
     * list判空
     */
    public static boolean notEmptyList(List list) {
        return list != null && list.size() > 0;
    }

    /**
     * list判空
     */
    public static boolean isEmptyList(List list) {
        return !notEmptyList(list);
    }

    /**
     * list兼容获取前X项
     */
    public static <T> List<T> safeSublist(List<T> list, int x) {
        List<T> result = new ArrayList<>();
        if (notEmptyList(list)) {
            int getNum = list.size();
            if (getNum > x) getNum = x;
            result.addAll(list.subList(0, getNum));
        }
        return result;
    }

    /**
     * 将数字转换成字母(0代表A)
     */
    public static String numToLetter(int num) {
        return (char) (num + 65) + "";
    }

    /**
     * 俩int数据相除得float
     */
    public static float twoIntdivideFloat(int a, int b) {
        return ((float) a) / ((float) b);
    }

    /**
     * 俩String 格式int数据相乘
     */
    public static String twoIntStrMutipyInt(String a, String b) {
        return parseInt(a) * parseInt(b) + "";
    }

    /**
     * 俩double字符串数据相乘得int字符串
     */
    public static String twoDoubleStrMultipyInt(String str1, String str2) {
        return (int) (parseDouble(str1) * parseDouble(str2)) + "";
    }

    /**
     * 俩double字符串数据相乘,保留两位小数
     */
    public static String twoDoubleStrMultipy(String str1, String str2) {
        return keepTwoDecimal(parseDouble(str1) * parseDouble(str2));
    }

    /**
     * 俩double字符串数据相乘,保留两位小数（百分比）
     */
    public static String twoDoubleStrMultipy2(String str1, String str2) {
        return keepTwoDecimal(parseDouble(str1) * parseDouble(str2) / 100);
    }

    /**
     * 比较两个double字符串的大小
     */
    public static double compareTwoStringDouble(String str1, String str2) {
        return parseDouble(str1) - parseDouble(str2);
    }

    /**
     * 比较两个double字符串的大小2
     */
    public static double compareTwoStringDouble2(String str1, String str2) {
        return parseDouble(str1) - parseDouble(str2) * 10000;
    }

    //endregion

    //region money

    /**
     * 保留两位小数——四舍五入
     */
    public static float roundNormalTwoPlace(float res) {
        try {
            return new BigDecimal(res).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 取整——四舍五入
     */
    public static String roundNormalZeroPlace(String res) {
        try {
            return new BigDecimal(res).setScale(0, BigDecimal.ROUND_UP).toString();
        } catch (Exception e) {
            return "0";
        }
    }

    /**
     * 保留两位小数——进位
     */
    public static double roundUpTwoPlace(double res) {
        try {
            return new BigDecimal(res).setScale(2, BigDecimal.ROUND_UP).doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 保留两位小数——进位
     */
    public static float roundUpTwoPlace(float res) {
        try {
            return new BigDecimal(res).setScale(2, BigDecimal.ROUND_UP).floatValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 保留两位小数——舍位
     */
    public static double roundDownTwoPlace(double res) {
        try {
            return new BigDecimal(res).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 保留两位小数——舍位
     */
    public static float roundDownTwoPlace(float res) {
        try {
            return new BigDecimal(res).setScale(2, BigDecimal.ROUND_DOWN).floatValue();
        } catch (Exception e) {
            return 0;
        }
    }

    //endregion

    //region split

    /**
     * 分割字符串只要前一个
     */
    public static String splitFront(String res, String reg) {
        String result = "";
        if (!TextUtils.isEmpty(res)) {
            String[] strs = res.split(reg);
            if (strs.length == 0) {//有reg且reg是末尾，也就是res == reg
            } else {
                if (strs.length == 1) {//没有reg || 空串 || 末尾是reg
                    if (!res.contains(reg)) {//没有reg || 空串
                    } else {//末尾是reg
                    }
                } else {//有reg且reg不是末尾，reg前面可以没有值
                }
                result = strs[0];
            }
        }
        return result;
    }

    /**
     * 分割字符串只要后一个
     */
    public static String splitBehind(String res, String reg) {
        String result = "";
        if (!TextUtils.isEmpty(res)) {
            String[] strs = res.split(reg);
            if (strs.length == 0) {//有reg且reg是末尾，也就是res == reg
            } else {
                if (strs.length == 1) {//没有reg || 空串 || 末尾是reg
                    if (!res.contains(reg)) {//没有reg || 空串
                    } else {//末尾是reg
                    }
                } else {//有reg且reg不是末尾，reg前面可以没有值
                    result = strs[1];
                }
            }
        }
        return result;
    }

    //endregion

    //region For EventBus

    public static String eventBusJoint(int a, int b) {
        return a + "w-w" + b;
    }

    public static String eventBusJoint(String a, String b) {
        return a + "w-w" + b;
    }

    public static String[] eventBusSplit(String s) {
        return s.split("w-w");
    }

    //endregion

    //region 保留固定位数小数

    /**
     * 保留X位小数
     */
    public static String keepXDecimal(Double res, int place) {
        StringBuilder pattern = new StringBuilder("0");
        if (place > 0) {
            for (int i = 0; i < place; i++) {
                if (i == 0) {
                    pattern.append(".0");
                } else {
                    pattern.append("0");
                }
            }
        }
        return new DecimalFormat(pattern.toString()).format(res);
    }

    /**
     * 保留一位小数
     */
    public static String keepOneDecimal(String res) {
        return keepOneDecimal(parseFloat(res));
    }

    /**
     * 保留一位小数
     */
    public static String keepOneDecimal(Float res) {
        return keepXDecimal(res.doubleValue(), 1);
    }

    /**
     * 保留0位小数
     */
    public static String keepZeroDecimal(Float res) {
        return keepXDecimal(res.doubleValue(), 0);
    }

    /**
     * 保留两位小数
     */
    public static String keepTwoDecimal(Double res) {
        return keepXDecimal(res, 2);
    }

    /**
     * 保留两位小数
     */
    public static String keepTwoDecimal(String res) {
        return keepTwoDecimal(parseDouble(res));
    }

    /**
     * 保留两位小数
     */
    public static String keepTwoDecimal(Float res) {
        return keepTwoDecimal(res.doubleValue());
    }

    /**
     * 保留八位小数
     */
    public static String keepEightDecimal(Double res) {
        return keepXDecimal(res, 8);
    }

    /**
     * 保留八位小数
     */
    public static String keepEightDecimal(String res) {
        return keepEightDecimal(parseDouble(res));
    }

    /**
     * 保留整数，并每隔3位一个分隔符
     */
    public static String keepZeroDecimalAddDouhao(String res) {
        return new DecimalFormat("###,###").format(parseInt(res));
    }

    /**
     * 除以10000后，保留两位小数
     */
    public static String keepTwoDecimalDivideWan(Float res) {
        return keepTwoDecimal(res / 10000f);
    }

    /**
     * 除以10000后，保留两位小数
     */
    public static String keepTwoDecimalDivideWan(int res) {
        return keepTwoDecimal(res / 10000f);
    }

    /**
     * 除以10000后，保留两位小数
     */
    public static String keepTwoDecimalDivideWan(String res) {
        return keepTwoDecimalDivideWan(parseFloat(res));
    }

    /**
     * 除以10000后，保留一位小数
     */
    public static String keepOneDecimalDivideWan(int res) {
        return keepOneDecimal(res / 10000f);
    }

    /**
     * 除以10000保留整数
     */
    public static String divideWan(int res) {
        return keepZeroDecimal(res / 10000f);
    }

    /**
     * 除以1000保留一位小数
     */
    public static String keepOneDecimalDivideQian(int res) {
        return keepOneDecimal(res / 1000f);
    }

    /**
     * 除以1000保留整数(13位时间戳转10位)
     */
    public static String divideQian(String res) {
        return (int) (parseFloat(res) / 1000f) + "";
    }

    /**
     * 乘以10000
     */
    public static String multiWan(String res) {
        return String.valueOf(parseFloat(res) * 10000f);
    }

    //endregion

    /**
     * 字节 转换为B MB GB
     *
     * @param size 字节大小
     * @return
     */
    public static String getPrintSize(long size) {
        long rest = 0;
        if (size < 1024) {
            return size + "B";
        } else {
            size /= 1024;
        }

        if (size < 1024) {
            return size + "KB";
        } else {
            rest = size % 1024;
            size /= 1024;
        }

        if (size < 1024) {
            size = size * 100;
            return size / 100 + "." + rest * 100 / 1024 % 100 + "MB";
        } else {
            size = size * 100 / 1024;
            return size / 100 + "." + size % 100 + "GB";
        }
    }

    public static boolean isValidUrl(String str) {
        str = str.toLowerCase();
        String domainRules = "com.cn|net.cn|org.cn|gov.cn|com.hk|公司|中国|网络|com|net|org|int|edu|gov|mil|arpa|Asia|biz|info|name|pro|coop|aero|museum|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cf|cg|ch|ci|ck|cl|cm|cn|co|cq|cr|cu|cv|cx|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|eh|es|et|ev|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gh|gi|gl|gm|gn|gp|gr|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|in|io|iq|ir|is|it|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|ml|mm|mn|mo|mp|mq|mr|ms|mt|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nt|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|pt|pw|py|qa|re|ro|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|st|su|sy|sz|tc|td|tf|tg|th|tj|tk|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|va|vc|ve|vg|vn|vu|wf|ws|ye|yu|za|zm|zr|zw";
        String regex = "^((https|http|ftp|rtsp|mms)?://)"
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@
                + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "(([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]+\\.)?" // 域名- www.
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名
                + "(" + domainRules + "))" // first level domain- .com or .museum
                + "(:[0-9]{1,4})?" // 端口- :80
                + "((/?)|" // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher isUrl = pattern.matcher(str);
        return isUrl.matches();
    }

    /**
     * 检测邮箱地址是否合法
     *
     * @param email 邮箱地址
     * @return true合法 false不合法
     */
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 隐藏手机号
     *
     * @param phoneNumber 手机号
     */
    public static String shieldPhoneNumber(@NonNull String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) return "";
        return phoneNumber.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 检测手机号是否合法
     *
     * @param phoneNumber 手机号
     * @return true合法 false不合法
     */
    public static boolean checkPhoneNumber(String phoneNumber) {
        String regex = "(1[0-9][0-9]|15[0-9]|18[0-9])\\d{8}";
        return Pattern.matches(regex, phoneNumber);
    }

    public static boolean isStartWithChinese(String str) {
        if (TextUtils.isEmpty(str))
            return false;
        return java.lang.Character.toString(str.charAt(0)).matches(
                "[\\u4E00-\\u9FA5]+");
    }

    @NotNull
    public static UrlEntity parseUrl(@Nullable String url) {
        UrlEntity entity = new UrlEntity();
        if (url == null) {
            return entity;
        }
        url = url.trim();
        if (url.equals("")) {
            return entity;
        }
        String[] urlParts = url.split("\\?");
        entity.baseUrl = urlParts[0];
        //没有参数
        if (urlParts.length == 1) {
            return entity;
        }
        //有参数
        String[] params = urlParts[1].split("&");
        entity.params = new HashMap<>();
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length > 1)
                entity.params.put(keyValue[0], keyValue[1]);
        }
        return entity;
    }

    public static class UrlEntity {

        public String baseUrl;

        public Map<String, String> params;
    }
}