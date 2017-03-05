package jmri.util.swing;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JComboBox.KeySelectionManager;
import jmri.NamedBean;
import jmri.util.AlphanumComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JComboBox varient for showing and selecting JMRI NamedBeans
 * from a specific manager.
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

    HashMap<String, NamedBean> displayToBean = new HashMap<String, NamedBean>();

    public jmri.Manager getManager()
    {
        return _manager;
    }
    
    public void refreshCombo() {
        updateComboBox((String) getSelectedItem());
    }

    void updateComboBox(String select) {
        displayToBean = new HashMap<String, NamedBean>();
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
        ArrayList<String> nameList = new ArrayList<String>(Arrays.asList(_manager.getSystemNameArray()));

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
                NamedBean nBean = null;
                nBean = _manager.getBeanBySystemName(name);

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
    public NamedBean getSelectedNamedBean() {
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
        NamedBean nBean = getSelectedNamedBean();
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
        NamedBean nBean = getSelectedNamedBean();
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
        NamedBean nBean = getSelectedNamedBean();
        if (nBean != null) {
            result = nBean.getDisplayName();
        }
        return result;
   }

    /**
     * Get the display order of the combobox
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

    public NamedBean getSelectedBean() {
        String selectedName = (String) super.getSelectedItem();
        return displayToBean.get(selectedName);
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

    public List<NamedBean> getExcludeItems()
    {
        return this.exclude;
    }

    public enum DisplayOptions {
        /**
         * Format the entries in the combo box using the
         * displayname
         */
        DISPLAYNAME(1),
        /**
         * Format the entries in the combo box using the username.
         * If the username value is blank for a bean then the system name is used
         */
        USERNAME(2),

        /**
         * Format the entries in the combo box using the systemname
         */
        SYSTEMNAME(3),

        /**
         * Format the entries in the combo box with the username
         * followed by the systemname
         */
        USERNAMESYSTEMNAME(4),

        /**
         * Format the entries in the combo box with the systemname
         * followed by the userame
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
            Map<Integer, DisplayOptions> map = new HashMap<Integer, DisplayOptions>();

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
