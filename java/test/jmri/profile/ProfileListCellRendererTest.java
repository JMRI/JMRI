package jmri.profile;

import javax.swing.JList;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood
 */
public class ProfileListCellRendererTest {
    
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }
    
    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
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
        Assert.assertNotNull(activeProfile); // this tests a non-null active profile
        String noProfileMessage = Bundle.getMessage("ProfileManagerDialog.profiles.toolTipText");
        String activeProfileMessage = Bundle.getMessage("ProfileTableModel.toolTip", activeProfile.getName(), activeProfile.getPath(), activeProfile.getId(), "");
        // null profile, selected index
        instance.getListCellRendererComponent(list, null, 0, false, false);
        Assert.assertEquals(noProfileMessage, list.getToolTipText());
        // null profile, no selected index
        instance.getListCellRendererComponent(list, null, -1, false, false);
        Assert.assertEquals(noProfileMessage, list.getToolTipText());
        // valid profile, selected index
        instance.getListCellRendererComponent(list, activeProfile, 0, false, false);
        Assert.assertEquals(activeProfileMessage, list.getToolTipText());
        // valid profile, no selected index
        instance.getListCellRendererComponent(list, activeProfile, -1, false, false);
        Assert.assertEquals(noProfileMessage, list.getToolTipText());
        // invalid profile, selected index
        instance.getListCellRendererComponent(list, ProfileManager.getDefault(), 0, false, false);
        Assert.assertEquals(noProfileMessage, list.getToolTipText());
        // invalid profile, no selected index
        instance.getListCellRendererComponent(list, ProfileManager.getDefault(), -1, false, false);
        Assert.assertEquals(noProfileMessage, list.getToolTipText());
    }
    
}
