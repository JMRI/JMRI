package jmri.jmrix.roco.z21.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.DccLocoAddress;
import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21Reply;

/**
 * Formatter for RailCom replies.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RailComReplyFormatter implements Z21MessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof Z21Reply && ((Z21Reply) m).getOpCode() == 0x0088;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not a RailCom reply");
        }
        Z21Reply r = (Z21Reply) m;
        int entries = r.getNumRailComDataEntries();
        StringBuilder datastring = new StringBuilder();
        for(int i = 0; i < entries ; i++) {
            DccLocoAddress address = r.getRailComLocoAddress(i);
            int rcvCount = r.getRailComRcvCount(i);
            int errorCount = r.getRailComErrCount(i);
            int speed = r.getRailComSpeed(i);
            int options = r.getRailComOptions(i);
            int qos = r.getRailComQos(i);
            datastring.append(Bundle.getMessage("Z21_RAILCOM_DATA",address,rcvCount,errorCount,options,speed,qos));
            datastring.append("\n");
        }
        return Bundle.getMessage("Z21_RAILCOM_DATACHANGED",entries,new String(datastring));
    }

}
