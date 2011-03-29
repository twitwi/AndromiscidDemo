package com.heeere.andromiscid;

import android.graphics.Bitmap;
import fr.prima.omiscid.user.connector.ConnectorListener;
import fr.prima.omiscid.user.connector.Message;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFilter;
import fr.prima.omiscid.user.service.ServiceProxy;
import java.nio.MappedByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.prima.omiscid.user.service.ServiceFilters.*;
import static fr.prima.omiscid.user.connector.ConnectorType.*;

/**
 *
 * @author twilight
 */
public class ServiceImageSource {


    /* static constructors */
    public static ServiceImageSource createSmartImageSourceAndConnect(Service localService, String localConnectorName, ServiceProxy remoteService) {
        return createSmartImageSourceAndConnect(localService, localConnectorName, remoteService, false);
    }

    public static ServiceImageSource createSmartImageSourceAndConnect(Service localService, String localConnectorName, ServiceProxy remoteService, boolean acceptLowQuality) {
        /* configuration filters */
        ServiceFilter rawFilter = hasConnector("rawVideoStream", OUTPUT);
        ServiceFilter jpegFilter = hasConnector("jpegVideoStream", OUTPUT);
        if (acceptLowQuality && jpegFilter.acceptService(remoteService)) {
            final BlockingQueue<ServiceImageSource> res = new ArrayBlockingQueue<ServiceImageSource>(1);
            localService.addConnectorListener(localConnectorName, new ConnectorListener() {

                public void messageReceived(Service service, String localConnectorName, Message message) {
                }

                public void disconnected(Service service, String localConnectorName, int peerId) {
                }

                public void connected(Service service, String localConnectorName, int peerId) {
                    try {
                        res.put(createJPEGImageSource(service, localConnectorName, peerId));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServiceImageSource.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            localService.connectTo(localConnectorName, remoteService, "jpegVideoStream");
            try {
                return res.take();
            } catch (InterruptedException ex) {
                Logger.getLogger(ServiceImageSource.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;

        } else if (rawFilter.acceptService(remoteService)) {
            final BlockingQueue<ServiceImageSource> res = new ArrayBlockingQueue<ServiceImageSource>(1);
            localService.addConnectorListener(localConnectorName, new ConnectorListener() {
                public void messageReceived(Service service, String localConnectorName, Message message) {
                }
                public void disconnected(Service service, String localConnectorName, int peerId) {
                }
                public void connected(Service service, String localConnectorName, int peerId) {
                    try {
                        res.put(createRawImageSource(service, localConnectorName, peerId));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServiceImageSource.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            localService.connectTo(localConnectorName, remoteService, "rawVideoStream");
            try {
                return res.take();
            } catch (InterruptedException ex) {
                Logger.getLogger(ServiceImageSource.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        } else {
            throw new IllegalArgumentException("Given service is not a candidate image source");
        }
    }
    public static ServiceImageSource createJPEGImageSource(Service localService, String localConnectorName, int remotePeerId) {
        ServiceImageSource result = new ServiceImageSource(Mode.JPEGCONNECTOR, localService, localConnectorName, remotePeerId);
        localService.addConnectorListener(localConnectorName, result.jpegConnectorListener());
        return result;
    }
    public static ServiceImageSource createRawImageSource(Service localService, String localConnectorName, int remotePeerId) {
        ServiceImageSource result = new ServiceImageSource(Mode.RAWCONNECTOR, localService, localConnectorName, remotePeerId);
        localService.addConnectorListener(localConnectorName, result.rawConnectorListener());
        return result;
    }

    /* public API */
    public synchronized void addBitmapSourceListener(BitmapSourceListener l) {
        bitmapSourceListeners.add(l);
    }

    public synchronized void removeBitmapSourceListener(BitmapSourceListener l) {
        bitmapSourceListeners.remove(l);
    }

    /**
     * Closes connection between local and remote service.
     */
    public void stop() {
        localService.closeConnection(localConnectorName, remotePeerId);
    }

    /* internal code */
    // TODO INTEGRATE IN OMISCID
    public static ServiceFilter connectorDescriptionContains(final String connectorName, final String what) {
        return new ServiceFilter() {
            public boolean acceptService(ServiceProxy serviceProxy) {
                return serviceProxy.getConnectorDescription(connectorName).contains(what);
            }
        };
    }
    final CopyOnWriteArrayList<BitmapSourceListener> bitmapSourceListeners = new CopyOnWriteArrayList<BitmapSourceListener>();

    static enum Mode {RAWCONNECTOR, JPEGCONNECTOR, SHAREDMEMORY}
    Mode mode;
    Service localService;
    String localConnectorName;
    int remotePeerId;
    private MappedByteBuffer mappedByteBuffer; // for shared memory

    private ServiceImageSource(Mode mode, Service localService, String localConnectorName, int remotePeerId) {
        this.mode = mode;
        this.localService = localService;
        this.localConnectorName = localConnectorName;
        this.remotePeerId = remotePeerId;
    }

    private void notifyStopped() {
        for (BitmapSourceListener l : bitmapSourceListeners) {
            l.stopped();
        }
    }

    private void notifyImage(Bitmap bmp) {
        for (BitmapSourceListener l : bitmapSourceListeners) {
            l.imageReceived(bmp);
        }
    }

    private boolean hasBufferedImageListeners() {
        return !bitmapSourceListeners.isEmpty();
    }

    private ConnectorListener jpegConnectorListener() {
        return new ConnectorListener() {

            public void messageReceived(Service service, String localConnectorName, Message message) {
                synchronized (ServiceImageSource.this) {
                    if (hasBufferedImageListeners()) {
                        Bitmap image = ImageTools.createBufferedImageFromJpegMessage(message);
                        notifyImage(image);
                    }
                }
            }

            public void disconnected(Service service, String localConnectorName, int peerId) {
                notifyStopped();
            }

            public void connected(Service service, String localConnectorName, int peerId) {
            }
        };
    }

    private ConnectorListener rawConnectorListener() {
        return new ConnectorListener() {

            public void messageReceived(Service service, String localConnectorName, Message message) {
                synchronized (ServiceImageSource.this) {
                    if (hasBufferedImageListeners()) {
                        Bitmap image = ImageTools.createBufferedImageFromRawMessage(message);
                        notifyImage(image);
                    }
                }
            }

            public void disconnected(Service service, String localConnectorName, int peerId) {
                notifyStopped();
            }

            public void connected(Service service, String localConnectorName, int peerId) {
            }
        };
    }

}
