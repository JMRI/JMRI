package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;
import jmri.Programmer;
import jmri.jmrit.progsupport.ProgModeSelector;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CombinedLocoSelListPaneTest {

    @Test
    public void testIsDecoderSelected() {
        ProgModeSelector sel = new ProgModeSelector() {
            Programmer programmer = new jmri.progdebugger.ProgDebugger();

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
        Assert.assertEquals("initial state", false, combinedlocosellistpane.isDecoderSelected());
        combinedlocosellistpane.mDecoderList.setSelectedIndex(1);
        Assert.assertEquals("after update", true, combinedlocosellistpane.isDecoderSelected());
    }

    @Test
    public void testSelectedDecoderType() {
        ProgModeSelector sel = new ProgModeSelector() {
            Programmer programmer = new jmri.progdebugger.ProgDebugger();

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
        Assert.assertEquals("after update", true, combinedlocosellistpane.isDecoderSelected());
        String stringRet = combinedlocosellistpane.selectedDecoderType();
        Assert.assertEquals("selected item", "NMRA standard register definitions (NMRA standard register definitions)",
                stringRet);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
