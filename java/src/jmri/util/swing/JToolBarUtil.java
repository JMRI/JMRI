package jmri.util.swing;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.jdom2.Element;

/**
 * Common utility methods for working with JToolBars.
 * <p>
 * Chief among these is the loadToolBar method, for creating a JToolBar from an
 * XML definition
 * <p>
 * Only parses top level of XML file, since ToolBars have only level.
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @since 2.9.4
 */
public class JToolBarUtil extends GuiUtilBase {

    static public JToolBar loadToolBar(String name) {
        return loadToolBar(name, null, null);  // tool bar without window or context
    }

    static public JToolBar loadToolBar(String name, WindowInterface wi, Object context) {
        Element root = rootFromName(name);

        JToolBar retval = new JToolBar(root.getChild("name").getText());

        for (Object item : root.getChildren("node")) {
            Action act = actionFromNode((Element) item, wi, context);
            if (act == null) {
                continue;
            }
            if (act.getValue(javax.swing.Action.SMALL_ICON) != null) {
                // icon present, add explicitly
                JButton b = new JButton((javax.swing.Icon) act.getValue(javax.swing.Action.SMALL_ICON));
                b.setAction(act);
                retval.add(b);
            } else {
                retval.add(new JButton(act));
            }
        }
        return retval;

    }
}
