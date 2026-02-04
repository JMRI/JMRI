package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private MarklinReply mr;

    @Test
    public void testDefaultMarklinReply() {
        Assertions.assertEquals(MarklinConstants.PRIO_1, mr.getPriority());
        Assertions.assertEquals(0, mr.getCommand());
    }

    @Test
    public void testSetGetCommand() {
        mr.setCommand( MarklinConstants.SYSCOMMANDSTART);
        Assertions.assertEquals(MarklinConstants.SYSCOMMANDSTART, mr.getCommand());

        mr.setCommand( MarklinConstants.AUTCOMMANDSTART);
        Assertions.assertEquals(MarklinConstants.AUTCOMMANDSTART, mr.getCommand());
    }

    @Test
    public void testSetGetAddress() {
        Assertions.assertEquals( 0, mr.getAddress());
        mr.setAddress( 0xFFFFFFFFL);
        Assertions.assertEquals(0xFFFFFFFFL, mr.getAddress());

        mr.setAddress( 0xA1B2C3D4L);
        Assertions.assertEquals(0xA1B2C3D4L, mr.getAddress());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        mr = new MarklinReply();
        m = mr;
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = null;
        mr = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinReplyTest.class);
}
