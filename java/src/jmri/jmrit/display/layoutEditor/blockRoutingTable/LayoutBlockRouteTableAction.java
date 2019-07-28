package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a Block Routing Table.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class LayoutBlockRouteTableAction extends AbstractAction {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     * <p>
     * @param name the action title
     * @param layoutBlock the layout block
     */
    public LayoutBlockRouteTableAction(String name, LayoutBlock layoutBlock) {
        super(name);
        this.layoutBlock = layoutBlock;
    }

    private LayoutBlock layoutBlock = null;

    private LayoutBlockRouteTable lbrTable;
    private JmriJFrame frame = null;

    private void createModel() {
        lbrTable = new LayoutBlockRouteTable(false, layoutBlock);
    }

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        frame = new JmriJFrame();
        frame.add(lbrTable);
        setTitle();
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }

    void setTitle() {
        if (layoutBlock != null) {
            frame.setTitle(Bundle.getMessage("BlockRoutingTableTitle") + " " + layoutBlock.getDisplayName());
        } else {
            frame.setTitle(Bundle.getMessage("BlockRoutingTableTitleShort"));
        }
    }

    String helpTarget() {
        return "package.jmri.jmrit.display.layoutEditor";
    }

}
