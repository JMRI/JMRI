package jmri.jmrix.ipocs.protocol.packets;

import jmri.util.JUnitUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

public class SignOfLifePacketTest {

    @Test
    public void getIdTest() {
        assertEquals(SignOfLifePacket.IDENT, new SignOfLifePacket().getId()); 
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
