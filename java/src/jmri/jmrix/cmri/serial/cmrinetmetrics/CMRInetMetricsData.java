// CMRInetMetricsData.java

package jmri.jmrix.cmri.serial.cmrinetmetrics;

/**
 * CMRInet metric data variables and access methods.
 * The metric data is not persistent between runs.
 * 
 * @author	    Chuck Catania  Copyright (C) 2016
 * @version         $Revision: 17977 $
 */
    public class CMRInetMetricsData
    {
        /**
         * CMRInet packet types
         */
        public static int _DGACK    = 0x41;
        public static int _CODELINE = 0x43;
        public static int _DGREAD   = 0x44;
        public static int _EOT      = 0x45;
        public static int _INIT     = 0x49;
        public static int _POLL     = 0x50;
        public static int _QUERY    = 0x51;
        public static int _READ     = 0x52;
        public static int _TRANSMIT = 0x54;
        public static int _DGWRITE  = 0x57;
        
        /**
         * Data items for network error counts
         */
        //-------------------------------------------------------------------
        public static String[] CMRInetMetricErrName = {
                                                    "Timeout",
                                                    "Truncated Receive",
                                                    "Truncated Reply",
                                                    "Unrecognized Response",
                                                    "Unrecognized Command",
                                                    "Datagram NAK",
                                                    };
        
        public static int CMRInetMetricTimeout       = 0;
        public static int CMRInetMetricTruncRecv     = 1;
        public static int CMRInetMetricTruncReply    = 2;
        public static int CMRInetMetricUnrecResponse = 3;
        public static int CMRInetMetricUnrecCommand  = 4;
        public static int CMRInetMetricDatagramNAK   = 5;
        public static int CMRInetMetricErrLAST       = CMRInetMetricDatagramNAK+1;
    
        public static int CMRInetMetricErrCount[] = {100, 200, 300, 
                                                     10000000, 400, 
                                                     500, 0};
       
        /**
         * Data array for network data counts
         */
        public static String[] CMRInetMetricDataName = {
                                                    "Poll/Response Time (ms)",
                                                    "Init Messages",
                                                   };
        
        //-------------------------------------------------------------------
        public static int CMRInetMetricPollResponse  = 0;
        public static int CMRInetMetricInitMsgs      = 1;
        public static int CMRInetMetricDataLAST      = CMRInetMetricInitMsgs+1;
        
        public static int CMRInetMetricDataCount[] = {0,0};
        
        
        /**
         * Variables used for poll/response measurements
         * 
         */
        public static long pollTicks;
        public static int  pollIntervalMS;
        public static int  pollCnt;
        public static int  pollCntMax = 10;
    
        public CMRInetMetricsData() {   
            super();
        }
        
        /**
         * Methods for Metric Error data
         * 
         */        
        // Get the error count
        //---------------------
        public static int getMetricErrValue( int metricName ){
            return CMRInetMetricErrCount[metricName];
        }
        // Set the error count
        public static void setMetricErrValue( int metricName, int value ){
            CMRInetMetricErrCount[metricName] = value;
        }
        // Increment the error count
        public static void incMetricErrValue( int metricName ){
            CMRInetMetricErrCount[metricName]++;
        }
        // Zero the particular error count
        public static void zeroMetricErrValue( int metricName ){
            CMRInetMetricErrCount[metricName] = 0;
        }
        // Zero the all of the error counts
        public static void clearAllErrMetrics(){
            for (int i=0; i!=CMRInetMetricErrLAST; i++)
             CMRInetMetricErrCount[i] = 0;
        }
        
        /**
         * Methods for Metric data
         * 
         */        
        // Get the metric value
        //---------------------
        public static int getMetricDataValue( int metricName ){
            return CMRInetMetricDataCount[metricName];
        }
        // Set the metric value
        public static void setMetricDataValue( int metricName, int value ){
            CMRInetMetricDataCount[metricName] = value;
        }
        // Increment the metric value
        public static void incMetricDataValue( int metricName ){
            CMRInetMetricDataCount[metricName]++;
        }
        // Zero the particular metric value
        public static void zeroMetricDataValue( int metricName ){
            CMRInetMetricDataCount[metricName] = 0;
        }
        // Zero the all of the metric values
        public static void clearAllDataMetrics(){
            for (int i=0; i!=CMRInetMetricDataLAST; i++)
             CMRInetMetricDataCount[i] = 0;
            
            pollTicks = 0;
            pollIntervalMS = 0;
            pollCnt = 0;
        }
        
        /**
         * Methods to manage the poll/response metric
         * Start the poll timer when a poll message is seen
         * 
         */
        public static void startPollIntervalTimer()
        {
            pollTicks = System.currentTimeMillis();
        }
        
        /**
         * Compute the poll/reply interval in milliseconds
         * Average over pollCnt polls to keep the Tablemodel update rate low
         * 
         */
        public static void computePollInterval()
        {
            long curTicks = System.currentTimeMillis();

            if (pollCnt++ <= pollCntMax)
            {
                if (pollTicks == 0) pollTicks = curTicks;
                pollIntervalMS = pollIntervalMS + (int)(curTicks-pollTicks);
                pollTicks = curTicks;
            }
            else
            {
                pollIntervalMS = pollIntervalMS/pollCntMax;
                CMRInetMetricDataCount[CMRInetMetricPollResponse] = pollIntervalMS;
                pollIntervalMS = 0;
                pollCnt = 0;
            }
        }        
  }

