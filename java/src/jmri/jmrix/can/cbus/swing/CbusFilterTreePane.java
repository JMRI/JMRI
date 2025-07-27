package jmri.jmrix.can.cbus.swing;

import java.awt.*;

import java.util.HashMap;

import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.*;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.*;
import jmri.util.TimerUtil;
import jmri.util.swing.JCheckBoxTree;
import jmri.util.swing.JCheckBoxTreeCellRenderer;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JSpinnerUtil;

import static jmri.jmrix.can.cbus.CbusFilterType.*;

/**
 * CbusFilterTreePane contains a JCheckBoxTree customised to display and
 * control CbusFilter using CbusFilterTypes.
 * @author Steve
 */
public class CbusFilterTreePane extends JPanel {

    final transient CbusFilterJCheckBoxTree cbt;
    final CbusFilter filter;
    private static final String SPACE = " ";
    private static final Dimension minimumSize = new Dimension(500, 300);
    private final HashMap<CbusFilterType, SpinnerJPanel> spinnerPanelMap = new HashMap<>();
    private final CanSystemConnectionMemo memo;

    public CbusFilterTreePane() {
        this(null);
    }

    /**
     * Create a new CbusFilterTreePane for a given system connection.
     * @param memo the System Connection to look up node names from.
     */
    public CbusFilterTreePane( CanSystemConnectionMemo memo ) {
        super();

        this.memo = memo;
        filter = new CbusFilter(this);
        cbt = new CbusFilterJCheckBoxTree();

        setSize(minimumSize);
        this.setLayout(new BorderLayout());
        
        DefaultTreeModel model = new DefaultTreeModel(filter.getTree());
        cbt.setModel(model);

        JScrollPane fPaneScroll = new JScrollPane();
        fPaneScroll.getViewport().add(cbt);

        fPaneScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fPaneScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        fPaneScroll.setVisible(true);

        add(BorderLayout.CENTER, fPaneScroll);
        this.add(BorderLayout.SOUTH, getBottomPanel());

        cbt.addPropertyChangeListener( pcl);

    }

    private final transient java.beans.PropertyChangeListener pcl = event ->  {
        if ( JCheckBoxTree.PROPERTY_CHANGE_CHECKBOX_STATUS.equals(event.getPropertyName()) ) {
            checkBoxesChanged();
        }
    };

    /**
     * Filter a CanReply or CanMessage.
     *
     * @param m CanMessage or CanReply
     * @return true when to apply filter, false to not filter.
     *
     */
    public boolean filter(@Nonnull jmri.jmrix.AbstractMessage m) {
       int result = filter.filter(m);
       return result > -1;
    }

    private JPanel getBottomPanel() {
        JPanel toReturn = new JPanel();
        toReturn.setLayout(new jmri.util.swing.WrapLayout());

        JButton jb = new JButton(Bundle.getMessage("ButtonResetCounts"));
        jb.addActionListener( e -> filter.resetCounts());

        toReturn.add(jb);
        toReturn.add(getNewSpinner(CFEVENTMIN));
        toReturn.add(getNewSpinner(CFEVENTMAX));
        toReturn.add(getNewSpinner(CFNODEMIN));
        toReturn.add(getNewSpinner(CFNODEMAX));

        return toReturn;
    }

    private void checkBoxesChanged() {

        setSpinnersEnabled( CFEVENTMAX , false);
        setSpinnersEnabled( CFEVENTMIN , false);

        setSpinnersEnabled( CFNODEMAX , false);
        setSpinnersEnabled( CFNODEMIN , false);

        java.util.Set<String> list = new java.util.HashSet<>();

        java.util.List<TreePath> paths = cbt.getCheckedPaths();
        for (TreePath tp : paths) {

            String xx = tp.getLastPathComponent().toString();
            if ( CbusFilter.ROOT_NODE_TEXT.equals(xx)) {
                continue;
            }

            list.add(xx);
            
            CbusFilterType ft = CbusFilterType.getFilterByName(xx);
            setSpinnersEnabled(ft, true);

        }
        filter.setFiltersByName(list);

    }

    private void setSpinnersEnabled( CbusFilterType fType, boolean enabled) {
        var xx = spinnerPanelMap.get(fType);
        if ( xx != null ) {
            xx.setEnabled(enabled);
        }
    }

    public void reset() {
        cbt.resetCheckingState();
    }

    private SpinnerJPanel getNewSpinner(@Nonnull CbusFilterType fType){
        SpinnerJPanel panel = new SpinnerJPanel( fType);
        spinnerPanelMap.put(fType, panel);
        return panel;
    }

    public void dispose() {
        cbt.removePropertyChangeListener(pcl);
        cbt.dispose();
    }

    private class SpinnerJPanel extends JPanel {

        final JSpinner spinner;

        SpinnerJPanel (CbusFilterType fType) {

            int startVal = 0;
            if ( fType == CFEVENTMAX || fType == CFNODEMAX ) {
                startVal = 65535;
            }

            SpinnerNumberModel s = new SpinnerNumberModel(startVal, 0, 65535, 1);
            spinner = new JSpinner(s);
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
            spinner.setEditor(editor);
            JSpinnerUtil.setCommitsOnValidEdit(spinner, true);

            spinner.addChangeListener((ChangeEvent e) -> {
                filter.setMinMax(fType, (Integer) spinner.getValue() );
                setModelFromMinMax(fType, (Integer) spinner.getValue() );
                cbt.repaint();
            });

            add(spinner);

            setBackground(Color.white);

            setBorder(BorderFactory.createTitledBorder(fType.getName()));

            SpinnerJPanel.this.setEnabled(false);

        }

        @Override
        public void setEnabled( boolean enabled) {
            super.setEnabled(enabled);
            spinner.setEnabled(enabled);
        }

        private SpinnerNumberModel getModelForType( CbusFilterType type ) {
            return (SpinnerNumberModel)spinnerPanelMap.get(type).spinner.getModel();
        }

        void setModelFromMinMax( CbusFilterType type, int newVal){
            switch (type) {
                case CFEVENTMAX:
                    getModelForType(CFEVENTMIN).setMaximum(newVal);
                    break;
                case CFEVENTMIN:
                    getModelForType(CFEVENTMAX).setMinimum(newVal);
                    break;
                case CFNODEMAX:
                    getModelForType(CFNODEMIN).setMaximum(newVal);
                    break;
                case CFNODEMIN:
                    getModelForType(CFNODEMAX).setMinimum(newVal);
                    break;
                default:
                    break;
            }
        }

    }

    private class CbusFilterJCheckBoxTree extends JCheckBoxTree {

        CbusFilterJCheckBoxTree() {
            super();
            setCellRenderer(new CbusFilterTreeCellRenderer());
            startRefreshTimer();
        }

        @Override
        public String getToolTipText( JmriMouseEvent e) {
            TreePath tp = getPathForLocation(e.getX(), e.getY());
            if ( tp != null ) {
                var ft = CbusFilterType.getFilterByName(tp.getLastPathComponent().toString());
                if ( ft != null ) {
                    return ft.getToolTip();
                }
            }
            return super.getToolTipText(e);
        }

        void dispose() {
            if ( task != null ) {
                disposed = true;
                task = null;
            }
        }

        private boolean disposed;

        private TimerTask task;
        private long iteration;
        private long numFrames = 0;

        private void startRefreshTimer() {

            int refreshPeriod = 300;

            task = new TimerTask() {
                @Override
                public void run() {

                    if ( disposed ) {
                        return;
                    }

                    iteration++;
                    if ( iteration % 10 == 0 ) {
                        // updateUI in case Node names added / numbers get larger
                        CbusFilterJCheckBoxTree.this.updateUI();
                    } else {
                        long total = filter.getFilteredMessage()+filter.getFilteredReply()
                            +filter.getPassedMessage()+filter.getPassedReply();
                        if ( numFrames != total ) {
                            numFrames = total;
                            // CAN frames heard, repaint to update the numbers.
                            CbusFilterJCheckBoxTree.this.repaint();
                        }
                    }
                    TimerUtil.scheduleOnGUIThread(task, refreshPeriod);
                }
            };
            TimerUtil.scheduleOnGUIThread(task, refreshPeriod);
        }

    }

    private static JLabel spaceLabel =  new JLabel(SPACE);
    private static final Color GREEN = new Color(2,48,48);
    private static final Color AMBER = new Color(139,128,0);

    private class CbusFilterTreeCellRenderer extends JCheckBoxTreeCellRenderer {

        private CbusFilterTreeCellRenderer() {
            toRet = new JPanel();
            toRet.setOpaque(false);
            toRet.setLayout(new BoxLayout(toRet, BoxLayout.X_AXIS));
        }

        private final JPanel toRet;

        @Override
        public JPanel getPanelExtras( DefaultMutableTreeNode node) {

            toRet.removeAll();
            if ( node == null ) {
                return toRet;
            }

            CbusFilterType filterType = CbusFilterType.getFilterByName(node.getUserObject().toString());
            addMinMaxText(filterType,toRet);
            addNodeAndLabelText(node, filter, toRet);
            addRootText(node, toRet);
            return toRet;
        }

        private void addNodeAndLabelText( DefaultMutableTreeNode node, CbusFilter filter, @Nonnull JPanel toRet){
            int nodeNum = filter.getNodeNumber(node);
            if ( nodeNum > 0 ) {
                toRet.add(new JLabel( SPACE + new CbusNameService(memo).getNodeName( nodeNum ) ));
            }
            var label = filter.getNumberFilteredLabel(node);
            if ( label != null ) {
                toRet.add(new JLabel(" "));
                toRet.add( label );
            }
        }

        private void addRootText( DefaultMutableTreeNode node, @Nonnull JPanel toRet){
            if ( node.isRoot() ) {

                JLabel label = new JLabel( SPACE + (filter.getPassedReply()+filter.getPassedMessage()) + SPACE);
                label.setForeground( GREEN );
                toRet.add(spaceLabel);
                toRet.add(label);
                toRet.add(spaceLabel);
                label = new JLabel( SPACE + (filter.getFilteredReply()+filter.getFilteredMessage()) + SPACE);
                label.setForeground( AMBER );
                toRet.add(label);
                toRet.add(spaceLabel);
            }
        }

        private void addMinMaxText( CbusFilterType filterType, @Nonnull JPanel toRet ) {
            if ( filterType == null ) {
                return;
            }
            JLabel label;
            switch (filterType) {
                case CFIN:
                    label = new JLabel( SPACE + filter.getPassedReply() + SPACE);
                    label.setForeground( GREEN );
                    toRet.add(spaceLabel);
                    toRet.add(label);
                    toRet.add(spaceLabel);
                    label = new JLabel( SPACE + filter.getFilteredReply() + SPACE);
                    label.setForeground( AMBER );
                    toRet.add(label);
                    break;
                case CFOUT:
                    label = new JLabel( SPACE + filter.getPassedMessage() + SPACE);
                    label.setForeground( GREEN );
                    toRet.add(spaceLabel);
                    toRet.add(label);
                    toRet.add(spaceLabel);
                    label = new JLabel( SPACE + filter.getFilteredMessage() + SPACE);
                    label.setForeground( AMBER );
                    toRet.add(label);
                    break;
                case CFEVENTMIN:
                    toRet.add(new JLabel( SPACE + filter.getEvMin() + SPACE));
                    break;
                case CFEVENTMAX:
                    toRet.add(new JLabel( SPACE + filter.getEvMax() + SPACE));
                    break;
                case CFNODEMIN:
                    toRet.add(new JLabel( SPACE + filter.getNdMin()+ SPACE));
                    break;
                case CFNODEMAX:
                    toRet.add(new JLabel( SPACE + filter.getNdMax()+ SPACE));
                    break;
                default:
                    break;
            }
        }

    }

}
