package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a single response from the XpressNet, with extensions
 * from Roco for the Z21.
 *
 * @author Paul Bender Copyright (C) 2018
 *
 */
public class Z21XNetReply extends XNetReply {

    // Create a new reply.
    public Z21XNetReply() {
        super();
    }

    // Create a new reply from an existing reply
    public Z21XNetReply(Z21XNetReply reply) {
        super(reply);
    }

    /**
     * Create a reply from an XNetMessage.
     * @param message message to create reply from.
     */
    public Z21XNetReply(Z21XNetMessage message) {
        super(message);
    }

    /**
     * Create a reply from a string of hex characters.
     * @param message hex character string.
     */
    public Z21XNetReply(String message) {
        super(message);
    }

    /**
     * Is this message a service mode response?
     */
    @Override
    public boolean isServiceModeResponse() {
        return ((getElement(0) == Z21Constants.LAN_X_CV_RESULT_XHEADER && 
                (getElement(1) == Z21Constants.LAN_X_CV_RESULT_DB0)) ||
                super.isServiceModeResponse());
    }

    @Override
    public boolean isFeedbackMessage() {
        return (this.getElement(0) == Z21Constants.LAN_X_TURNOUT_INFO ||
                 super.isFeedbackMessage());
    }


    private static final List<XPressNetMessageFormatter> formatterList = new ArrayList<>();
    /**
     * @return a string representation of the reply suitable for display in the
     * XpressNet monitor.
     */
    @Override
    public String toMonitorString(){
        if(formatterList.isEmpty()) {
            try {
                Reflections reflections = new Reflections("jmri.jmrix");
                Set<Class<? extends XPressNetMessageFormatter>> f = reflections.getSubTypesOf(XPressNetMessageFormatter.class);
                for (Class<?> c : f) {
                    log.debug("Found formatter: {}", f.getClass().getName());
                    Constructor<?> ctor = c.getConstructor();
                    formatterList.add((XPressNetMessageFormatter) ctor.newInstance());
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException |
                     IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                log.error("Error instantiating formatter", e);
            }
        }

        return formatterList.stream()
                .filter(f -> f.handlesMessage(this))
                .findFirst().map(f -> f.formatMessage(this))
                .orElse(this.toString());
    }

    private static final Logger log = LoggerFactory.getLogger(Z21XNetReply.class);
}
