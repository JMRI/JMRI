package jmri.jmrix.ipocs.configurexml;

import static org.mockito.Mockito.mock;

import org.jdom2.Element;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class IpocsLightManagerXmlTest {

  @Test
  public void ConstructorTest() {
    Assert.assertNotNull("IpocsLightManagerXml constructor", new IpocsLightManagerXml());
  }

  @BeforeEach
  public void setUp() {
    jmri.util.JUnitUtil.setUp();
    jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
  }

  @AfterEach
  public void tearDown() {
    jmri.util.JUnitUtil.tearDown();
  }
  
  @Test
  public void loadTest() {
    Element element = mock(Element.class);
    new IpocsLightManagerXml().load(element, null);
   }

   @Test
   public void setStoreelementClassTest() {
    Element element = mock(Element.class);
    new IpocsLightManagerXml().setStoreElementClass(element);
   }
}
