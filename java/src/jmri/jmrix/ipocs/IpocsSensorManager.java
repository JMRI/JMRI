package jmri.jmrix.ipocs;

import jmri.Sensor;
import jmri.SystemConnectionMemo;
import jmri.managers.AbstractSensorManager;

public class IpocsSensorManager extends AbstractSensorManager {

  public IpocsSensorManager(SystemConnectionMemo memo) {
    super(memo);
  }

  private IpocsPortController getPortController() {
    return ((IpocsSystemConnectionMemo)memo).getPortController();
  }

  @Override
  protected Sensor createNewSensor(String systemName, String userName) {
    return new IpocsSensor(getPortController(), systemName, userName);
  }
}
