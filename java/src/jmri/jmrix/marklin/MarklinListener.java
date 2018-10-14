package jmri.jmrix.marklin;

/**
 * Define the interface for listening to traffic on the Marklin communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface MarklinListener extends jmri.jmrix.AbstractMRListener {

    public void message(MarklinMessage m);

    public void reply(MarklinReply m);
}
