package com.heeere.andromiscid.demo.withnb;

import android.graphics.Bitmap;
import fr.prima.omiscid.dnssd.interf.DNSSDFactory;
import fr.prima.omiscid.user.connector.Message;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFilters;
import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.ServiceProxy;
import fr.prima.omiscid.user.service.ServiceRepository;
import fr.prima.omiscid.user.service.ServiceRepositoryListener;
import fr.prima.omiscid.user.connector.ConnectorType;
import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import fr.prima.omiscid.user.connector.ConnectorListener;
import fr.prima.omiscid.user.exception.ConnectionRefused;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoServiceClientNB extends Activity {
    public static String branding = "nb";
    ServiceFactory f;
    ServiceRepository repo;
    Service s;
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
                s.closeAllConnections();
                //notifyUser("click click click");
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

    ConcurrentLinkedQueue<ServiceProxy> services = new ConcurrentLinkedQueue<ServiceProxy>();

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

        repo = f.createServiceRepository();

        repo.addListener(
                new ServiceRepositoryListener() {
                    @Override
                    public synchronized void serviceAdded(ServiceProxy serviceProxy) {
                        notifyUser("added " + serviceProxy.getPeerIdAsString() + " " + serviceProxy.getName());
                        boolean shouldConnect = services.isEmpty();
                        if (services.size() > 1) {
                            services.add(serviceProxy);
                        } else {
                            // this case is for the common case of a service get connected to then the others get added
                            // -> without this code, the first button press appear to do nothing because the first service is still next in the queue
                            ServiceProxy first = services.poll();
                            services.add(serviceProxy);
                            if (first == null) { // defensive thing
                                shouldConnect = true;
                            } else {
                                services.add(first);                                
                            }
                        }
                        if (shouldConnect) {
                            connectNext();
                        }
                    }
                    @Override
                    public synchronized void serviceRemoved(ServiceProxy serviceProxy) {
                        notifyUser("removed " + serviceProxy.getPeerIdAsString() + " " + serviceProxy.getName());
                        services.remove(serviceProxy);
                    }

                    @Override
                    protected synchronized void finalize() throws Throwable {
                        super.finalize();
                        notifyUser("gc'ed");
                    }

                }, ServiceFilters.nameIs("ServiceVideo"));
        s.addConnectorListener("sink", new ConnectorListener() {
            public void messageReceived(Service srvc, String string, Message msg) {
                notifyRaw(msg.getBuffer().clone());
            }

            public void disconnected(Service srvc, String string, int i) {
                connectNext();
            }

            public void connected(Service srvc, String string, int i) {
            }
        });
    }
    public void connectNext() {
        Logger.getLogger(VideoServiceClientNB.class.getName()).log(Level.INFO, "CONNECT NEXT "+services.size());
        ServiceProxy next = services.poll();
        if (next != null) {
            services.add(next);
            try {
                s.connectTo("sink", next, "jpegVideoStream");
            } catch (ConnectionRefused ex) {
                services.remove(next);
                connectNext();
            }
        }
    }

    AtomicReference<Bitmap> toPost = new AtomicReference<Bitmap>();
    AtomicReference<byte[]> rawToPost = new AtomicReference<byte[]>();
    private void notifyRaw(byte[] clone) {
        rawToPost.set(clone);
        handler.post(new Runnable() {
            public void run() {
                updateTheRaw();
            }
        });
    }
    private void notifyBmp(final Bitmap bmp) {
        toPost.set(bmp);
        handler.post(new Runnable() {
            public void run() {
                updateTheBitmap();
            }
        });
    }
    // runned in EDT
    private void updateTheRaw() {
        byte[] toSet = rawToPost.getAndSet(null);
        if (toSet != null) {
            camview.setImageBitmap(BitmapFactory.decodeByteArray(toSet, 0, toSet.length));
        }
    }
    private void updateTheBitmap() {
        Bitmap toSet = toPost.getAndSet(null);
        if (toSet != null) {
            camview.setImageBitmap(toSet);
        }
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
        /*
        if (source != null) {
            source.stop();
            source = null;
        }*/
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
