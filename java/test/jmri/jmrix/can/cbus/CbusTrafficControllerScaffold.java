package jmri.jmrix.can.cbus;

import java.util.List;

import jmri.jmrix.AbstractMessage;

/**
 * TrafficControllerScaffold for jmrix.can.cbus classes.
 * @author Steve Young Copyright (C) 2022
 */
public class CbusTrafficControllerScaffold extends jmri.jmrix.can.TrafficControllerScaffold {

    /**
     * Create a new TC Scaffold and set the System Connection to use it.
     * @param memo the System Connection to use with this Traffic Controller.
     */
    public CbusTrafficControllerScaffold(jmri.jmrix.can.CanSystemConnectionMemo memo){
        super();
        memo.setTrafficController(CbusTrafficControllerScaffold.this);
    }

    /**
     * Get a String translation of Outbound messages. e.g.
     * Outbound 3
     * Request Node in Setup Parameters RQNP  : [5f8] 10
     * Request Module Name RQMN  : [5f8] 11
     * Set Node Number SNN NN:65432  : [5f8] 42 FF 98
     * 
     * @return human readable form of outbound message queue.
     */
    public String getTranslatedOutbound() {
        return getFormatted(outbound,"Outbound ");
    }

    /**
     * Get a String translation of Inbound messages. e.g.
     * Inbound 2
     * Node Acknowledges Write WRACK NN:1234  : [5f8] 59 04 D2
     * Node Number of Events NUMEV NN:1234  Events: 2 : [5f8] 74 04 D2 02
     * 
     * @return human readable form of inbound message queue.
     */
    public String getTranslatedInbound() {
        return getFormatted(inbound,"Inbound ");
    }

    private String getFormatted(List<?> messages, String direction) {
        StringBuilder sb = new StringBuilder(direction);
        sb.append(messages.size());
        messages.forEach((singleMessage) -> {
            sb.append(System.lineSeparator());
            AbstractMessage msg = (AbstractMessage)singleMessage;
            if (CbusOpCodes.isKnownOpc(msg) ) {
                sb.append(Bundle.getMessage("CBUS_" + CbusOpCodes.decodeopc(msg)));
                sb.append(" ").append(CbusOpCodes.decodeopc(msg));
            }
            sb.append(" ").append(CbusOpCodes.decode(msg));
            sb.append(" : ").append(singleMessage.toString());
        });
        sb.append(System.lineSeparator());
        return sb.toString();
    }

}
