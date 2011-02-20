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

import java.util.ArrayList;
import java.util.Enumeration;
import javax.jmdns.ServiceInfo;

public class ServiceInformation implements fr.prima.omiscid.dnssd.interf.ServiceInformation {

    private ServiceInfo serviceInfo;

    /* package */ServiceInformation(String type, String name) {
        serviceInfo = ServiceInfo.create(type, name, 0, 0, 0, ServiceInfo.NO_VALUE);
    }

    /* package */ServiceInformation(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    // /*package*/ ServiceInformation(String type, String name, int port, String
    // text) {
    // serviceInfo = new ServiceInfo(type,name,port,text);
    // }
    //
    // /*package*/ ServiceInformation(String type, String name, int port, int
    // weight, int priority, byte[] text) {
    // serviceInfo = new ServiceInfo(type,name,port,weight,priority,text);
    // }

    public int getPort() {
        return serviceInfo.getPort();
    }

    public String getFullName() {
        return serviceInfo.getQualifiedName();
    }

    public String getHostName() {
        return serviceInfo.getServer();
    }

    public String getRegType() {
        return serviceInfo.getType();
    }

    public byte[] getProperty(String key) {
        return serviceInfo.getPropertyBytes(key);
    }

    public String getStringProperty(String key) {
        return serviceInfo.getPropertyString(key);
    }

    public Iterable<String> getPropertyKeys() {
        ArrayList<String> res = new ArrayList<String>();
        Enumeration enumeration = serviceInfo.getPropertyNames();
        while (enumeration.hasMoreElements()) {
            res.add((String) enumeration.nextElement());
        }
        return res;
    }

}
