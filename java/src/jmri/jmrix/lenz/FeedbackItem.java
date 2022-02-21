package jmri.jmrix.lenz;

import javax.annotation.CheckForNull;
import jmri.Turnout;

/**
 * Represents a single response from the XpressNet.
 *
 * @author svatopluk.dedic@gmail.com Copyright (C) 2020
 *
 */
public class FeedbackItem {
    private final int number;
    private final int data;
    private final XNetReply reply;

    protected FeedbackItem(XNetReply reply, int number, int data) {
        this.number = number;
        this.data = data;
        this.reply = reply;
    }

    /**
     * Determines if the feedback was solicited.
     * @return {@code true}, if feedback was solicited.
     */
    public boolean isUnsolicited() {
        return reply.isUnsolicited();
    }

    /**
     * Returns the (base) address of the item.
     * For turnouts, return the reported address. For encoders,
     * return the address of the first contained sensor
     * @return the address.
     */
    public int getAddress() {
        return number;
    }

    /**
     * Determines if the feedback is for the given Turnout address
     * @param address address to check
     * @return {@code true}, if the item applies to the address.
     */
    public boolean matchesAddress(int address) {
        if (isAccessory()) {
            return number == address;
        } else {
            return ((address - 1) & ~0x03) + 1 == number;
        }
    }

    /**
     * Determines if the turnout motion has completed. Requires decoder/switch
     * feedback to be processed by the command station; always {@code false} if not connected.
     * @return {@code true} if the motion is complete.
     */
    public boolean isMotionComplete() {
        return (data & 0x80) == 0;
    }

    /**
     * Returns the feedback type.
     * <ul>
     * <li> 0: Turnout without feedback
     * <li> 1: Turnout with feedback
     * <li> 2: Feedback encoder
     * <li> 3: reserved, invalid
     * </ul>
     * @return feedback type.
     */
    public int getType() {
        return (data & 0b0110_0000) >> 5;
    }

    /**
     * Translates raw value in {@link #getAccessoryStatus} into Turnout's CLOSED/THROWN
     * values
     * @return {@link Turnout#CLOSED}, {@link Turnout#THROWN} or -1 for inconsistent.s
     */
    public int getTurnoutStatus() {
        int t = getType();
        if (t > 1) {
            return -1;
        }
        switch (getAccessoryStatus()) {
            case 0x01: return Turnout.CLOSED;
            case 0x02: return Turnout.THROWN;
            default: // fall through
        }
        return -1;
    }

    /**
     * Returns true, if the feedback is from feedback encoder.
     * @return {@code true} for encoder feedback.
     */
    public boolean isEncoder() {
        return getType() == 2;
    }

    /**
     * Returns true, if the feedback is from turnout (accessory).
     * @return {@code true} for turnout feedback.
     */
    public boolean isAccessory() {
        return getType() < 2;
    }

    /**
     * Gives status value as specified in XPressNet.
     * <ul>
     * <li> 0x00: turnout was not operated
     * <li> 0x01: last command was "0", turnout left, CLOSED.
     * <li> 0x02: last command was "1", turnout right, THROWN.
     * <li> 0x03: reserved, invalid
     * </ul>
     * The method returns 0x03, if the feedback is not for accessory.
     * @return accessory state.
     */
    public int getAccessoryStatus() {
        if (!isAccessory()) {
            return 0x03;    // invalid
        }
        return (number & 0x01) != 0 ? (data & 0b0011) : (data & 0b1100) >> 2;
    }

    /**
     * Returns encoder feedback for the given sensor. The function return {@code null}
     * if the sensor number is not within this FeedbackItem range, or the item does
     * not represent an encoder feedback.
     * @param sensorNumber sensor number, starting with 1.
     * @return The sensor's reported bit value (true/false) or {@code null}, if
     * no encoder feedback for the sensor is found.
     */
    @CheckForNull
    public Boolean getEncoderStatus(int sensorNumber) {
        if (!matchesAddress(number) || isAccessory()) {
            return null;
        } else {
            return (data & (1 << ((sensorNumber -1) % 4))) > 0;
        }
    }

    /**
     * Returns a FeedbackItem instance for the other accessory address reported in the
     * item. Returns {@code null} for non-accessory feedbacks.
     * @return instance for the paired accessory, or {@code null}.
     */
    public FeedbackItem pairedAccessoryItem() {
        if (!isAccessory()) {
            return null;
        }
        int a = (number & 0x01) != 0 ? number + 1 : number - 1;
        return new FeedbackItem(reply, a, data);
    }
}
