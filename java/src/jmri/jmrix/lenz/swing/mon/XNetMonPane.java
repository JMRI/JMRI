package jmri.jmrix.lenz.swing.mon;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.*;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Panel displaying (and logging) XpressNet messages derived from XNetMonFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2004-2014
 * @author Giorgio Terdina Copyright (C) 2007
 */
public class XNetMonPane extends jmri.jmrix.AbstractMonPane implements XNetListener {

    protected XNetTrafficController tc = null;
    protected XNetSystemConnectionMemo memo = null;

    private List<XPressNetMessageFormatter> formatterList;


    @Override
    public String getTitle() {
        return (Bundle.getMessage("MenuItemXNetCommandMonitor"));
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof XNetSystemConnectionMemo) {
            memo = (XNetSystemConnectionMemo) context;
            tc = memo.getXNetTrafficController();
            // connect to the TrafficController
            tc.addXNetListener(~0, this);
            try {
                formatterList = new ArrayList<>();

                Reflections reflections = new Reflections("jmri.jmrix.lenz.messageformatters");
                Set<Class<? extends XPressNetMessageFormatter>> f = reflections.getSubTypesOf(XPressNetMessageFormatter.class);
                for(Class<?> c : f){
                    log.debug("Found formatter: {}", f.getClass().getName());
                    Constructor<?> ctor = c.getConstructor();
                    formatterList.add((XPressNetMessageFormatter) ctor.newInstance());
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException |
                     IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                log.error("Error instantiating formatter", e);
            }
        }
    }

    /**
     * Initialize the data source.
     */
    @Override
    protected void init() {
    }

    @Override
    public void dispose() {
        // disconnect from the LnTrafficController
        tc.removeXNetListener(~0, this);
        // and unwind swing
        super.dispose();
    }

    @Override
    public void logMessage(String messagePrefix, String rawPrefix, Message message){
        // display the raw data if requested
        StringBuilder raw = new StringBuilder(rawPrefix);
        if (rawCheckBox.isSelected()) {
            raw.append(message.toString());
        }

        // display the decoded data
        String text = formatterList.stream()
                .filter(f -> f.handlesMessage(message))
                .findFirst().map(f -> f.formatMessage(message))
                .orElse(message.toString());
        nextLine(messagePrefix + " " + text + "\n", raw.toString());
    }

    @Override
    public synchronized void message(XNetReply l) { // receive an XpressNet message and log it
        logMessage("","packet:",l);
    }

    /**
     * Listen for the messages to the LI100/LI101
     */
    @Override
    public synchronized void message(XNetMessage l) {
        logMessage("", "packet:", l);
    }

    /**
     * Handle a timeout notification
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        log.debug("Notified of timeout on message {}", msg.toString());
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemXNetCommandMonitor"), XNetMonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(XNetSystemConnectionMemo.class));
        }
    }

    private static final Logger log = LoggerFactory.getLogger(XNetMonPane.class);
}
