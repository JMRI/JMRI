package jmri.jmrit.operations.trains.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.configurexml.LocoIconXml;
import jmri.jmrit.operations.trains.TrainIcon;
import jmri.jmrit.roster.RosterEntry;
import org.jdom.Element;

/**
 * Handle configuration for display.TrainIcon objects.
 *
 * @author mstevetodd Copyright: Copyright (c) 2012
 * @version $Revision: $
 */
public class TrainIconXml extends LocoIconXml {

	public TrainIconXml() {
	}

	/**
	 * Default implementation for storing the contents of a
	 * TrainIcon
	 * @param o Object to store, of type TrainIcon
	 * @return Element containing the complete info
	 */
	public Element store(Object o) {

		TrainIcon p = (TrainIcon)o;
		if (!p.isActive()) return null;  // if flagged as inactive, don't store

		Element element = new Element("trainicon");
		element.setAttribute("train", p.getTrain().getId());
		element.setAttribute("trainName", p.getTrain().getName());
		storeCommonAttributes(p, element);

		// include contents
		if (p.getUnRotatedText()!=null) element.setAttribute("text", p.getUnRotatedText());
		storeTextInfo(p, element);
		element.setAttribute("icon", "yes");
		element.setAttribute("dockX", ""+p.getDockX());
		element.setAttribute("dockY", ""+p.getDockY());
//		element.setAttribute("iconId", p.getIconId());
		element.addContent(storeIcon("icon", (NamedIcon)p.getIcon()));
		RosterEntry entry = p.getRosterEntry();
		if (entry != null) element.setAttribute("rosterentry", entry.getId());

		element.setAttribute("class", this.getClass().getName());

		return element;
	}


	public boolean load(Element element) {
		log.error("Invalid method called");
		return false;
	}

	/**
	 * Create a PositionableLabel, then add to a target JLayeredPane
	 * @param element Top level Element to unpack.
	 * @param o  an Editor as an Object
	 */
	public void load(Element element, Object o) {
		log.warn("loading of TrainIcon not implemented");
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainIconXml.class.getName());
}