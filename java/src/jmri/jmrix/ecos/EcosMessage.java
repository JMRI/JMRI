package jmri.jmrix.ecos;


/**
 * Encodes a message to an Ecos command station.
 * <p>
 * The {@link EcosReply} class handles the response from the command station.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Daniel Boudreau Copyright (C) 2007
 */
public class EcosMessage extends jmri.jmrix.AbstractMRMessage {

    public EcosMessage() {
        super();
    }

    // create a new one
    public EcosMessage(int i) {
        super(i);
    }

    // copy one
    public EcosMessage(EcosMessage m) {
        super(m);
    }

    // from String
    public EcosMessage(String m) {
        super(m);
    }

    static public EcosMessage getProgMode() {
        EcosMessage m = new EcosMessage();
        return m;
    }

    static public EcosMessage getExitProgMode() {
        EcosMessage m = new EcosMessage();
        return m;
    }

}
