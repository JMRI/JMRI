package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Steve Young (C) 2021
 */
public class ReporterTableDataModelTest extends AbstractBeanTableDataModelBase<Reporter> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }
    
    @Override
    public int getModelColumnCount(){
        return 6;
    }
    
    @Test
    @Override
    public void testGetBaseColumnNames() {
        assertEquals("Column0 - Bean toString",Bundle.getMessage("ColumnSystemName"), t.getColumnName(0));
        assertEquals("Column1 - UserName",Bundle.getMessage("ColumnUserName"), t.getColumnName(1));
        assertEquals("Column2 - Report",Bundle.getMessage("LabelReport"), t.getColumnName(2));
        assertEquals("Column3 - User Comment",Bundle.getMessage("ColumnComment"), t.getColumnName(3));
        assertEquals("Column4 - Delete button","", t.getColumnName(4));
        
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        t = new ReporterTableDataModel(InstanceManager.getDefault(ReporterManager.class));
    }

    @Test
    public void testGetValueNonCollectingReporter() {
        NonCollectingReporter r = new NonCollectingReporter("IR99");
        InstanceManager.getDefault(ReporterManager.class).register(r);

        // Initially no report
        Assert.assertNull(t.getValue("IR99"));

        // Regular string report
        r.setReport("Loco 123");
        Assert.assertEquals("Loco 123", t.getValue("IR99"));

        // Reportable object
        jmri.Reportable reportable = new jmri.Reportable() {
            @Override
            public String toReportString() {
                return "Address 123 East";
            }
        };
        r.setReport(reportable);
        Assert.assertEquals("Address 123 East", t.getValue("IR99"));
    }

    @Test
    public void testGetValueCollectingReporter() {
        ReporterManager mgr = InstanceManager.getDefault(ReporterManager.class);
        Reporter r = mgr.provideReporter("IR2");
        Assert.assertTrue(r instanceof jmri.jmrix.internal.TrackReporter);
        jmri.jmrix.internal.TrackReporter tr = (jmri.jmrix.internal.TrackReporter) r;

        // Initially empty collection
        Assert.assertNull(t.getValue("IR2"));

        // Single object
        tr.pushEast("Loco 100");
        Assert.assertEquals("Loco 100", t.getValue("IR2"));

        // Multiple objects concatenated with plus signs
        tr.pushEast("Loco 200");
        Assert.assertEquals("Loco 200 + Loco 100", t.getValue("IR2"));

        tr.pushEast("Loco 300");
        Assert.assertEquals("Loco 300 + Loco 200 + Loco 100", t.getValue("IR2"));
    }

    @Test
    public void testGetValueCollectingReporterWithReportable() {
        ReporterManager mgr = InstanceManager.getDefault(ReporterManager.class);
        Reporter r = mgr.provideReporter("IR3");
        Assert.assertTrue(r instanceof jmri.jmrix.internal.TrackReporter);
        jmri.jmrix.internal.TrackReporter tr = (jmri.jmrix.internal.TrackReporter) r;

        jmri.Reportable loco1 = new jmri.Reportable() {
            @Override
            public String toReportString() {
                return "Address 1001(L) East";
            }
        };
        jmri.Reportable loco2 = new jmri.Reportable() {
            @Override
            public String toReportString() {
                return "Address 2002(L) West";
            }
        };

        tr.pushEast(loco1);
        tr.pushEast(loco2);

        Assert.assertEquals("Address 2002(L) West + Address 1001(L) East", t.getValue("IR3"));
    }

    @Test
    public void testGetValueNullReporter() {
        Assert.assertEquals("", t.getValue("NON_EXISTENT_REPORTER"));
    }

    private static class NonCollectingReporter extends jmri.implementation.AbstractReporter {
        private int state = 0;
        NonCollectingReporter(String sysName) {
            super(sysName);
        }
        @Override
        public int getState() {
            return state;
        }
        @Override
        public void setState(int s) {
            state = s;
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (t!=null){
            t.dispose();
        }
        t = null;
        JUnitUtil.tearDown();
    }
    
}
