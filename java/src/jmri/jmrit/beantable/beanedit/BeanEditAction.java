package jmri.jmrit.beantable.beanedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * Provides the basic information and structure for for a editing the details of
 * a bean object.
 * 
 * @param <B> the type of supported NamedBean
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public abstract class BeanEditAction<B extends NamedBean> extends AbstractAction {

    public BeanEditAction(String s) {
        super(s);
    }

    public BeanEditAction() {
        super("Bean Edit");
    }

    B bean;

    public void setBean(B bean) {
        this.bean = bean;
    }

    /**
     * Call to create all the different tabs that will be added to the frame.
     */
    protected void initPanels() {
        basicDetails();
    }

    /**
     * Initialise panels to be at start of Tabbed Panel menu.
     * Default empty.
     */
    protected void initPanelsFirst() {
    }

    /**
     * Initialise panels to be at end of Tabbed Panel menu.
     * Startup usage details and Properties.
     */
    protected void initPanelsLast() {
        usageDetails();
        propertiesDetails();
    }

    JTextField userNameField = new JTextField(20);
    JTextArea commentField = new JTextArea(3, 30);
    JScrollPane commentFieldScroller = new JScrollPane(commentField);
    private JLabel statusBar = new JLabel(Bundle.getMessage("ItemEditStatusInfo", Bundle.getMessage("ButtonApply")));

    /**
     * Create a generic panel that holds the basic bean information System Name,
     * User Name, and Comment.
     *
     * @return a new panel
     */
    BeanItemPanel basicDetails() {
        BeanItemPanel basic = new BeanItemPanel();

        basic.setName(Bundle.getMessage("Basic"));
        basic.setLayout(new BoxLayout(basic, BoxLayout.Y_AXIS));

        basic.addItem(new BeanEditItem(new JLabel(bean.getSystemName()), Bundle.getMessage("ColumnSystemName"), null));
        //Bundle.getMessage("ConnectionHint", "N/A"))); // TODO get connection name from nbMan.getSystemPrefix()

        basic.addItem(new BeanEditItem(userNameField, Bundle.getMessage("ColumnUserName"), null));

        basic.addItem(new BeanEditItem(commentFieldScroller, Bundle.getMessage("ColumnComment"), null));

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBasicItems(e);
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetBasicItems(e);
            }
        });
        bei.add(basic);
        return basic;
    }

    /**
     * Create a generic panel that holds Bean usage details.
     *
     * @return a new panel
     */
    BeanItemPanel usageDetails() {
        BeanItemPanel usage = new BeanItemPanel();

        usage.setName(Bundle.getMessage("Usage"));
        usage.setLayout(new BoxLayout(usage, BoxLayout.Y_AXIS));

        usage.addItem(new BeanEditItem(null, null, Bundle.getMessage("UsageText", bean.getDisplayName())));

        ArrayList<String> listeners = new ArrayList<>();
        for (String ref : bean.getListenerRefs()) {
            if (!listeners.contains(ref)) {
                listeners.add(ref);
            }
        }

        Object[] strArray = new Object[listeners.size()];
        listeners.toArray(strArray);
        JList<Object> list = new JList<>(strArray);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black)));
        usage.addItem(new BeanEditItem(listScroller, Bundle.getMessage("ColumnLocation"), null));

        bei.add(usage);
        return usage;
    }
    private BeanPropertiesTableModel<B> propertiesModel;

    /**
     * Create a generic panel that holds Bean Property details.
     *
     * @return a new panel
     */
    BeanItemPanel propertiesDetails() {
        BeanItemPanel properties = new BeanItemPanel();
        properties.setName(Bundle.getMessage("Properties"));
        properties.addItem(new BeanEditItem(null, null, Bundle.getMessage("NamedBeanPropertiesTableDescription")));
        properties.setLayout(new BoxLayout(properties, BoxLayout.Y_AXIS));
        propertiesModel = new BeanPropertiesTableModel<>();
        JTable jtAttributes = new JTable();
        jtAttributes.setModel(propertiesModel);
        JScrollPane jsp = new JScrollPane(jtAttributes);
        Dimension tableDim = new Dimension(400, 200);
        jsp.setMinimumSize(tableDim);
        jsp.setMaximumSize(tableDim);
        jsp.setPreferredSize(tableDim);
        properties.addItem(new BeanEditItem(jsp, "", null));
        properties.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                propertiesModel.updateModel(bean);
            }
        });
        properties.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                propertiesModel.setModel(bean);
            }
        });

        bei.add(properties);
        return properties;
    }

    @OverridingMethodsMustInvokeSuper
    protected void saveBasicItems(ActionEvent e) {
        String uname = bean.getUserName();
        if (uname == null && !userNameField.getText().isEmpty()) {
            renameBean(userNameField.getText());
        } else if (uname != null && !uname.equals(userNameField.getText())) {
            if (userNameField.getText().isEmpty()) {
                removeName();
            } else {
                renameBean(userNameField.getText());
            }
        }
        bean.setComment(commentField.getText());
    }

    @OverridingMethodsMustInvokeSuper
    protected void resetBasicItems(ActionEvent e) {
        userNameField.setText(bean.getUserName());
        commentField.setText(bean.getComment());
    }

    abstract protected String helpTarget();

    protected ArrayList<BeanItemPanel> bei = new ArrayList<>(5);
    JmriJFrame f;

    protected Component selectedTab = null;
    private final JTabbedPane detailsTab = new JTabbedPane();

    /**
     * Apply Button.
     * Accessible so Edit Actions can set custom tool tip.
     */
    protected JButton applyBut;
    
    public void setSelectedComponent(Component c) {
        selectedTab = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (bean == null) {
            // display message in status bar TODO
            log.error("No bean set so unable to edit a null bean");  // NOI18N
            return;
        }
        if (f == null) {
            f = new JmriJFrame(Bundle.getMessage("EditBean", bean.getBeanType(), bean.getDisplayName()), false, false);
            f.addHelpMenu(helpTarget(), true);
            applyBut = new JButton(Bundle.getMessage("ButtonApply")); // create before initPanels()
            java.awt.Container containerPanel = f.getContentPane();
            initPanelsFirst();
            initPanels();
            initPanelsLast();

            int i=0;
            for (BeanItemPanel bi : bei) {
                addToPanel(bi, bi.getListOfItems());
                detailsTab.add(bi, bi.getName(), i);
                detailsTab.setEnabledAt(i, bi.isEnabled());
                detailsTab.setToolTipTextAt(i, bi.getToolTipText());
                i++;
            }
            containerPanel.add(detailsTab, BorderLayout.CENTER);

            // shared bottom panel part
            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.PAGE_AXIS));
            // shared status bar above buttons
            JPanel panelStatus = new JPanel();
            statusBar.setFont(statusBar.getFont().deriveFont(0.9f * userNameField.getFont().getSize())); // a bit smaller
            statusBar.setForeground(Color.gray);
            panelStatus.add(statusBar);
            bottom.add(panelStatus);

            // shared buttons
            JPanel buttons = new JPanel();
            applyBut.addActionListener(this::applyButtonAction);
            JButton okBut = new JButton(Bundle.getMessage("ButtonOK"));
            okBut.addActionListener((ActionEvent e1) -> {
                applyButtonAction(e1);
                f.dispose();
            });
            JButton cancelBut = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelBut.addActionListener(this::cancelButtonAction);
            buttons.add(applyBut);
            buttons.add(okBut);
            buttons.add(cancelBut);
            bottom.add(buttons);
            containerPanel.add(bottom, BorderLayout.SOUTH);
        }
        for (BeanItemPanel bi : bei) {
            bi.resetField();
        }
        persistSelectedTab(); // use persistence unless specified by overriding class
        if (selectedTab != null) {
            detailsTab.setSelectedComponent(selectedTab);
        }
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelButtonAction(null);
            }
        });
        f.pack();
        f.setVisible(true);
    }

    /**
     * Selects previously selected Tab Index for override class name.
     * Adds listener when Tab changed update UI preference.
     */
    private void persistSelectedTab(){
        String TAB_SELECT_STRING = "selectedTabIndex"; // NOI18N
        Object obj = InstanceManager.getDefault(UserPreferencesManager.class)
            .getProperty(getClass().getName(), TAB_SELECT_STRING);
        int previoustab = (obj!=null ? (Integer) obj : 0);
        // make sure that valid index selected in case a tab is removed in future.
        detailsTab.setSelectedIndex(Math.max(Math.min(detailsTab.getTabCount()-1, previoustab),0));
        // add listener
        detailsTab.getModel().addChangeListener((ChangeEvent evt) -> {
            InstanceManager.getDefault(UserPreferencesManager.class)
                .setProperty(getClass().getName(), TAB_SELECT_STRING, detailsTab.getSelectedIndex());
        });
    
    }

    protected void applyButtonAction(ActionEvent e) {
        save();
    }

    protected void cancelButtonAction(ActionEvent e) {
        f.dispose();
    }

    /**
     * Set out the panel based upon the items passed in via the ArrayList.
     *
     * @param panel JPanel to add stuff to
     * @param items a {@link BeanEditItem} list of key-value pairs for the items
     *              to add
     */
    protected void addToPanel(JPanel panel, List<BeanEditItem> items) {
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cD = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        cL.fill = GridBagConstraints.HORIZONTAL;
        cL.insets = new Insets(4, 0, 0, 15);   // inset for left hand column (description)
        cR.insets = new Insets(4, 10, 13, 15); // inset for help (right hand column, multi line text area)
        cD.insets = new Insets(4, 0, 0, 0);    // top inset 4, up from 2 to align JLabel with JTextField
        cD.anchor = GridBagConstraints.NORTHWEST;
        cL.anchor = GridBagConstraints.NORTHWEST;

        int y = 0;
        JPanel p = new JPanel();

        for (BeanEditItem it : items) {
            // add the 3 elements on a JPanel to the parent panel grid layout
            if (it.getDescription() != null && it.getComponent() != null) {
                JLabel descript = new JLabel(it.getDescription() + ":", JLabel.LEFT);
                if (it.getDescription().isEmpty()) {
                    descript.setText("");
                }
                cL.gridx = 0;
                cL.gridy = y;
                cL.ipadx = 3;

                gbLayout.setConstraints(descript, cL);
                p.setLayout(gbLayout);
                p.add(descript, cL);

                cD.gridx = 1;
                cD.gridy = y;

                Component thing = it.getComponent();
                //log.debug("descript: '" + it.getDescription() + "', thing: " + thing.getClass().getName());
                if (thing instanceof JComboBox
                        || thing instanceof JTextField
                        || thing instanceof JCheckBox
                        || thing instanceof JRadioButton) {
                    cD.insets = new Insets(0, 0, 0, 0); // put a little higher than a JLabel
                } else if (thing instanceof JColorChooser) {
                    cD.insets = new Insets(-6, 0, 0, 0); // move it up
                } else {
                    cD.insets = new Insets(4, 0, 0, 0); // reset
                }
                gbLayout.setConstraints(thing, cD);
                p.add(thing, cD);

                cR.gridx = 2;
                cR.gridwidth = 1;
                cR.anchor = GridBagConstraints.WEST;

            } else {
                cR.anchor = GridBagConstraints.CENTER;
                cR.gridx = 0;
                cR.gridwidth = 3;
            }
            cR.gridy = y;
            if (it.getHelp() != null) {
                JTextPane help = new JTextPane();
                help.setText(it.getHelp());
                gbLayout.setConstraints(help, cR);
                formatTextAreaAsLabel(help);
                p.add(help, cR);
            }
            y++;
        }
        panel.add(p);
    }

    void formatTextAreaAsLabel(JTextPane pane) {
        pane.setOpaque(false);
        pane.setEditable(false);
        pane.setBorder(null);
    }

    public void save() {
        String feedback = Bundle.getMessage("ItemUpdateFeedback", bean.getBeanType())
                + " " + bean.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        // provide feedback to user, can be overwritten by save action error handler
        statusBar.setText(feedback);
        statusBar.setForeground(Color.gray);
        for (BeanItemPanel bi : bei) {
            bi.saveItem();
        }
    }

    static boolean validateNumericalInput(String text) {
        if (text.length() != 0) {
            try {
                Integer.parseInt(text);
            } catch (java.lang.NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }

    NamedBeanHandleManager nbMan = InstanceManager.getDefault(NamedBeanHandleManager.class);

    abstract protected B getByUserName(String name);

    /**
     * Generic method to change the user name of a Bean.
     *
     * @param _newName string to use as the new user name
     */
    public void renameBean(String _newName) {
        if (!allowBlockNameChange("Rename", _newName)) return;  // NOI18N
        B nBean = bean;
        String oldName = nBean.getUserName();

        String value = _newName;

        if (value.equals(oldName)) {
            //name not changed.
            return;
        } else {
            B nB = getByUserName(value);
            if (nB != null) {
                log.error("User name is not unique {}", value); // NOI18N
                String msg;
                msg = java.text.MessageFormat.format(Bundle.getMessage("WarningUserName"),
                        new Object[]{("" + value)});
                JmriJOptionPane.showMessageDialog(f, msg,
                        Bundle.getMessage("WarningTitle"),
                        JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        nBean.setUserName(value);
        if (!value.isEmpty()) {
            if (oldName == null || oldName.isEmpty()) {
                if (!nbMan.inUse(nBean.getSystemName(), nBean)) {
                    return;
                }
                String msg = Bundle.getMessage("UpdateToUserName",
                        new Object[]{nBean.getBeanType(), value, nBean.getSystemName()});
                int optionPane = JmriJOptionPane.showConfirmDialog(f,
                        msg, Bundle.getMessage("UpdateToUserNameTitle"),
                        JmriJOptionPane.YES_NO_OPTION);
                if (optionPane == JmriJOptionPane.YES_OPTION) {
                    //This will update the bean reference from the systemName to the userName
                    try {
                        nbMan.updateBeanFromSystemToUser(nBean);
                    } catch (jmri.JmriException ex) {
                        //We should never get an exception here as we already check that the username is not valid
                    }
                }

            } else {
                nbMan.renameBean(oldName, value, nBean);
            }

        } else {
            //This will update the bean reference from the old userName to the SystemName
            nbMan.updateBeanFromUserToSystem(nBean);
        }
    }

    /**
     * Generic method to remove the user name from a bean.
     */
    public void removeName() {
        if (!allowBlockNameChange("Remove", "")) return;  // NOI18N
        String msg = java.text.MessageFormat.format(Bundle.getMessage("UpdateToSystemName"),
                new Object[]{bean.getBeanType()});
        int optionPane = JmriJOptionPane.showConfirmDialog(f,
                msg, Bundle.getMessage("UpdateToSystemNameTitle"),
                JmriJOptionPane.YES_NO_OPTION);
        if (optionPane == JmriJOptionPane.YES_OPTION) {
            nbMan.updateBeanFromUserToSystem(bean);
        }
        bean.setUserName(null);
    }

    /**
     * Determine whether it is safe to rename/remove a Block user name.
     * <p>The user name is used by the LayoutBlock to link to the block and
     * by Layout Editor track components to link to the layout block.
     * @param changeType This will be Remove or Rename.
     * @param newName For Remove this will be empty, for Rename it will be the new user name.
     * @return true to continue with the user name change.
     */
    boolean allowBlockNameChange(String changeType, String newName) {
        if (!bean.getBeanType().equals("Block")) return true;  // NOI18N

        // If there is no layout block or the block has no user name, Block rename and remove are ok without notification.
        String oldName = bean.getUserName();
        if (oldName == null) return true;
        LayoutBlock layoutBlock = jmri.InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(oldName);
        if (layoutBlock == null) return true;

        // Remove is not allowed if there is a layout block
        if (changeType.equals("Remove")) {
            log.warn("Cannot remove user name for block {}", oldName);  // NOI18N
                JmriJOptionPane.showMessageDialog(f,
                        Bundle.getMessage("BlockRemoveUserNameWarning", oldName),  // NOI18N
                        Bundle.getMessage("WarningTitle"),  // NOI18N
                        JmriJOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Confirmation dialog
        int optionPane = JmriJOptionPane.showConfirmDialog(f,
                Bundle.getMessage("BlockChangeUserName", oldName, newName),  // NOI18N
                Bundle.getMessage("QuestionTitle"),  // NOI18N
                JmriJOptionPane.YES_NO_OPTION);
        return optionPane == JmriJOptionPane.YES_OPTION;
    }

    /**
     * TableModel for edit of Bean properties.
     * <p>
     * At this stage we purely use this to allow the user to delete properties,
     * not to add them. Changing properties is possible but only for strings.
     * Based upon the code from the RosterMediaPane
     */
    private static class BeanPropertiesTableModel<B extends NamedBean> extends AbstractTableModel {

        Vector<KeyValueModel> attributes;
        String titles[];
        boolean wasModified;

        private static class KeyValueModel {

            public KeyValueModel(String k, Object v) {
                key = k;
                value = v;
            }
            public String key;
            public Object value;
        }

        public BeanPropertiesTableModel() {
            titles = new String[2];
            titles[0] = Bundle.getMessage("NamedBeanPropertyName");
            titles[1] = Bundle.getMessage("NamedBeanPropertyValue");
        }

        public void setModel(B nb) {
            attributes = new Vector<>(nb.getPropertyKeys().size());
            Iterator<String> ite = nb.getPropertyKeys().iterator();
            while (ite.hasNext()) {
                String key = ite.next();
                KeyValueModel kv = new KeyValueModel(key, nb.getProperty(key));
                attributes.add(kv);
            }
            wasModified = false;
        }

        public void updateModel(B nb) {
            if (!wasModified()) {
                return; //No changed made
            }   // add and update keys
            for (int i = 0; i < attributes.size(); i++) {
                KeyValueModel kv = attributes.get(i);
                if ((kv.key != null)
                        && // only update if key value defined, will do the remove too
                        ((nb.getProperty(kv.key) == null) || (!kv.value.equals(nb.getProperty(kv.key))))) {
                    nb.setProperty(kv.key, kv.value);
                }
            }
            //remove undefined keys

            Iterator<String> ite = nb.getPropertyKeys().iterator();
            while (ite.hasNext()) {
                if (!keyExist(ite.next())) // not a very efficient algorithm!
                {
                    ite.remove();
                }
            }
            wasModified = false;
        }

        private boolean keyExist(Object k) {
            if (k == null) {
                return false;
            }
            for (int i = 0; i < attributes.size(); i++) {
                if (k.equals(attributes.get(i).key)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return attributes.size();
        }

        @Override
        public String getColumnName(int col) {
            return titles[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row < attributes.size()) {
                if (col == 0) {
                    return attributes.get(row).key;
                }
                if (col == 1) {
                    return attributes.get(row).value;
                }
            }
            return "...";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            KeyValueModel kv;

            if (row < attributes.size()) // already exist?
            {
                kv = attributes.get(row);
            } else {
                kv = new KeyValueModel("", "");
            }

            if (col == 0) // update key
            //Force keys to be save as a single string with no spaces
            {
                if (!keyExist(((String) value).replaceAll("\\s", ""))) // if not exist
                {
                    kv.key = ((String) value).replaceAll("\\s", "");
                } else {
                    setValueAt(value + "-1", row, col); // else change key name
                    return;
                }
            }

            if (col == 1) // update value
            {
                kv.value = value;
            }
            if (row < attributes.size()) // existing one
            {
                attributes.set(row, kv);
            } else {
                attributes.add(row, kv); // new one
            }
            if ((col == 0) && (kv.key.isEmpty())) {
                attributes.remove(row); // actually maybe remove
            }
            wasModified = true;
            fireTableCellUpdated(row, col);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public boolean wasModified() {
            return wasModified;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BeanEditAction.class);

}
