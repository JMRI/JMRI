package jmri.jmrix.ipocs.configurexml;

import java.io.IOException;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.ipocs.IpocsConnectionConfig;
import jmri.jmrix.ipocs.IpocsPortController;

public class IpocsConnectionConfigXml extends AbstractConnectionConfigXml {
  private final static Logger log = LoggerFactory.getLogger(IpocsConnectionConfigXml.class);
  IpocsPortController portController;

  public IpocsConnectionConfigXml() {
    super();
  }

  @Override
  public Element store(Object o) {
    Element e = new Element("connection");
    log.error(o.getClass().getName());
    getInstance((IpocsConnectionConfig)o);

    storeCommon(e, portController);
    e.setAttribute("port", "10000");
    e.setAttribute("enableAutoConf", "true");
    e.setAttribute("class", this.getClass().getName());

    extendElement(e);
    return e;
  }

  @Override
  public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
    getInstance();
    try {
      // TODO do this
      //int port = Integer.parseInt(shared.getAttribute("port").getValue());
      //portController.setPort(port);
    } catch (NullPointerException ex) {
    }
    try {
      // TODO do this
      //boolean autoConf = shared.getAttribute("enableAutoConf").getValue().equals("true");
      //portController.setAutoConf(hostName);
    } catch (NullPointerException ex) {
    }
    loadCommon(shared, perNode, portController);
    register();

    if (!portController.getDisabled()) {
      portController.configure();
      try {
        portController.connect();
      } catch (IOException ex) {
        log.error("Unable to start service: {0}", ex.getMessage());
      }
    }
    return true;
  }

  @Override
  protected void getInstance() {
    portController = new IpocsPortController();
    portController.getSystemConnectionMemo().setPortController(portController);
  }

  protected void getInstance(IpocsConnectionConfig o) {
    portController = (IpocsPortController)o.getAdapter();
  }

  @Override
  protected void register() {
    this.register(new IpocsConnectionConfig(portController));
  }
}