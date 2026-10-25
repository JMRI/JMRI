package jmri.jmrix.marklin;

/**
 * Functional interface for sending raw MCAN frames over a transport layer.
 * <p>
 * Designed with zero JMRI dependencies for reusability across standalone
 * gateways e.g. McanTcpBridge, microservices or CLI tools.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
@FunctionalInterface
public interface McanFrameSender {

    /**
     * Send a raw 13-byte MCAN frame over the underlying transport.
     *
     * @param frame 13-element integer array representing the raw MCAN CAN frame
     */
    void sendFrame(int[] frame);
}
