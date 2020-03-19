package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.InstanceManagerDelegate;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.jmris.AbstractSignalHeadServer;
import jmri.jmris.JmriConnection;

/**
 * Simple Server interface between the JMRI Sensor manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class SimpleSignalHeadServer extends AbstractSignalHeadServer {

    private DataOutputStream output;
    private JmriConnection connection;
    private final InstanceManagerDelegate instanceManagerDelegate;

    public SimpleSignalHeadServer(JmriConnection connection){
        this(connection,new InstanceManagerDelegate());
    }

    public SimpleSignalHeadServer(JmriConnection connection,InstanceManagerDelegate instanceManagerDelegate) {
        super();
        this.connection = connection;
        this.instanceManagerDelegate = instanceManagerDelegate;
    }

    public SimpleSignalHeadServer(DataInputStream inStream, DataOutputStream outStream){
        this(inStream,outStream, new InstanceManagerDelegate());
    }

    public SimpleSignalHeadServer(DataInputStream inStream, DataOutputStream outStream, InstanceManagerDelegate instanceManagerDelegate) {
        super();
        output = outStream;
        this.instanceManagerDelegate = instanceManagerDelegate;
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String signalHeadName, int Status) throws IOException {
        this.addSignalHeadToList(signalHeadName);
        this.sendMessage("SIGNALHEAD " + signalHeadName + " " + this.nameForAppearance(Status) + "\n");
    }

    @Override
    public void sendErrorStatus(String signalHeadName) throws IOException {
        this.sendMessage("SIGNALHEAD ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        String[] status = statusString.split("\\s+");
        if (status.length == 3) {
            this.setSignalHeadAppearance(status[1], status[2]);
        } else {
            SignalHead signalHead = instanceManagerDelegate.getDefault(jmri.SignalHeadManager.class).getSignalHead(status[1]);
            if(signalHead != null) {
               this.sendStatus(signalHead.getSystemName(), signalHead.getAppearance());
            } else {
               sendErrorStatus(status[1]);
            }
        }
    }

    private void sendMessage(String message) throws IOException {
        if (this.output != null) {
            this.output.writeBytes(message);
        } else {
            this.connection.sendMessage(message);
        }
    }
}
