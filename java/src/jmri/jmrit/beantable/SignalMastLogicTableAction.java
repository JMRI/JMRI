package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.table.TableRowSorter;

import jmri.*;
import jmri.jmrit.beantable.signalmast.SignalMastLogicTableDataModel;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.managers.DefaultSignalMastLogicManager;
import jmri.util.ThreadingUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

public class SignalMastLogicTableAction extends AbstractTableAction<SignalMastLogic> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s title of the action
     */
    public SignalMastLogicTableAction(String s) {
        super(s);
    }

    public SignalMastLogicTableAction() {
        this(Bundle.getMessage("TitleSignalMastLogicTable"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableRowSorter<BeanTableDataModel<SignalMastLogic>> sorter = new TableRowSorter<>(m);
        JTable dataTable = m.makeJTable(m.getMasterClassName(), m, sorter);
        // create the frame
        f = new jmri.jmrit.beantable.BeanTableFrame<SignalMastLogic>(m, helpTarget(), dataTable) {
        };
        setMenuBar(f);
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }

    /**
     * Insert a table specific Tools menu. Account for the Window and Help
     * menus, which are already added to the menu bar as part of the creation of
     * the JFrame, by adding the Tools menu 2 places earlier unless the table is
     * part of the ListedTableFrame, that adds the Help menu later on.
     *
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame<SignalMastLogic> f) {
        final JmriJFrame finalF = f;   // needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenu before 'Window' and 'Help'
        int offset = 1;
        log.debug("setMenuBar number of menu items = {}", pos);
        for (int i = 0; i <= pos; i++) {
            if (menuBar.getComponent(i) instanceof JMenu) {
                if (((AbstractButton) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                    offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                }
            }
        }
        JMenu pathMenu = new JMenu(Bundle.getMessage("MenuTools"));
        menuBar.add(pathMenu, pos + offset);
        JMenuItem item = new JMenuItem(Bundle.getMessage("MenuItemAutoGen"));
        pathMenu.add(item);
        item.addActionListener( e -> autoCreatePairs(finalF));
        item = new JMenuItem(Bundle.getMessage("MenuItemAutoGenSections"));
        pathMenu.add(item);
        item.addActionListener( e -> {
            ((DefaultSignalMastLogicManager) InstanceManager.getDefault(SignalMastLogicManager.class)).generateSection();
            InstanceManager.getDefault(SectionManager.class).generateBlockSections();
            JmriJOptionPane.showMessageDialog(finalF, Bundle.getMessage("SectionGenerationComplete"));
        });
        JMenuItem setSMLDirSensors = new JMenuItem(Bundle.getMessage("MenuItemAddDirectionSensors"));
        pathMenu.add(setSMLDirSensors);
        setSMLDirSensors.addActionListener( e -> {
            int n = InstanceManager.getDefault(SignalMastLogicManager.class).setupSignalMastsDirectionSensors();
            if (n > 0) {
                JmriJOptionPane.showMessageDialog(finalF, java.text.MessageFormat.format(
                        Bundle.getMessage("MenuItemAddDirectionSensorsErrorCount"), n),
                        Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
            }
        });

    }

    @Override
    protected void createModel() {
        m = new SignalMastLogicTableDataModel();
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSignalMastLogicTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalMastLogicTable";// NOI18N
    }

    @Override
    protected void addPressed(ActionEvent e) {
        sigLog.setMast(null, null);
        sigLog.actionPerformed(e);
    }

    private JmriJFrame signalMastLogicFrame = null;
    private JLabel sourceLabel = new JLabel();

    void autoCreatePairs(JmriJFrame f) {
        if (!InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
            int response = JmriJOptionPane.showConfirmDialog(f, Bundle.getMessage("EnableLayoutBlockRouting"),
                    Bundle.getMessage("TitleBlockRouting"), JmriJOptionPane.YES_NO_OPTION);
            if (response == 0) {
                InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(true);
                JmriJOptionPane.showMessageDialog(f, Bundle.getMessage("LayoutBlockRoutingEnabled"));
            } else {
                return;
            }
        }
        signalMastLogicFrame = new JmriJFrame(Bundle.getMessage("DiscoverSignalMastPairs"), false, false);
        signalMastLogicFrame.setPreferredSize(null);
        JPanel panel1 = new JPanel();
        sourceLabel = new JLabel(Bundle.getMessage("DiscoveringSignalMastPairs"));
        panel1.add(sourceLabel);
        signalMastLogicFrame.add(panel1);
        signalMastLogicFrame.pack();
        signalMastLogicFrame.setVisible(true);

        final JCheckBox genSect = new JCheckBox(Bundle.getMessage("AutoGenSectionAfterLogic"));
        genSect.setToolTipText(Bundle.getMessage("AutoGenSectionAfterLogicToolTip"));
        Object[] params = {Bundle.getMessage("AutoGenSignalMastLogicMessage"), " ", genSect};
        int retval = JmriJOptionPane.showConfirmDialog(f, params, Bundle.getMessage("AutoGenSignalMastLogicTitle"),
                JmriJOptionPane.YES_NO_OPTION);

        if ( retval == JmriJOptionPane.YES_OPTION ) {
            InstanceManager.getDefault(SignalMastLogicManager.class).addPropertyChangeListener(propertyGenerateListener);
            // This process can take some time, so we do split it off then return to Swing/AWT
            Runnable r = () -> {
                //While the global discovery is taking place we remove the listener as this can result in a race condition.
                ((SignalMastLogicTableDataModel)m).setSuppressUpdate(true);
                try {
                    InstanceManager.getDefault(SignalMastLogicManager.class).automaticallyDiscoverSignallingPairs();
                } catch (JmriException e) {
                    // Notify of problem
                    try {
                        SwingUtilities.invokeAndWait(() -> {
                            InstanceManager.getDefault(SignalMastLogicManager.class).removePropertyChangeListener(propertyGenerateListener);
                            JmriJOptionPane.showMessageDialog(f, e.toString());
                            signalMastLogicFrame.setVisible(false);
                        });
                    } catch (java.lang.reflect.InvocationTargetException ex) {
                        log.error("failed to notify of problem with automaticallyDiscoverSignallingPairs", ex);
                    } catch (InterruptedException ex) {
                        log.error("interrupted while notifying of problem with automaticallyDiscoverSignallingPairs", ex);
                    }
                }

                // process complete, update GUI
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        m.updateNameList();
                        ((SignalMastLogicTableDataModel)m).setSuppressUpdate(false);
                        m.fireTableDataChanged();
                        if (genSect.isSelected()) {
                            ((DefaultSignalMastLogicManager) InstanceManager.getDefault(SignalMastLogicManager.class)).generateSection();
                            InstanceManager.getDefault(SectionManager.class).generateBlockSections();
                        }
                    });
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    log.error("failed to update at end of automaticallyDiscoverSignallingPairs", ex);
                } catch (InterruptedException ex) {
                    log.error("interrupted during update at end of automaticallyDiscoverSignallingPairs", ex);
                }
            };
            Thread thr = ThreadingUtil.newThread(r, "Discover Signal Mast Logic");  // NOI18N
            thr.start();

        } else {
            signalMastLogicFrame.setVisible(false);
        }
    }

    private final PropertyChangeListener propertyGenerateListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (SignalMastLogicManager.PROPERTY_AUTO_GENERATE_COMPLETE.equals(evt.getPropertyName())) {
                if (signalMastLogicFrame != null) {
                    signalMastLogicFrame.setVisible(false);
                }
                InstanceManager.getDefault(SignalMastLogicManager.class).removePropertyChangeListener(this);
                JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("SignalMastPairGenerationComplete"));
            } else if (SignalMastLogicManager.PROPERTY_AUTO_GENERATE_UPDATE.equals(evt.getPropertyName())) {
                sourceLabel.setText((String) evt.getNewValue());
                signalMastLogicFrame.pack();
                signalMastLogicFrame.repaint();
            }
        }
    };

    private final jmri.jmrit.signalling.SignallingAction sigLog = new jmri.jmrit.signalling.SignallingAction();

    @Override
    protected String getClassName() {
        return SignalMastLogicTableAction.class.getName();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalMastLogicTableAction.class);
}
