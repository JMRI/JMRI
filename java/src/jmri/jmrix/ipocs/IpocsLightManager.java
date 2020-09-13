package jmri.jmrix.ipocs;

import jmri.Light;
import jmri.SystemConnectionMemo;
import jmri.managers.AbstractLightManager;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsLightManager extends AbstractLightManager {

  public IpocsLightManager(SystemConnectionMemo memo) {
    super(memo);
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
