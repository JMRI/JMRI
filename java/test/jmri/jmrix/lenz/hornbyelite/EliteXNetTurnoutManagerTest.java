package jmri.jmrix.lenz.hornbyelite;

import jmri.Turnout;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmrix.lenz.hornbyelite.EliteXNetTurnoutManager class.
 *
 * @author Bob Jacobsen Copyright 2004
 */
public class EliteXNetTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "XT" + i;
    }

    XNetInterfaceScaffold lnis;

    @Test    
    @Override
    public void testMisses() {
        // try to get nonexistant turnouts
        assertThat(l.getByUserName("foo")).isNull();
        assertThat(l.getBySystemName("bar")).isNull();
    }

    @Test
    public void testEliteXNetMessages() {
        // send messages for 20, 21
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
        EliteXNetTurnout xt20 = (EliteXNetTurnout) l.getBySystemName("XT20");
        EliteXNetTurnout xt21 = (EliteXNetTurnout) l.getBySystemName("XT21");
        assertThat(xt20).isNotNull();
        assertThat(xt21).isNotNull();

        assertThat(l.getNamedBeanSet()).contains(xt20,xt21);
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout newTurnout = l.newTurnout("XT21", "my name");
        log.debug("received turnout value {}", newTurnout);

        assertThat(newTurnout).isNotNull();

        // make sure loaded into tables
        log.debug("by system name: {}", l.getBySystemName("XT21"));
        log.debug("by user name:   {}", l.getByUserName("my name"));

        assertThat(l.getBySystemName("XT21")).isEqualTo(newTurnout);
        assertThat(l.getByUserName("my name")).isEqualTo(newTurnout);
    }

    @Test
    @Override
    public void testThrownText(){
         assertThat(l.getThrownText()).isEqualTo(Bundle.getMessage("TurnoutStateThrown"));
    }

    @Test
    @Override
    public void testClosedText(){
        assertThat(l.getClosedText()).isEqualTo(Bundle.getMessage("TurnoutStateClosed"));
    }

    @AfterEach
    public void tearDown() {
        lnis.getSystemConnectionMemo().getXNetTrafficController().terminateThreads();
        JUnitUtil.tearDown();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface, register
        lnis = new XNetInterfaceScaffold(new HornbyEliteCommandStation());
        // create and register the manager object
        l = new EliteXNetTurnoutManager(lnis.getSystemConnectionMemo());
        jmri.InstanceManager.setTurnoutManager(l);
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetTurnoutManagerTest.class);

}
