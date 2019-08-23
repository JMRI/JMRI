package apps.PanelPro;

import java.io.IOException;

import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.Ignore;
import org.junit.Test;

/**
 * This is more of an acceptance test than a unit test, loading a series
 * of connection user profiles in PanelPro.
 * <p>
 * It confirms that the entire application can start up and configure itself.
 * <p>
 * When format of user configuration (profile) files is changed, check the
 * sets in java/test/apps/PanelPro/profiles/ to match or allow for conversion
 * dialogs.
 * Also check the required TESTMAXTIME in {@link apps.LaunchJmriAppBase} to
 * prevent timeouts on app startup tests if structure of data develops.
 * 
 * @author Paul Bender Copyright (C) 2017, 2019
 * @author Bob Jacobsen Copyright (C) 2017
 */
@Ignore("Replaced with a Cucumber test")
public class PanelProTest extends apps.LaunchJmriAppBase {

    @Override
    protected void launch(String[] args) {
        PanelPro.main(args);
    }
    
    @Override
    protected void extraChecks() {
        JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("Main initialization done") != null;}, "last Info line seen");
    }
    
    @Test
    public void testLaunchEasyDcc() throws IOException {
        runOne("EasyDcc_Simulator", "PanelPro", "PanelPro version");
        // param 1 is profile folder name, param 2 and 3 must match Console output
    }

    @Test
    public void testLaunchGrapevine() throws IOException {
        runOne("Grapevine_Simulator", "PanelPro", "PanelPro version");
        JUnitAppender.suppressWarnMessage("Timeout can't be handled due to missing node (index 1)");
        JUnitAppender.suppressWarnMessage("Timeout can't be handled due to missing node (index 0)");
    }

    @Test
    public void testLaunchLocoNet() throws IOException {
        runOne("LocoNet_Simulator", "PanelPro", "PanelPro version");
        JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnPowerManager LnTrackStatusUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
        JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnSensorUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
        JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnSensorUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
    }

    @Test
    public void testLaunchSprog() throws IOException {
        runOne("Sprog_Simulator", "PanelPro", "PanelPro version");
    }

    @Test
    public void testLaunchTmcc() throws IOException {
        runOne("TMCC_Simulator", "PanelPro", "PanelPro version");
    }

    @Test
    public void testLaunchInitLoop() throws IOException {
        runOne("Prevent_Init_Loop", "PanelPro", "PanelPro version");
        JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnPowerManager LnTrackStatusUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
        JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnSensorUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
    }

}
