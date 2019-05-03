package jmri.implementation;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.progdebugger.ProgDebugger;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock to provide a CommandStation object for tests
 *
 * @author	Bob Jacobsen Copyright 2018
 *
 */

public class MockCommandStation implements CommandStation {

    String name = "I";
    public MockCommandStation() {}
    public MockCommandStation(String name) {this.name = name;}
    
    @Override
    public boolean sendPacket(byte[] packet, int repeats) {
        lastPacket = packet;
        return true;
    }

    @Override
    public String getUserName() {
        return name;
    }

    @Override
    public String getSystemPrefix() {
        return name;
    }

    public byte[] lastPacket;
}

