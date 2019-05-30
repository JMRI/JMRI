package jmri.jmrix.acela;

/**
 * Interface to send/receive Acela information.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public interface AcelaInterface {

    public void addAcelaListener(AcelaListener l);

    public void removeAcelaListener(AcelaListener l);

    boolean status();   // true if the implementation is operational

    void sendAcelaMessage(AcelaMessage m, AcelaListener l);  // 2nd arg gets the reply

}
