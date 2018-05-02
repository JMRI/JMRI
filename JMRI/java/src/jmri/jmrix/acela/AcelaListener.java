package jmri.jmrix.acela;

/**
 * Listener interface to be notified about Acela traffic
 *
 * @author Bob Jacobsen Copyright (C) 2001
  *
 * @author Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public interface AcelaListener extends jmri.jmrix.AbstractMRListener {

    public void message(AcelaMessage m);

    public void reply(AcelaReply m);
}
