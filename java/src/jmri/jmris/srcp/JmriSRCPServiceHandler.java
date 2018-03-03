package jmri.jmris.srcp;

/**
 * This class provides access to the service handlers for individual object
 * types which can be passed to a parser visitor object.
 *
 * In addition to service handlers handling the connection's services, This
 * class keeps track of connection details the parser visitor may be asked to
 * return.
 *
 * @author Paul Bender Copyright (C) 2014
 *
 */
public class JmriSRCPServiceHandler extends jmri.jmris.ServiceHandler {

    public JmriSRCPServiceHandler(int port) {
        super();
        _session_number = port + (jmri.InstanceManager.getDefault(jmri.Timebase.class).getTime().getTime());
    }

    public long getSessionNumber() {
        return _session_number;
    }

    private long _session_number = 0;

    // _client_version holds the SRCP version the client supplied to the 
    // server during handshake.
    private String _client_version = "";

    public String getClientVersion() {
        return _client_version;
    }

    public void setClientVersion(String ver) {
        _client_version = ver;
    }

    // runmode tells the server whether it is in handshake mode
    // or run mode.
    private boolean runmode = false;

    // we can only set runmode to true, we can't go back to handshake mode.
    public void setRunMode() {
        runmode = true;
    }

    public boolean getRunMode() {
        return runmode;
    }

    // commandMode tells the server whether it is in command mode or info
    // mode, once runmode is set.  The default is command mode (per the protocol).
    private boolean commandMode = true;

    public boolean isCommandMode() {
        return commandMode;
    }

    public void setCommandMode(boolean mode) {
        commandMode = mode;
    }

}
