package jmri.jmrix.easydcc;

/**
 * Layout interface, similar to command station
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface EasyDccInterface {

    void addEasyDccListener(EasyDccListener l);

    void removeEasyDccListener(EasyDccListener l);

    boolean status(); // true if the implementation is operational

    void sendEasyDccMessage(EasyDccMessage m, EasyDccListener l); // 2nd arg gets the reply

}
