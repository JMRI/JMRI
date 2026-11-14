package jmri.jmrit.logix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import jmri.Scale;
import jmri.ScaleManager;
import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantPreferencesPanelTest extends PreferencesPanelTestBase<WarrantPreferencesPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        prefsPanel = new WarrantPreferencesPanel();
    }

    @Override
    @AfterEach
    public void tearDown() {
        prefsPanel = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testScalesAreJmrisListAndCustom() {
        JComboBox<?> box = scaleBox(prefsPanel);
        List<String> offered = new ArrayList<>();
        for (int i = 0; i < box.getItemCount() - 1; i++) {
            offered.add(box.getItemAt(i).toString());
        }
        List<String> expected = new ArrayList<>();
        for (Scale scale : ScaleManager.getScales()) {
            if (!"CUSTOM".equals(scale.getScaleName())) {
                expected.add(Bundle.getMessage("scaleItem", scale.getUserName(),
                        Float.toString((float) scale.getScaleRatio())));
            }
        }
        assertEquals(expected, offered, "JMRI's scales, without its CUSTOM one");
        assertTrue(offered.contains(Bundle.getMessage("scaleItem", "UK-OO", "76.2")), "OO offered");
        assertTrue(offered.contains(Bundle.getMessage("scaleItem", "HO", "87.1")), "HO offered");
        assertTrue(box.getItemAt(box.getItemCount() - 1) instanceof WarrantPreferencesPanel.CustomDialog,
                "the custom item last");
    }

    @Test
    public void testStoredRatioInTheListIsSelected() {
        WarrantPreferences.getDefault().setLayoutScale(76.2f);
        JComboBox<?> box = scaleBox(new WarrantPreferencesPanel());
        assertEquals(Bundle.getMessage("scaleItem", "UK-OO", "76.2"), String.valueOf(box.getSelectedItem()));
    }

    @Test
    public void testStoredRatioNotInTheListIsKeptAsCustom() {
        // 1:22.5, offered as G before the list was JMRI's, whose G is 1:32
        WarrantPreferences.getDefault().setLayoutScale(22.5f);
        JComboBox<?> box = scaleBox(new WarrantPreferencesPanel());
        assertTrue(box.getSelectedItem() instanceof WarrantPreferencesPanel.CustomDialog);
        assertEquals(Bundle.getMessage("scaleItem", Bundle.getMessage("custom"), "22.5"),
                String.valueOf(box.getSelectedItem()));
        assertEquals(22.5f, WarrantPreferences.getDefault().getLayoutScale(), "the setting unchanged");
    }

    private static JComboBox<?> scaleBox(Container c) {
        JComboBox<?> box = findScaleBox(c);
        assertNotNull(box, "the Layout Scale box");
        return box;
    }

    private static JComboBox<?> findScaleBox(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JComboBox && ((JComboBox<?>) child).getItemCount() > 0
                    && ((JComboBox<?>) child).getItemAt(0) instanceof WarrantPreferencesPanel.ScaleData) {
                return (JComboBox<?>) child;
            }
            if (child instanceof Container) {
                JComboBox<?> found = findScaleBox((Container) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // private static final Logger log = LoggerFactory.getLogger(WarrantPreferencesPanelTest.class);

}
