/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

public class CTCConstants {
    public static final int LEFTTRAFFIC = 0;            // Indicator lights are presently left traffic
    public static final int SIGNALSNORMAL = 1;          // Indicator lights are presently all stop
    public static final int RIGHTTRAFFIC = 2;           // Indicator lights are presently right traffic
    public static final int OUTOFCORRESPONDENCE = 3;    // Indicator lights are all unlit
    
    public static final int SWITCHNORMAL = 0;           // Indicator lights are presently for normal direction of traffic (typically straight)
    public static final int SWITCHREVERSED = 1;         // Indicator lights are presently for reversed direction of traffic (typically curved)
            
    public static final int CTC_UNKNOWN = -1;            // For signals or switches (UNKNOWN is defined by JMRI, ergo the "CTC_" prefix.
}
