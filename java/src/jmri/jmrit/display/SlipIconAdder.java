package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a simple editor for creating a Single or Double Slip Icon object.
 * Allows drops from icons dragged from a Catalog preview pane. Also implements
 * dragging a row from the turnout table to be dropped on a turnout label
 * <p>
 * To work right, the SlipTurnoutIcon needs to have all images the same size.
 * Based upon MultiSensorIconAdder by Bob Jacobsen {@literal &} Pete Cressman
 *
 * @author Bob Jacobsen Copyright (c) 2007
 * @author Kevin Dickerson Copyright (c) 2010
 *
 */
public class SlipIconAdder extends IconAdder {

    HashMap<String, NamedBeanHandle<Turnout>> _turnoutMap = new HashMap<String, NamedBeanHandle<Turnout>>();
    int _lastIndex = 0;

    public static final String NamedBeanFlavorMime = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=jmri.NamedBean";

    public SlipIconAdder() {
        super();
    }

    public SlipIconAdder(String type) {
        super(type);
    }

    int doubleSlip = 0x00;

    public void setTurnoutType(int dblSlip) {
        doubleSlip = dblSlip;
        doubleSlipButton.setSelected(false);
        singleSlipButton.setSelected(false);
        threeWayButton.setSelected(false);
        scissorButton.setSelected(false);
        switch (dblSlip) {
            case 0x00:
                doubleSlipButton.setSelected(true);
                break;
            case 0x02:
                singleSlipButton.setSelected(true);
                break;
            case 0x04:
                threeWayButton.setSelected(true);
                break;
            case 0x08:
                scissorButton.setSelected(true);
                break;
            default:
                log.warn("Unhandled dbslip code: {}", dblSlip);
                break;
        }
    }

    public int getTurnoutType() {
        return doubleSlip;
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        _turnoutMap = new HashMap<String, NamedBeanHandle<Turnout>>();
        _lastIndex = 0;
        super.reset();
    }

    JRadioButton doubleSlipButton = new JRadioButton(Bundle.getMessage("DoubleSlip"));
    JRadioButton singleSlipButton = new JRadioButton(Bundle.getMessage("SingleSlip"));
    JRadioButton threeWayButton = new JRadioButton(Bundle.getMessage("ThreeWay"));
    JRadioButton scissorButton = new JRadioButton(Bundle.getMessage("Scissor"));

    JRadioButton singleDirection = new JRadioButton(Bundle.getMessage("SingleSlipRoute"));
    JRadioButton lowerWestToLowerEastButton = new JRadioButton(Bundle.getMessage("LowerWestToLowerEast"));
    JRadioButton upperWestToUpperEastButton = new JRadioButton(Bundle.getMessage("UpperWestToUpperEast"));

    /**
     * {@inheritDoc} First look for a table selection to set the sensor. If not,
     * then look to change the icon image (super).
     */
    @Override
    public void makeIconPanel(boolean useDefaults) {
        if (_iconPanel != null) {
            this.remove(_iconPanel);
        }
        //super.makeIconPanel();
        Dimension dim = null;
        _iconPanel = new JPanel();
        _iconPanel.setLayout(new BoxLayout(_iconPanel, BoxLayout.Y_AXIS));

        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(doubleSlipButton);
        typeGroup.add(singleSlipButton);
        typeGroup.add(threeWayButton);
        typeGroup.add(scissorButton);
        JPanel _typePanel = new JPanel();
        _typePanel.add(doubleSlipButton);
        _typePanel.add(singleSlipButton);
        _typePanel.add(threeWayButton);
        _typePanel.add(scissorButton);
        _iconPanel.add(_typePanel);
        doubleSlipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                slipUpdate(0x00);
            }
        });
        singleSlipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                slipUpdate(0x02);
            }
        });
        threeWayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                slipUpdate(0x04);
            }
        });
        scissorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                slipUpdate(0x08);
            }
        });

        if (lowerWestToLowerEastButton.getActionListeners().length > 0) {
            lowerWestToLowerEastButton.removeActionListener(lowerWestToLowerEastButton.getActionListeners()[0]);
        }
        if (upperWestToUpperEastButton.getActionListeners().length > 0) {
            upperWestToUpperEastButton.removeActionListener(upperWestToUpperEastButton.getActionListeners()[0]);
        }
        if (getTurnoutType() == 0x02) {
            ButtonGroup group = new ButtonGroup();
            group.add(lowerWestToLowerEastButton);
            group.add(upperWestToUpperEastButton);
            lowerWestToLowerEastButton.setText(Bundle.getMessage("LowerWestToLowerEast"));
            upperWestToUpperEastButton.setText(Bundle.getMessage("UpperWestToUpperEast"));
            JPanel _buttonSlipPanel = new JPanel();
            _buttonSlipPanel.add(lowerWestToLowerEastButton);
            _buttonSlipPanel.add(upperWestToUpperEastButton);
            _iconPanel.add(_buttonSlipPanel);
            lowerWestToLowerEastButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    updateSingleSlipRoute(false);
                }
            });
            upperWestToUpperEastButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    updateSingleSlipRoute(true);
                }
            });
        } else if (getTurnoutType() == 0x04) {
            ButtonGroup group = new ButtonGroup();
            lowerWestToLowerEastButton.setText(Bundle.getMessage("ToLower"));
            upperWestToUpperEastButton.setText(Bundle.getMessage("ToUpper"));
            group.add(lowerWestToLowerEastButton);
            group.add(upperWestToUpperEastButton);
            JPanel _buttonSlipPanel = new JPanel();
            _buttonSlipPanel.add(lowerWestToLowerEastButton);
            _buttonSlipPanel.add(upperWestToUpperEastButton);
            _iconPanel.add(_buttonSlipPanel);
        } else if (getTurnoutType() == 0x08) {
            ButtonGroup group = new ButtonGroup();
            lowerWestToLowerEastButton.setText("4 Turnouts");
            upperWestToUpperEastButton.setText("2 Turnouts");
            group.add(lowerWestToLowerEastButton);
            group.add(upperWestToUpperEastButton);
            JPanel _buttonSlipPanel = new JPanel();
            _buttonSlipPanel.add(lowerWestToLowerEastButton);
            _buttonSlipPanel.add(upperWestToUpperEastButton);
            _iconPanel.add(_buttonSlipPanel);
            lowerWestToLowerEastButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    changeNumScissorTurnouts();
                }
            });
            upperWestToUpperEastButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    changeNumScissorTurnouts();
                }
            });
        }

        JPanel rowPanel = null;
        int cnt = 0;
        int numTurnoutPanels = 2;
        if ((doubleSlip == 0x08) && (lowerWestToLowerEastButton.isSelected())) {
            numTurnoutPanels = 4;
        }
        for (int i = 0; i < numTurnoutPanels; i++) {
            if (rowPanel == null) {
                rowPanel = new JPanel();
                rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
                rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            }

            JPanel p3 = new DropPanel();
            p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
            JPanel p4 = new JPanel();
            String label;
            String key;
            NamedBeanHandle<Turnout> turnout;
            if (i == 0) {
                if (doubleSlip == 0x04) {
                    label = Bundle.getMessage("FirstTurnout");
                } else if (doubleSlip == 0x08) {
                    if (lowerWestToLowerEastButton.isSelected()) {
                        label = Bundle.getMessage("UpperWestTurnout");
                    } else {
                        label = Bundle.getMessage("RHCrossing");
                    }
                } else {
                    label = Bundle.getMessage("WestTurnout");
                }
                key = "west";
            } else if (i == 1) {
                key = "east";
                if (doubleSlip == 0x04) {
                    label = Bundle.getMessage("SecondTurnout");
                } else if (doubleSlip == 0x08) {
                    if (lowerWestToLowerEastButton.isSelected()) {
                        label = Bundle.getMessage("UpperEastTurnout");
                    } else {
                        label = Bundle.getMessage("LHCrossing");
                    }
                } else {
                    label = Bundle.getMessage("EastTurnout");
                }
            } else if (i == 2) {
                key = "lowerwest";
                label = Bundle.getMessage("LowerWestTurnout");
            } else {
                key = "lowereast";
                label = Bundle.getMessage("LowerEastTurnout");
            }

            turnout = _turnoutMap.get(key);
            JLabel k = new JLabel(key);
            k.setName(key);
            k.setVisible(false);
            p3.add(k);
            p4.add(new JLabel(label));
            p3.add(p4);
            p4 = new JPanel();
            String name = Bundle.getMessage("notSet");
            java.awt.Color color = java.awt.Color.RED;
            if (turnout != null) {
                name = turnout.getName();
                color = java.awt.Color.BLACK;
            }
            p4.setBorder(BorderFactory.createLineBorder(color));
            p4.add(new JLabel(name));
            p4.setMaximumSize(p4.getPreferredSize());
            p3.add(p4);

            JPanel p13 = new JPanel();
            p13.setLayout(new BoxLayout(p13, BoxLayout.X_AXIS));
            p13.add(p3);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(p13);
            panel.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));

            rowPanel.add(panel);
            rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

            cnt++;
            if ((cnt % 2) == 0) {
                _iconPanel.add(rowPanel);
                rowPanel = null;
            }
            dim = panel.getPreferredSize();
        }
        while ((cnt % 2) != 0) {
            java.util.Objects.requireNonNull(rowPanel, "rowPanel should have been non-null in this case");
            rowPanel.add(Box.createRigidArea(dim));
            cnt++;
        }
        if (rowPanel != null) {
            _iconPanel.add(rowPanel);
            _iconPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        }
        rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        JPanel panel = null;
        cnt = 0;
        for (int i = _iconOrderList.size() - 1; i >= 0; i--) {
            if (panel == null) {
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            }
            String key = _iconOrderList.get(i);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(new JLabel(Bundle.getMessage(key)));
            p.add(_iconMap.get(key));
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            if ((cnt & 1) != 0) {
                _iconPanel.add(panel);
                _iconPanel.add(Box.createVerticalStrut(STRUT_SIZE));
                panel = null;
            }
            cnt++;
        }
        if (panel != null) {
            _iconPanel.add(panel);
            _iconPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        }
        this.add(_iconPanel, 0);
        valueChanged(null);

        pack();
    }

    void changeNumScissorTurnouts() {
        if (upperWestToUpperEastButton.isSelected()) {
            _turnoutMap.remove("lowerwest");
            _turnoutMap.remove("lowereast");
        }
        makeIconPanel(true);
    }

    void slipUpdate(int slip) {
        //If what we are setting to is the same as already set do nothing.
        if (slip == doubleSlip) {
            return;
        }
        if ((doubleSlip == 0x04) || (doubleSlip == 0x08)) {
            delete(4);
            delete(3);
            delete(2);
            _turnoutMap.remove("lowerwest");
            _turnoutMap.remove("lowereast");
            //We need to reset the icons back for a slip
            setIcon(3, "LowerWestToUpperEast",
                    "resources/icons/smallschematics/tracksegments/os-slip-lower-west-upper-east.gif");
            setIcon(2, "UpperWestToLowerEast",
                    "resources/icons/smallschematics/tracksegments/os-slip-upper-west-lower-east.gif");
            setIcon(4, "LowerWestToLowerEast",
                    "resources/icons/smallschematics/tracksegments/os-slip-lower-west-lower-east.gif");
            setIcon(0, "BeanStateInconsistent",
                    "resources/icons/smallschematics/tracksegments/os-slip-error-full.gif");
            setIcon(1, "BeanStateUnknown",
                    "resources/icons/smallschematics/tracksegments/os-slip-unknown-full.gif");
        }

        if (slip == 0x04) {
            //We need to setup the base icons for a three way.
            delete(5);
            delete(4);
            delete(3);
            delete(2);
            setIcon(3, "Upper",
                    "resources/icons/smallschematics/tracksegments/os-3way-upper.gif");
            setIcon(2, "Middle",
                    "resources/icons/smallschematics/tracksegments/os-3way-middle.gif");
            setIcon(4, "Lower",
                    "resources/icons/smallschematics/tracksegments/os-3way-lower.gif");
            setIcon(0, "BeanStateInconsistent",
                    "resources/icons/smallschematics/tracksegments/os-3way-error.gif");
            setIcon(1, "BeanStateUnknown",
                    "resources/icons/smallschematics/tracksegments/os-3way-unknown.gif");
            upperWestToUpperEastButton.setSelected(true);
        } else if (slip == 0x08) {
            //We need to setup the base icons for a Scissor.
            delete(5);
            setIcon(3, "LowerWestToUpperEast",
                    "resources/icons/smallschematics/tracksegments/os-double-crossover-lower-west-upper-east.gif");
            setIcon(2, "UpperWestToLowerEast",
                    "resources/icons/smallschematics/tracksegments/os-double-crossover-upper-west-lower-east.gif");
            setIcon(4, "LowerWestToLowerEast",
                    "resources/icons/smallschematics/tracksegments/os-double-crossover-closed.gif");

            setIcon(0, "BeanStateInconsistent",
                    "resources/icons/smallschematics/tracksegments/os-double-crossover-error.gif");
            setIcon(1, "BeanStateUnknown",
                    "resources/icons/smallschematics/tracksegments/os-double-crossover-unknown.gif");

            upperWestToUpperEastButton.setSelected(true);
        }

        switch (slip) {
            case 0x00:
                delete(4);
                setIcon(4, "LowerWestToLowerEast",
                        "resources/icons/smallschematics/tracksegments/os-slip-lower-west-lower-east.gif");
                setIcon(5, "UpperWestToUpperEast",
                        "resources/icons/smallschematics/tracksegments/os-slip-upper-west-upper-east.gif");
                break;
            case 0x02:
                delete(5);
                updateSingleSlipRoute(false);
                break;
            default:
                log.warn("Unhandled slip code: {}", slip);
                break;
        }
        doubleSlip = slip;
        makeIconPanel(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete(ActionListener addIconAction, boolean changeIconAction,
            boolean addToTable, boolean update) {
        super.complete(addIconAction, changeIconAction, addToTable, update);
        _table.setDragEnabled(true);
        _table.setTransferHandler(new ExportHandler());
        valueChanged(null);
    }

    class ExportHandler extends TransferHandler {

        /** {@inheritDoc} */
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        /** {@inheritDoc} */
        @Override
        public Transferable createTransferable(JComponent c) {
            return new TransferableNamedBean();
        }

        /** {@inheritDoc} */
        @Override
        public void exportDone(JComponent c, Transferable t, int action) {
        }
    }

    class TransferableNamedBean implements Transferable {

        DataFlavor dataFlavor;

        TransferableNamedBean() {
            try {
                dataFlavor = new DataFlavor(NamedBeanFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                log.error("Unable to find class supporting {}", NamedBeanFlavorMime, cnfe);
            }
        }

        /** {@inheritDoc} */
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            //if (log.isDebugEnabled()) log.debug("TransferableNamedBean.getTransferDataFlavors ");
            return new DataFlavor[]{dataFlavor};
        }

        /** {@inheritDoc} */
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            //if (log.isDebugEnabled()) log.debug("TransferableNamedBean.isDataFlavorSupported ");
            return dataFlavor.equals(flavor);
        }

        /** {@inheritDoc} */
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (log.isDebugEnabled()) {
                log.debug("TransferableNamedBean.getTransferData ");
            }
            if (isDataFlavorSupported(flavor)) {
                return getTableSelection();
            }
            return null;
        }
    }

    /**
     * {@inheritDoc} Activate Add to Panel button when all icons are assigned
     * sensors.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (_addButton == null) {
            return;
        }
        int numTurnouts = 2;
        if ((doubleSlip == SlipTurnoutIcon.SCISSOR) && (lowerWestToLowerEastButton.isSelected())) {
            numTurnouts = 4;
        }
        if (_turnoutMap.size() == numTurnouts) {
            _addButton.setEnabled(true);
            _addButton.setToolTipText(null);
            //checkIconSizes();
        } else {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(Bundle.getMessage("ToolTipAssignTurnouts"));
        }
    }

    void delete(int index) {
        if (index >= _iconOrderList.size()) {
            return;
        }
        String key = _iconOrderList.get(index);
        if (log.isDebugEnabled()) {
            log.debug("delete(" + index + ") Sizes: _iconMap= " + _iconMap.size()
                    + ", _iconOrderList= " + _iconOrderList.size());
        }
        _iconMap.remove(key);
        _iconOrderList.remove(index);
    }

    /**
     * Returns a new NamedIcon object for your own use. see NamedIcon
     * getIcon(String key) in super
     *
     * @param index of key
     * @return Unique object
     */
    public NamedIcon getIcon(int index) {
        return (NamedIcon) _iconMap.get(_iconOrderList.get(index)).getIcon();
    }

    /**
     * Returns a Turnout object for your own use. see NamedIcon getIcon(String
     * key) in super
     *
     * @param index of key
     * @return Unique object
     */
    public NamedBeanHandle<Turnout> getTurnout(String index) {
        return _turnoutMap.get(index);
    }

    public void setTurnout(String key, NamedBeanHandle<Turnout> turnout) {
        _turnoutMap.put(key, turnout);
    }

    void updateSingleSlipRoute(boolean single) {
        delete(4);
        if (single) {
            upperWestToUpperEastButton.setSelected(true);
            setIcon(4, "Slip",
                    "resources/icons/smallschematics/tracksegments/os-slip-upper-west-upper-east.gif");
        } else {
            lowerWestToLowerEastButton.setSelected(true);
            setIcon(4, "Slip",
                    "resources/icons/smallschematics/tracksegments/os-slip-lower-west-lower-east.gif");
        }
        makeIconPanel(true);
    }

    public void setSingleSlipRoute(boolean single) {
        if (single) {
            upperWestToUpperEastButton.setSelected(true);
        } else {
            lowerWestToLowerEastButton.setSelected(true);
        }
    }

    public boolean getSingleSlipRoute() {
        if (upperWestToUpperEastButton.isSelected()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean putTurnout(String key, Turnout turnout) {
        String name = turnout.getUserName();
        if (name == null) {
            name = turnout.getSystemName();
        }
        Iterator<NamedBeanHandle<Turnout>> iter = _turnoutMap.values().iterator();
        while (iter.hasNext()) {
            if (name.equals(iter.next().getName())) {
                JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                        Bundle.getMessage("DupTurnoutName"),
                        new Object[]{name}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        _turnoutMap.put(key, jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, turnout));
        return true;
    }

    /**
     * Enables the active Slip icons to receive dragged icons
     */
    class DropPanel extends JPanel implements DropTargetListener {

        DataFlavor dataFlavor;

        DropPanel() {
            try {
                dataFlavor = new DataFlavor(NamedBeanFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                log.error("Unable to find class supporting {}", NamedBeanFlavorMime, cnfe);
            }
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        }

        /** {@inheritDoc} */
        @Override
        public void dragExit(DropTargetEvent dte) {
        }

        /** {@inheritDoc} */
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
        }

        /** {@inheritDoc} */
        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropPanel.dragOver");
        }

        /** {@inheritDoc} */
        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        /** {@inheritDoc} */
        @Override
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if (e.isDataFlavorSupported(dataFlavor)) {
                    Turnout turnout = (Turnout) tr.getTransferData(dataFlavor);
                    if (turnout != null) {
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        DropTarget target = (DropTarget) e.getSource();
                        JPanel panel = (JPanel) target.getComponent();
                        JComponent comp = (JLabel) panel.getComponent(0);
                        if (putTurnout(comp.getName(), turnout)) {
                            makeIconPanel(true);
                        }
                        e.dropComplete(true);
                        if (log.isDebugEnabled()) {
                            log.debug("DropPanel.drop COMPLETED for "
                                    + comp.getName());
                        }
                        return;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("DropPanel.drop REJECTED!");
                        }
                        e.rejectDrop();
                    }
                }
            } catch (IOException ioe) {
                if (log.isDebugEnabled()) {
                    log.debug("DropPanel.drop REJECTED!");
                }
                e.rejectDrop();
            } catch (UnsupportedFlavorException ufe) {
                if (log.isDebugEnabled()) {
                    log.debug("DropPanel.drop REJECTED!");
                }
                e.rejectDrop();
            }
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SlipIconAdder.class);
}
