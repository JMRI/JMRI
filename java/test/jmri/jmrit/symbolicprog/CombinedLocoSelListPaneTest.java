package jmri.jmrit.symbolicprog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JLabel;

import jmri.Programmer;
import jmri.jmrit.progsupport.ProgModeSelector;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class CombinedLocoSelListPaneTest {

    @Test
    public void testIsDecoderSelected() {
        ProgModeSelector sel = new ProgModeSelector() {
            private final Programmer programmer = new jmri.progdebugger.ProgDebugger();

            @Override
            public Programmer getProgrammer() {
                return programmer;
            }

            @Override
            public boolean isSelected() {
                return true;
            }

            @Override
            public void dispose() {
            }
        };

        JLabel val1 = new JLabel();
        // ensure a valid DecoderIndexFile
        jmri.jmrit.decoderdefn.DecoderIndexFile.resetInstance();
        CombinedLocoSelListPane combinedlocosellistpane = new CombinedLocoSelListPane(val1, sel);
        assertFalse( combinedlocosellistpane.isDecoderSelected(), "initial state");
        combinedlocosellistpane.mDecoderList.setSelectedIndex(1);
        assertTrue( combinedlocosellistpane.isDecoderSelected(), "after update");
    }

    @Test
    public void testSelectedDecoderType() {
        ProgModeSelector sel = new ProgModeSelector() {
            private final Programmer programmer = new jmri.progdebugger.ProgDebugger();

            @Override
            public Programmer getProgrammer() {
                return programmer;
            }

            @Override
            public boolean isSelected() {
                return true;
            }

            @Override
            public void dispose() {
            }
        };

        JLabel val1 = new JLabel();
        // ensure a valid DecoderIndexFile
        jmri.jmrit.decoderdefn.DecoderIndexFile.resetInstance();

        CombinedLocoSelListPane combinedlocosellistpane = new CombinedLocoSelListPane(val1, sel);
        combinedlocosellistpane.mDecoderList.setSelectedIndex(4);
        assertTrue( combinedlocosellistpane.isDecoderSelected(), "after update");
        String stringRet = combinedlocosellistpane.selectedDecoderType();
        assertEquals( "SUSI Output Mapping definitions (SUSI Output Mapping definitions)",
                stringRet, "selected item");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
