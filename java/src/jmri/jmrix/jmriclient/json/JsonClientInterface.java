package jmri.jmrix.jmriclient.json;

/**
 * Layout interface, similar to a command station.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author rhwood
 */
public interface JsonClientInterface {

    public void addJsonClientListener(JsonClientListener l);

    public void removeJsonClientListener(JsonClientListener l);

    boolean status();   // true if the implementation is operational

    void sendJsonClientMessage(JsonClientMessage message, JsonClientListener l);  // 2nd arg gets the reply
}
