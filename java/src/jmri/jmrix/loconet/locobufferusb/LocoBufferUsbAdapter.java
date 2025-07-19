package jmri.jmrix.loconet.locobufferusb;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Override {@link jmri.jmrix.loconet.locobuffer.LocoBufferAdapter} so that it refers to the
 * (switch) settings on the LocoBuffer-USB.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2005
 */
public class LocoBufferUsbAdapter extends LocoBufferAdapter {

    public LocoBufferUsbAdapter() {
        super();
        options.remove(option1Name);
    }

    @Override
    protected void reportOpen(String portName) {
        log.info("Connecting LocoBuffer-USB via {} {}", portName, currentSerialPort);
    }

    /**
     * Always on flow control
     */
    @Override
    protected void setLocalFlowControl() {
        FlowControl flow = FlowControl.RTSCTS;
        setFlowControl(currentSerialPort, flow);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud57600")};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{57600};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static Logger log = LoggerFactory.getLogger(LocoBufferUsbAdapter.class);

}
