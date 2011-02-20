/**
 * Copyright© 2005-2006 INRIA/Université Pierre Mendès-France/Université Joseph Fourier.
 *
 * O3MiSCID (aka OMiSCID) Software written by Sebastien Pesnel, Dominique
 * Vaufreydaz, Patrick Reignier, Remi Emonet and Julien Letessier.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package fr.prima.omiscid.dnssd.jmdns;

import java.io.IOException;

import fr.prima.omiscid.dnssd.interf.DNSSDFactory;
import fr.prima.omiscid.dnssd.interf.ServiceBrowser;
import fr.prima.omiscid.dnssd.interf.ServiceRegistration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jmdns.JmDNS;

public class DNSSDFactoryJmdns implements DNSSDFactory {

    private JmDNS jmdns;

    public DNSSDFactoryJmdns() {
        try {
            this.jmdns = JmDNS.create();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    try {
                        jmdns.close();
                    } catch (IOException ex) {
                        Logger.getLogger(DNSSDFactoryJmdns.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Logger.getLogger(DNSSDFactoryJmdns.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public ServiceBrowser createServiceBrowser(String registrationType) {
        return new fr.prima.omiscid.dnssd.jmdns.ServiceBrowser(jmdns, registrationType + ".local.");
    }

    public ServiceRegistration createServiceRegistration(String serviceName, String registrationType) {
        return new fr.prima.omiscid.dnssd.jmdns.ServiceRegistration(jmdns, serviceName, registrationType + ".local.");
    }

}
