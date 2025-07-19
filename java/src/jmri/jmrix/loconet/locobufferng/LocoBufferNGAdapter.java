package jmri.jmrix.loconet.locobufferng;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Override {@link jmri.jmrix.loconet.locobuffer.LocoBufferAdapter} so that it refers to the
 * (switch) settings on the LocoBuffer-NG.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2005, 2021
 */
public class LocoBufferNGAdapter extends LocoBufferAdapter {

    public LocoBufferNGAdapter() {
        super();
        options.remove(option1Name);
    }

    @Override
    protected void reportOpen(String portName) {
        log.info("Connecting LocoBuffer-NG via {} {}", portName, currentSerialPort);
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

    private final static Logger log = LoggerFactory.getLogger(LocoBufferNGAdapter.class);

}
