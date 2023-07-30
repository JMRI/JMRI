package jmri.jmrix.marklin;

/**
 * Define the interface for listening to traffic on the Marklin communications
 * link.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface MarklinListener extends jmri.jmrix.AbstractMRListener {

    void message(MarklinMessage m);

    void reply(MarklinReply m);
}
