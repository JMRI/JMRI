package jmri.jmrix.ipocs;

import jmri.jmrix.AbstractConnectionConfig;
import jmri.jmrix.PortAdapter;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpocsConnectionConfig extends AbstractConnectionConfig {
  private final static Logger log = LoggerFactory.getLogger(IpocsConnectionConfig.class);
  IpocsPortController portController;

  public IpocsConnectionConfig() {
    super();
  }

  public IpocsConnectionConfig(IpocsPortController portController) {
    super();
    this.portController = portController;
  }

  @Override
  public String name() {
    return "IPOCS Connection";
  }

  @Override
  protected void setInstance() {
    if (portController == null) {
      portController = new IpocsPortController();
      portController.configure();
    }
  }

  @Override
  public String getInfo() {
    return "IPOCS";
  }

  @Override
  public PortAdapter getAdapter() {
    return portController;
  }

  @Override
  protected void checkInitDone() {
  }

  @Override
  public void updateAdapter() {
  }

  @Override
  public void loadDetails(JPanel details) {
    _details = details;
    setInstance();
  }

  @Override
  protected void showAdvancedItems() {
  }

  @Override
  public String getManufacturer() {
    return IpocsConnectionTypeList.IPOCSMR;
  }

  @Override
  public void setManufacturer(String manufacturer) {
    // Manufacturer cannot be changed
    log.error("Tried to change manufacturer to {}", manufacturer);
  }

  @Override
  public String getConnectionName() {
    return "IPOCSMR";
  }

  private boolean isDisabled = false;

  @Override
  public boolean getDisabled() {
    return isDisabled;
  }

  @Override
  public void setDisabled(boolean disable) {
    this.isDisabled = disable;
  }
}
