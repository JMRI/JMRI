package jmri.jmrix.loconet.cmdstnconfig;

import java.util.Iterator;
import jmri.jmrit.XmlFile;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen
 */
public class XmlConfig extends XmlFile {

    public XmlConfig() {
    }

    static void dumpNodes(Element root) {
        int depth = 0;
        dumpNode(root, depth);
    }

    @SuppressWarnings("unchecked")
    static void dumpNode(Element node, int depth) {
        int leader;
        for (leader = 0; leader < depth; leader++) {
            System.out.print('\t');
        }

        System.out.print(node.getName());
        Iterator<Attribute> attributes = node.getAttributes().iterator();
        Attribute attribute;
        while (attributes.hasNext()) {
            attribute = attributes.next();
            System.out.print(" " + attribute.getName() + " = " + attribute.getValue());
        }
        System.out.println();
        Iterator<Element> children = node.getChildren().iterator();
        depth++;
        while (children.hasNext()) {
            dumpNode(children.next(), depth);
        }
    }

  // initialize logging
    //private final static Logger log = LoggerFactory.getLogger(XmlConfig.class);

}
