package jmri.jmrix.ipocs.configurexml;

import static org.mockito.Mockito.mock;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class IpocsTurnoutManagerXmlTest {

  @Test
  public void testCtor() {
    Assert.assertNotNull("IpocsSensorManagerXml constructor", new IpocsTurnoutManagerXml());
  }

  @BeforeEach
  public void setUp() {
    jmri.util.JUnitUtil.setUp();
  }

  @AfterEach
  public void tearDown() {
    jmri.util.JUnitUtil.tearDown();
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
}
