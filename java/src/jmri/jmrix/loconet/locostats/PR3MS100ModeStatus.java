package jmri.jmrix.loconet.locostats;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * PR3 (MS100 mode) Interface status
 * 
 * @author Bob Milhaupt Copyright (C) 2017
 */
@API(status = EXPERIMENTAL)
public class PR3MS100ModeStatus {
   public PR3MS100ModeStatus(int a, int b, int c) {
        goodMsgCnt = a;
        badMsgCnt = b;
        ms100status = c;
    }
   public int goodMsgCnt;
   public int badMsgCnt;
   public int ms100status;
}
