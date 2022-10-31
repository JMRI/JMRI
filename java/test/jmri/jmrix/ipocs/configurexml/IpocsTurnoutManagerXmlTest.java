package jmri.jmrix.ipocs.configurexml;

import static org.mockito.Mockito.mock;

import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.Assert;
import org.junit.jupiter.api.*;

public class IpocsTurnoutManagerXmlTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("IpocsSensorManagerXml constructor", new IpocsTurnoutManagerXml());
    }

    @Test
    public void loadTest() {
        Element element = mock(Element.class);
        new IpocsTurnoutManagerXml().load(element, null);
    }

    @Test
    public void setStoreelementClassTest() {
        Element element = mock(Element.class);
        new IpocsTurnoutManagerXml().setStoreElementClass(element);
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
