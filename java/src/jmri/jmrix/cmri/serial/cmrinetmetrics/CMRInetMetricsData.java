package jmri.jmrix.cmri.serial.cmrinetmetrics;

/**
 * CMRInet metric data variables and access methods.
 * The metric data is not persistent between runs.
 * 
 * @author Chuck Catania  Copyright (C) 2016, 2018
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
        public static final String[] CMRInetMetricErrName = {
                                                    "Timeout",
                                                    "Truncated Receive",
                                                    "Truncated Reply",
                                                    "Unrecognized Response",
                                                    "Unrecognized Command"
                                                    };
        
        public final static int CMRInetMetricTimeout       = 0;
        public final static int CMRInetMetricTruncRecv     = 1;
        public final static int CMRInetMetricTruncReply    = 2;
        public final static int CMRInetMetricUnrecResponse = 3;
        public final static int CMRInetMetricUnrecCommand  = 4;
        public final static int CMRInetMetricErrLAST       = CMRInetMetricUnrecCommand+1;
    
        public int CMRInetMetricErrCount[]           = {
                                                     0,0, 
                                                     0,0, 
                                                     0,0
                                                    };
       
        /**
         * Data array for network data counts
         */
        public static final String[] CMRInetMetricDataName = {
                                                    "Poll/Response Time (ms)",
                                                    "Init Messages",
                                                   };
        
        //-------------------------------------------------------------------
        public static int CMRInetMetricPollResponse  = 0;
        public static int CMRInetMetricInitMsgs      = 1;
        public static int CMRInetMetricDataLAST      = CMRInetMetricInitMsgs+1;
        
        public int CMRInetMetricDataCount[] = {0,0};
        
        
        /**
         * Variables used for poll/response measurements
         * 
         */
        public long pollTicks;
        public int  pollIntervalMS;
        public int  pollCnt;
        public int  pollCntMax = 10;
    
        public CMRInetMetricsData() {   
            super();
        }
        
        /**
         * Methods for Metric Error data
         * 
         */        
        // Get the error count
        //---------------------
        synchronized public int getMetricErrValue( int metricName ){
            return CMRInetMetricErrCount[metricName];
        }
        // Set the error count
        synchronized public void setMetricErrValue( int metricName, int value ){
            CMRInetMetricErrCount[metricName] = value;
        }
        // Increment the error count
        synchronized public void incMetricErrValue( int metricName ){
            CMRInetMetricErrCount[metricName]++;
        }
        // Zero the particular error count
        synchronized public void zeroMetricErrValue( int metricName ){
            CMRInetMetricErrCount[metricName] = 0;
        }
        // Zero the all of the error counts
        synchronized public void clearAllErrMetrics(){
            for (int i=0; i!=CMRInetMetricErrLAST; i++)
             CMRInetMetricErrCount[i] = 0;
        }
        // Get Error Count
        synchronized public int getMetricErrorCount(int metricName){
             return CMRInetMetricErrCount[metricName];
        }
       
        /**
         * Methods for Metric data
         * 
         */        
        // Get the metric value
        //---------------------
        synchronized public int getMetricDataValue( int metricName ){
            return CMRInetMetricDataCount[metricName];
        }
        // Set the metric value
        synchronized public void setMetricDataValue( int metricName, int value ){
            CMRInetMetricDataCount[metricName] = value;
        }
        // Increment the metric value
        synchronized public void incMetricDataValue( int metricName ){
            CMRInetMetricDataCount[metricName]++;
        }
        // Zero the particular metric value
        synchronized public void zeroMetricDataValue( int metricName ){
            CMRInetMetricDataCount[metricName] = 0;
        }
        // Set the metric error count
        synchronized public void setMetricErrorValue( int metricName, int value ){
            CMRInetMetricErrCount[metricName] = value;
        }
        // Zero the all of the metric values
        synchronized public void clearAllDataMetrics(){
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
        public void startPollIntervalTimer()
        {
            pollTicks = System.currentTimeMillis();
        }
        
        /**
         * Compute the poll/reply interval in milliseconds
         * Average over pollCnt polls to keep the Tablemodel update rate low
         * 
         */
        public void computePollInterval()
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

