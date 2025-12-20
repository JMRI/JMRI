package jmri.jmrix.openlcb;

import jmri.CommandStation;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.openlcb.*;

/**
 * OpenLcb implementation of part of the CommandStation interface.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2024
 */
public class OlcbCommandStation implements CommandStation {

    public OlcbCommandStation(CanSystemConnectionMemo memo) {
        this.memo = memo;
    }

    /**
     * OpenLCB/LCC does not have a complete, generic process for 
     * commanding that a generic DCC packet be sent to the rails.
     *<p>
     * For now, this method just logs an error when invokved
     *
     * @param packet  Byte array representing the packet, including the
     *                error-correction byte. Must not be null.
     * @param repeats Number of times to repeat the transmission, capped at 9
     * @return {@code true} if the operation succeeds, {@code false} otherwise.
     */
    @Override
    public boolean sendPacket(byte[] packet, int repeats) {
        jmri.util.LoggingUtil.warnOnce(log, "OpenLCB/LCC does not implement sending generic DCC packets to the rails");
        return false;
    }

    CanSystemConnectionMemo memo;

    @Override
    public String getUserName() {
        if (memo == null) {
            return "OpenLCB";
        }
        return memo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        if (memo == null) {
            return "M";
        }
        return memo.getSystemPrefix();
    }

    @Override
    public void sendAccSignalDecoderPkt(int address, int aspect, int count) {
        Connection connection = memo.get(OlcbInterface.class).getOutputConnection();
        NodeID srcNodeID = memo.get(OlcbInterface.class).getNodeId();
        
        int num = address-1+4;        
        var content = new byte[]{0x01, 0x01, 0x02, 0x00, 0x01, (byte)((num/256)&0xFF), (byte)(num&0xff), (byte) aspect};
        EventID eventID = new EventID(content);
        Message m = new ProducerConsumerEventReportMessage(srcNodeID, eventID);
        for (int i = 0; i<count; i++) {
            connection.put(m, null);
        }
    }

    @Override
    public void sendAltAccSignalDecoderPkt(int address, int aspect, int count) {
        // The alternate space is +4 from the regular space
        sendAccSignalDecoderPkt(address+4, aspect, count);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbCommandStation.class);

}
