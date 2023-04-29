package jmri.jmrit.beantable.signalmast;

import jmri.*;
import jmri.implementation.VirtualSignalMast;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Steve Young Copyright(C) 2023
 */
public class SignalMastLogicTableDataModelTest extends jmri.jmrit.beantable.AbstractBeanTableDataModelBase<SignalMastLogic> {

    @Override
    protected int getModelColumnCount() {
        return 9;
    }

    @Override
    protected SignalMastLogic createBean(){
        VirtualSignalMast sm1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "mast 1");
        sm1.setComment("comment source");
        VirtualSignalMast sm2 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)", "mast 2");
        sm2.setComment("comment destination");
        // provide a signal mast logic:
        SignalMastLogic sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).newSignalMastLogic(sm1);
        Assertions.assertNotNull(sml);
        sml.setDestinationMast(sm2);
        sml.allowAutoMaticSignalMastGeneration(false, sm2);
        return sml;
    }

    @Disabled
    @Override
    public void testNBFromUserSystemNames() {}

    @Disabled
    @Override
    public void testGetBaseColumnNames() {}

    @Disabled
    @Override
    public void testGetBaseColumnClasses() {}
    
    @Test
    public void testToolTips() {
    
        createBean();
        Assertions.assertEquals(1, t.getRowCount());

        Assertions.assertEquals("<html>comment source</html>", t.getCellToolTip(null, 0, SignalMastLogicTableDataModel.SOURCECOL));
        Assertions.assertEquals(null, t.getCellToolTip(null, 0, SignalMastLogicTableDataModel.SOURCEAPPCOL));
        Assertions.assertEquals("<html>comment destination</html>", t.getCellToolTip(null, 0, SignalMastLogicTableDataModel.DESTCOL));
        Assertions.assertEquals(null, t.getCellToolTip(null, 0, SignalMastLogicTableDataModel.DESTAPPCOL));
        Assertions.assertEquals(null, t.getCellToolTip(null, 0, SignalMastLogicTableDataModel.COMCOL));
        Assertions.assertEquals(null, t.getCellToolTip(null, 0, SignalMastLogicTableDataModel.DELCOL));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        t = new SignalMastLogicTableDataModel();
    }

    @Override
    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(t);
        t.dispose();

        JUnitUtil.tearDown();
    }

}
