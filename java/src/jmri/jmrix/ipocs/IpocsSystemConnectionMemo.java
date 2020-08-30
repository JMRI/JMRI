package jmri.jmrix.ipocs;

import jmri.TurnoutManager;

public class IpocsSystemConnectionMemo extends jmri.jmrix.DefaultSystemConnectionMemo implements jmri.jmrix.ConfiguringSystemConnectionMemo {
  private IpocsPortController portController;

  public IpocsSystemConnectionMemo() {
    super("P", "IPOCS");
    jmri.InstanceManager.store(this, IpocsSystemConnectionMemo.class);
  }

  @Override
  public void configureManagers() {
    jmri.InstanceManager.setTurnoutManager(getTurnoutManager());
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
