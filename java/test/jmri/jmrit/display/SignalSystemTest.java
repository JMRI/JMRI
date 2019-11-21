package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test signal system via a specific layout file
 *
 * This is in jmri.jmrit.display instead of jmri.implementation because the
 * files it loads display the signals for debugging purposes; if their panels
 * were editted out, jmri.implementation would be a better location
 *
 * @author Bob Jacobsen Copyright 2016
 * @since 4.3.2
 */
public class SignalSystemTest {

    @Test
    public void testLoadSimplePanelOBlocksDB1969() throws jmri.JmriException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        // load file
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new java.io.File("java/test/jmri/jmrit/display/verify/SimplePanel_OBlocks-DB1969.xml"));

        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        // check aspects
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0002)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0003)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0009)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0010)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0004)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0007)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0011)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0005)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0006)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0008)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:shunting_dwarf($0012)", "Sh0");

        InstanceManager.turnoutManagerInstance().getTurnout("IT201").setCommandedState(Turnout.CLOSED);

        checkAspect("IF$vsm:DB-HV-1969:block_distant($0002)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0003)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0009)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0010)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0004)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0007)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0011)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0005)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0006)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0008)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:shunting_dwarf($0012)", "Sh0");

        InstanceManager.sensorManagerInstance().getSensor("IS101").setState(Sensor.ACTIVE);

        checkAspect("IF$vsm:DB-HV-1969:block_distant($0002)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0003)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0009)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0010)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0004)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0007)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0011)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0005)", "Hp00");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0006)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0008)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:shunting_dwarf($0012)", "Sh0");

        InstanceManager.sensorManagerInstance().getSensor("IS102").setState(Sensor.ACTIVE);

        checkAspect("IF$vsm:DB-HV-1969:block_distant($0002)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0003)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0009)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:block_distant($0010)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0004)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0007)", "Hp1+Vr1");
        checkAspect("IF$vsm:DB-HV-1969:entry_distant($0011)", "Hp0");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0005)", "Hp00");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0006)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:exit_distant($0008)", "Hp1+Vr0");
        checkAspect("IF$vsm:DB-HV-1969:shunting_dwarf($0012)", "Sh0");

        EditorFrameOperator efo = new EditorFrameOperator("DB1969 Control Panel Editor");
        efo.closeFrameWithConfirmations();

    }

    @Test
    public void testLoadAA1UPtest() throws jmri.JmriException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // load file
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).setStabilisedSensor("IS_ROUTING_DONE");

        InstanceManager.getDefault(ConfigureManager.class)
                .load(new java.io.File("java/test/jmri/jmrit/display/verify/AA1UPtest.xml"));

        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        jmri.util.JUnitUtil.waitFor(() -> {
            return InstanceManager.sensorManagerInstance().provideSensor("IS_ROUTING_DONE").getKnownState() == jmri.Sensor.ACTIVE;
        },
                "LayoutEditor stabilized sensor went ACTIVE");

        // check aspects
        checkAspect("IF$vsm:BNSF-1996:SE-1A($0152)", "Stop");  // not on visual panel

        checkAspect("IF$vsm:UP-2008:SL-3A($0170)", "Advance Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0171)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0172)", "Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0173)", "Restricting");
        checkAspect("IF$vsm:UP-2008:SL-3A($0174)", "Advance Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0176)", "Clear");
        checkAspect("IF$vsm:UP-2008:SL-3A($0178)", "Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0179)", "Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0181)", "Clear");
        checkAspect("IF$vsm:UP-2008:SL-3A($0183)", "Clear");
        checkAspect("IF$vsm:UP-2008:SL-3A($0185)", "Advance Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0186)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0230)", "Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0231)", "Advance Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0232)", "Clear");
        checkAspect("IF$vsm:UP-2008:SL-3A($0233)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0234)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0235)", "Restricting");
        checkAspect("IF$vsm:UP-2008:SL-3A($0236)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0237)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0238)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0239)", "Clear");
        checkAspect("IF$vsm:UP-2008:SL-3A($0240)", "Clear");
        checkAspect("IF$vsm:UP-2008:SL-3A($0241)", "Restricting");
        checkAspect("IF$vsm:UP-2008:SL-3A($0242)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0243)", "Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0244)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0245)", "Approach");
        checkAspect("IF$vsm:UP-2008:SL-3A($0246)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0247)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3A($0248)", "Stop");
        checkAspect("IF$vsm:UP-2008:SL-3L2($0249)", "Approach Restricting");
        checkAspect("IF$vsm:UP-2008:SL-3L2($0250)", "Approach");

        final int numErrorMessages = 19;
        // clean up messages from file
        for (int i=0; i < numErrorMessages; i++) {
            jmri.util.JUnitAppender.assertErrorMessageStartsWith("No facing block found for source mast IF$vsm:BNSF-1996:SL-");
        }

        EditorFrameOperator efo = new EditorFrameOperator("AA1UPtest Layout");
        efo.closeFrameWithConfirmations();

    }

    void checkAspect(String mastName, String aspect) {
        SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(mastName);
        if (mast != null) {
            // wait present or error
            jmri.util.JUnitUtil.waitFor(() -> {
                return mast.getAspect().equals(aspect);
            },
                    "mast " + mastName + "currently showing \"" + mast.getAspect() + "\", expected aspect \"" + aspect + "\"," );
        } else {
            Assert.fail("Mast " + mastName + " not found");
        }
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
