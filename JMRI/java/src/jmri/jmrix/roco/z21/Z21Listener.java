package jmri.jmrix.roco.z21;

/**
 * Interface for Z21 protocol Listeners
 *
 * @author	Paul Bender Copyright (C) 2014
 */
public interface Z21Listener extends jmri.jmrix.AbstractMRListener {

    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message from the layout.
     *
     * @param msg The received z21 reply. Note that this same object may be
     * presented to multiple users. It should not be modified here.
     */
    public void reply(Z21Reply msg);

    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message sent to the layout. Normally, this function will do
     * nothing.
     *
     * @param msg The received z21 message. Note that this same object may be
     * presented to multiple users. It should not be modified here.
     */
    public void message(Z21Message msg);

}
