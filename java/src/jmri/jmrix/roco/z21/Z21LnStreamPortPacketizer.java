package jmri.jmrix.roco.z21;

import java.io.DataInputStream;
import java.io.OutputStream;
import jmri.jmrix.loconet.streamport.LnStreamPortPacketizer;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Override the default LnStreamPortPacketizer
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class Z21LnStreamPortPacketizer extends LnStreamPortPacketizer {

    public Z21LnStreamPortPacketizer(LocoNetSystemConnectionMemo m) {
        super(m);
        echo = true;
    }
}
