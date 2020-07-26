package jmri.implementation;

import jmri.CommandStation;

/**
 * Mock to provide a CommandStation object for tests
 *
 * @author Bob Jacobsen Copyright 2018
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

