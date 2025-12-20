package jmri.jmrix;

/**
 * Serial port listener
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface SerialPortDataListener {

    void serialEvent(SerialPortEvent serialPortEvent);

    public int getListeningEvents();

}
