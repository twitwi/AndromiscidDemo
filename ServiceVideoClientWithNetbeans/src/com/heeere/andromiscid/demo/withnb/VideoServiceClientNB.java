package com.heeere.andromiscid.demo.withnb;

import android.graphics.Bitmap;
import fr.prima.omiscid.dnssd.interf.DNSSDFactory;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFilters;
import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.ServiceProxy;
import fr.prima.omiscid.user.service.ServiceRepository;
import fr.prima.omiscid.user.service.ServiceRepositoryListener;
import fr.prima.omiscid.user.connector.ConnectorType;
import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
import fr.prima.omiscid.user.util.Utility;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.heeere.andromiscid.BitmapSourceListener;
import com.heeere.andromiscid.ServiceImageSource;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoServiceClientNB extends Activity {
    public static String branding = "nb";
    ServiceFactory f;
    ServiceRepository repo;
    Service s;
    ServiceImageSource source;
    android.net.wifi.WifiManager.MulticastLock lock;
    android.os.Handler handler = new android.os.Handler();
    private ImageView camview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        camview = (ImageView) findViewById(R.id.cam);
        Button button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                notifyUser("click click click");
                //s.sendToAllClients("androidInited", Utility.message("Hi from " + branding));
            }
        });

        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    setUp();
                } catch (IOException ex) {
                    Logger.getLogger(VideoServiceClientNB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 2000);

    }

    /** Called when the activity is first created. */
    private void setUp() throws IOException {
        DNSSDFactory.DefaultFactory.factoryToTryFirst = "fr.prima.omiscid.dnssd.jmdns.DNSSDFactoryJmdns";
        DNSSDFactory.DefaultFactory.verboseMode = true;
        DNSSDFactory.DefaultFactory.verboseModeMore = true;

        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
        lock = wifi.createMulticastLock("HeeereDnssdLock");
        lock.setReferenceCounted(true);
        lock.acquire();

        f = new ServiceFactoryImpl();
        notifyUser("onStart " + branding);
        s = f.create("ServiceOnAndroid");
        notifyUser("s created");
        s.addConnector("sink", "...", ConnectorType.INPUT);

        //s.start();
        //notifyUser("s started");
        final BitmapSourceListener listener = new BitmapSourceListener() {
            public void stopped() {
            }

            public void imageReceived(Bitmap bmp) {
                notifyBmp(bmp);
            }
        };

        repo = f.createServiceRepository();

        repo.addListener(
                new ServiceRepositoryListener() {
                    @Override
                    public synchronized void serviceRemoved(ServiceProxy serviceProxy) {
                        notifyUser("removed " + serviceProxy.getPeerIdAsString() + " " + serviceProxy.getName());
                        if (source != null) {
                            source.stop();
                            source = null;
                        }
                    }

                    @Override
                    public synchronized void serviceAdded(ServiceProxy serviceProxy) {
                        notifyUser("added " + serviceProxy.getPeerIdAsString() + " " + serviceProxy.getName());
                        if (source == null) {
                            source = ServiceImageSource.createSmartImageSourceAndConnect(s, "sink", serviceProxy, true);
                            source.addBitmapSourceListener(listener);
                        }
                    }

                    @Override
                    protected synchronized void finalize() throws Throwable {
                        super.finalize();
                        notifyUser("gc'ed");
                    }
                }, ServiceFilters.nameIs("ServiceVideo"));
    }

    private void notifyBmp(final Bitmap bmp) {
        handler.postDelayed(new Runnable() {
            public void run() {
                camview.setImageBitmap(bmp);
            }
        }, 1);
    }

    private void notifyUser(final String msg) {
        handler.postDelayed(new Runnable() {
            public void run() {

                TextView t = (TextView) findViewById(R.id.text);
                t.setText(msg + "\n=== " + t.getText());
            }
        }, 1);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //new Thread(){public void run() {setUp();}}.start();
    }

    @Override
    protected void onDestroy() {
        if (repo != null) {
            repo.stop();
        }
        if (source != null) {
            source.stop();
            source = null;
        }
        if (s != null) {
            s.closeAllConnections();
            s.stop();
        }
        if (lock != null) {
            lock.release();

        }
        super.onStop();
    }
}
