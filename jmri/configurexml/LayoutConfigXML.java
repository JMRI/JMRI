package jmri.configurexml;

import java.io.File;
import org.jdom.*;
import org.jdom.output.*;
import com.sun.java.util.collections.List;
import jmri.*;

/**
 * Provides the mechanisms for storing an entire layout configuration
 * to XML.  "Layout" refers to the hardware:  Specific communcation
 * systems, etc.
 * @see <A HREF="package-summary.html">Package summary for details of the overall structure</A>
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.3 $
 */
public class LayoutConfigXML extends jmri.jmrit.XmlFile {

    public LayoutConfigXML() {
    }

	public void writeFile(File file) {
		try {
			// This is taken in large part from "Java and XML" page 368

			// create root element
			Element root = new Element("layout-config");
			Document doc = new Document(root);
			doc.setDocType(new DocType("layout-config","layout-config.dtd"));

			// add top-level elements
			Element turnouts;  // will fill this with turnout info
			root.addContent(turnouts = new Element("turnouts"))
                .addContent(new Element("sensors"))
                .addContent(new Element("blocks"))
                .addContent(new Element("signals"));

            // get the turnouts and store
            TurnoutManager tm = InstanceManager.turnoutManagerInstance();
            if (tm!=null) {
                com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();

                while (iter.hasNext()) {
                    String sname = (String)iter.next();
                    String uname = tm.getBySystemName(sname).getUserName();
                    Element elem = new Element("turnout")
                            .addAttribute("systemName", sname);
                    if (uname!=null) elem.addAttribute("userName", uname);
                    log.debug("store turnout "+sname+":"+uname);
                    turnouts.addContent(elem);

                }
            }
			// write the result to selected file
			java.io.FileOutputStream o = new java.io.FileOutputStream(file);
			XMLOutputter fmt = new XMLOutputter();
			fmt.setNewlines(true);   // pretty printing
			fmt.setIndent(true);
			fmt.output(doc, o);
        }
		catch (Exception e) {
			log.error(e);
		}
	}

    public void readFile(File fi) {
        try {
            Element root = super.rootFromFile(fi);
            // get the turnouts
            Element turnouts = root.getChild("turnouts");
			List turnoutList = turnouts.getChildren("turnout");
			if (log.isDebugEnabled()) log.debug("Found "+turnoutList.size()+" turnouts");
            TurnoutManager tm = InstanceManager.turnoutManagerInstance();

			for (int i=0; i<turnoutList.size(); i++) {
				if ( ((Element)(turnoutList.get(i))).getAttribute("systemName") == null) {
					  if (log.isDebugEnabled()) log.debug("unexpected null in systemName "+((Element)(turnoutList.get(i)))+" "+((Element)(turnoutList.get(i))).getAttributes());
					  break;
				}
                String sysName = ((Element)(turnoutList.get(i))).getAttribute("systemName").getValue();
                String userName = null;
        		if ( ((Element)(turnoutList.get(i))).getAttribute("userName") != null)
                    userName = ((Element)(turnoutList.get(i))).getAttribute("userName").getValue();

                tm.newTurnout(sysName, userName);
            }

        }
        catch (org.jdom.JDOMException e) { log.error("Exception reading: "+e); }
        catch (java.io.IOException e) { log.error("Exception reading: "+e); }
    }

	static public String fileLocation = "layout"+File.separator;

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutConfigXML.class.getName());
}