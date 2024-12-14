package jmri.jmrix.ipocs.configurexml;

import java.io.IOException;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.ipocs.IpocsConnectionConfig;
import jmri.jmrix.ipocs.IpocsPortController;
import jmri.jmrix.ipocs.IpocsSystemConnectionMemo;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsConnectionConfigXml extends AbstractConnectionConfigXml {
  private final static Logger log = LoggerFactory.getLogger(IpocsConnectionConfigXml.class);
  IpocsPortController portController;

  public IpocsConnectionConfigXml() {
    super();
  }

  @Override
  public Element store(Object o) {
    Element e = new Element("connection");
    getInstance((IpocsConnectionConfig)o);

    storeCommon(e, portController);
    e.setAttribute("port", String.valueOf(portController.getPort()));
    e.setAttribute("class", this.getClass().getName());

    extendElement(e);
    return e;
  }

  @Override
  public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
    getInstance();
    try {
      short port = (short)shared.getAttribute("port").getIntValue();
      portController.setPort(port);
    } catch (org.jdom2.DataConversionException ex) {
      log.warn("Could not parse port attribute: {}", shared.getAttribute("port"));
    } catch (NullPointerException ex) {
      log.error("No port attribute availableCould not parse port attribute", ex);
    }
    loadCommon(shared, perNode, portController);
    register();

    if (!portController.getDisabled()) {
      portController.configure();
      try {
        portController.connect();
      } catch (IOException ex) {
        log.error("Unable to start service: {}", ex.getMessage());
      }
    }
    return true;
  }

  @Override
  protected void getInstance() {
    portController = new IpocsPortController(new IpocsSystemConnectionMemo());
    portController.getSystemConnectionMemo().setPortController(portController);
  }

  protected void getInstance(IpocsConnectionConfig o) {
    portController = o.getAdapter();
  }

  @Override
  protected void register() {
    this.register(new IpocsConnectionConfig(portController));
  }

  @Override
  protected void dispose() {
    portController.dispose();
  }
}
