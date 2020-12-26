package jmri.jmrix.ipocs.configurexml;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jdom2.Element;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.ipocs.IpocsConnectionConfig;
import jmri.jmrix.ipocs.IpocsPortController;

public class IpocsConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractConnectionConfigXmlTestBase {

  private IpocsPortController portController;
  private IpocsConnectionConfig connConfig;

  @BeforeEach
  @Override
  public void setUp() {
    jmri.util.JUnitUtil.setUp();
    jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    xmlAdapter = new IpocsConnectionConfigXml();
    portController = mock(IpocsPortController.class);
    when(portController.getOptions()).thenReturn(new String[]{});
    when(portController.getDisabled()).thenReturn(true);
    connConfig = mock(IpocsConnectionConfig.class);
    when(connConfig.getAdapter()).thenReturn(portController);
    cc = connConfig;
  }

  @AfterEach
  @Override
  public void tearDown() {
    jmri.util.JUnitUtil.tearDown();
    xmlAdapter = null;
    cc = null;
  }

  @Test
  public void specificLoadTest() {
    assertThrows(NullPointerException.class, () -> xmlAdapter.load(null));
    assertThrows(NullPointerException.class, () -> xmlAdapter.load(new Element("connection")));
  }
}
