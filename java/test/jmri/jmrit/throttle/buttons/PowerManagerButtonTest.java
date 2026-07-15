package jmri.jmrit.throttle.buttons;

import javax.swing.*;
import javax.swing.JPanel;

import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.*;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Steve Young Copyright (C)2026
 */
@DisabledIfHeadless
public class PowerManagerButtonTest {

    @Test
    void testDisplayStates() throws JmriException {

        PowerManager pm = new jmri.PowerManagerScaffold();
        pm.setPower(PowerManager.UNKNOWN);

        t = new PowerManagerButton(true, pm){
            @Override
                protected void loadIcons() {
                setPowerOnIcon(new NamedIcon("resources/icons/throttles/GreenPowerLED.gif", "resources/icons/throttles/GreenPowerLED.gif"));
                setPowerOffIcon(new NamedIcon("resources/icons/throttles/RedPowerLED.gif", "resources/icons/throttles/RedPowerLED.gif"));
                setPowerIdleIcon(new NamedIcon("resources/icons/throttles/YellowPowerLED.gif", "resources/icons/throttles/YellowPowerLED.gif"));
                setPowerUnknownIcon(new NamedIcon("resources/icons/throttles/YellowPowerLED.gif", "resources/icons/throttles/YellowPowerLED.gif"));
            }
        };
        assertNotNull(t);

        JFrame f = new JFrame("PowerManagerButtonTest States");
        JPanel p = new JPanel();
        p.add(t);
        f.add(p);
        ThreadingUtil.runOnGUI(() -> {
            f.pack();
            f.setVisible(true);
        });

        JFrameOperator jfo = new JFrameOperator(f.getTitle()); // waits for frame visible
        assertNotNull(jfo);

        JButtonOperator jbo = new JButtonOperator(jfo);
        assertEquals(Bundle.getMessage("PowerStateUnknown"), jbo.getText());
        assertEquals(Bundle.getMessage("LayoutPowerUnknown"), jbo.getToolTipText());

        pm.setPower(PowerManager.OFF);
        assertEquals(Bundle.getMessage("PowerStateOff"), jbo.getText());
        assertEquals(Bundle.getMessage("LayoutPowerOff"), jbo.getToolTipText());

        pm.setPower(PowerManager.ON);
        assertEquals(Bundle.getMessage("PowerStateOn"), jbo.getText());
        assertEquals(Bundle.getMessage("LayoutPowerOn"), jbo.getToolTipText());

        pm.setPower(PowerManager.IDLE);
        assertEquals(Bundle.getMessage("PowerStateIdle"), jbo.getText());
        assertEquals(Bundle.getMessage("LayoutPowerIdle"), jbo.getToolTipText());

        JUnitUtil.dispose(f);

    }

    @Test
    void testButtonClicks() throws JmriException {

        PowerManager pm = new jmri.PowerManagerScaffold(){
            @Override
            public boolean implementsIdle() {
                return true;
            }
        };
        pm.setPower(PowerManager.UNKNOWN);

        t = new PowerManagerButton(false, pm){
            @Override
                protected void loadIcons() {
                setPowerOnIcon(new NamedIcon("resources/icons/throttles/GreenPowerLED.gif", "resources/icons/throttles/GreenPowerLED.gif"));
                setPowerOffIcon(new NamedIcon("resources/icons/throttles/RedPowerLED.gif", "resources/icons/throttles/RedPowerLED.gif"));
                setPowerIdleIcon(new NamedIcon("resources/icons/throttles/YellowPowerLED.gif", "resources/icons/throttles/YellowPowerLED.gif"));
                setPowerUnknownIcon(new NamedIcon("resources/icons/throttles/YellowPowerLED.gif", "resources/icons/throttles/YellowPowerLED.gif"));
            }
        };
        assertNotNull(t);

        JFrame f = new JFrame("PowerManagerButtonTest States");
        JPanel p = new JPanel();
        p.add(t);
        f.add(p);
        ThreadingUtil.runOnGUI(() -> {
            f.pack();
            f.setVisible(true);
        });

        JFrameOperator jfo = new JFrameOperator(f.getTitle()); // waits for frame visible
        assertNotNull(jfo);
        JButtonOperator jbo = new JButtonOperator(jfo);


        // UNKNOWN > OFF
        jbo.doClick();
        assertEquals(PowerManager.OFF, pm.getPower());

        // OFF > ON
        jbo.doClick();
        assertEquals(PowerManager.ON, pm.getPower());

        // ON > OFF
        jbo.doClick();
        assertEquals(PowerManager.OFF, pm.getPower());

        // IDLE > OFF
        pm.setPower(PowerManager.IDLE);
        assertEquals(PowerManager.IDLE, pm.getPower());
        jbo.doClick();
        assertEquals(PowerManager.OFF, pm.getPower());

        JUnitUtil.dispose(f);
    }

    private PowerManagerButton t;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.tearDown();
    }

}
