package jmri.jmrix.ipocs;

import jmri.Sensor;
import jmri.SystemConnectionMemo;
import jmri.managers.AbstractSensorManager;

import javax.annotation.Nonnull;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsSensorManager extends AbstractSensorManager {

  public IpocsSensorManager(SystemConnectionMemo memo) {
    super(memo);
  }

  private IpocsPortController getPortController() {
    return ((IpocsSystemConnectionMemo)memo).getPortController();
  }

  @Override
  protected Sensor createNewSensor(@Nonnull String systemName, String userName) {
    return new IpocsSensor(getPortController(), systemName, userName);
  }
}
