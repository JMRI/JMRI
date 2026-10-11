package jmri.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JList;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Randall Wood
 */
public class ProfileListCellRendererTest {
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
        JUnitUtil.resetProfileManager();
    }

    /**
     * Test of getListCellRendererComponent method, of class ProfileListCellRenderer.
     */
    @Test
    public void testGetListCellRendererComponent() {
        JList<Profile> list = new JList<>(new ProfileListModel());
        list.setToolTipText(null);
        ProfileListCellRenderer instance = new ProfileListCellRenderer();
        Profile activeProfile = ProfileManager.getDefault().getActiveProfile();
        assertNotNull(activeProfile); // this tests a non-null active profile
        String noProfileMessage = Bundle.getMessage("ProfileManagerDialog.profiles.toolTipText");
        String activeProfileMessage = Bundle.getMessage("ProfileTableModel.toolTip", activeProfile.getName(), activeProfile.getPath(), activeProfile.getId(), "");
        // null profile, selected index
        instance.getListCellRendererComponent(list, null, 0, false, false);
        assertEquals(noProfileMessage, list.getToolTipText());
        // null profile, no selected index
        instance.getListCellRendererComponent(list, null, -1, false, false);
        assertEquals(noProfileMessage, list.getToolTipText());
        // valid profile, selected index
        instance.getListCellRendererComponent(list, activeProfile, 0, false, false);
        assertEquals(activeProfileMessage, list.getToolTipText());
        // valid profile, no selected index
        instance.getListCellRendererComponent(list, activeProfile, -1, false, false);
        assertEquals(noProfileMessage, list.getToolTipText());
        // invalid profile, selected index
        instance.getListCellRendererComponent(list, ProfileManager.getDefault(), 0, false, false);
        assertEquals(noProfileMessage, list.getToolTipText());
        // invalid profile, no selected index
        instance.getListCellRendererComponent(list, ProfileManager.getDefault(), -1, false, false);
        assertEquals(noProfileMessage, list.getToolTipText());
    }
    
}
