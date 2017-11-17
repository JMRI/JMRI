package jmri.jmrix.anyma;

import static java.lang.System.arraycopy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import jmri.jmrix.UZBPortAdapter;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Adapter to allow the system connection memo and multiple
 * AnymaDMX_ managers to be handled.
 * <P>
 * @author George Warner Copyright (c) 2017
 * @since 4.9.6
 */
public class AnymaDMX_UsbPortAdapter extends UZBPortAdapter {

    //private AnymaDMX_Controller dmx = null;

    private byte[] old_data = new byte[512];
    private byte[] new_data = new byte[512];

    public AnymaDMX_UsbPortAdapter() {
        super(new AnymaDMX_SystemConnectionMemo());
        log.debug("*    Constructor");

        getSystemConnectionMemo().setAdapter(this);

        setVendorID((short) 0x16C0);
        setProductID((short) 0x05DC);

        ScheduledExecutorService execService
                = Executors.newScheduledThreadPool(5);

        execService.scheduleAtFixedRate(() -> {
            // if the new_data has changed...
            if (!Arrays.equals(old_data, new_data)) {
                // find indexes to first/last bytes that are different
                int from = old_data.length;
                int to = 0;
                for (int i = 0; i < old_data.length; i++) {
                    if (old_data[i] != new_data[i]) {
                        from = Math.min(from, i);
                        to = i;
                    }
                }
                if (from <= to) {
                    int len = to - from + 1;
                    byte[] buf = new byte[len];
                    System.arraycopy(new_data, from, buf, 0,
                            Math.min(new_data.length, len));
                    if (setChannelRangeValues(from, to, buf)) {
                        arraycopy(new_data, from, old_data, from, len);
                    }
                }
            }
        }, 0, 100L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        log.debug("*    dispose() called.");
        //dmx.shutdown(); // terminate all DMX connections.
        super.dispose();
    }

    @Override
    public void configure() {
        log.debug("*    configure() called.");
        getSystemConnectionMemo().configureManagers();
    }

    @Override
    public DataInputStream getInputStream() {
        log.debug("*    getInputStream() called.");
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        log.debug("*    getOutputStream() called.");
        return null;
    }

    /**
     * set a channel's value
     *
     * @param channel the channel (0 - 511 inclusive)
     * @param value   the value
     */
    public void setChannelValue(int channel, byte value) {
//        byte buf[] = {value};
//        setChannelRangeValues(channel, channel, buf);

        if ((0 <= channel) && (channel <= 511)) {
            new_data[channel] = value;
        }

        //channel = MathUtil.pin(channel, 1, 512) - 1;
        //byte requestType = UsbConst.REQUESTTYPE_TYPE_VENDOR
        //        | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
        //        | UsbConst.ENDPOINT_DIRECTION_OUT;
        //byte request = 0x01;    // anyma dmx cmd_SetSingleChannel
        //int e = sendControlTransfer(requestType, request, value, channel, null);
        //if (e < 0) {
        //    log.error("sendControlTransfer error: " + e);
        //}
    }

    /**
     * set the values for a range of channels
     *
     * @param from the beginning index (inclusive)
     * @param to   the ending index (inclusive)
     * @param buf  the data to send
     * @note the from/to indexes are 0-511 (inclusive)
     */
    private boolean setChannelRangeValues(int from, int to, byte buf[]) {
        from = MathUtil.pin(from, 0, 511);
        to = MathUtil.pin(to, from, 511);
        int len = to - from + 1;
        byte requestType = UsbConst.REQUESTTYPE_TYPE_VENDOR
                | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                | UsbConst.ENDPOINT_DIRECTION_OUT;
        byte request = 0x02;    // anyma dmx cmd_SetChannelRange
        boolean f = sendControlTransfer(requestType, request, len, from, buf);
        //if (f) {
        //    log.error("sendControlTransfer error");
        //}
        return f;
    }

    public boolean sendControlTransfer(int requestType, int request, int value, int index, byte[] data) {
        boolean result = false;    // assume failure (pessimist!)
        if (usbDevice != null) {
            try {
                UsbControlIrp usbControlIrp = usbDevice.createUsbControlIrp(
                        (byte) requestType, (byte) request,
                        (short) value, (short) index);
                if (data == null) {
                    data = new byte[0];
                }
                usbControlIrp.setData(data);
                usbControlIrp.setLength(data.length);

                //log.debug("sendControlTransfer,  requestType: {}, request: {}, value: {}, index: {}, data: {}", requestType, request, value, index, getByteString(data));
                usbDevice.syncSubmit(usbControlIrp);
                result = true; // it's good!
            } catch (IllegalArgumentException | UsbException | UsbDisconnectedException e) {
                //log.error("Exception " + e);
                //e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public AnymaDMX_SystemConnectionMemo getSystemConnectionMemo() {
        log.debug("*    getSystemConnectionMemo() called.");
        return (AnymaDMX_SystemConnectionMemo) super.getSystemConnectionMemo();
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_UsbPortAdapter.class);
}
