package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.can.TestTrafficController;
import org.openlcb.*;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbThrottleManager class.
 *
 * @author Bob Jacobsen Copyright 2008, 2010, 2011
 * @author Paul Bender Copyright (C) 2016
 */
public class OlcbThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private static OlcbSystemConnectionMemo memo;
    static Connection connection;
    static NodeID nodeID = new NodeID(new byte[]{1, 0, 0, 0, 0, 0});
    static java.util.ArrayList<Message> messages;

    @Override
    @BeforeEach
    public void setUp() {
        tm = new OlcbThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
       tm = null;
    }

    @BeforeAll
    public static void preClassInit() {
        JUnitUtil.setUp();
        // we need to set up the memo because as a throttle is created in test, a message will be generated
        messages = new java.util.ArrayList<>();
        connection = new AbstractConnection() {
            @Override
            public void put(Message msg, Connection sender) {
                messages.add(msg);
            }
        };

        memo = new jmri.jmrix.openlcb.OlcbSystemConnectionMemo();
        TestTrafficController tc = new TestTrafficController();
        memo.setTrafficController(tc);
        memo.setInterface(new OlcbInterface(nodeID, connection) {
            @Override
            public Connection getOutputConnection() {
                return connection;
            }
        });
    }

    @AfterAll
    public static void postClassTearDown() {
        if (memo != null && memo.getInterface() != null ) {
            memo.getTrafficController().terminateThreads();
            memo.getInterface().dispose();
        }
        memo = null;
        connection = null;
        nodeID = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
