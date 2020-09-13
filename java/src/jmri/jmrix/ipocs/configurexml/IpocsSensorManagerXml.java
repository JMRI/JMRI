package jmri.jmrix.ipocs.configurexml;

import org.jdom2.Element;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.managers.configurexml.AbstractSensorManagerConfigXML;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsSensorManagerXml extends AbstractSensorManagerConfigXML {

  @Override
  public void setStoreElementClass(Element turnouts) {
    turnouts.setAttribute("class", IpocsTurnoutManagerXml.class.getName());
  }

  @Override
  public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
    return loadSensors(shared);
  }
}
