package jmri.jmrix.anyma;

import static java.lang.System.arraycopy;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.usb.UsbConst;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traffic controller for Anyma DMX
 * <P>
 * @author George Warner Copyright (c) 2017
 * @since 4.9.6
 */
public class AnymaDMX_TrafficController {

    private byte[] old_data = new byte[512];
    private byte[] new_data = new byte[512];
    private ScheduledExecutorService execService = null;
    private AnymaDMX_UsbPortAdapter controller = null;

    public AnymaDMX_TrafficController() {
        execService = Executors.newScheduledThreadPool(5);
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

    /**
     * Make connection to existing PortController (adapter) object.
     */
    public void connectPort(AnymaDMX_UsbPortAdapter p) {
        if (controller != null) {
            log.warn("connectPort called when already connected");
        } else {
            log.debug("connectPort invoked");
        }
        controller = p;
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
    protected boolean setChannelRangeValues(int from, int to, byte buf[]) {
        from = MathUtil.pin(from, 0, 511);
        to = MathUtil.pin(to, from, 511);
        int len = to - from + 1;
        byte requestType = UsbConst.REQUESTTYPE_TYPE_VENDOR
                | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                | UsbConst.ENDPOINT_DIRECTION_OUT;
        byte request = 0x02;    // anyma dmx cmd_SetChannelRange
        boolean f = controller.sendControlTransfer(requestType, request, len, from, buf);
        //if (f) {
        //    log.error("sendControlTransfer error");
        //}
        return f;
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_TrafficController.class);
}
