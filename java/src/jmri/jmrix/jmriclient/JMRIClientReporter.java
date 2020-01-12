package jmri.jmrix.jmriclient;

import jmri.implementation.AbstractReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMRIClient implementation of the Reporter interface.
 * <p>
 *
 * Description: extend jmri.AbstractReporter for JMRIClient layouts
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientReporter extends AbstractReporter implements JMRIClientListener {

    // data members
    private int _number;   // reporter number
    private JMRIClientTrafficController tc = null;
    private String transmitName = null;

    /**
     * JMRIClient reporters use the reporter number on the remote host.
     */
    public JMRIClientReporter(int number, JMRIClientSystemConnectionMemo memo) {
        super(memo.getSystemPrefix() + "R" + number);
        transmitName = memo.getTransmitPrefix() + "R" + number;
        _number = number;
        tc = memo.getJMRIClientTrafficController();
        // At construction, register for messages
        tc.addJMRIClientListener(this);
        // Then request status.
        requestUpdateFromLayout();
    }

    public int getNumber() {
        return _number;
    }

    public void requestUpdateFromLayout() {
        // get the message text
        String text = "REPORTER " + transmitName + "\n";

        // create and send the message itself
        tc.sendJMRIClientMessage(new JMRIClientMessage(text), this);
    }

    // to listen for status changes from JMRIClient system
    @Override
    public void reply(JMRIClientReply m) {
        String message = m.toString();
        log.debug("Message Received: " + m);
        log.debug("length " + message.length());
        if (!message.contains(transmitName + " ") &&
            !message.contains(transmitName + "\n") &&
            !message.contains(transmitName + "\r") ) {
            return; // not for us
        } else {
            String text = "REPORTER " + transmitName + "\n";
            if (!message.equals(text)) {
                String report = message.substring(text.length());
                log.debug("setting report to " + report);
                setReport(report);  // this is an update of the report.
            } else {
                log.debug("setting report to null");
                setReport(null); // this is an update, but it is just 
                // telling us the transient current 
                // report is no longer valid.
            }
        }
    }

    @Override
    public void message(JMRIClientMessage m) {
    }

    private int state = UNKNOWN;

    @Override
    public void setState(int s) {
        state = s;
    }

    @Override
    public int getState() {
        return state;
    }

    private final static Logger log = LoggerFactory.getLogger(JMRIClientReporter.class);

}
