package jmri.jmrit.beantable.light;

import java.util.Locale;

import javax.swing.JFrame;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.VariableLight;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021, 2025
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class LightIntensityPaneTest {

    @Test
    public void testCTor() {
        LightIntensityPane t = new LightIntensityPane(true);
        assertNotNull(t);
    }

    @Test
    public void testGetSetIntensityEnglish() {

        Locale existing = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            LightIntensityPane t = new LightIntensityPane(false);
            assertNotNull(t);

            LightManager mgr = InstanceManager.getDefault(LightManager.class);
            Light li = mgr.provideLight("I1LIPTL1");
            assertNotNull(li);
            assertInstanceOf(VariableLight.class, li);

            VariableLight l = (VariableLight)li;
            l.setMinIntensity(0.22d);
            l.setMaxIntensity(0.81d);
            l.setTransitionTime(1.23d);
            t.setToLight(l);

            JFrame f = new JFrame("Test LightIntensityPane");
            f.getContentPane().add(t);
            ThreadingUtil.runOnGUI( () -> {
                f.pack();
                f.setVisible(true);
            });

            JFrameOperator jfo = new JFrameOperator(f.getTitle());
            assertNotNull(jfo);

            JTextFieldOperator tfo = new JTextFieldOperator(jfo, 0);
            assertEquals("22 %", tfo.getText());
            tfo.clearText();
            tfo.typeText("33 %");

            tfo = new JTextFieldOperator(jfo, 1);
            assertEquals("81 %", tfo.getText());
            tfo.setText("66 %");

            tfo = new JTextFieldOperator(jfo, 2);
            assertEquals("1.23", tfo.getText());
            tfo.setText("7.89");

            t.setLightFromPane(l);

            // JUnitUtil.waitFor(30000);

            assertEquals(0.33d, l.getMinIntensity());
            assertEquals(0.66d, l.getMaxIntensity());
            assertEquals(7.89d, l.getTransitionTime());

            JUnitUtil.dispose(f);
            jfo.waitClosed();
        } finally {
            Locale.setDefault(existing);
        }

    }

    @Test
    public void testGetSetIntensityItalian() {

        Locale existing = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ITALY);
            LightIntensityPane t = new LightIntensityPane(false);
            assertNotNull(t);

            LightManager mgr = InstanceManager.getDefault(LightManager.class);
            Light li = mgr.provideLight("I1LIPTL2");
            assertNotNull(li);
            assertInstanceOf(VariableLight.class, li);
            VariableLight l = (VariableLight)li;

            l.setMinIntensity(0.22d);
            l.setMaxIntensity(0.81d);
            l.setTransitionTime(1.23d);
            t.setToLight(l);

            JFrame f = new JFrame("Test LightIntensityPane It");
            f.getContentPane().add(t);
            ThreadingUtil.runOnGUI( () -> {
                f.pack();
                f.setVisible(true);
            });
            JFrameOperator jfo = new JFrameOperator(f.getTitle());
            assertNotNull(jfo);

            JTextFieldOperator tfo = new JTextFieldOperator(jfo, 0);
            assertEquals("22 %", tfo.getText());
            tfo.clearText();
            tfo.typeText("33 %");

            tfo = new JTextFieldOperator(jfo, 1);
            assertEquals("81 %", tfo.getText());
            tfo.setText("66 %");

            tfo = new JTextFieldOperator(jfo, 2);
            assertEquals("1,23", tfo.getText());
            tfo.setText("7,89");

            t.setLightFromPane(l);
            assertEquals(0.33d, l.getMinIntensity());
            assertEquals(0.66d, l.getMaxIntensity());
            assertEquals(7.89d, l.getTransitionTime());

            JUnitUtil.dispose(f);
            jfo.waitClosed();
        } finally {
            Locale.setDefault(existing);
        }

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
