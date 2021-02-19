package jmri.jmrix.ipocs.configurexml;

import org.jdom2.Element;

import jmri.managers.configurexml.AbstractLightManagerConfigXML;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsLightManagerXml extends AbstractLightManagerConfigXML {

  @Override
  public void setStoreElementClass(Element turnouts) {
    turnouts.setAttribute("class", IpocsTurnoutManagerXml.class.getName());
  }

  @Override
  public boolean load(Element shared, Element perNode) {
    return loadLights(shared);
  }
}
