package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.display.layoutEditor.LayoutBlock;

/**
 * Swing action to create and register a Block Routing Table.
 * <P>
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class LayoutBlockRouteTableAction extends AbstractAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     */
    public LayoutBlockRouteTableAction(String s, LayoutBlock layoutBlock) {
        super(s);
        lBlock = layoutBlock;
    }

    //static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    private LayoutBlock lBlock = null;

    LayoutBlockRouteTable m;
    //LayoutBlockNeighbourTable mn;
    jmri.util.JmriJFrame f;

    void createModel() {

        m = new LayoutBlockRouteTable(false, lBlock);

    }

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new jmri.util.JmriJFrame();
        f.add(m);
        setTitle();
        f.pack();
        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }

    void setTitle() {
        if (lBlock != null) {
            f.setTitle(Bundle.getMessage("BlockRoutingTableTitle") + " " + lBlock.getDisplayName());
        } else {
            f.setTitle(Bundle.getMessage("BlockRoutingTableTitleShort"));
        }
    }

    String helpTarget() {
        return "package.jmri.jmrit.display.layoutEditor";
    }

}
