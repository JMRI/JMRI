package jmri.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckReturnValue;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.JTextComponent;
import jmri.Manager;
import jmri.NamedBean;
import jmri.util.AlphanumComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JComboBox variant for showing and selecting JMRI NamedBeans from a specific
 * manager.
 */
public class JmriBeanComboBox extends JComboBox<String> implements java.beans.PropertyChangeListener {

    /**
     * Create a default Jmri Combo box for the given bean manager.
     *
     * @param inManager the jmri manager that is used to populate the combo box
     */
    public JmriBeanComboBox(Manager inManager) {
        this(inManager, null, DisplayOptions.DISPLAYNAME);
    }

    /**
     * Create a Jmri Combo box for the given bean manager, with the Namedbean
     * nBean already selected and the items displayed and ordered by
     * displayOrder.
     *
     * @param inManager      the jmri manager that is used to populate the combo
     *                       box
     * @param inNamedBean    the namedBean that should automatically be selected
     * @param inDisplayOrder the way in which the namedbeans should be
     *                       displayed: by System or Display Name
     */
    public JmriBeanComboBox(Manager inManager, NamedBean inNamedBean, DisplayOptions inDisplayOrder) {
        _displayOrder = inDisplayOrder;
        _manager = inManager;
        setSelectedBean(inNamedBean);
        //setEditable(true);
        _manager.addPropertyChangeListener(new DedupingPropertyChangeListener(this));
        setKeySelectionManager(new BeanSelectionManager());

        //fires when drop down list item is selected
        addItemListener((ItemEvent event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                validateText();
            }
        });

        //fires when key is released while typing in combox editor
        getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
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
            Object oldValueObject = e.getOldValue();
            int oldValueInt = (oldValueObject == null) ? 0 : Integer.parseInt(oldValueObject.toString());
            Object newValueObject = e.getNewValue();
            int newValueInt = (newValueObject == null) ? 0 : Integer.parseInt(newValueObject.toString());
            if (oldValueInt < newValueInt) {
                addSelectionInterval(oldValueInt, newValueInt);
            } else if (oldValueInt > newValueInt) {
                removeSelectionInterval(newValueInt, oldValueInt);
            }
        } else if (e.getPropertyName().equals("DisplayListName")) {
            refreshCombo();
        }
    }

    private String _lastSelected = "";
    private DisplayOptions _displayOrder;
    private boolean _firstBlank = false;

    private Manager _manager;

    private HashMap<String, NamedBean> displayToBean = new HashMap<>();

    public Manager getManager() {
        return _manager;
    }

    public void refreshCombo() {
        updateComboBox((String) getSelectedItem());
    }

    private void updateComboBox(String inSelect) {
        displayToBean = new HashMap<>();
        removeAllItems();

        String[] displayList = getDisplayList();

        for (int i = 0; i < displayList.length; i++) {
            addItem(displayList[i]);
            if ((inSelect != null) && (displayList[i].equals(inSelect))) {
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
     * Get the display list used by this combo box.
     *
     * @return the display list used by this combo box
     */
    public String[] getDisplayList() {
        ArrayList<String> nameList = new ArrayList<>(Arrays.asList(_manager.getSystemNameArray()));

        exclude.stream().filter((bean) -> (bean != null)).forEachOrdered((bean) -> {
            nameList.remove(bean.getSystemName());
        });

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

                        case DISPLAYNAME:
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
     * Get the selected namedBean.
     *
     * @return the selected bean or null if there is no selection
     */
    public NamedBean getSelectedBean() {
        String selectedName = (String) super.getSelectedItem();
        return displayToBean.get(selectedName);
    }

    /**
     * Get the User Name of the selected namedBean.
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
     * Get the System Name of the selected namedBean.
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
     * Get the Display Name of the selected namedBean.
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
     * Get the User Name of the selection in this JmriBeanComboBox (based on
     * typed in text or drop down list).
     *
     * @return the username or null if no selection
     */
    public String getUserName() {
        String result = null;
        NamedBean b;

        if (isEditable()) {
            result = NamedBean.normalizeUserName(getText());
            if (result == null) {
                result = "";
            }

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
     * typed in text or drop down list).
     *
     * @return the display name or null if no selection
     */
    @CheckReturnValue
    public String getDisplayName() {
        String result = null;
        NamedBean b;

        if (isEditable()) {
            result = NamedBean.normalizeUserName(getText());
            if (result == null) {
                result = "";
            }
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
     * Get the text from the editor for this JmriBeanComboBox.
     *
     * @return the text
     */
    public String getText() {
        return getEditor().getItem().toString();
    }   // getText

    /**
     * Set the text from the editor for this JmriBeanComboBox
     *
     * @param inText the text to set
     */
    public void setText(String inText) {
        getEditor().setItem(inText);
        if ((inText != null) && !inText.isEmpty()) {
            setSelectedBeanByName(inText);
        } else {
            setSelectedIndex(-1);
        }
        validateText();
    }   // setText

    /**
     * Get the display order of the combobox.
     *
     * @return the display order of this combobox
     */
    public DisplayOptions getDisplayOrder() {
        return _displayOrder;
    }

    /**
     * Set the display order of the combobox.
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
     * Insert a blank entry at the top of the list.
     *
     * @param inFirstItemBlank true to insert, false to remove
     */
    public void setFirstItemBlank(boolean inFirstItemBlank) {
        if (_firstBlank == inFirstItemBlank) {
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
        _firstBlank = inFirstItemBlank;
    }

    public void setSelectedBean(NamedBean inNamedBean) {
        String selectedItem = "";
        if (inNamedBean != null) {
            String uname = inNamedBean.getUserName();
            switch (_displayOrder) {
                case DISPLAYNAME:
                    selectedItem = inNamedBean.getDisplayName();
                    break;

                case USERNAME:
                    selectedItem = inNamedBean.getUserName();
                    break;

                case SYSTEMNAME:
                    selectedItem = inNamedBean.getSystemName();
                    break;

                case USERNAMESYSTEMNAME:
                    if (uname != null && !uname.equals("")) {
                        selectedItem = uname + " - " + inNamedBean.getSystemName();
                    } else {
                        selectedItem = inNamedBean.getSystemName();
                    }
                    break;

                case SYSTEMNAMEUSERNAME:
                    if (uname != null && !uname.equals("")) {
                        selectedItem = inNamedBean.getSystemName() + " - " + uname;
                    } else {
                        selectedItem = inNamedBean.getSystemName();
                    }
                    break;

                default:
                    selectedItem = inNamedBean.getDisplayName();
            }
        } else if (_firstBlank) {
            _lastSelected = "";
        }
        _lastSelected = selectedItem;
        updateComboBox(_lastSelected);
    }

    public void setSelectedBeanByName(String inBeanName) {
        if (inBeanName == null) {
            return;
        }
        NamedBean nBean = _manager.getNamedBean(inBeanName);
        setSelectedBean(nBean);
    }

    List<NamedBean> exclude = new ArrayList<>();

    public void excludeItems(List<NamedBean> inExcludeList) {
        this.exclude = inExcludeList;
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
    // note:  if _validateMode is true
    //           if text is valid set textfield background to green else red
    //       if _validateMode is false
    //           if text is valid set textfield background to green else yellow
    private void validateText() {
        ComboBoxEditor cbe = getEditor();
        JTextComponent c = (JTextComponent) cbe.getEditorComponent();
        String comboBoxText = cbe.getItem().toString();

        if (isEditable() && !comboBoxText.isEmpty()) {
            setOpaque(true);
            if (null != getNamedBean()) {
                c.setBackground(new Color(0xBDECB6));   //pastel green
            } else if (_validateMode) {
                c.setBackground(new Color(0xFFC0C0));   //pastel red
            } else {
                c.setBackground(new Color(0xFDFD96));   //pastel yellow
            }
        } else {
            setOpaque(false);
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
        NamedBean result;

        Manager uDaManager = getManager();

        String comboBoxText = NamedBean.normalizeUserName(getText());
        if (comboBoxText != null) {

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

                if (found) {    //if we found it there then...
                    //walk the namedBeanList...
                    List<NamedBean> namedBeanList = uDaManager.getNamedBeanList();

                    for (NamedBean namedBean : namedBeanList) {
                        //checking to see if it matches "<sname> - <uname>" or "<uname> - <sname>"
                        String uname = namedBean.getUserName();
                        String sname = namedBean.getSystemName();

                        if ((null != uname)) {
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
        }
        return null;
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

        public static DisplayOptions valueOf(int inDisplayOptionInt) {
            return enumMap.get(inDisplayOptionInt);
        }

        public int getValue() {
            return value;
        }
    }

    public void dispose() {
        _manager.removePropertyChangeListener(this);

    }

    static class BeanSelectionManager implements KeySelectionManager {

        long lastKeyTime = 0;
        String pattern = "";

        // FIXME: What is the correct type for the combo model here? This class may need refactored significantly to fix this?
        @Override
        public int selectionForKey(char inKey, @SuppressWarnings("rawtypes") javax.swing.ComboBoxModel model) {
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
                pattern += ("" + inKey).toLowerCase();
            } else {
                pattern = ("" + inKey).toLowerCase();
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
    }   // BeanSelectionManager

    public void setEnabledItems(ListSelectionModel inEnabledItems) {
        getEnabledComboBoxRenderer().setEnabledItems(inEnabledItems);
    }

    public ListSelectionModel getEnabledItems() {
        return getEnabledComboBoxRenderer().getEnabledItems();
    }

    public void addSelectionInterval(int inMinIndex, int inMaxIndex) {
        ListSelectionModel lsm = getEnabledItems();
        if (lsm != null) {
            lsm.addSelectionInterval(inMinIndex, inMaxIndex);
        }
    }

    public void removeSelectionInterval(int inMinIndex, int inMaxIndex) {
        ListSelectionModel lsm = getEnabledItems();
        if (lsm != null) {
            lsm.removeSelectionInterval(inMinIndex, inMaxIndex);
        }
    }

    public void setItemEnabled(int inIndex, boolean inEnabled) {
        ListSelectionModel lsm = getEnabledItems();
        if (lsm != null) {
            if (inEnabled) {
                lsm.addSelectionInterval(inIndex, inIndex);
            } else {
                lsm.removeSelectionInterval(inIndex, inIndex);
            }
        }
    }

    public boolean isItemEnabled(int inIndex) {
        boolean result = false;
        ListSelectionModel lsm = getEnabledItems();
        if (lsm != null) {
            result = lsm.isSelectedIndex(inIndex);
        }
        return result;
    }

    public void enableItem(int inIndex) {
        setItemEnabled(inIndex, true);
    }

    public void disableItem(int inIndex) {
        setItemEnabled(inIndex, false);
    }

    public void setEnabledColor(Color inEnabledColor) {
        getEnabledComboBoxRenderer().setEnabledColor(inEnabledColor);
    }

    public Color getEnabledColor() {
        return getEnabledComboBoxRenderer().getEnabledColor();
    }

    public void setDisabledColor(Color inDisabledColor) {
        getEnabledComboBoxRenderer().setDisabledColor(inDisabledColor);
    }

    public Color getDisabledColor() {
        return getEnabledComboBoxRenderer().getDisabledColor();
    }

    public void setEnabledBackgroundColor(Color inEnabledBackgroundColor) {
        getEnabledComboBoxRenderer().setEnabledBackgroundColor(inEnabledBackgroundColor);
    }

    public Color getEnabledBackgroundColor() {
        return getEnabledComboBoxRenderer().getEnabledBackgroundColor();
    }

    public void setDisabledBackgroundColor(Color inDisabledBackgroundColor) {
        getEnabledComboBoxRenderer().setDisabledBackgroundColor(inDisabledBackgroundColor);
    }

    public Color getDisabledBackgroundColor() {
        return getEnabledComboBoxRenderer().getDisabledBackgroundColor();
    }

    /**
     * Use {@link #getEnabledComboBoxRenderer() } exclusively to access this
     * object.
     */
    private EnabledComboBoxRenderer _enableRenderer = null;

    private EnabledComboBoxRenderer getEnabledComboBoxRenderer() {
        if (_enableRenderer == null) {
            _enableRenderer = new EnabledComboBoxRenderer();
            setRenderer(_enableRenderer);
            ListSelectionModel lsm = _enableRenderer.getEnabledItems();
            lsm.addSelectionInterval(0, _manager.getNamedBeanList().size());
        }
        return _enableRenderer;
    }

    static class EnabledComboBoxRenderer extends BasicComboBoxRenderer {

        private ListSelectionModel _enabledItems;
        private Color _enabledColor = super.getForeground();
        private Color _disabledColor = Color.lightGray;
        private Color _enabledBackgroundColor = super.getBackground();
        private Color _disabledBackgroundColor = super.getBackground();

        public EnabledComboBoxRenderer() {
            _enabledItems = new DefaultListSelectionModel();
        }

        public EnabledComboBoxRenderer(ListSelectionModel inEnabledItems) {
            super();
            _enabledItems = inEnabledItems;
        }

        public void setEnabledItems(ListSelectionModel inEnabledItems) {
            _enabledItems = inEnabledItems;
        }

        public ListSelectionModel getEnabledItems() {
            return _enabledItems;
        }

        public void setItemEnabled(int inIndex, boolean inEnabled) {
            if (_enabledItems != null) {
                if (inEnabled) {
                    _enabledItems.addSelectionInterval(inIndex, inIndex);
                } else {
                    _enabledItems.removeSelectionInterval(inIndex, inIndex);
                }
            }
        }

        public boolean isItemEnabled(int inIndex) {
            boolean result = false;
            if (_enabledItems != null) {
                result = _enabledItems.isSelectedIndex(inIndex);
            }
            return result;
        }

        public void setEnabledColor(Color inEnabledColor) {
            _enabledColor = inEnabledColor;
        }

        public Color getEnabledColor() {
            return _enabledColor;
        }

        public void setDisabledColor(Color inDisabledColor) {
            _disabledColor = inDisabledColor;
        }

        public Color getDisabledColor() {
            return _disabledColor;
        }

        public void setEnabledBackgroundColor(Color inEnabledBackgroundColor) {
            _enabledBackgroundColor = inEnabledBackgroundColor;
        }

        public Color getEnabledBackgroundColor() {
            return _enabledBackgroundColor;
        }

        public void setDisabledBackgroundColor(Color inDisabledBackgroundColor) {
            _disabledBackgroundColor = inDisabledBackgroundColor;
        }

        public Color getDisabledBackgroundColor() {
            return _disabledBackgroundColor;
        }

        @Override
        public Component getListCellRendererComponent(JList inList, Object inValue,
                int inIndex, boolean isSelected, boolean inCellHasFocus) {

            Component c = super.getListCellRendererComponent(inList, inValue, inIndex,
                    isSelected, inCellHasFocus);

            if (_enabledItems.isSelectedIndex(inIndex)) {
                c.setBackground(_enabledBackgroundColor);
                c.setForeground(_enabledColor);
            } else {    // not enabled
                if (isSelected) {
                    c.setBackground(UIManager.getColor("ComboBox.background"));
                } else {
                    c.setBackground(_disabledBackgroundColor);
                }
                c.setForeground(_disabledColor);
            }
            return c;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(JmriBeanComboBox.class);

}
