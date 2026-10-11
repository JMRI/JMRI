package jmri.jmrit.beantable.signalmast;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;

import org.junit.jupiter.api.*;

/**
 * @author Bob Jacobsen Copyright 2018
 */
public class SignalHeadSignalMastAddPaneTest extends AbstractSignalMastAddPaneTestBase {

    /** {@inheritDoc} */
    @Override
    protected SignalMastAddPane getOTT() { return new SignalHeadSignalMastAddPane(); }    

    @Test
    public void testSetMast() {
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH1") {
                    @Override
                    protected void updateOutput() {
                    }
                }
        );
        SignalHeadSignalMast s1 = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user name");
        MatrixSignalMast m1 = new MatrixSignalMast("IF$xsm:basic:one-low($0001)-3t", "user");

        SignalHeadSignalMastAddPane vp = new SignalHeadSignalMastAddPane();

        assertTrue(vp.canHandleMast(s1));
        assertFalse(vp.canHandleMast(m1));

        vp.setMast(null);

        SignalSystem basicSys = InstanceManager.getDefault(SignalSystemManager.class).getSystem("basic");
        assertNotNull(basicSys);

        vp.setAspectNames(s1.getAppearanceMap(), basicSys);
        vp.setMast(s1);

        vp.setAspectNames(m1.getAppearanceMap(), basicSys);
        vp.setMast(m1);

        JUnitAppender.assertErrorMessage("mast was wrong type: IF$xsm:basic:one-low($0001)-3t jmri.implementation.MatrixSignalMast");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
