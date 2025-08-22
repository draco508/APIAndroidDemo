package com.server;


import android.os.Looper;


import com.application.ApplicationController;
import com.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppWebServer extends Thread {


    private boolean isRunning;
    private ServerSocket mServerSocket;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String carriageReturn = "\r\n";
    private static boolean isStarted = false;
    public int webPort;

    long time = System.currentTimeMillis();

    static SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    private List<String> allPaths = new ArrayList<>();

    public static void startServer() {
        if (isStarted) {
            return;
        }

        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        isStarted = true;
        Thread t = new AppWebServer();
        t.start();

    }

    public AppWebServer() {
        try {
            allPaths = Utils.listAssetFiles(ApplicationController.getInstance().getApplicationContext(), "");

            mServerSocket = new ServerSocket(ApplicationController.getInstance().hostPort);
            mServerSocket.setReuseAddress(true);
        } catch (Exception e) {
            Utils.log("new ServerSocket===>>>" + e.getMessage());
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        isRunning = true;
        while (isRunning) {
            try {
                Socket socket = mServerSocket.accept();
                if (socket == null) {
                    continue;
                }

                try {
                    newSocket(socket);
                } catch (Exception e) {
                    Utils.log("error processClient===>>>" + e.getMessage());
                }

            } catch (IOException e) {
                Utils.log("error IOException===>>>" + e.getMessage());
            }
        }
    }

    private String getSocketId(Socket client) {
        return "http:/" + client.getInetAddress().toString() + ":" + client.getPort();
    }

    private void newSocket(Socket socket) throws IOException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream out = null;
                DataInputStream in = null;
                try {
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }




                while (true) {
                    String requestHeader = "";
                    String temp = "";
                    try {
                        while ((temp = in.readLine()) != null) {
                            if (temp.isEmpty()) break;

                            if (requestHeader.isEmpty()) {
                                requestHeader = temp;
                            } else {
                                requestHeader = requestHeader + "\n" + temp;
                            }

                        }
                    } catch (IOException e) {
                        Utils.log("socket error when read");
                        throw new RuntimeException(e);
                    }


                    long startAt = 0;
                    long endAt = 0;
                    String method = "";
                    String url = null;
                    Matcher matcher = null;
                    matcher = Pattern.compile("\\S*").matcher(requestHeader);
                    if (matcher.find()) {
                        method = matcher.group();
                    }



                    if (method.equals("OPTIONS")) {
                        try {
                            out.write(getOptionsHeader());
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        continue;
                    }

                    if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                        break;
                    }

                    long contentLength = -1;
                    matcher = Pattern.compile("Content-Length:( )*(?<contentlength>(\\d)+)").matcher(requestHeader);
                    if (matcher.find()) {
                        contentLength = Long.parseLong(matcher.group("contentlength"));
                    }

                    matcher = Pattern.compile("Range:( )*bytes( )*=*(?<start>(\\d)+)").matcher(requestHeader);
                    if (matcher.find()) {
                        startAt = Long.parseLong(matcher.group("start"));
                    }

                    matcher = Pattern.compile("Range:.*-( )*(?<end>(\\d)+)").matcher(requestHeader);
                    if (matcher.find()) {
                        endAt = Long.parseLong(matcher.group("end"));
                    }

                    matcher = Pattern.compile("\\S*\\s(?<url>\\S*)").matcher(requestHeader);
                    if (matcher.find()) {
                        url = matcher.group("url");
                    }
                    if (method.equals("POST")) {
                        Utils.log("yyyy contentLength123===", contentLength);
                        try {
                            long t = System.currentTimeMillis();
                            byte[] data = new byte[1024*1024*40];
                            long count = contentLength;
                            while (count > 0) {
                                int r = in.read(data);
                                if (r <= 0) {
                                    break;
                                } else {
                                    count = count - r;
                                }
                            }

                            out.write(getPostHeader());
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {

                        String segments[] = url.split("/");
                        String fileName = segments.length > 0 ? segments[segments.length - 1] : "";
                        if (fileName.contains("?")) {
                            fileName = fileName.split("\\?")[0];
                        }
                        String path = "";

                        for (int i = 0; i < allPaths.size(); i++) {
                            if (allPaths.get(i).endsWith("/" + fileName)) {
                                path = allPaths.get(i);
                            }
                        }

                        if (path.isEmpty()) {
                            try {
                                out.write(getHeader(0, null));
                                out.flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            continue;
                        }

                        InputStream inputStream = null;
                        long fileLength =0;
                        try {
                            inputStream = ApplicationController.getInstance().getApplicationContext().getAssets().open(path);
                             fileLength =  inputStream.available();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (endAt == 0) {
                            endAt = fileLength - 1;
                        }

                        String contentType = Utils.getContentType(path);




                        try {
                            byte[]header_byte = getHeader(fileLength, startAt, endAt, contentType);

                            out.write(header_byte);
                            if (method.equals("GET")) {
                                if (startAt > 0) {
                                    inputStream.skip(startAt);
                                }
                                Utils.copyStream(inputStream, out, endAt - startAt + 1);
                            }

                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        }).start();


    }

    private String makeResponseHeader(String filePath, long fileLength, long startAt, long endAt, boolean isLive) {
        String headers = "";
        String contentType = Utils.getContentType(filePath);
        String etag = Integer.toHexString((filePath + "-" + time).hashCode());
        long newLen = Math.max(endAt - startAt + 1, 0);
        if (startAt == 0) {
            headers += "HTTP/1.1 200 OK \r\n";
        } else {
            headers += "HTTP/1.1 206 Partial Content \r\n";
        }
        headers += "Content-Type: " + contentType + "\r\n";
        // headers += "Accept-Ranges: bytes\r\n";
        // headers += "Etag: " + etag + "\r\n";
//        headers += "Content-Range: bytes " + startAt + "-" + endAt + "/" + fileLength + "\r\n";
//        if (!isLive) {
//            headers += "Content-Length: " + newLen + "\r\n";
//        }
//        headers += "Content-Length: " + newLen + "\r\n";
        if (1 < 0/*test safari*/) {
            headers += "Accept-Ranges: bytes\r\n";
            headers += "Content-Range: bytes " + startAt + "-" + endAt + "/" + fileLength + "\r\n";
            headers += "Content-Length: " + newLen + "\r\n";
        }
        headers += "Date: " + gmtFrmt.format(new Date()) + "\r\n";
        headers += "Connection: keep-alive\r\n";
        headers += "Etag: " + etag + "\r\n";
        headers += "\r\n";
        return headers;
    }


    private String makeResponseHeader2(String filePath, long fileLength, long startAt, long endAt, boolean isLive) {
        String headers = "";
        String contentType = Utils.getContentType(filePath);
        String etag = Integer.toHexString((filePath + "-" + time).hashCode());
        long newLen = Math.max(endAt - startAt + 1, 0);
        if (startAt == 0) {
            headers += "HTTP/1.1 200 OK\r\n";
        } else {
            headers += "HTTP/1.1 206 Partial Content\r\n";
        }
        headers += "Content-Type: " + contentType + "\r\n";

        headers += "Etag: " + etag + "\r\n";
        //headers += "Content-Range: bytes " + startAt + "-" + endAt + "/" + fileLength + "\r\n";
        if (!isLive) {
            headers += "Content-Length: " + newLen + "\r\n";
            headers += "Accept-Ranges: none\r\n";
        } else {
            headers += "Accept-Ranges: bytes\r\n";
        }
        //headers += "Accept-Ranges: bytes\r\n";
        headers += "Connection: keep-alive\r\n";
        headers += "Date: " + gmtFrmt.format(new Date()) + "\r\n";

        headers += "\r\n";
        return headers;
    }

    private String getHeader(File file, long start, long end) throws IOException {
        String etag = Integer.toHexString((file.getAbsolutePath() + time).hashCode());

        long fileSize = file.length() + 1000;
        String headers = "";
        long newLen = end - start + 1;
        if (newLen < 0) newLen = 0;

        if (start > 0) {// It is a seek or skip request if there's a Range
            // header
            headers += "HTTP/1.1 206 Partial Content\r\n";
            headers += "Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n";
            headers += "Accept-Ranges: bytes\r\n";
            headers += "Content-Length: " + newLen + "\r\n";
            // headers += "Content-Range: bytes " + cbSkip + "-" + "/" + fileSize + "\r\n";
            //headers += "Content-Range: bytes " + start + "-" + end + "/" + fileSize + "\r\n";

            headers += "Content-Range: bytes " + start + "-" + "\r\n";
            headers += "Connection: keep-alive\r\n";
            headers += "Etag: " + etag + "\r\n";

            headers += "\r\n";
        } else {
            headers += "HTTP/1.1 200 OK\r\n";
            headers += "Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n";
            headers += "Accept-Ranges: bytes\r\n";
            headers += "Content-Length: " + newLen + "\r\n";
            //  headers += "Content-Range: bytes " + start + "-" + end + "/" + fileSize + "\r\n";
            headers += "Connection: keep-alive\r\n";
            headers += "Etag: " + etag + "\r\n";
            headers += "\r\n";
        }
        return headers;
    }

    private String getHeader(File file, long cbSkip) throws IOException {
        String etag = Integer.toHexString((file.getAbsolutePath() + time).hashCode());

        long fileSize = -1;
        String headers = "";
        if (cbSkip > 0) {// It is a seek or skip request if there's a Range
            // header
            headers += "HTTP/1.1 206 Partial Content\r\n";
            headers += "Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n";
            headers += "Accept-Ranges: bytes\r\n";
            // headers += "Content-Length: " + (fileSize - cbSkip) + "\r\n";
            // headers += "Content-Range: bytes " + cbSkip + "-" + "/" + fileSize + "\r\n";

            headers += "Content-Range: bytes " + cbSkip + "-" + "\r\n";
            headers += "Connection: keep-alive\r\n";
            headers += "Etag: " + etag + "\r\n";

            headers += "\r\n";
        } else {
            headers += "HTTP/1.1 200 OK\r\n";
            headers += "Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n";
            headers += "Accept-Ranges: bytes\r\n";
            //     headers += "Content-Length: " + fileSize + "\r\n";
            headers += "Connection: keep-alive\r\n";
            headers += "Etag: " + etag + "\r\n";
            headers += "\r\n";
        }
        return headers;
    }

    private long getCbSkip(String header) {
        String str = Utils.getStringBetween(header, "bytes=", "-");
        if (str != null) {
            return Long.parseLong(str);
        }
        return 0;
    }

    private byte[] getHeader(long lengthFile, long startAt, long endAt, String contentType) throws IOException {
        String headers = "";
        if (endAt - startAt + 1 == lengthFile) {// It is a seek or skip request if there's a Range
            // header
            headers += "HTTP/1.1 200 OK\r\n";
        } else {
            headers += "HTTP/1.1 206 Partial Content\r\n";
//            headers += "HTTP/1.1 200 OK\r\n";
//            headers += "Content-Type: " + contentType + "\r\n";
//            headers += "Accept-Ranges: bytes\r\n";
//            headers += "Content-Length: " + lengthFile + "\r\n";
//            if (1 > 0) {
//                headers += "cross-origin-embedder-policy: require-corp\r\n";
//                headers += "cross-origin-opener-policy: same-origin\r\n";
//                headers += "cross-origin-resource-policy: cross-origin\r\n";
//                headers += "Access-Control-Allow-Origin: *\r\n";
//            }
//            headers += "Connection: Keep-Alive\r\n";
//            headers += "\r\n";
        }


        headers += "Content-Type: " + contentType + "\r\n";
        headers += "Accept-Ranges: bytes\r\n";
        headers += "Content-Length: " + (endAt - startAt + 1) + "\r\n";
        if(startAt!=0 ||endAt!=lengthFile-1 ){
            headers += "Content-Range: bytes " + startAt + "-" + (endAt) + "/" + lengthFile + "\r\n";
        }

        if (1 < 0) {
            headers += "cross-origin-embedder-policy: require-corp\r\n";
            headers += "cross-origin-opener-policy: same-origin\r\n";
            headers += "cross-origin-resource-policy: cross-origin\r\n";
            headers += "Access-Control-Allow-Origin: *\r\n";
        }
        headers += "Connection: Keep-Alive\r\n";
        headers += "\r\n";


        return headers.getBytes(CHARSET);
    }

    private byte[] getOptionsHeader() {
        String headers = "";
        headers += "HTTP/1.1 200 OK" + carriageReturn;
        headers += "Allow: GET, HEAD, POST, OPTIONS" + carriageReturn;
        headers += "Access-Control-Allow-Origin: *" + carriageReturn;
        headers += "Access-Control-Allow-Methods: POST, GET, OPTIONS" + carriageReturn;
        headers += "Access-Control-Allow-Headers: X-PINGOTHER, Content-Type" + carriageReturn;
        headers += "Access-Control-Max-Age: 86400" + carriageReturn;
//        if (length == 0) {
//            headers += "HTTP/1.1 404 OK" + carriageReturn;
//        } else {
//            headers += "HTTP/1.1 200 OK" + carriageReturn;
//        }
//        if (typeFile != null) {
//            headers += "Content-Type: " + typeFile + carriageReturn;
//        }
//
//        headers += "Content-Length: " + length + carriageReturn;
//        headers += "Access-Control-Expose-Headers: Content-Length" + carriageReturn;
//        headers += "Connection: Keep-Alive" + carriageReturn;
        headers += carriageReturn;
        return headers.getBytes(CHARSET);
    }

    private byte[] getHeader(long length, String typeFile) {
        String headers = "";
        if (length == 0) {
            headers += "HTTP/1.1 404 OK" + carriageReturn;
        } else {
            headers += "HTTP/1.1 200 OK" + carriageReturn;
        }
        if (typeFile != null) {
            headers += "Content-Type: " + typeFile + carriageReturn;
        }

        headers += "Content-Length: " + length + carriageReturn;
        headers += "Access-Control-Expose-Headers: Content-Length" + carriageReturn;
        headers += "Connection: Keep-Alive" + carriageReturn;
        headers += carriageReturn;
        return headers.getBytes(CHARSET);
    }

    private byte[] getPostHeader() {
        String headers = "";
        headers += "HTTP/1.1 200 OK" + carriageReturn;
        headers += "Content-Length: " + 0 + carriageReturn;
        if (1 > 0) {
            headers += "cross-origin-embedder-policy: require-corp\r\n";
            headers += "cross-origin-opener-policy: same-origin\r\n";
            headers += "cross-origin-resource-policy: cross-origin\r\n";
            headers += "Access-Control-Allow-Origin: *\r\n";
        }
        headers += "Connection: Keep-Alive" + carriageReturn;
        headers += carriageReturn;
        return headers.getBytes(CHARSET);
    }

}