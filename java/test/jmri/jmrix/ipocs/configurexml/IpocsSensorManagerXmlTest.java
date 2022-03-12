package jmri.jmrix.ipocs.configurexml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.Assert;

import org.junit.jupiter.api.*;

public class IpocsSensorManagerXmlTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("IpocsSensorManagerXml constructor", new IpocsSensorManagerXml());
    }

    @Test
    public void loadTest() {
        Element element = mock(Element.class);
        assertDoesNotThrow(() -> new IpocsSensorManagerXml().load(element, null));
    }

    @Test
    public void setStoreelementClassTest() {
        Element element = mock(Element.class);
        new IpocsSensorManagerXml().setStoreElementClass(element);
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
