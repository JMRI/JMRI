package jmri.jmrit.dispatcher;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

public class TrainInfoFileSummaryTest {

    @Test
    public void createTest() {
        TrainInfoFileSummary t = new TrainInfoFileSummary("FileName",
                "TrainName","TransitName","StartBlockName", "EndBlockName","DCCAddress");
        Assert.assertNotNull("exists",t);
        Assert.assertEquals("FileName",t.getFileName());
        Assert.assertEquals("TrainName",t.getTrainName());
        Assert.assertEquals("TransitName",t.getTransitName());
        Assert.assertEquals("StartBlockName",t.getStartBlockName());
        Assert.assertEquals("EndBlockName",t.getEndBlockName());
        Assert.assertEquals("DCCAddress",t.getDccAddress());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainInfoTest.class);

}
