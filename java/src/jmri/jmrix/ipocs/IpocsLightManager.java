package jmri.jmrix.ipocs;

import jmri.Light;
import jmri.SystemConnectionMemo;
import jmri.managers.AbstractLightManager;

public class IpocsLightManager extends AbstractLightManager {

  public IpocsLightManager(SystemConnectionMemo memo) {
    super(memo);
    // TODO Auto-generated constructor stub
  }

  private IpocsPortController getPortController() {
    return ((IpocsSystemConnectionMemo)memo).getPortController();
  }

  @Override
  public boolean validSystemNameConfig(String systemName) {
    return false;
  }

  @Override
  protected Light createNewLight(String systemName, String userName) {
    return new IpocsLight(getPortController(), systemName, userName);
  }
  
}
