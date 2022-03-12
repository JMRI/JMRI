package jmri.jmrit.withrottle;

/**
 * WiThrottle Interface to let a class know of device status changes.
 *
 * @author Brett Hoffman Copyright (C) 2009
 * @author Created by Brett Hoffman on:
 * @author 11/18/09.
 */
import java.util.EventListener;

public interface DeviceListener extends EventListener {

    /**
     * A new device has connected.
     *
     * @param device the device that has connected.
     */
    public void notifyDeviceConnected(DeviceServer device);

    /**
     * A device has quit and needs to be removed.
     *
     * @param device the device that has quit.
     */
    public void notifyDeviceDisconnected(DeviceServer device);

    /**
     * A device has changed its address.
     *
     * @param device the device with changed address.
     */
    public void notifyDeviceAddressChanged(DeviceServer device);

    /**
     * Some info (name, UDID) about the device has changed. Also used to detect
     * duplicate of same device.
     * @param device for which info has changed.
     *
     */
    public void notifyDeviceInfoChanged(DeviceServer device);

}
