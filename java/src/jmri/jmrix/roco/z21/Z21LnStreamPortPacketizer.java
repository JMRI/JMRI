package jmri.jmrix.roco.z21;

import jmri.jmrix.loconet.streamport.LnStreamPortPacketizer;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Override the default LnStreamPortPacketizer
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public class Z21LnStreamPortPacketizer extends LnStreamPortPacketizer {

    public Z21LnStreamPortPacketizer(LocoNetSystemConnectionMemo m) {
        super(m);
        echo = true;
    }
}
