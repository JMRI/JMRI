package jmri.jmrix.can.cbus.swing.hubpane;

/**
 * @author Bob Jacobsen Copyright (C) 2009, 2010, 2012, 2022
 * @author Steve Young Copyright (C) 2022
 */
public class CbusHubPane extends jmri.jmrix.openlcb.swing.hub.HubPane {

    public CbusHubPane(){
        super(5550, false);
        zero_conf_addr = "_cbus-can._tcp.local.";
    }

    @Override
    protected jmri.jmrix.can.adapters.gridconnect.GridConnectMessage getMessageFrom( jmri.jmrix.can.CanMessage m ) {
        return new jmri.jmrix.can.adapters.gridconnect.canrs.MergMessage(m);
    }

    @Override
    protected jmri.jmrix.can.adapters.gridconnect.GridConnectReply getBlankReply( ) {
        return new jmri.jmrix.can.adapters.gridconnect.canrs.MergReply();
    }

}
