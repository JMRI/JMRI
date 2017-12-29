package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a Block Routing Table.
 * <P>
 * @author Kevin Dickerson Copyright (C) 2011
 */
@SuppressWarnings("serial")
@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED") //no Serializable support at present
public class LayoutBlockRouteTableAction extends AbstractAction {

    /**
     * Create an action with a specific title.
     * <P>
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

    //static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");
    private LayoutBlock layoutBlock = null;

    private LayoutBlockRouteTable table;
    //LayoutBlockNeighbourTable mn;
    private JmriJFrame frame = new JmriJFrame();

    private void createModel() {
        table = new LayoutBlockRouteTable(false, layoutBlock);
    }

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        frame = new JmriJFrame();
        frame.add(table);
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
