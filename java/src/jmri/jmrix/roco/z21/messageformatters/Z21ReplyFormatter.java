package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Reply;

/**
 * @author Paul Bender Copyright (C) 2024
 */
public class Z21ReplyFormatter implements jmri.jmrix.roco.z21.Z21MessageFormatter {

    @Override
    public String formatMessage(jmri.jmrix.Message m) {
        return m.toMonitorString();
    }

    @Override
    public Boolean handlesMessage(jmri.jmrix.Message m) {
        return m instanceof Z21Reply;
    }
}
