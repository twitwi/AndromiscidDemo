/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.andromiscid.demo.onhost;

import fr.prima.omiscid.dnssd.interf.DNSSDFactory;
import fr.prima.omiscid.user.connector.ConnectorListener;
import fr.prima.omiscid.user.connector.ConnectorType;
import fr.prima.omiscid.user.connector.Message;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.ServiceFilters;
import fr.prima.omiscid.user.service.ServiceProxy;
import fr.prima.omiscid.user.service.ServiceRepository;
import fr.prima.omiscid.user.service.ServiceRepositoryListener;
import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
import fr.prima.omiscid.user.util.Utility;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author twilight
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        //DNSSDFactory.DefaultFactory.factoryToTryFirst = "jmdns";
        DNSSDFactory.DefaultFactory.verboseMode = true;
        ServiceFactory factory = new ServiceFactoryImpl();
        ServiceRepository repository = factory.createServiceRepository();
        final Service localService = factory.create("ServiceOnPC");

        repository.addListener(new ServiceRepositoryListener() {

            public void serviceAdded(ServiceProxy serviceProxy) {
                System.out.println("Service Found " + serviceProxy.getPeerIdAsString() + " ... connecting to it");
                localService.connectTo("hostInited", serviceProxy, "androidWaiting");
            }

            public void serviceRemoved(ServiceProxy serviceProxy) {
                System.out.println("Service Removed " + serviceProxy.getPeerIdAsString());
            }
        }, ServiceFilters.nameIs("ServiceOnAndroid"));


        localService.addConnector("hostInited", "we will init the connection for this one", ConnectorType.INOUTPUT);
        localService.addConnector("hostWaiting", "we will be passive here", ConnectorType.INOUTPUT);

        ConnectorListener logger = new ConnectorListener() {
            public void connected(Service service, String localConnectorName, int peerId) {
                System.out.println("Connector " + localConnectorName + " connected to " + Utility.intTo8HexString(peerId));
            }
            public void messageReceived(Service service, String localConnectorName, Message message) {
                System.out.println("Connector " + localConnectorName + " received from " + Utility.intTo8HexString(message.getPeerId()) + ": " + message.getBufferAsStringUnchecked());
            }

            public void disconnected(Service service, String localConnectorName, int peerId) {
                System.out.println("Connector " + localConnectorName + " disconnected from " + Utility.intTo8HexString(peerId));
            }

        };
        localService.addConnectorListener("hostInited", logger);
        localService.addConnectorListener("hostWaiting", logger);
        localService.addConnectorListener("hostWaiting", new ConnectorListener() {
            public void messageReceived(Service service, String localConnectorName, Message message) {
                System.out.println("Sending pong reply through connector " + localConnectorName + " to " + Utility.intTo8HexString(message.getPeerId()));
                service.sendReplyToMessage(Utility.message("pong"), message);
            }
            public void disconnected(Service service, String localConnectorName, int peerId) {}
            public void connected(Service service, String localConnectorName, int peerId) {}
        });

        localService.start();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (localService.getConnectorClientCount("hostInited") > 0) {
                    System.out.println("Sending hello to all through connector hostInited");
                    localService.sendToAllClients("hostInited", Utility.message("Hello at " + new Date().toString()));
                }
            }
        }, 1000, 5000);
    }

}
