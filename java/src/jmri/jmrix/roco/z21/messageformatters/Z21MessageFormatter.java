package jmri.jmrix.roco.z21.messageformatters;

public class Z21MessageFormatter implements jmri.jmrix.roco.z21.Z21MessageFormatter {
    public Z21MessageFormatter() {
    }

    public Boolean handlesMessage(jmri.jmrix.Message m) {
        return m instanceof jmri.jmrix.roco.z21.Z21Message;
    }

    public String formatMessage(jmri.jmrix.Message m) {
        return m.toMonitorString();
    }
}
