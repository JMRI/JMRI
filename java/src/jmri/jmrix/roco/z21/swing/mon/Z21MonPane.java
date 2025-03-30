package jmri.jmrix.roco.z21.swing.mon;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.*;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Panel displaying (and logging) Z21 messages derived from Z21MonFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2004-2024
 * @author Giorgio Terdina Copyright (C) 2007
 */
public class Z21MonPane extends jmri.jmrix.AbstractMonPane implements Z21Listener {

    protected Z21SystemConnectionMemo memo = null;

    private List<Z21MessageFormatter> formatterList;

    @Override
    public String getTitle() {
        return (Bundle.getMessage("Z21TrafficTitle"));
    }

    @Override
    public void initContext(Object context) {
        if (context instanceof Z21SystemConnectionMemo) {
            memo = (Z21SystemConnectionMemo) context;
            // connect to the TrafficController
            memo.getTrafficController().addz21Listener(this);
            try {
                formatterList = new ArrayList<>();

                Reflections reflections = new Reflections("jmri.jmrix.roco.z21");
                Set<Class<? extends Z21MessageFormatter>> f = reflections.getSubTypesOf(Z21MessageFormatter.class);
                for(Class<?> c : f){
                    log.debug("Found formatter: {}", f.getClass().getName());
                    Constructor<?> ctor = c.getConstructor();
                    formatterList.add((Z21MessageFormatter) ctor.newInstance());
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
        // disconnect from the TrafficController
        memo.getTrafficController().removez21Listener(this);
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
    public synchronized void reply(Z21Reply l) { // receive an XpressNet message and log it
        logMessage(l);
    }

    /**
     * Listen for the messages to the LI100/LI101
     */
    @Override
    public synchronized void message(Z21Message l) {
        logMessage(l);
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("Z21TrafficTitle"),
                    Z21MonPane.class.getName());
            setContext(jmri.InstanceManager.
                    getDefault(Z21SystemConnectionMemo.class));
        }
    }
    private static final Logger log = LoggerFactory.getLogger(Z21MonPane.class);

}
