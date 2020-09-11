package jmri.jmrix.ipocs.configurexml;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.ipocs.IpocsConnectionConfig;
import jmri.jmrix.ipocs.IpocsPortController;

public class IpocsConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractConnectionConfigXmlTestBase {

  private IpocsPortController portController;

  @BeforeEach
  @Override
  public void setUp() {
    jmri.util.JUnitUtil.setUp();
    jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    xmlAdapter = new IpocsConnectionConfigXml();
    portController = mock(IpocsPortController.class);
    when(portController.getOptions()).thenReturn(new String[] {});
    cc = new IpocsConnectionConfig(portController);
    ((IpocsConnectionConfigXml)xmlAdapter).getInstance((IpocsConnectionConfig)cc);
  }

  @AfterEach
  @Override
  public void tearDown() {
    jmri.util.JUnitUtil.tearDown();
    xmlAdapter = null;
    cc = null;
  }
}
