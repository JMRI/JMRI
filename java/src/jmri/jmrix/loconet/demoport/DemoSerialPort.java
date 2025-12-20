package jmri.jmrix.loconet.demoport;

import java.io.*;

import jmri.SystemConnectionMemo;
import jmri.jmrix.*;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.ThreadingUtil;

/**
 * Demonstration of replacing the serial port with a fake port.
 * This class requires a working LocoNet serial port connection.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class DemoSerialPort extends AbstractSerialPortController {

    private final DemoPanel _panel;
    private BufferedOutputStream _outputStream;

    DemoSerialPort(DemoPanel panel, SystemConnectionMemo memo) {
        super(memo);
        this._panel = panel;
    }

    @Override
    public void configure() {
        // Do nothing
    }

    @Override
    public String openPort(String portName, String appName) {
        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting via {} {}", portName, currentSerialPort);

        setBaudRate(currentSerialPort, 57600);
        configureLeads(currentSerialPort, true, true);
        setFlowControl(currentSerialPort, FlowControl.RTSCTS);

        setComPortTimeouts(currentSerialPort, Blocking.READ_SEMI_BLOCKING, 100);

        // report status
        reportPortStatus(log, portName);

        opened = true;

        return null; // indicates OK return
    }

    public void startDemo() {
        log.error("startDemo()");
        AbstractSerialPortController pc = _panel.getPortController();
        if (pc == null) {
            log.error("startDemo(). PortController is null");
            return;
        }
        String portName = pc.getCurrentPortName();
        log.error("Serial port: {}", pc.getPortSettingsString());
        pc.replacePortWithFakePort();
        String result = openPort(portName, "JMRI app");
        if (result == null) {
            _panel.addMessage("Connection successful\n");
            ThreadingUtil.newThread(new LocoNetListener(getInputStream()),
                    "Demo serial port")
                    .start();
            _outputStream = new BufferedOutputStream(getOutputStream());
        } else {
            _panel.addMessage(result);
        }
    }

    public void throwTurnout(int turnout, boolean throwTurnout) {
        try {
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_SW_REQ);
            msg.setElement(1, turnout-1);
            msg.setElement(2, throwTurnout ? 0x10 : 0x30);
            msg.setParity();
            for (int i=0; i < msg.getNumDataElements(); i++) {
                _outputStream.write(msg.getElement(i));
            }
            _outputStream.flush();
        } catch (IOException ex) {
            log.error("Exception: {}", ex.getMessage());
        }
    }


    private final class LocoNetListener implements Runnable {

        private final InputStream _stream;
        private final int[] data = new int[256];
        private int pos = 0;

        private LocoNetListener(InputStream stream) {
            this._stream = stream;
        }

        private int numBytesInMessage() {
            switch (data[0] & 0xE0) {
                case 0x80: return 2;
                case 0xA0: return 4;
                case 0xC0: return 6;
                case 0xE0:
                    if (pos >= 2) return data[1];
                    else return 255; // We have only first byte so we don't know length yet.
                default:
                    throw new IllegalArgumentException("Unknown length of package");
            }
        }

        @Override
        public void run() {
            while (! Thread.interrupted() ) {   // loop until asked to stop
                try {
                    int b = _stream.read();
                    _panel.addMessage(String.format("%02X ", b));
                    if (b < 128 && pos == 0) {
                        // We are in the middle of a message and have missed
                        // the beginning of the message. Ignore it.
                        continue;
                    }
                    data[pos++] = b;
                    if (pos >= numBytesInMessage()) {
                        LocoNetMessage msg = new LocoNetMessage(data);
                        _panel.addMessage(msg.toMonitorString());
                        pos = 0;
                    }
                } catch (InterruptedIOException ex) {
                    // Do nothing, just ignore the error
                } catch (IOException ex) {
                    log.error("Exception: {}", ex.getMessage());
                    return;
                }
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DemoSerialPort.class);
}
