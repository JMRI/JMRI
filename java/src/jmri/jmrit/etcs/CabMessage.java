package jmri.jmrit.etcs;

import java.util.Date;
import java.util.Random;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;

import org.apiguardian.api.API;

/**
 * Class to represent a CabMessage to send to the ERTMS DMI.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class CabMessage {

    private String message;
    private final Date sentDate;
    private final int intValue;
    private boolean ackRequired;
    private Date confirmedDate = null;
    private final String messageId;

    private static Random random = new Random();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Create a new CabMessage.
     * @param msg String of message value
     * @param intValue 1 if system status / important, 2 for auxiliary messages.
     * @param ackReqd true if an acknowledgement is required, else false.
     */
    public CabMessage(String msg, int intValue, boolean ackReqd) {
        this(generatedId(), msg, intValue, ackReqd );
    }

    /**
     * Create a new CabMessage.
     * @param messageId unique ID String.
     * @param msg String of message value
     * @param intValue 1 if system status / important, 2 for auxiliary messages.
     * @param ackReqd true if an acknowledgement is required, else false.
     */
    public CabMessage(@Nonnull String messageId, String msg, int intValue, boolean ackReqd) {
        this.messageId = messageId;
        this.message = msg;
        this.sentDate = InstanceManager.getDefault(jmri.Timebase.class).getTime();
        this.intValue = intValue;
        ackRequired = ackReqd;
    
    }

    /**
     * Get the Message String.
     * @return String of the Message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the unique Message ID.
     * @return the Message ID.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Get the Date that the Message was Sent.
     * @return the date of message creation.
     */
    public Date getSentTime() {
        return new Date(sentDate.getTime());
    }

    /**
     * Get the Time the message was confirmed by the driver.
     * @return time confirmed, else null if unconfirmed.
     */
    @CheckForNull
    public Date getConfirmedTime() {
        return new Date(confirmedDate.getTime());
    }

    /**
     * Set the CabMessage as confirmed.
     * Sets the confirmed time.
     */
    public void setConfirmed() {
        confirmedDate = InstanceManager.getDefault(jmri.Timebase.class).getTime();
        ackRequired = false;
    }

    /**
     * Get if a driver acknowledgement is required for this message.
     * @return true if acknowledgement required, else false.
     */
    public boolean getAckRequired() {
        return ackRequired;
    }

    /**
     * Get Message Group.
     * 1 - system status messages and the important plain/fixed text messages
     *         received from track-side.
     * 2 - auxiliary plain/fixed text messages received from track-side.
     * @return the group this message is in.
     */
    public int getGroup() {
        return intValue;
    }

    private static String generatedId(){
        int length = 20;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }

}
