package jmri.jmrix;

/**
 * Serial port event
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class SerialPortEvent {

    private final com.fazecast.jSerialComm.SerialPortEvent event;

    SerialPortEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
        this.event = event;
    }

    public int getEventType() {
        return event.getEventType();
    }

}
