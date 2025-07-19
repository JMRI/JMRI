package jmri.jmrix.loconet.alm;

/**
 * An enumeration to assist with ALM message handling.
 *
 * @author Bob Milhaupt  Copyright (C) 2022
 */
public enum AlmMsgTypes {
    NOT_ALM_MSG, ALM_ROUTCAPQ, ALM_ROUTCAPREP,
    ALM_RDQ, ALM_ROUTDATW, ALM_ROUTDATREP,
    ALM_BAW, ALM_ROUTECSCAPREP, ALM_UNKNOWN
}
