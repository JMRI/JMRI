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
 * Traffic controller for Anyma DMX.
 *
 * @author George Warner Copyright (c) 2017-2018
 * @since 4.9.6
 */
public class AnymaDMX_TrafficController {

    private byte[] old_data = new byte[512];
    private byte[] new_data = new byte[512];
    private ScheduledExecutorService execService = null;
    private AnymaDMX_UsbPortAdapter controller = null;

    /**
     * Create a new AnymaTrafficController instance.
     */
    public AnymaDMX_TrafficController() {
       // this forces first pass to transmit everything
        Arrays.fill(old_data, (byte) -1);

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
                    System.arraycopy(new_data, from, buf, 0, len);
                    if (sendChannelRangeValues(from, to, buf)) {
                        arraycopy(new_data, from, old_data, from, len);
                    }
                }
            }
        }, 0, 100L, TimeUnit.MILLISECONDS); // 10 times per second
    }

    /**
     * Make connection to existing PortController (adapter) object.
     *
     * @param p the AnymaDMX_UsbPortAdapter we're connecting to
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
     * @param channel the channel (1 - 512 inclusive)
     * @param value   the value
     */
    public void setChannelValue(int channel, byte value) {
        if ((1 <= channel) && (channel <= 512)) {
            new_data[channel - 1] = value;
        }
    }

    /**
     * set the values for a range of channels
     *
     * @param from the beginning index (inclusive)
     * @param to   the ending index (inclusive)
     * @param buf  the data to send
     * note: the from/to indexes are 1-512 (inclusive)
     */
    public void setChannelRangeValues(int from, int to, byte buf[]) {
        if ((1 <= from) && (from <= 512) && (1 <= to) && (to <= 512)) {
            int len = to - from + 1;
            if (len == buf.length) {
                arraycopy(new_data, from - 1, buf, 0, len);
            } else {
                log.error("range does not match buffer size");
            }
        } else {
            log.error("channel(s) out of range (1-512): {from: {}, to: {}}.",
                    from, to);
        }
    }

    /**
     * send the values for a range of channels (to the controller)
     *
     * @param from the beginning index (inclusive)
     * @param to   the ending index (inclusive)
     * @param buf  the data to send
     * @return true if successful
     * note: the from/to indexes are 0-511 (inclusive)
     */
    private boolean sendChannelRangeValues(int from, int to, byte buf[]) {
        boolean result = false; // assume failure (pessimist!)
        if (controller != null) {
            from = MathUtil.pin(from, 0, 511);
            to = MathUtil.pin(to, from, 511);
            int len = to - from + 1;
            byte requestType = UsbConst.REQUESTTYPE_TYPE_VENDOR
                    | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                    | UsbConst.ENDPOINT_DIRECTION_OUT;
            byte request = 0x02;    // anyma dmx cmd_SetChannelRange
            result = controller.sendControlTransfer(
                    requestType, request, len, from, buf);
        }
        return result;
    }

    /**
     * Clean up threads and local storage.
     */
    public void dispose(){
       // modified from the javadoc for ExecutorService 
       execService.shutdown(); // Disable new tasks from being submitted
       try {
          // Wait a while for existing tasks to terminate
          if (!execService.awaitTermination(60, TimeUnit.SECONDS)) {
             execService.shutdownNow(); // Cancel currently executing tasks
             // Wait a while for tasks to respond to being cancelled
             if (!execService.awaitTermination(60, TimeUnit.SECONDS))
                 log.error("Pool did not terminate");
          }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            execService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_TrafficController.class);

}
