//SimpleSignalHeadServer.java
package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.jmris.AbstractSignalHeadServer;
import org.apache.log4j.Logger;

/**
 * Simple Server interface between the JMRI Sensor manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
public class SimpleSignalHeadServer extends AbstractSignalHeadServer {

    private DataOutputStream output;
    static Logger log = Logger.getLogger(SimpleSignalHeadServer.class.getName());

    public SimpleSignalHeadServer(DataInputStream inStream, DataOutputStream outStream) {
        super();
        output = outStream;
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String signalHeadName, int Status) throws IOException {
        this.addSignalHeadToList(signalHeadName);
        output.writeBytes("SIGNALHEAD " + signalHeadName + " " + this.nameForAppearance(Status) + "\n");
    }

    @Override
    public void sendErrorStatus(String signalHeadName) throws IOException {
        output.writeBytes("SIGNALHEAD ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        String[] status = statusString.split("\\s+");
        if (status.length == 3) {
            this.setSignalHeadAppearance(status[1], status[2]);
        } else {
            SignalHead signalHead = InstanceManager.signalHeadManagerInstance().getSignalHead(status[1]);
            this.sendStatus(signalHead.getSystemName(), signalHead.getAppearance());
        }
    }
}
