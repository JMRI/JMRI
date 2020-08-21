package jmri.jmrix;

/**
 * Basic interface for messages to and from the layout hardware.
 *
 * @author jake Copyright 2008
 */
public interface Message {

    /**
     * Get a particular element in a Message.
     * @param n Element Index.
     * @return single element of message.
     */
    int getElement(int n);

    /**
     * Get the number of data elements in a Message.
     * @return number elements.
     */
    int getNumDataElements();

    /**
     * Set a single Data Element at a particular index.
     * @param n index of element.
     * @param v value of element.
     */
    void setElement(int n, int v);

    /**
     * {@inheritDoc}
     */
    @Override
    String toString();


    /*
     * @return a human readable representation of the message.
     */
    public default String toMonitorString(){
          return toString();
    }

}
