package jmri.jmrix;

/**
 * Basic interface for messages to and from the layout hardware.
 *
 * @author jake Copyright 2008
 */
public interface Message {

    int getElement(int n);

    int getNumDataElements();

    void setElement(int n, int v);

    @Override
    String toString();


    /*
     * @return a human readable representation of the message.
     */
    public default String toMonitorString(){
          return toString();
    }

}
