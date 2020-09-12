package jmri.jmrix.ipocs;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class IpocsConnectionConfigTest {
  @Test
  public void constructorTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    assertNotNull(new IpocsConnectionConfig());
    assertNotNull(new IpocsConnectionConfig(portController));
  }

  @Test
  public void nameTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals("IPOCS Connection", cc.name());
  }

  @Test
  public void getInfoTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals("IPOCS", cc.getInfo());
  }

  @Test
  public void getAdapter() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals(portController, cc.getAdapter());
  }

  @Test
  public void checkInitDoneTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.checkInitDone();
  }

  @Test
  public void updateAdapterTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.updateAdapter();
  }

  @Test
  public void showAdvancedItemsTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.showAdvancedItems();
  }

  @Test
  public void getManufacturerTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals(IpocsConnectionTypeList.IPOCSMR, cc.getManufacturer());
  }

  @Test
  public void setManufacturerTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.setManufacturer("");
    jmri.util.JUnitAppender.suppressErrorMessage("Tried to change manufacturer to ");
  }

  @Test
  public void getConnectionNameTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    assertEquals("IPOCSMR", cc.getConnectionName());
  }

  @Test
  public void disabledTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    IpocsConnectionConfig cc = new IpocsConnectionConfig(portController);
    cc.setDisabled(true);
    assertEquals(true, cc.getDisabled());
  }

}
