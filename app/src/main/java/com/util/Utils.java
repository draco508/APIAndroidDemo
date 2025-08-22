package com.util;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.Uri;
import android.net.http.SslError;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.zip.Deflater;

public class Utils {
    private static final String CONFIGURATION_NAME = "youplay";

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String secToTime(int sec) {
        int seconds = sec % 60;
        int minutes = sec / 60;
        if (minutes >= 60) {
            int hours = minutes / 60;
            minutes %= 60;
            if (hours >= 24) {
                int days = hours / 24;
                return String.format("%d days %02d:%02d:%02d", days, hours % 24, minutes, seconds);
            }
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static boolean isZeroArray(byte[] bytes) {

        int size_check = 10;
        int length = bytes.length;
        int interval = (length - 1) / size_check;
        for (int i = 0; i < size_check; i++) {
            if (bytes[i * interval] != 0) {
                return false;
            }
        }

        return true;
    }

    public static void handleSslError(final Context context, final SslError error) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String message = "SSL Certificate error.";
        switch (error.getPrimaryError()) {
            case SslError.SSL_DATE_INVALID:
                message = message + " The date of the certificate is invalid.";
                break;
            case SslError.SSL_UNTRUSTED:
                message = message + " The certificate authority is not trusted.";
                break;
            case SslError.SSL_EXPIRED:
                message = message + " The certificate has expired.";
                break;
            case SslError.SSL_IDMISMATCH:
                message = message + " The certificate Hostname mismatch.";
                break;
            case SslError.SSL_NOTYETVALID:
                message = message + " The certificate is not yet valid.";
                break;
        }

        builder.setTitle("The app cannot be started.");
        builder.setMessage(message);
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();


    }

    //    public void log(String... msg) {
//        String joined2 = String.join(",", msg);
//        Utils.log(joined2);
//
//    }

    public static void log(Object... o) {

        String[] mss = new String[o.length];
        for (int i = 0; i < o.length; i++) {
            mss[i] = o[i] == null ? "null" : o[i].toString();
        }
//        String msg;
//        if (o instanceof String) {
//            msg = (String) o;
//        } else {
//            msg = o.toString();
//        }

        String message = String.join(" ", mss);

        String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

        //  System.out.println("mylog:" + className + "." + methodName + "():" + lineNumber + " | " + message);
        Log.i("mylog", className + "." + methodName + "():" + lineNumber + " | " + message);

    }

    public static void setTint(Drawable drawable, int color) {
        try {
            drawable.setTint(color);
        } catch (Throwable e) {
            drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    public static String convertTimeString(JSONObject obj) {
        try {
            if (obj.has("abbr_time")) {
                return obj.getString("abbr_time");
            } else if (obj.has("timestamp") || obj.has("time")) {
                Calendar cal = Calendar.getInstance(Locale.getDefault());
                long t = obj.has("timestamp") ? obj.getLong("timestamp") : obj.getLong("time");
                if (t < 9999999999L) {
                    t = t * 1000;
                }
                cal.setTimeInMillis(t);
                return DateFormat.format("dd-MMM-yyyy", cal).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String readAssets(Context context, String file_name) {
        InputStream input;
        try {
            AssetManager assetManager = context.getAssets();
            input = assetManager.open(file_name);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            return new String(buffer);

        } catch (IOException ignored) {

        }
        return null;
    }

    public static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static int pxToDp(Context context, int px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    public static boolean isExistPrefKey(Context context, String key) {
        if (key == null) {
            return false;
        }
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        return editPreference.contains(key);
    }

    public static int loadKeyInt(Context context, String key, int defaultValue) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        return editPreference.getInt(key, defaultValue);
    }

    public static void saveKeyInt(Context context, String key, int value) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = editPreference.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static boolean loadKeyBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        return editPreference.getBoolean(key, defaultValue);
    }

    public static void saveKeyBoolean(Context context, String key, boolean value) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = editPreference.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void saveKeyString(Context context, String key, String value) {
        if (key == null) {
            return;
        }
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = editPreference.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String loadKeyString(Context context, String key, String defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        return editPreference.getString(key, defaultValue);
    }

    public static String loadKeyString_oldversion(Context context, String key, String defaultValue) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        return editPreference.getString(key, defaultValue);
    }

    public static void deleteKey(Context context, String key) {
        if (key == null) {
            return;
        }
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        editPreference.edit().remove(key).apply();
    }

    public static float loadKeyFloat(Context context, String key, float defaultValue) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        return editPreference.getFloat(key, defaultValue);
    }

    public static void saveKeyFloat(Context context, String key, float value) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = editPreference.edit();
        editor.putFloat(key, value);
        editor.apply();
    }


    public static long loadKeyLong(Context context, String key, long defaultValue) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        return editPreference.getLong(key, defaultValue);
    }

    public static void saveKeyLong(Context context, String key, long value) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = editPreference.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void clearAllPrefApp(Context context) {
        SharedPreferences editPreference = context.getSharedPreferences(CONFIGURATION_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = editPreference.edit();
        editor.clear();
        editor.apply();
    }

    public static void clearPrefApp(Context context) {
        SharedPreferences prefA = context.getSharedPreferences(CONFIGURATION_NAME, MODE_PRIVATE);
        Map<String, ?> prefsMap = prefA.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            if (key.startsWith("no_clear___") || key.startsWith("pref__")) {
                continue;
            }
            prefA.edit().remove(key).apply();
        }
    }

    public static long getPrefAppSize(Context context) {
        long size = 0;
        SharedPreferences prefA = context.getSharedPreferences(CONFIGURATION_NAME, MODE_PRIVATE);
        Map<String, ?> prefsMap = prefA.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            if (key.startsWith("no_clear___")) {
                continue;
            }
            String data = entry.getValue().toString();
            try {
                byte[] byteArray = data.getBytes(StandardCharsets.UTF_8);
                size = size + byteArray.length;
            } catch (Exception ignored) {
            }
        }
        return size;
    }

    public static void clearCookieByDomain(String domain) {
        boolean secure = true;
        switch (domain) {
            case "facebook":
                domain = ".facebook.com";
                break;
            case "instagram":
                domain = ".instagram.com";
                break;
            case "twitter":
                domain = ".twitter.com";
                break;
        }
        CookieManager cm = CookieManager.getInstance();



        /* Cookies are stored by domain, and are not different for different schemes (i.e. http vs
         * https) (although they do have an optional 'secure' flag.) */
        domain = "http" + (secure ? "s" : "") + "://" + domain;
        String cookieGlob = cm.getCookie(domain);
        if (cookieGlob != null) {
            String[] cookies = cookieGlob.split(";");
            for (String cookieTuple : cookies) {
                String[] cookieParts = cookieTuple.split("=");

                /* setCookie has changed a lot between different versions of Android with respect to
                 * how it handles cookies like these, which are set in order to clear an existing
                 * cookie.  This way of invoking it seems to work on all versions. */
                CookieManager.getInstance().setCookie(domain, cookieParts[0] + "=;");

                /* These calls have worked for some subset of the the set of all versions of
                 * Android:
                 * cm.setCookie(domain, cookieParts[0] + "=");
                 * cm.setCookie(domain, cookieParts[0]); */
            }
            CookieManager.getInstance().flush();
        }
    }


    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String getStringAgo(int creation_time) {
        int off = (int) (System.currentTimeMillis() / 1000 - creation_time);
        int one_min = 60;
        int one_hour = 60 * 60;
        int one_day = 24 * 60 * 60;
        int one_month = 31 * 24 * 60 * 60;
        int one_year = 365 * 24 * 60 * 60;

        if (off > one_year) {
            int number_year = off / one_year;
            int number_month = (off - number_year * one_year) / one_month;
            String out;
            if (number_year == 1) {
                out = "1 year";
            } else {
                out = number_year + " years";
            }

            if (number_month > 0) {
                if (number_month == 1) {
                    return out + ", 1 month";
                } else {
                    return out + ", " + number_month + " months";
                }
            }
            return out;
        } else if (off > one_month) {
            int number = off / one_month;
            if (number == 1) {
                return number + " month";
            } else {
                return number + " months";
            }

        } else if (off > one_day) {
            int number = off / one_day;
            if (number == 1) {
                return number + " day";
            } else {
                return number + " days";
            }
        } else if (off > one_hour) {
            int number = off / one_hour;
            if (number == 1) {
                return number + " hr";
            } else {
                return number + " hrs";
            }
        } else if (off > one_min) {
            int number = off / one_min;
            if (number == 1) {
                return number + " min";
            } else {
                return number + " mins";
            }
        } else {
            return "Just now";
        }

    }

    public static ArrayList<JSONObject> convertToArrayList(JSONArray jArr) {
        ArrayList<JSONObject> list = new ArrayList<>();
        try {
            for (int i = 0; i < jArr.length(); i++) {
                list.add(jArr.getJSONObject(i));
            }
        } catch (JSONException ignored) {
        }

        return list;
    }

    public static JSONArray convertToJSONArray(ArrayList<JSONObject> list) {
        JSONArray jArr = new JSONArray();

        for (int i = 0; i < list.size(); i++) {
            jArr.put(list.get(i));
        }

        return new JSONArray(list);
    }

    public static String getCookie(String siteName, String CookieName) {
        String CookieValue = null;
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if (cookies != null) {
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                if (ar1.contains(CookieName)) {
                    String[] temp1 = ar1.split("=");
                    if (temp1.length > 1)
                        CookieValue = temp1[1];
                }
            }
        }
        return CookieValue;
    }

    public static String setParamForUrl(String url, String key, String value) {
        if (url == null) {
            return null;
        }
        String prefix = "";
        if (url.startsWith("base:")) {
            prefix = "base:";
            url = url.replace("base:", "");
        }
        Uri uri = Uri.parse(url);
        Uri.Builder newUri = uri.buildUpon();
        newUri.appendQueryParameter(key, value);
        return prefix + newUri.toString();
    }

    public static String getParamFromUrl(String url, String key) {
        try {
            if (!url.startsWith("http")) {
                if (url.startsWith("/")) {
                    url = "https://www.example.com" + url;
                } else {
                    url = "https://www.example.com/" + url;
                }
            }
            Uri uri = Uri.parse(url);
            return uri.getQueryParameter(key);
        } catch (Exception e) {

        }
        return null;
    }


    public static String toBase64(String message) {
        byte[] data;
        data = message.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(data, Base64.DEFAULT);
    }


    public static void sendMail(Context context, String subject, String body) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String[] to = {"thuthao.dev@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            switch (extension) {
                case "js":
                    return "text/javascript";
                case "woff":
                    return "application/font-woff";
                case "woff2":
                    return "application/font-woff2";
                case "ttf":
                    return "application/x-font-ttf";
                case "eot":
                    return "application/vnd.ms-fontobject";
                case "svg":
                    return "image/svg+xml";
                case "css":
                    return "text/css";
                case "ico":
                    return "image/x-icon";
            }
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getDomain(String url) {
        if (url != null) {
            int index = url.indexOf("/", 8);
            if (index > -1) {
                return url.substring(0, index);
            } else {
                return url;
            }
        }
        return "null";
    }

    public static int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).
        //
        // In particular, do NOT do 'Random rand = new Random()' here or you
        // will get not very good / not very random results.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive

        return rand.nextInt((max - min) + 1) + min;
    }


    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }


    public static void writeToFile(File file, String fcontent) {
        FileOutputStream fos;
        byte[] data = fcontent.getBytes();
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFile(File file) {

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            return text.toString();
        } catch (IOException e) {
        }
        return null;
    }

    public static JSONObject createDefaultThemeJson() {
        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("off_check", true);
//            jsonObject.put("on_check", false);
//            jsonObject.put("on_theme", "Dark:#181a1b:#eeeeee");
//            jsonObject.put("auto_check", false);
//            jsonObject.put("daytime", "Light:none");
//            jsonObject.put("nighttime", "Dark:#181a1b:#eeeeee");
//        } catch (JSONException e) {
//        }
        try {
            jsonObject.put("off_check", false);
            jsonObject.put("on_check", true);
            jsonObject.put("on_theme", "Deep Wine:#2C041C:#ffffff");
            jsonObject.put("auto_check", false);
            jsonObject.put("daytime", "Light:none");
            jsonObject.put("nighttime", "Dark:#181a1b:#eeeeee");
        } catch (JSONException e) {
        }
        return jsonObject;
    }

    public static String getThemeFromJson(JSONObject themeJson) {
        String theme = "Default:#FFFFFF:#000000:light";
        try {
            if (themeJson.getBoolean("on_check")) {
                theme = themeJson.getString("on_theme");
            } else if (themeJson.getBoolean("auto_check")) {
                if (isDayTime()) {
                    theme = themeJson.getString("daytime");
                } else {
                    theme = themeJson.getString("nighttime");
                }
            }
        } catch (JSONException e) {
        }
        return theme;
    }

    public static boolean isDayTime() {
        Calendar cal = Calendar.getInstance(); //Create Calendar-Object
        cal.setTime(new Date());               //Set the Calendar to now
        int hour = cal.get(Calendar.HOUR_OF_DAY); //Get the hour from the calendar
        if (hour >= 6 && hour < 18)              // Check if hour is between 8 am and 11pm
        {
            return true;
        }
        return false;
    }

    public static void hideKeybroad(Context context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Utils.log("error ==>" + e.getMessage());
        }

    }

//    public static String getFinalURL(Context context,String url) {
//        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
//        if (netInfo != null && netInfo.isConnected()) {
//            try {
//                URL urlServer = new URL(url);
//                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
//                urlConn.setRequestProperty("User-Agent", AppController.getInstance().getUserAgent());
//                urlConn.setInstanceFollowRedirects(false);
//                urlConn.setConnectTimeout(5000); //<- 3Seconds Timeout
//                urlConn.connect();
//
//                int responseCode = urlConn.getResponseCode();
//
//                if(responseCode==200){
//                    return url;
//                }else{
//                    String location = urlConn.getHeaderField( "Location" );
//                    return location;
//                }
//            } catch (MalformedURLException e1) {
//                Utils.log( "getFinalURL e111===>>>"+e1.getMessage() );
//                return null;
//            } catch (IOException e) {
//                Utils.log( "getFinalURL eee===>>>"+e.getMessage() );
//            }
//        }
//        return null;
//    }

    public static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getFaviconUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return "https://www.google.com/s2/favicons?domain=" + host + "&sz=128";

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return url;
    }


    public static boolean isVideoUrl(final String url) {
        Uri Download_Uri = Uri.parse(url);
        String filename = Download_Uri.getLastPathSegment();
        if (filename == null) {
            return false;
        }
        filename = filename.toLowerCase();
        return filename.endsWith(".mp4") || filename.endsWith(".webm");
    }

    public static boolean isImageUrl(final String url) {
        Uri Download_Uri = Uri.parse(url);
        String filename = Download_Uri.getLastPathSegment();
        if (filename == null) {
            return false;
        }
        filename = filename.toLowerCase();
        return filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg") || filename.endsWith(".gif") || filename.endsWith(".bmp");
    }


    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }


    public static String convertBufferedReader2String(BufferedReader reader) {
        String newLine = System.getProperty("line.separator");
        try {
            StringBuilder result = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                if (result.length() > 0) {
                    result.append(newLine);
                }
                result.append(line);
                if (line.length() == 0) {
                    break;
                }
            }
            return result.toString();
        } catch (IOException e) {
            Utils.log("IOException===>>>" + e.getMessage());
        }
        return null;
    }

    public static String convertInputStream2String(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream));
        return convertBufferedReader2String(reader);
    }

    public static String getStringBetween(String s, String start, String end) {
        try {
            if (s.indexOf(start) == -1) return null;
            s = s.substring(s.indexOf(start) + start.length());
            if (s.indexOf(end) == -1) return null;
            s = s.substring(0, s.indexOf(end));
            return s;
        } catch (Exception e) {
            return null;
        }
    }


    public static String getMime(String fileName) {
        if (fileName.toLowerCase().contains(".mp4")) {
            return "video/mp4";
        }
        if (fileName.toLowerCase().contains(".avi")) {
            return "video/avi";
        }
        if (fileName.toLowerCase().contains(".mov")) {
            return "video/mov";
        }
        return null;
    }

    private static void readAndDelete(File file) throws FileNotFoundException, IOException {
        System.out.println("---------------------------------------");
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        String s = null;
        byte[] b = null;
        int len = 0;
        int i = 0;
        // while( (s = raf.readLine())!=null)
        //remove lines 0-5
        for (int j = 0; j < 5; j++) {

            removeNthLine(file, 0);

        }


    }

    public static void removeNthLine(File f, int toRemove) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        toRemove = 0;
        // Leaf n first lines unchanged.
        for (int i = 0; i < toRemove; i++)
            raf.readLine();

        // Shift remaining lines upwards.
        long writePos = raf.getFilePointer();
        raf.readLine();
        long readPos = raf.getFilePointer();

        byte[] buf = new byte[1024];
        int n;
        while (-1 != (n = raf.read(buf))) {
            raf.seek(writePos);
            raf.write(buf, 0, n);
            readPos += n;
            writePos += n;
            raf.seek(readPos);
        }

        raf.setLength(writePos);
        raf.close();
    }


    public static void removeBytes(File f, int toRemove) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        raf.seek(toRemove);
        long writePos = 0;
        long readPos = toRemove;

        byte[] buf = new byte[1024 * 1024];
        int n;
        while (-1 != (n = raf.read(buf))) {
            raf.seek(writePos);
            raf.write(buf, 0, n);
            readPos += n;
            writePos += n;
            raf.seek(readPos);
        }
        raf.setLength(writePos);
        raf.close();
    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024 * 1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public static void deleteDir(File file) {

        File[] contents = file.listFiles();

        if (contents != null) {
            for (File f : contents) {
                //Utils.log("fff==>>"+f.getAbsolutePath()+"==="+f.isDirectory());
                if (f.isDirectory()) {
                    deleteDir(f);
                } else {
                    f.delete();
                }
            }
        }
        file.delete();
    }

    private static boolean isLocalPortFree3(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isTcpPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            // setReuseAddress(false) is required only on macOS,
            // otherwise the code will not work correctly on that platform
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port), 1);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean isLocalPortFree2(int port) {
        try {

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress("localhost", port)).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

//    private static boolean isLocalPortFree(String host, int port){
//        // Assume no connection is possible.
//        boolean result = false;
//
//        (new ServerSocket(port)).close();
//        result = true;
//
//        return result;
//    }

    public static boolean available(int port) {

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // Do nothing
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    static ArrayList<Integer> usedPorts = new ArrayList<>();

    public static boolean available_old(int port) {

        if (usedPorts.contains(port)) {
            return false;
        }


        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            usedPorts.add(port);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    //{1024-65535}
    public static int findFreePort(int used) {
        int[] ports = new int[]{9999, 9900, 9800, 9700, 9600};
        for (int i = 0; i < ports.length; i++) {
            if (available(ports[i]) && ports[i]!=used) {
                return ports[i];
            }
        }

        for (int i = 8000; i < 9999; i++) {
            if (available(i)&& ports[i]!=used) {
                return i;
            }
        }
        return 0;
    }

    public static ArrayList<String> listOfIpAddresses() {
        ArrayList<String> arrayOfIps = new ArrayList<String>(); // preferred IPs
        ArrayList<String> arrayOfIps4 = new ArrayList<String>(); // IPv4 addresses
        ArrayList<String> arrayOfIps6 = new ArrayList<String>(); // IPv6 addresses
        ArrayList<String> arrayOfIpsL = new ArrayList<String>(); // loopback


        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();

                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    String theIpTemp = inetAddress.getHostAddress();

                    if (inetAddress instanceof Inet6Address) {
                        arrayOfIps6.add(theIpTemp);
                        continue;
                    } else if (inetAddress.isLoopbackAddress()) {
                        arrayOfIpsL.add(theIpTemp);
                        continue;
                    } else if (intf.getDisplayName().matches("wlan.*")) {
                        arrayOfIps.add(0, theIpTemp); // prefer IPv4 on wlan interface
                        continue;
                    } else {
                        arrayOfIps4.add(theIpTemp);
                        continue;
                    }
                }
            }

            // append IP lists in order of preference
            arrayOfIps.addAll(arrayOfIps4);
            arrayOfIps.addAll(arrayOfIps6);
            arrayOfIps.addAll(arrayOfIpsL);

            if (arrayOfIps.size() == 0) {
                String firstIp = "0.0.0.0";
                arrayOfIps.add(firstIp);
            }

        } catch (SocketException ex) {
            Log.e("httpServer", ex.toString());
        }

        return arrayOfIps;
    }

    public static String getLocalIpAddress2() {
        //rmnet_data0 is created for USB tethering and for cellular connection
        String ip = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        ip = inetAddress.getHostAddress();
//                        if (!ip.contains("192.168")) {
//                            return ip;
//                        }
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return ip;
    }

    public static byte[] getFileInfo(String str) {
        final int max_length = 500;
        int length_str = str.getBytes().length;
        if (length_str > max_length) {
            Utils.log("header too long");
            return null;
        }
        for (int i = 0; i < max_length - length_str; i++) {
            str = str + " ";
        }
        return str.getBytes();
    }

    public static byte[] joinByteArray(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (int i = 0; i < arrays.length; i++) {
            length = length + arrays[i].length;
        }
        byte[] c = new byte[length];
        int offset = 0;
        for (int i = 0; i < arrays.length; i++) {
            System.arraycopy(arrays[i], 0, c, offset, arrays[i].length);
            offset = offset + arrays[i].length;
        }

        return c;
    }

    public static byte[] concat(List<byte[]> list) {
        int length = 0;
        for (int i = 0; i < list.size(); i++) {
            length = length + list.get(i).length;
        }
        byte[] c = new byte[length];
        int offset = 0;
        for (int i = 0; i < list.size(); i++) {
            System.arraycopy(list.get(i), 0, c, offset, list.get(i).length);
            offset = offset + list.get(i).length;
        }

        return c;
    }


    public static byte[] compressImageToBytes(Bitmap image, int maxSize) {
        return compressImage(image, Bitmap.CompressFormat.JPEG, maxSize);
    }

    /**
     * Compress the image size with JPEG format limit to the maxSize(kb).
     *
     * @param image
     * @param maxSize
     * @return
     */
    public static Bitmap compressImage(Bitmap image, int maxSize) {
        byte[] bytes = compressImage(image, Bitmap.CompressFormat.JPEG,
                maxSize);
        ByteArrayInputStream isBm = new ByteArrayInputStream(bytes);
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * Compress the image size limit to the maxSize(kb).
     *
     * @param image
     * @param compressFormat
     * @param maxSize
     * @return
     */
    public static byte[] compressImage(Bitmap image,
                                       Bitmap.CompressFormat compressFormat, int maxSize) {
        long ttt = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        image.compress(compressFormat, 80, baos);
        int options = 80;
        while (baos.toByteArray().length / 1024 > maxSize) {
            baos.reset();
            image.compress(compressFormat, options, baos);
            if (options <= 10) {
                options -= 4;
            } else {
                options -= 10;
            }
            if (options < 0) {
                break;
            }
        }
        Utils.log("time compressImage" + (System.currentTimeMillis() - ttt));
        return baos.toByteArray();
    }

    private String getHeader(long length, String typeFile) {
        String headers = "";
        headers += "HTTP/1.1 200 OK\r\n";
        headers += "Content-Type: " + typeFile + "\r\n";
        headers += "Content-Length: " + length + "\r\n";
        headers += "Connection: Keep-Alive\r\n";

        headers += "\r\n";
        return headers;
    }


    public static byte[] bitmap2Bgr(Bitmap bitmap) {


            /*
        convert nếu đầu vào không là ARGB_8888
         ColorMatrix colorMatrix = new ColorMatrix();
        ColorFilter colorFilter = new ColorMatrixColorFilter(
                colorMatrix);
        Bitmap argbBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(argbBitmap);
        Paint paint = new Paint();

        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(bitmap, 0, 0, null);
     */
        long t = System.currentTimeMillis();

        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888)
            throw new IllegalArgumentException("Bitmap must be in ARGB_8888 format");

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        byte[] bytes = new byte[pixels.length * 3];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        t = System.currentTimeMillis();
        int i = 0;
        for (int pixel : pixels) {
            // Get components assuming is ARGB
            int R = (pixel >> 16) & 0xff;
            int G = (pixel >> 8) & 0xff;
            int B = pixel & 0xff;


            bytes[i++] = (byte) B;
            bytes[i++] = (byte) G;
            bytes[i++] = (byte) R;
        }

        return bytes;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        int rotation;
        switch (orientation) {
            case Surface.ROTATION_0:
                rotation = 90;
                break;
            case Surface.ROTATION_180:
                rotation = 270;
                break;
            case Surface.ROTATION_270:
                rotation = 180;
                break;
            case Surface.ROTATION_90:
            default:
                rotation = 0;
                break;
        }

        matrix.setRotate(rotation);
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] compressBytes(final byte[] input) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] buf = new byte[1024 * 8];
        final Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        compressor.setInput(input);
        compressor.finish();
        while (!compressor.finished()) {
            final int compressedDataLength = compressor.deflate(buf);
            bos.write(buf, 0, compressedDataLength);
        }
        compressor.end();
        return bos.toByteArray();
    }

    public static MediaCodecInfo selectCodec(String mimeType) {

        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    public static String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();
        // Use try and catch to avoid the exceptions
        try {
            URL url = new URL(theUrl); // creating a url object
            URLConnection urlConnection = url.openConnection(); // creating a urlconnection object

            // wrapping the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            // reading from the urlconnection using the bufferedreader
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);


        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap rotateAndFlipBitmap(Bitmap source, float angle, boolean xFlip, boolean yFlip) {
        Matrix matrix = new Matrix();
        //  matrix.postTranslate(-source.getWidth()/2,-source.getHeight()/2);
        matrix.postRotate(angle);
        //matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
    }

    public static int getOrientationChanged(final int orientation) {
        int currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
        if (orientation >= 315 || orientation < 45) {
            currentOrientation = Surface.ROTATION_0;
        } else if (orientation >= 45 && orientation < 135) {
            currentOrientation = Surface.ROTATION_270;
        } else if (orientation >= 135 && orientation < 225) {
            currentOrientation = Surface.ROTATION_180;
        } else if (orientation >= 225 && orientation < 315) {
            currentOrientation = Surface.ROTATION_90;
        }
        return currentOrientation;
    }

    public static byte[] readFile(String pathFile) {
        try {
            File ifile = new File(pathFile);
            if (ifile.exists()) {
                FileInputStream fis = new FileInputStream(pathFile);
                byte[] buffer = new byte[fis.available()];
                while (fis.read(buffer) != -1) {
                }
                fis.close();
                return buffer;
            }
        } catch (Exception er) {
        }
        return null;
    }


    public static String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray)
                    .getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = null;
        }

        return "http://" + ipAddressString;
    }

    public static String getContentType(String filePath) {
        try {
            return Files.probeContentType(new File(filePath).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyStream(InputStream inputStream, OutputStream outputStream, long countMount) {
        byte[] buff = new byte[0];
        int total = 0;
        while (countMount > 0) {
            try {
                int available = inputStream.available();
                if (available > 0) {

                    available = (int) Math.min(available, countMount);
                    buff = new byte[available];
                    int read = inputStream.read(buff, 0, available);
                    if (read > 0) {
                        outputStream.write(buff, 0, read);

                        total = total + read;
                        countMount -= read;
                       // Thread.sleep(1);
                    }
                } else {
                    Thread.sleep(1);
                }

            } catch (Exception e) {
                Utils.log("error123===>>>", e.getMessage());
                break;
            }
        }
        try {
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.log("total===>>>", total);
    }

    public static boolean isDirectory(Context context, String path) throws IOException {
        //If list returns any entries, than the path is a directory
        String[] files = context.getAssets().list(path);
        if (files != null && files.length > 0) {
            return true;
        } else {
            try {
                //If we can open a stream then the path leads to a file
                context.getAssets().open(path);
                return false;
            } catch (Exception ex) {
                //.open() throws exception if it's a directory that you're passing as a parameter
                return true;
            }
        }
    }

    public static List<String> listAssetFiles(Context context, String path) {
        List<String> files = new ArrayList<>();
        String[] list;

        try {
            list = context.getAssets().list(path);
            if (list.length > 0) {
                for (String file : list) {

                    if (isDirectory(context, path + "/" + file)) { // <<-- check if filename has a . then it is a file - hopefully directory names dont have .
                        if (path.equals("")) {
                            files.addAll(listAssetFiles(context, file)); // <<-- To get subdirectory files and directories list and check
                        } else {
                            files.addAll(listAssetFiles(context, path + "/" + file)); // <<-- For Multiple level subdirectories
                        }
                    } else {
                        files.add(path + "/" + file);
                    }
                }

            } else {
                System.out.println("Failed Path = " + path);
                System.out.println("Check path again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }
}
