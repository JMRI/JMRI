package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests that {@link DispatcherFrame#checkBlocksNotInAllocatedSection} works
 * out the Blocks to check, including level crossings and crossovers on the
 * panels, once per call and not once for every allocated Section.
 */
@DisabledIfHeadless
public class DispatcherFrameBlockCheckTest {

    private LayoutEditor le;
    private DispatcherFrame d;

    private static Section section(String name) {
        Block b = new Block("IB" + name, name);
        List<Block> blocks = new ArrayList<>();
        blocks.add(b);
        Section s = mock(Section.class);
        when(s.getDisplayName()).thenReturn(name);
        when(s.getBlockList()).thenAnswer(invocation -> new ArrayList<>(blocks));
        return s;
    }

    @Test
    public void testBlocksWorkedOutOnce() {
        for (int i = 0; i < 10; i++) {
            AllocatedSection as = mock(AllocatedSection.class);
            Section allocated = section("A" + i);
            when(as.getSection()).thenReturn(allocated);
            d.getAllocatedSectionsList().add(as);
        }
        Section s = section("S");

        assertNull(d.checkBlocksNotInAllocatedSection(s, null), "no conflict");

        long lookups = mockingDetails(s).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("getBlockList")).count();
        // once for the Section, once each for level crossings and crossovers on the panel
        assertTrue(lookups <= 3, "Blocks of the Section looked up " + lookups + " times");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        le = new LayoutEditor("Block Check Test");
        d = InstanceManager.getDefault(DispatcherFrame.class);
    }

    @AfterEach
    public void tearDown() {
        if (d != null) {
            d.getAllocatedSectionsList().clear();
            JUnitUtil.dispose(d);
        }
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
