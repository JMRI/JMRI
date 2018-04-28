package jmri.jmrix.openlcb;

import jmri.implementation.AbstractTurnoutTestBase;

/**
 * Tests inherited from the abstract turnout test base, specialized for the OlcbTurnout. This is
 * testing a subset of the OlcbTurnout functionality, but tests common JMRI functionality that is
 * not specifically duplicated in the OlcbTurnoutTest.
 * Created by Balazs Racz on 1/11/18.
 */

public class OlcbTurnoutInheritedTest extends AbstractTurnoutTestBase {
    OlcbTestInterface tif;
    int baselineListeners;

    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tif = new OlcbTestInterface();
        tif.waitForStartup();
        baselineListeners = tif.iface.numMessageListeners();
        OlcbTurnout ot = new OlcbTurnout("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", tif.iface);
        ot.finishLoad();
        t = ot;
    }

    @Override
    public int numListeners() {
        return tif.iface.numMessageListeners() - baselineListeners;
    }

    @Override
    public void checkThrownMsgSent() throws InterruptedException {
        tif.flush();
        tif.assertSentMessage(":X195B4C4CN0102030405060708;");
    }

    @Override
    public void checkClosedMsgSent() throws InterruptedException {
        tif.flush();
        tif.assertSentMessage(":X195B4C4CN0102030405060709;");
    }
}
