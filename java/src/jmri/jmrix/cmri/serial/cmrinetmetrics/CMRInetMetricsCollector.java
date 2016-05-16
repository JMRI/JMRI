// CMRInetMetricsCollector.java

package jmri.jmrix.cmri.serial.cmrinetmetrics;

import jmri.jmrix.cmri.serial.SerialListener;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;
import jmri.jmrix.cmri.serial.SerialTrafficController;

/**
 * Listener class for collecting CMRInet network traffic and error messages
 * @author	    Chuck Catania  Copyright (C) 2016
 * @version         $Revision: 17977 $
 */

public class CMRInetMetricsCollector implements SerialListener {

    /**
     * Method to add metrics listener to the SerialTrafficController
     * Clears all metric data
     * Only one instance of the collector is running
     */
    public CMRInetMetricsCollector() {
        SerialTrafficController.instance().addSerialListener(this);
        CMRInetMetricsData.clearAllErrMetrics();
        CMRInetMetricsData.clearAllDataMetrics();
    }

    protected void init() {
        System.out.println("CMRInetMetricsCollector - init");
    }

    /**
     * Transmit packets
     * Monitor any transmit packets to collect metrics      
     */
    public synchronized void message(SerialMessage l) 
    { 
       int aPacketTypeID = 0;

       if (l.getNumDataElements() < 2)
	{
           CMRInetMetricsData.incMetricErrValue(CMRInetMetricsData.CMRInetMetricTruncRecv);
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
            CMRInetMetricsData.incMetricDataValue(CMRInetMetricsData.CMRInetMetricInitMsgs);
           break;
               
           case 0x4D:        // (M) NMRA Mast
           break;
               
           case 0x50:        // (P) Poll
            CMRInetMetricsData.startPollIntervalTimer();
           break;

           case 0x51:        // (Q) Query
           case 0x52:        // (R) Read
           case 0x54:        // (T) Transmit
           case 0x57:        // (W) Datagram Write
           break;
               
           default: 
            CMRInetMetricsData.incMetricErrValue(CMRInetMetricsData.CMRInetMetricUnrecCommand);
           return;
       }
    }
    
    /**
     * Receive packets
     * Monitor any read (reply) packets to collect metrics     
     */
    public synchronized void reply(SerialReply l) 
    { 
       int aPacketTypeID = 0;

       if (l.getNumDataElements() < 2)
	{
           CMRInetMetricsData.incMetricErrValue(CMRInetMetricsData.CMRInetMetricTruncReply);
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
            CMRInetMetricsData.computePollInterval();
           break;
               
           case 0x49:        // (I) Initialize 
           case 0x4D:        // (M) NMRA Mast
           case 0x50:        // (P) Poll
           case 0x51:        // (Q) Query
           break;
               
           case 0x52:        // (R) Read
            CMRInetMetricsData.computePollInterval();
           break;
               
           case 0x54:        // (T) Transmit
           case 0x57:        // (W) Datagram Write
           break;
               
           default: 
            CMRInetMetricsData.incMetricErrValue(CMRInetMetricsData.CMRInetMetricUnrecCommand);
           return;
       }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CMRInetMetricsCollector.class.getName());
}

/* @(#)CMRInetMetricsCollector.java */
