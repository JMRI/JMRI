package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;
import jmri.Programmer;
import jmri.jmrit.progsupport.ProgModeSelector;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

public class CombinedLocoSelListPaneTest extends TestCase {

    public CombinedLocoSelListPaneTest(String s) {
        super(s);
    }

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

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CombinedLocoSelListPaneTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CombinedLocoSelListPaneTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
        JUnitUtil.initConfigureManager();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
