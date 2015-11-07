package org.swellrt.android.service;



import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.Socket;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.atmosphere.wasync.impl.AtmosphereRequest.AtmosphereRequestBuilder;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider;

public class WaveSocketWAsync implements WaveSocket {

    public final static int EVENT_ON_OPEN = 1;
    public final static int EVENT_ON_CLOSE = 2;
    public final static int EVENT_ON_MESSAGE = 3;
    public final static int EVENT_ON_EXCEPTION = 4;
    public final static int EVENT_ON_CLOSE_ERROR = 5;



    final static int WAVE_MESSAGE_SEPARATOR = '|';
    final static String WAVE_MESSAGE_END_MARKER = "}|";

    public final static String TAG = "WaveSocketWAsync";

    /**
     * @param message
     * @return
     */
    private static boolean isPackedWaveMessage(String message) {
        return message.indexOf(WAVE_MESSAGE_SEPARATOR) == 0;
    }

    /**
     * @param packedMessage
     * @return
     */
    private static List<String> unpackWaveMessages(String packedMessage) {

        List<String> messages = new ArrayList<String>();

        if (isPackedWaveMessage(packedMessage)) {

            while (packedMessage.indexOf(WAVE_MESSAGE_SEPARATOR) == 0 && packedMessage.length() > 1) {
                packedMessage = packedMessage.substring(1);
                int marker = packedMessage.indexOf(WAVE_MESSAGE_END_MARKER);
                String splitMessage = packedMessage.substring(0, marker + 1);
                messages.add(splitMessage);
                packedMessage = packedMessage.substring(marker + 1);
            }
        }

        return messages;
    }

    // A gateway between socket thread and UI thread
    private Handler uiHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(android.os.Message msg) {


            switch (msg.arg1) {

                case EVENT_ON_OPEN:
                    callback.onConnect();
                    break;
                case EVENT_ON_CLOSE:
                    callback.onDisconnect();
                    break;
                case EVENT_ON_MESSAGE:

                    // Decode from Base64 because of Atmosphere Track Message Lenght server
                    // feauture
                    // NOTE: no Charset is specified, so this relies on UTF-8 as default
                    // charset
                    String coded = (String) msg.obj;

                    String decoded = new String(Base64.decode(coded,Base64.DEFAULT));


                    // Ignore heart-beat messages
                    // NOTE: is heart beat string always " "?
                    if (decoded == null || decoded.isEmpty() || decoded.startsWith(" ")
                            || decoded.startsWith("  ")) return;

                    if (isPackedWaveMessage(decoded)) {
                        List<String> unpacked = unpackWaveMessages(decoded);
                        for (String s : unpacked) {
                            callback.onMessage(s);
                        }

                    } else {
                        // Filter non JSON messages
                        // TODO remove, use atmosphere client properly
                        if (decoded.startsWith("{"))
                            callback.onMessage(decoded);
                    }

                    break;
                case EVENT_ON_EXCEPTION:
                    callback.onDisconnect();
                    break;
                case EVENT_ON_CLOSE_ERROR:
                    callback.onDisconnect();
                    break;

            }

        };

    };

    private final String urlBase;
    private Socket socket = null;
    private final WaveSocketCallback callback;
    private String sessionId;


    private class WebSocketRunnable implements Runnable {


        final String urlBase;
        final Handler uiHandler;
        final String sessionId;

        public WebSocketRunnable(String urlBase, Handler uiHandler, String sessionId) {
            this.urlBase = urlBase;
            this.uiHandler = uiHandler;
            this.sessionId = sessionId;
        }

        @Override
        public void run() {

            /*
            * Configure the Grizzly provider in the Async Http Client:
            * <a href='http://github.com/Atmosphere/wasync/wiki/Configuring-the-underlying-AHC-provider'>configure AHC</a>
            */

            AsyncHttpClientConfig.Builder ahcConfigBuilder = new AsyncHttpClientConfig.Builder();

            // Allow connections from any server, please use only for debug purpose
            if (SwellRTConfig.DISABLE_SSL_CHECK) {

                HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                // References to AsycHttpClient, Grizzly and SSL
                // https://groups.google.com/forum/#!topic/asynchttpclient/wgaAs3lszbI
                // http://stackoverflow.com/questions/21833804/how-to-make-https-calls-using-asynchttpclient

                // Issue 93740: Lollipop breaks SSL/TLS connections when using Jetty
                // https://code.google.com/p/android/issues/detail?id=93740

                // Support for SSL connections accepting self signed cert
                SSLContext sslContext = null;
                try {
                    sslContext = SSLContext.getInstance("TLS");

                    sslContext.init(null, new X509TrustManager[] { new X509TrustManager() {

                        public void checkClientTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }


                        public void checkServerTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[] {};
                        }

                    } }, new SecureRandom());

                } catch (Exception e) {
                    Log.e(TAG, "Error creating SSLContext", e);
                }

                ahcConfigBuilder.setSSLContext(sslContext).setHostnameVerifier(hostnameVerifier);

            }

            AsyncHttpClientConfig ahcConfig = ahcConfigBuilder.build();
            AsyncHttpClient ahc = new AsyncHttpClient(new GrizzlyAsyncHttpProvider(ahcConfig), ahcConfig);

            AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);

            AtmosphereRequestBuilder requestBuilder = client.newRequestBuilder()
                    .method(Request.METHOD.GET).trackMessageLength(true).uri(WaveSocketWAsync.this.urlBase)
                    // .transport(Request.TRANSPORT.WEBSOCKET)
                    .transport(Request.TRANSPORT.LONG_POLLING)
                    .header("Cookie", "WSESSIONID=" + sessionId);

            // Using waitBeforeUnlocking(2000) option to avoid high delay on long-polling connection
            WaveSocketWAsync.this.socket = client
                    .create(client.newOptionsBuilder().runtime(ahc).build())
                    .on(Event.OPEN.name(), new Function<String>() {

                        @Override
                        public void on(String arg0) {

                            Message msg = uiHandler.obtainMessage();
                            msg.arg1 = EVENT_ON_OPEN;
                            uiHandler.sendMessage(msg);

                        }

                    }).on(Event.CLOSE.name(), new Function<String>() {

                        @Override
                        public void on(String arg0) {

                            Message msg = uiHandler.obtainMessage();
                            msg.arg1 = EVENT_ON_CLOSE;
                            uiHandler.sendMessage(msg);

                        }

                    }).on(Event.REOPENED.name(), new Function<String>() {

                        @Override
                        public void on(String arg0) {

                            Message msg = uiHandler.obtainMessage();
                            msg.arg1 = EVENT_ON_OPEN;
                            uiHandler.sendMessage(msg);

                        }

                    }).on(Event.MESSAGE.name(), new Function<String>() {

                        @Override
                        public void on(String arg) {

                            Message msg = uiHandler.obtainMessage();
                            msg.arg1 = EVENT_ON_MESSAGE;
                            msg.obj = arg;
                            uiHandler.sendMessage(msg);

                        }

                    }).on(new Function<Throwable>() {

                        @Override
                        public void on(Throwable t) {

                            Message msg = uiHandler.obtainMessage();
                            msg.arg1 = EVENT_ON_EXCEPTION;
                            msg.obj = t;
                            uiHandler.sendMessage(msg);
                        }

                    });

            try {
                WaveSocketWAsync.this.socket.open(requestBuilder.build(), -1, TimeUnit.MILLISECONDS);
            } catch (IOException e) {

                WaveSocketWAsync.this.socket = null;

                Message msg = uiHandler.obtainMessage();
                msg.arg1 = EVENT_ON_CLOSE_ERROR;
                msg.obj = e;
                uiHandler.sendMessage(msg);

            }
        }




    } // Runnable


    public WaveSocketWAsync(final WaveSocket.WaveSocketCallback callback, String urlBase,
                            String sessionId) {
        this.urlBase = urlBase;
        this.callback = callback;
        this.sessionId = sessionId;
    }

    @Override
    public void connect() {

        if (socket != null) {
            throw new RuntimeException("Trying to start an already existing socket");
        }

        new Thread(new WebSocketRunnable(urlBase, uiHandler, sessionId)).start();

    }

    @Override
    public void disconnect() {

        // TODO handle threads properly! this is a mess
        new Thread(new Runnable() {

            @Override
            public void run() {
                WaveSocketWAsync.this.socket.close();
            }

        }).start();

    }



    @Override
    public void sendMessage(final String message) {

        // TODO handle threads properly! this is a mess
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    socket.fire(message);
                } catch (IOException e) {

                    Message msg = uiHandler.obtainMessage();
                    msg.arg1 = EVENT_ON_EXCEPTION;
                    msg.obj = e;
                    uiHandler.sendMessage(msg);

                }

            }

        }).start();

    }

}

