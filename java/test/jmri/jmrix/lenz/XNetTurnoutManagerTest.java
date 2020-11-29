package jmri.jmrix.lenz;

import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmrix.lenz.XNetTurnoutManager class.
 *
 * @author Bob Jacobsen Copyright 2004
 */
public class XNetTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "XT" + i;
    }

    protected XNetInterfaceScaffold lnis;

    @Test
    @Override
    public void testMisses() {
        // try to get nonexistant turnouts
        assertThat(l.getByUserName("foo")).isNull();
        assertThat(l.getBySystemName("bar")).isNull();
    }

    @Test
    public void testXNetMessages() {
        // send messages for 21, 22
        // notify that somebody else changed it...
        XNetReply m1 = new XNetReply();
        m1.setElement(0, 0x42);
        m1.setElement(1, 0x05);
        m1.setElement(2, 0x02);
        m1.setElement(3, 0x45);
        lnis.sendTestMessage(m1);

        // notify that somebody else changed it...
        XNetReply m2 = new XNetReply();
        m2.setElement(0, 0x42);
        m2.setElement(1, 0x05);
        m2.setElement(2, 0x04);
        m2.setElement(3, 0x43);
        lnis.sendTestMessage(m2);

        // try to get turnouts to see if they exist
        Turnout xt21 = l.getBySystemName("XT21");
        Turnout xt22 = l.getBySystemName("XT22");
        assertThat(xt21).isNotNull();
        assertThat(xt22).isNotNull();

        assertThat(l.getNamedBeanSet()).containsOnly(xt21,xt22);
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        TurnoutManager t = jmri.InstanceManager.turnoutManagerInstance();

        Turnout o = t.newTurnout("XT21", "my name");

        log.debug("received turnout value {}", o);
        assertThat(o).isNotNull();

        log.debug("by system name: {}", t.getBySystemName("XT21"));
        log.debug("by user name:   {}", t.getByUserName("my name"));

        assertThat(t.getBySystemName("XT21")).isNotNull();
        assertThat(t.getByUserName("my name")).isNotNull();
    }

    @Test
    public void testGetSystemPrefix() {
        assertThat(l.getSystemPrefix()).isEqualTo("X");
    }

    @Test
    public void testAllowMultipleAdditions() {
        assertThat(l.allowMultipleAdditions("foo")).isTrue();
    }

    @Test
    @Override
    public void testThrownText() {
        assertThat(l.getThrownText()).isEqualTo(Bundle.getMessage("TurnoutStateThrown"));
    }

    @Test
    @Override
    public void testClosedText() {
        assertThat(l.getClosedText()).isEqualTo(Bundle.getMessage("TurnoutStateClosed"));
    }

    @AfterEach
    public void tearDown() {
        lnis.terminateThreads();
        lnis = null;
        l = null;
        JUnitUtil.tearDown();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface, register
        lnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        l = new XNetTurnoutManager(lnis.getSystemConnectionMemo());
        jmri.InstanceManager.setTurnoutManager(l);
    }

    private final static Logger log = LoggerFactory.getLogger(XNetTurnoutManagerTest.class);

}
