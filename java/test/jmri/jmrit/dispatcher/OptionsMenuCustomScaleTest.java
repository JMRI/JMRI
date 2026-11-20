package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Objects;

import javax.swing.JMenuItem;

import jmri.InstanceManager;
import jmri.Scale;
import jmri.ScaleManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Dispatcher's Options offer the CUSTOM scale, with its ratio beside the
 * scale box.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class OptionsMenuCustomScaleTest {

    private Scale custom;
    private double customRatio;

    @Test
    public void testSetCustomRatio() {
        assertTrue(OptionsMenu.setCustomRatio(custom, 100));
        assertEquals(100, custom.getScaleRatio(), 1e-9);

        // a Timetable layout on CUSTOM may refuse a ratio
        VetoableChangeListener refuse = evt -> {
            throw new PropertyVetoException("refused", evt);
        };
        custom.addVetoableChangeListener("ScaleRatio", refuse);
        try {
            assertFalse(OptionsMenu.setCustomRatio(custom, 50));
            jmri.util.JUnitAppender.assertWarnMessageStartsWith("The ratio change for CUSTOM scale to 50.0 was rejected");
            jmri.util.JUnitAppender.assertWarnMessageStartsWith("CUSTOM scale ratio 50.0 not accepted");
        } finally {
            custom.removeVetoableChangeListener("ScaleRatio", refuse);
        }
        assertEquals(100, custom.getScaleRatio(), 1e-9, "unchanged");
    }

    @Test
    @DisabledIfHeadless
    public void testChooseCustomInOptions() {
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        OptionsMenu menu = new OptionsMenu(d);
        JMenuItem open = null;
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item != null && item.getText().startsWith(Bundle.getMessage("OptionWindowItem"))) {
                open = item;
            }
        }
        JMenuItem item = Objects.requireNonNull(open);
        ThreadingUtil.runOnGUI(item::doClick);
        JFrameOperator options = new JFrameOperator(Bundle.getMessage("OptionWindowItem"));
        try {
            assertFalse(menu.customRatioSpinner.isEnabled(), "the options file's scale is G");

            ThreadingUtil.runOnGUI(() -> menu.layoutScaleBox.setSelectedItem(custom));
            assertTrue(menu.customRatioSpinner.isEnabled(), "CUSTOM chosen: its ratio may be set");
            assertEquals(customRatio, ((Number) menu.customRatioSpinner.getValue()).doubleValue(), 1e-9);
            ThreadingUtil.runOnGUI(() -> menu.customRatioSpinner.setValue(100.0));
            // with no Layout Editor panel, Apply warns about Auto Turnouts: not what this tests
            ThreadingUtil.runOnGUI(() -> menu.autoTurnoutsCheckBox.setSelected(false));
            new JButtonOperator(options, Bundle.getMessage("ButtonApply")).doClick();
            assertSame(custom, d.getScale());
            assertEquals(100, custom.getScaleRatio(), 1e-9);

            ThreadingUtil.runOnGUI(() -> menu.layoutScaleBox.setSelectedItem(ScaleManager.getScale("HO")));
            assertFalse(menu.customRatioSpinner.isEnabled(), "HO has its own ratio");
        } finally {
            ThreadingUtil.runOnGUI(() -> options.getWindow().dispose());
            JUnitUtil.dispose(d);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
        // the options the Dispatcher reads when it is created: scale G
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        custom = Objects.requireNonNull(ScaleManager.getScale("CUSTOM"));
        customRatio = custom.getScaleRatio();
    }

    @AfterEach
    public void tearDown() throws PropertyVetoException {
        // the scales are shared by every test: put CUSTOM back as it was
        custom.setScaleRatio(customRatio);
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
