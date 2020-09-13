package jmri.jmrix.ipocs;

import jmri.LightManager;
import jmri.SensorManager;
import jmri.TurnoutManager;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsSystemConnectionMemo extends jmri.jmrix.DefaultSystemConnectionMemo implements jmri.jmrix.ConfiguringSystemConnectionMemo {
  private IpocsPortController portController;

  public IpocsSystemConnectionMemo() {
    super("P", "IPOCS");
    jmri.InstanceManager.store(this, IpocsSystemConnectionMemo.class);
  }

  @Override
  public void configureManagers() {
    jmri.InstanceManager.setTurnoutManager(getTurnoutManager());
    jmri.InstanceManager.setLightManager(getLightManager());
    jmri.InstanceManager.setSensorManager(getSensorManager());
    register();
  }

  @Override
  public <B extends jmri.NamedBean> java.util.Comparator<B> getNamedBeanComparator(final Class<B> type) {
    return new jmri.util.NamedBeanComparator<>();
  }

  @Override
  protected java.util.ResourceBundle getActionModelResourceBundle() {
    return null;
  }

  public IpocsSensorManager getSensorManager() {
    if (getDisabled()) {
      return null;
    }
    return (IpocsSensorManager) classObjectMap.computeIfAbsent(SensorManager.class, (c) -> {
      IpocsSensorManager t = new IpocsSensorManager(this);
      return t;
    });
  }

  public IpocsLightManager getLightManager() {
    if (getDisabled()) {
      return null;
    }
    return (IpocsLightManager) classObjectMap.computeIfAbsent(LightManager.class, (c) -> {
      IpocsLightManager t = new IpocsLightManager(this);
      return t;
    });
  }

  public IpocsTurnoutManager getTurnoutManager() {
    if (getDisabled()) {
      return null;
    }
    return (IpocsTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class, (c) -> {
      IpocsTurnoutManager t = new IpocsTurnoutManager(this);
      return t;
    });
  }
  
  public IpocsPortController getPortController() {
      return portController;
  }

  public void setPortController(IpocsPortController portController) {
      this.portController = portController;
  }
}
