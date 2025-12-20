package jmri.jmrix.jserialcomm;

import jmri.jmrix.SerialPortEvent;

/**
 * Implementation of serial port event using jSerialComm.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class JSerialPortEvent implements SerialPortEvent {

    private final com.fazecast.jSerialComm.SerialPortEvent event;

    JSerialPortEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
        this.event = event;
    }

    @Override
    public int getEventType() {
        return event.getEventType();
    }

}
