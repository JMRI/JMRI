package jmri.jmris.simpleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.PowerManager;
import jmri.jmris.AbstractPowerServer;
import jmri.jmris.JmriConnection;
import jmri.jmris.simpleserver.parser.JmriServerParser;
import jmri.jmris.simpleserver.parser.ParseException;
import jmri.jmris.simpleserver.parser.SimpleNode;
import jmri.jmris.simpleserver.parser.SimpleVisitor;
import jmri.jmris.simpleserver.parser.TokenMgrError;

/**
 * Simple Server interface between the JMRI power manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class SimplePowerServer extends AbstractPowerServer {

    private DataOutputStream output;
    private JmriConnection connection;
    public SimplePowerServer(DataInputStream inStream, DataOutputStream outStream) {
        output = outStream;
        mgrOK();
    }

    public SimplePowerServer(JmriConnection cnctn) {
        this.connection = cnctn;
        mgrOK();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(int Status) throws IOException {
        switch (Status) {
            case PowerManager.ON:
                this.sendStatus("POWER ON\n");
                break;
            case PowerManager.OFF:
                this.sendStatus("POWER OFF\n");
                break;
            default:
                this.sendStatus("POWER UNKNOWN\n");
                break;
        }
    }

    @Override
    public void sendErrorStatus() throws IOException {
        this.sendStatus("POWER ERROR\n");
    }

    @Override
    public void parseStatus(String statusString) throws jmri.JmriException {
        JmriServerParser p = new JmriServerParser(new java.io.StringReader(statusString));
        try{
           try{
              SimpleNode e=p.powercmd();
              SimpleVisitor v = new SimpleVisitor();
              e.jjtAccept(v,this);
              if(v.getOutputString() != null ){
                 sendStatus(v.getOutputString());
              } 
           } catch(ParseException | TokenMgrError pe){
              sendErrorStatus();
           }
        } catch(IOException ioe) {
          // we should check to see if there is an  
        }
    }

    public void sendStatus(String status) throws IOException {
        if (this.output != null) {
            this.output.writeBytes(status);
        } else {
            this.connection.sendMessage(status);
        }
    }
}
