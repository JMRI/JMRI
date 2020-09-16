package jmri.jmrix.ipocs;

import jmri.SystemConnectionMemo;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsTurnoutManager extends AbstractTurnoutManager {

  public IpocsTurnoutManager(SystemConnectionMemo memo) {
    super(memo);
  }

  private IpocsPortController getPortController() {
    return ((IpocsSystemConnectionMemo)memo).getPortController();
  }

  @Override
  protected Turnout createNewTurnout(String systemName, String userName) {
    return new IpocsTurnout(getPortController(), systemName, userName);
  }
}
