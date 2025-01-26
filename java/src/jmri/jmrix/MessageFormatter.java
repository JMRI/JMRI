package jmri.jmrix;

/**
 * Interface for classes that translate @Ref{Message} objects into strings
 *
 * @author Paul Bender Copyright (C) 2024
 */
public interface MessageFormatter {

    /**
     * Determine if this formatter can handle the message
     * @param m message to check
     * @return true if this formatter can handle the message
     */
    public boolean handlesMessage(Message m);

    /**
     * Format the message into a string
     * @param m message to format
     * @return formatted string
     */
    public String formatMessage(Message m);

}
