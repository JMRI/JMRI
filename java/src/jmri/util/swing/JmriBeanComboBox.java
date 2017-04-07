package jmri.util.swing;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComboBox.KeySelectionManager;
import javax.swing.text.JTextComponent;
import jmri.NamedBean;
import jmri.util.AlphanumComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JComboBox varient for showing and selecting JMRI NamedBeans from a specific
 * manager.
 */
public class JmriBeanComboBox extends JComboBox<String> implements java.beans.PropertyChangeListener {

    /*
     * Create a default Jmri Combo box for the given bean manager
     * @param manager the jmri manager that is used to populate the combo box
     */
    public JmriBeanComboBox(jmri.Manager manager) {
        this(manager, null, DisplayOptions.DISPLAYNAME);
    }

    /*
     * Create a Jmri Combo box for the given bean manager, with the Namedbean already selected and the items displayed and ordered
     * @param manager the jmri manager that is used to populate the combo box
     * @param nBean the namedBean that should automatically be selected
     * @param displayOrder the way in which the namedbeans should be displayed
     */
    public JmriBeanComboBox(jmri.Manager manager, NamedBean nBean, DisplayOptions displayOrder) {
        _displayOrder = displayOrder;
        _manager = manager;
        setSelectedBean(nBean);
        //setEditable(true);
        _manager.addPropertyChangeListener(this);
        setKeySelectionManager(new beanSelectionManager());

        //fires when drop down list item is selected
        addItemListener((ItemEvent event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                JmriBeanComboBox cb = (JmriBeanComboBox) event.getSource();
                validateText();
            }
        });

        //fires when key is released while typing in combox editor
        getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                JTextComponent c = (JTextComponent) event.getSource();
                JmriBeanComboBox cb = (JmriBeanComboBox) c.getParent();
                validateText();
            }
        });
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new NamedBean is available in the manager
            _lastSelected = (String) getSelectedItem();
            updateComboBox(_lastSelected);
            log.debug("Update triggered in name list");
        } else if (e.getPropertyName().equals("DisplayListName")) {
            refreshCombo();
        }
    }

    String _lastSelected = "";
    DisplayOptions _displayOrder;
    boolean _firstBlank = false;

    jmri.Manager _manager;

    HashMap<String, NamedBean> displayToBean = new HashMap<>();

    public jmri.Manager getManager() {
        return _manager;
    }

    public void refreshCombo() {
        updateComboBox((String) getSelectedItem());
    }

    void updateComboBox(String select) {
        displayToBean = new HashMap<>();
        removeAllItems();

        String[] displayList = getDisplayList();

        for (int i = 0; i < displayList.length; i++) {
            addItem(displayList[i]);
            if ((select != null) && (displayList[i].equals(select))) {
                setSelectedIndex(i);
            }
        }
        if (_firstBlank) {
            super.insertItemAt("", 0);
            if (_lastSelected == null || _lastSelected.equals("")) {
                setSelectedIndex(0);
            }
        }
    }

    /**
     * Get the display list used by this combo box
     *
     * @return the display list used by this combo box
     */
    public String[] getDisplayList() {
        ArrayList<String> nameList = new ArrayList<>(Arrays.asList(_manager.getSystemNameArray()));

        for (NamedBean bean : exclude) {
            if (bean != null) {
                nameList.remove(bean.getSystemName());
            }
        }

        String[] displayList = new String[nameList.size()];

        if (_displayOrder == DisplayOptions.SYSTEMNAME) {
            displayList = nameList.toArray(displayList);
        } else {
            for (int i = 0; i < nameList.size(); i++) {
                String name = nameList.get(i);
                NamedBean nBean = _manager.getBeanBySystemName(name);

                if (nBean != null) {
                    String uname = nBean.getUserName();
                    switch (_displayOrder) {
                        case DISPLAYNAME:
                            displayList[i] = nBean.getDisplayName();
                            break;

                        case USERNAME:
                            if (uname != null && !uname.equals("")) {
                                displayList[i] = uname;
                            } else {
                                displayList[i] = name;
                            }
                            break;

                        case USERNAMESYSTEMNAME:
                            if (uname != null && !uname.equals("")) {
                                displayList[i] = nBean.getUserName() + " - " + name;
                            } else {
                                displayList[i] = name;
                            }
                            break;

                        case SYSTEMNAMEUSERNAME:
                            if (uname != null && !uname.equals("")) {
                                displayList[i] = name + " - " + nBean.getUserName();
                            } else {
                                displayList[i] = name;
                            }
                            break;

                        default:
                            displayList[i] = nBean.getDisplayName();
                    }
                    displayToBean.put(displayList[i], nBean);
                }
            }
        }
        java.util.Arrays.sort(displayList, new AlphanumComparator());
        return displayList;
    }

    /**
     * Get the selected namedBean
     *
     * @return the selected bean or null if there is no selection
     */
    public NamedBean getSelectedBean() {
        String selectedName = (String) super.getSelectedItem();
        return displayToBean.get(selectedName);
    }

    /**
     * Get the User name of the selected namedBean
     *
     * @return the user name of the selected bean or null if there is no
     *         selection
     */
    public String getSelectedUserName() {
        String result = null;
        NamedBean nBean = getSelectedBean();
        if (nBean != null) {
            result = nBean.getDisplayName();
        }
        return result;
    }

    /**
     * Get the system name of the selected namedBean
     *
     * @return the system name of the selected bean or null if there is no
     *         selection
     */
    public String getSelectedSystemName() {
        String result = null;
        NamedBean nBean = getSelectedBean();
        if (nBean != null) {
            result = nBean.getSystemName();
        }
        return result;
    }

    /**
     * Get the display name of the selected namedBean
     *
     * @return the display name of the selected bean or null if there is no
     *         selection
     */
    public String getSelectedDisplayName() {
        String result = null;
        NamedBean nBean = getSelectedBean();
        if (nBean != null) {
            result = nBean.getDisplayName();
        }
        return result;
    }

    /**
     * Get the user name of the selection in this JmriBeanComboBox (based on
     * typed in text or drop down list).
     *
     * @return the username or null if no selection
     */
    public String getUserName() {
        String result = null;
        NamedBean b;

        if (isEditable()) {
            result = getEditor().getItem().toString();
            result = (null != result) ? result.trim() : "";

            b = getNamedBean();
        } else {
            b = getSelectedBean();
        }
        if (null != b) {
            result = b.getUserName();
        }
        return result;
    }   //getUserName

    /**
     * Get the display name for the selection in this JmriBeanComboBox (based on
     * typed in text or drop down list)
     *
     * @return the display name or null if no selection
     */
    public String getDisplayName() {
        String result = null;
        NamedBean b = null;

        if (isEditable()) {
            result = getEditor().getItem().toString();
            result = (null != result) ? result.trim() : "";

            b = getNamedBean();
        } else {
            b = getSelectedBean();
        }
        if (null != b) {
            result = b.getDisplayName();
        }
        return result;
    }   //getDisplayName

    /**
     * Get the display order of the combobox.
     *
     * @return the display order of this combobox
     */
    public DisplayOptions getDisplayOrder() {
        return _displayOrder;
    }

    /**
     * Set the display order of the combobox
     *
     * @param inDisplayOrder - the desired display order for this combobox
     */
    public void setDisplayOrder(DisplayOptions inDisplayOrder) {
        if (_displayOrder != inDisplayOrder) {
            NamedBean selectedBean = getSelectedBean();
            _displayOrder = inDisplayOrder;
            //refreshCombo();
            setSelectedBean(selectedBean);
        }
    }

    /**
     * Insert a blank entry at the top of the list
     *
     * @param blank true to insert, false to remove
     */
    public void setFirstItemBlank(boolean blank) {
        if (_firstBlank == blank) {
            return; // no Change to make
        }
        if (_firstBlank) {
            super.removeItemAt(0);
        } else {
            super.insertItemAt("", 0);
            if (_lastSelected == null || _lastSelected.equals("")) {
                setSelectedIndex(0);
            }
        }
        _firstBlank = blank;
    }

    public void setSelectedBean(NamedBean nBean) {
        String selectedItem = "";
        if (nBean != null) {
            String uname = nBean.getUserName();
            switch (_displayOrder) {
                case DISPLAYNAME:
                    selectedItem = nBean.getDisplayName();
                    break;

                case USERNAME:
                    selectedItem = nBean.getUserName();
                    break;

                case SYSTEMNAME:
                    selectedItem = nBean.getSystemName();
                    break;

                case USERNAMESYSTEMNAME:
                    if (uname != null && !uname.equals("")) {
                        selectedItem = uname + " - " + nBean.getSystemName();
                    } else {
                        selectedItem = nBean.getSystemName();
                    }
                    break;

                case SYSTEMNAMEUSERNAME:
                    if (uname != null && !uname.equals("")) {
                        selectedItem = nBean.getSystemName() + " - " + uname;
                    } else {
                        selectedItem = nBean.getSystemName();
                    }
                    break;

                default:
                    selectedItem = nBean.getDisplayName();
            }
        } else if (_firstBlank) {
            _lastSelected = "";
        }
        _lastSelected = selectedItem;
        updateComboBox(_lastSelected);
    }

    public void setSelectedBeanByName(String name) {
        if (name == null) {
            return;
        }
        NamedBean nBean = _manager.getNamedBean(name);
        setSelectedBean(nBean);
    }

    List<NamedBean> exclude = new ArrayList<NamedBean>();

    public void excludeItems(List<NamedBean> exclude) {
        this.exclude = exclude;
        _lastSelected = getSelectedDisplayName();
        updateComboBox(_lastSelected);
    }

    public List<NamedBean> getExcludeItems() {
        return this.exclude;
    }

    /**
     * validate mode determines if entry validation is performed when text is
     * typed into an editable JmriBeanComboBox
     */
    private boolean _validateMode = false;

    public void setValidateMode(boolean inValidateMode) {
        if (_validateMode != inValidateMode) {
            _validateMode = inValidateMode;
        }
    }

    public boolean isValidateMode() {
        return _validateMode;
    }

    // this is called to validate that the text in the textfield
    // is a valid member of the managed data.
    //note:  if _validateMode is true
    //           if text is valid set textfield background to green else red
    //       if _validateMode is false
    //           if text is valid set textfield background to green else yellow
    private void validateText() {
        ComboBoxEditor cbe = getEditor();
        JTextComponent c = (JTextComponent) cbe.getEditorComponent();
        String comboBoxText = cbe.getItem().toString();

        if (isEditable() && !comboBoxText.isEmpty()) {
            if (null != getNamedBean()) {
                c.setBackground(new Color(0xBDECB6));   //pastel green
            } else if (_validateMode) {
                c.setBackground(new Color(0xFFC0C0));   //pastel red
            } else {
                c.setBackground(new Color(0xFDFD96));   //pastel yellow
            }
        } else {
            c.setBackground(new Color(0xFFFFFF));   //white (pastel grey?)
        }
    }   //validateText

    /**
     * Get the bean for ether the typed in text or selected item from this
     * ComboBox.
     *
     * @return the selected bean or null if no selection
     */
    public NamedBean getNamedBean() {
        NamedBean result = null;

        jmri.Manager uDaManager = getManager();

        String comboBoxText = getEditor().getItem().toString();
        comboBoxText = (null != comboBoxText) ? comboBoxText.trim() : "";

        //try user name
        result = uDaManager.getBeanByUserName(comboBoxText);

        if (null == result) {
            //try system name
            //note: don't use getBeanBySystemName here
            //throws an IllegalArgumentException if text is invalid
            result = uDaManager.getNamedBean(comboBoxText);
        }

        if (null == result) {
            //quick search to see if text matches anything in the drop down list
            String[] displayList = getDisplayList();
            boolean found = false;  //assume failure (pessimist!)

            for (String item : displayList) {
                if (item.equals(comboBoxText)) {
                    found = true;
                    break;
                }
            }

            if (found) {    //if we found it there then…
                //walk the namedBeanList…
                List<NamedBean> namedBeanList = uDaManager.getNamedBeanList();

                for (NamedBean namedBean : namedBeanList) {
                    //checking to see if it matches "<sname> - <uname>" or "<uname> - <sname>"
                    String uname = namedBean.getUserName();
                    String sname = namedBean.getSystemName();

                    if ((null != uname) && (null != sname)) {
                        String usname = uname + " - " + sname;
                        String suname = sname + " - " + uname;

                        if (comboBoxText.equals(usname) || comboBoxText.equals(suname)) {
                            result = namedBean;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }   //getBean

    public enum DisplayOptions {
        /**
         * Format the entries in the combo box using the display name.
         */
        DISPLAYNAME(1),
        /**
         * Format the entries in the combo box using the username. If the
         * username value is blank for a bean then the system name is used.
         */
        USERNAME(2),
        /**
         * Format the entries in the combo box using the system name.
         */
        SYSTEMNAME(3),
        /**
         * Format the entries in the combo box with the username followed by the
         * system name.
         */
        USERNAMESYSTEMNAME(4),
        /**
         * Format the entries in the combo box with the system name followed by
         * the username.
         */
        SYSTEMNAMEUSERNAME(5);

        //
        // following code maps enumsto int and int to enum
        //
        private int value;

        private static final Map<Integer, DisplayOptions> enumMap;

        private DisplayOptions(int value) {
            this.value = value;
        }

        //Build an immutable map of String name to enum pairs.
        static {
            Map<Integer, DisplayOptions> map = new HashMap<>();

            for (DisplayOptions instance : DisplayOptions.values()) {
                map.put(instance.getValue(), instance);
            }
            enumMap = Collections.unmodifiableMap(map);
        }

        public static DisplayOptions valueOf(int displayOptionInt) {
            return enumMap.get(displayOptionInt);
        }

        public int getValue() {
            return value;
        }
    }

    public void dispose() {
        _manager.removePropertyChangeListener(this);
    }

    static class beanSelectionManager implements KeySelectionManager {

        long lastKeyTime = 0;
        String pattern = "";

        // FIXME: What is the correct type for the combo model here? This class may need refactored significantly to fix this?
        @Override
        public int selectionForKey(char aKey, @SuppressWarnings("rawtypes") javax.swing.ComboBoxModel model) {
            // Find index of selected item
            int selIx = 01;
            Object sel = model.getSelectedItem();
            if (sel != null) {
                for (int i = 0; i < model.getSize(); i++) {
                    if (sel.equals(model.getElementAt(i))) {
                        selIx = i;
                        break;
                    }
                }
            }

            // Get the current time
            long curTime = System.currentTimeMillis();

            // If last key was typed less than 300 ms ago, append to current pattern
            if (curTime - lastKeyTime < 300) {
                pattern += ("" + aKey).toLowerCase();
            } else {
                pattern = ("" + aKey).toLowerCase();
            }

            // Save current time
            lastKeyTime = curTime;

            // Search forward from current selection
            for (int i = selIx + 1; i < model.getSize(); i++) {
                String s = model.getElementAt(i).toString().toLowerCase();
                if (s.startsWith(pattern)) {
                    return i;
                }
            }

            // Search from top to current selection
            for (int i = 0; i < selIx; i++) {
                if (model.getElementAt(i) != null) {
                    String s = model.getElementAt(i).toString().toLowerCase();
                    if (s.startsWith(pattern)) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(JmriBeanComboBox.class.getName());
}
