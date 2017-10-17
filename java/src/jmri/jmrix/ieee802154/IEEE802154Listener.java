package jmri.jmrix.ieee802154;

/**
 * Listener interface to be notified about serial traffic
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
 */
abstract public interface IEEE802154Listener extends jmri.jmrix.AbstractMRListener {

    public void message(IEEE802154Message m);

    public void reply(IEEE802154Reply m);

}
