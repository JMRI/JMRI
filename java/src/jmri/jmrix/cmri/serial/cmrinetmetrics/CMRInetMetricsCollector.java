package jmri.jmrix.cmri.serial.cmrinetmetrics;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialListener;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;

/**
 * Listener class for collecting CMRInet network traffic and error messages.
 *
 * @author Chuck Catania  Copyright (C) 2016, 2017, 2018
 */

public class CMRInetMetricsCollector implements SerialListener {
    
    /**
     * Collected data instance.
     */
    private CMRInetMetricsData _data = new CMRInetMetricsData();
    
    //public CMRInetMetricsCollector(CMRISystemConnectionMemo memo) { // memo unused?
    //    super();
    //}
    
    public CMRInetMetricsCollector() {

        _data.clearAllErrMetrics();
        _data.clearAllDataMetrics();
        
    }

    protected void init() {
        log.info("CMRInetMetricsCollector - init");
    }
    
    /**
     * Expose collected data.
     * @return collected data
     */
    public  CMRInetMetricsData getMetricData() { return _data; }
    

    /**
     * Transmit packets.
     * Monitor any transmit packets to collect metrics.
     */
    @Override
    public synchronized void message(SerialMessage l) 
    { 
       int aPacketTypeID = 0;

       if (l.getNumDataElements() < 2)
       {
           _data.incMetricErrValue(CMRInetMetricsData.CMRInetMetricTruncRecv);
           return;
        } 
       
       aPacketTypeID = l.getElement(1);
       switch(aPacketTypeID)
       {                     // Packet Type
           case 0x41:        // (A) Datagram Ack
           case 0x43:        // (C) Code Line
           case 0x44:        // (D) Datagram Read
           case 0x45:        // (E) EOT
           break;
               
           case 0x49:        // (I) Initialize 
            _data.incMetricDataValue(CMRInetMetricsData.CMRInetMetricInitMsgs);
           break;
               
           case 0x4D:        // (M) NMRA Mast
           break;
               
           case 0x50:        // (P) Poll
            _data.startPollIntervalTimer();
           break;

           case 0x51:        // (Q) Query
           case 0x52:        // (R) Read
           case 0x54:        // (T) Transmit
           case 0x57:        // (W) Datagram Write
           break;
               
           default: 
            _data.incMetricErrValue(CMRInetMetricsData.CMRInetMetricUnrecCommand);
       }
    }
    
    /**
     * Receive packets.
     * Monitor any read (reply) packets to collect metrics.
     */
    @Override
    public synchronized void reply(SerialReply l) 
    { 
       int aPacketTypeID = 0;

       if (l.getNumDataElements() < 2)
       {
           _data.incMetricErrValue(CMRInetMetricsData.CMRInetMetricTruncReply);
           return;
        } 
       
       aPacketTypeID = l.getElement(1);
       switch(aPacketTypeID)
       {                     // Packet Type
           case 0x41:        // (A) Datagram Ack
           case 0x43:        // (C) Code Line
           case 0x44:        // (D) Datagram Read
           break;
               
           case 0x45:        // (E) EOT
           case 0x52:        // (R) Read
            _data.computePollInterval();
           break;
               
           case 0x49:        // (I) Initialize 
           case 0x4D:        // (M) NMRA Mast
           case 0x50:        // (P) Poll
           case 0x51:        // (Q) Query
           break;

           case 0x54:        // (T) Transmit
           case 0x57:        // (W) Datagram Write
           break;
               
           default: 
            _data.incMetricErrValue(CMRInetMetricsData.CMRInetMetricUnrecCommand);
       }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CMRInetMetricsCollector.class.getName());

}
