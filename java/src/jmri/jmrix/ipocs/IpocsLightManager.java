package jmri.jmrix.ipocs;

import jmri.Light;
import jmri.SystemConnectionMemo;
import jmri.managers.AbstractLightManager;

import javax.annotation.Nonnull;

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
  public boolean validSystemNameConfig(@Nonnull String systemName) {
    return false;
  }

  @Override
  @Nonnull
  protected Light createNewLight(@Nonnull String systemName, String userName) throws IllegalArgumentException {
    return new IpocsLight(getPortController(), systemName, userName);
  }
  
}
