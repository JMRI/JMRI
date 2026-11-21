package jmri.time.implementation.configurexml;

import jmri.InstanceManager;
import jmri.time.RateSetter;
import jmri.time.TimeProvider;
import jmri.time.TimeProviderManager;
import jmri.time.implementation.InternalDateTime;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InternalDateTimeXml.
 */
public class InternalDateTimeXmlTest {

    @Test
    public void testRateIsKeptOnLoad() throws Exception {
        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class).getCurrentTimeProvider();
        assertInstanceOf(InternalDateTime.class, tp);
        RateSetter rateSetter = (RateSetter) tp.getRate();

        rateSetter.setRate(4.0);
        InternalDateTimeXml xml = new InternalDateTimeXml();
        Element element = xml.store(tp);

        rateSetter.setRate(1.0);
        assertTrue(xml.load(element, null));
        assertEquals(4.0, tp.getRate().getRate(), 0.001);
    }

    @Test
    public void testBadRateFailsLoad() throws Exception {
        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class).getCurrentTimeProvider();
        InternalDateTimeXml xml = new InternalDateTimeXml();
        Element element = xml.store(tp);
        element.getChild("rate").setText("fast");

        assertFalse(xml.load(element, null));
        JUnitAppender.assertErrorMessage("Rate is not a double: fast");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
