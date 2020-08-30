package jmri.jmrix.ipocs.configurexml;

import org.jdom2.Element;

import jmri.managers.configurexml.AbstractTurnoutManagerConfigXML;

public class IpocsTurnoutManagerXml extends AbstractTurnoutManagerConfigXML {

  @Override
  public void setStoreElementClass(Element turnouts) {
    turnouts.setAttribute("class", IpocsTurnoutManagerXml.class.getName());
  }

  @Override
  public boolean load(Element shared, Element perNode) {
    return loadTurnouts(shared, perNode);
  }
}
