package jmri.swing;

import com.alexandriasoftware.swing.JInputValidatorPreferences;
import com.alexandriasoftware.swing.NonVerifyingValidator;
import com.alexandriasoftware.swing.Validation;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import jmri.Manager;
import jmri.NamedBean;
import jmri.util.NamedBeanComparator;
import jmri.util.NamedBeanUserNameComparator;
import jmri.util.ThreadingPropertyChangeListener;

/**
 * A {@link javax.swing.JComboBox} for {@link jmri.NamedBean}s.
 * <p>
 * Validation of user input to select a NamedBean is limited to setting the
 * selection to a NamedBean matching the typed input, and is always enabled
 * unless {@link #setEditable(boolean)} is called against this JComboBox with
 * {@code false}. API hooks exist for more complex validation, although they
 * currently do nothing.
 * <p>
 * <strong>Note:</strong> It is recommended that implementations that exclude
 * some NamedBeans from the combo box call {@link #setToolTipText(String)} to
 * provide a context specific reason for excluding those items. The default tool
 * tip reads (example for Turnouts) "Turnouts not shown cannot be used in this
 * context.", but a better tool tip (example for Signal Heads when creating a
 * Signal Mast) may be "Signal Heads not shown are assigned to another Signal
 * Mast."
 *
 * @param <B> the supported type of NamedBean
 */
public class NamedBeanComboBox<B extends NamedBean> extends JComboBox<B> {

    private final Manager<B> manager;
    private DisplayOptions displayOptions;
    private boolean allowNull = false;
    private boolean validatingInput = false;
    private String beanInUse = "NamedBeanComboBoxBeanInUse";
    private String noMatchingBean = "NamedBeanComboBoxNoMatchingBean";
    private final Set<B> excludedItems = new HashSet<>();
    private final PropertyChangeListener managerListener = ThreadingPropertyChangeListener.guiListener(evt -> sort());

    /**
     * Create a ComboBox without a selection using the
     * {@link DisplayOptions#DISPLAYNAME} to sort NamedBeans.
     *
     * @param manager the Manager backing the ComboBox
     */
    public NamedBeanComboBox(Manager<B> manager) {
        this(manager, null);
    }

    /**
     * Create a ComboBox with an existing selection using the
     * {@link DisplayOptions#DISPLAYNAME} to sort NamedBeans.
     *
     * @param manager   the Manager backing the ComboBox
     * @param selection the NamedBean that is selected or null to specify no
     *                  selection
     */
    public NamedBeanComboBox(Manager<B> manager, B selection) {
        this(manager, selection, DisplayOptions.DISPLAYNAME);
    }

    /**
     * Create a ComboBox with an existing selection using the specified display
     * order to sort NamedBeans.
     *
     * @param manager      the Manager backing the ComboBox
     * @param selection    the NamedBean that is selected or null to specify no
     *                     selection
     * @param displayOrder the sorting scheme for NamedBeans
     */
    public NamedBeanComboBox(Manager<B> manager, B selection, DisplayOptions displayOrder) {
        this.manager = manager;
        setToolTipText(Bundle.getMessage("NamedBeanComboBoxDefaultToolTipText", this.manager.getBeanTypeHandled(true)));
        setDisplayOrder(displayOrder);
        setEditable(false);
        NamedBeanRenderer renderer = new NamedBeanRenderer();
        setRenderer(renderer);
        setKeySelectionManager(renderer);
        this.manager.addPropertyChangeListener("beans", managerListener);
        this.manager.addPropertyChangeListener("DisplayListName", managerListener);
        sort();
        setSelectedItem(selection);
    }

    public Manager<B> getManager() {
        return manager;
    }

    public DisplayOptions getDisplayOrder() {
        return displayOptions;
    }

    public void setDisplayOrder(DisplayOptions displayOrder) {
        if (displayOptions != displayOrder) {
            displayOptions = displayOrder;
            sort();
        }
    }

    /**
     * Is this JComboBox validating typed input?
     *
     * @return true if validating input; false otherwise
     */
    public boolean isValidatingInput() {
        return validatingInput;
    }

    /**
     * Set if this JComboBox validates typed input.
     *
     * @param validatingInput true to validate; false to prevent validation
     */
    public void setValidatingInput(boolean validatingInput) {
        this.validatingInput = validatingInput;
    }

    /**
     * Is this JComboBox allowing a null object to be selected?
     *
     * @return true if allowing a null selection; false otherwise
     */
    public boolean isAllowNull() {
        return allowNull;
    }

    /**
     * Set if this JComboBox allows a null object to be selected. If so, the
     * null object is placed first in the displayed list of NamedBeans.
     *
     * @param allowNull true if allowing a null selection; false otherwise
     */
    public void setAllowNull(boolean allowNull) {
        this.allowNull = allowNull;
    }

    /**
     * {@inheritDoc}
     *
     * @return the selected item as the supported type of NamedBean or null if
     *         there is no selection, or {@link #isAllowNull()} is true and the
     *         null object is selected
     */
    @Override
    public B getSelectedItem() {
        return getItemAt(getSelectedIndex());
    }

    @Override
    public void setEditable(boolean editable) {
        Component ec = getEditor().getEditorComponent();
        if (editable && ec instanceof JTextComponent) {
            JTextComponent jtc = (JTextComponent) ec;
            jtc.setInputVerifier(new NonVerifyingValidator(jtc) {
                @Override
                protected Validation getValidation(JComponent componenet, JInputValidatorPreferences preferences) {
                    String text = jtc.getText();
                    if (validatingInput && text != null && !text.isEmpty()) {
                        B bean = manager.getNamedBean(text);
                        if (bean != null && excludedItems.contains(bean)) {
                            return new Validation(Validation.Type.DANGER, Bundle.getMessage(beanInUse, manager.getBeanTypeHandled(), bean.getFullyFormattedDisplayName()));
                        } else if (bean == null) {
                            return new Validation(Validation.Type.DANGER, Bundle.getMessage(noMatchingBean, manager.getBeanTypeHandled(), text));
                        }
                    }
                    return new Validation(Validation.Type.NONE, ""); // NOI18N
                }
            });
        } else {
            ((JComponent) super.getEditor().getEditorComponent()).setInputVerifier(null);
        }
        super.setEditable(editable);
    }

    /**
     * Get the display name of the selected item.
     *
     * @return the display name of the selected item or null if the selected
     *         item is null or there is no selection
     */
    public String getSelectedItemDisplayName() {
        B item = getSelectedItem();
        return item != null ? item.getDisplayName() : null;
    }

    /**
     * Get the system name of the selected item.
     *
     * @return the system name of the selected item or null if the selected item
     *         is null or there is no selection
     */
    public String getSelectedItemSystemName() {
        B item = getSelectedItem();
        return item != null ? item.getSystemName() : null;
    }

    /**
     * Get the user name of the selected item.
     *
     * @return the user name of the selected item or null if the selected item
     *         is null or there is no selection
     */
    public String getSelectedItemUserName() {
        B item = getSelectedItem();
        return item != null ? item.getUserName() : null;
    }

    /**
     * Set the selected item by either its user name or system name.
     *
     * @param name the name of the item to select
     * @throws IllegalArgumentException if {@link #isAllowNull()} is false and
     *                                  no bean exists by name or name is null
     */
    public void setSelectedItemByName(String name) {
        B item = null;
        if (name != null) {
            item = manager.getNamedBean(name);
        }
        if (item == null && !allowNull) {
            throw new IllegalArgumentException();
        }
        setSelectedItem(item);
    }

    public void dispose() {
        manager.removePropertyChangeListener(managerListener);
    }

    private void sort() {
        B selectedItem = getSelectedItem();
        Comparator<B> comparator = new NamedBeanComparator<>();
        if (displayOptions == DisplayOptions.USERNAME || displayOptions == DisplayOptions.USERNAMESYSTEMNAME) {
            comparator = new NamedBeanUserNameComparator<>();
        }
        TreeSet<B> set = new TreeSet<>(comparator);
        set.addAll(manager.getNamedBeanSet());
        set.removeAll(excludedItems);
        Vector<B> vector = new Vector<>(set);
        if (allowNull) {
            vector.insertElementAt(null, 0);
        }
        setModel(new DefaultComboBoxModel<>(vector));
        setSelectedItem(selectedItem); // retain selection
    }

    private void validateInput() {
        ComboBoxEditor cbe = getEditor();
        JTextField c = (JTextField) cbe.getEditorComponent();
        String text = c.getText();
        if (isEditable() && !text.isEmpty()) {
            B item = manager.getNamedBean(text);
            if (item != null) {
                setSelectedItem(item);
            }
        }
    }

    /**
     * Set the translation key to be used when a typed in bean name matches a
     * named bean has been included in a call to
     * {@link #setExcludedItems(java.util.Set)} and {@link #isValidatingInput()}
     * is {@code true}.
     *
     * @param beanInUseKey a translatable bundle key where {@code {0}} is the
     *                     result of {@link jmri.Manager#getBeanTypeHandled()}
     *                     and {1} is the result of
     *                     {@link jmri.NamedBean#getFullyFormattedDisplayName()}
     *                     for the matching bean
     */
    public void setNoMatchingToolTipBeanInUse(String beanInUseKey) {
        beanInUse = beanInUseKey;
    }

    /**
     * Set the translation key to be used when a typed in bean name does not
     * match a named bean and {@link #isValidatingInput()} is {@code true}.
     *
     * @param noMatchingBeanKey a translatable bundle key where {@code {0}} is the
     *                     result of {@link jmri.Manager#getBeanTypeHandled()}
     *                     and {1} is the typed input
     */
    public void setNoMatchingToolTipNoMatchingBean(String noMatchingBeanKey) {
        noMatchingBean = noMatchingBeanKey;
    }

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
        private final int value;
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

    private class NamedBeanRenderer implements ListCellRenderer<B>, JComboBox.KeySelectionManager {

        protected DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        private final long timeFactor;
        private long lastTime;
        private long time;
        private String prefix = "";

        public NamedBeanRenderer() {
            Long l = (Long) UIManager.get("ComboBox.timeFactor");
            timeFactor = l != null ? l : 1000;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends B> list, B value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                switch (displayOptions) {
                    case SYSTEMNAMEUSERNAME:
                        label.setText(value.getFullyFormattedDisplayName(false));
                        break;
                    case DISPLAYNAME:
                        label.setText(value.getDisplayName());
                        break;
                    case USERNAME:
                        String userName = value.getUserName();
                        label.setText((userName != null && !userName.isEmpty()) ? userName : value.getSystemName());
                        break;
                    case USERNAMESYSTEMNAME:
                        label.setText(value.getFullyFormattedDisplayName());
                        break;
                    case SYSTEMNAME:
                    default:
                        label.setText(value.getSystemName());
                }
            }
            return label;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked") // unchecked cast due to API constraints
        public int selectionForKey(char key, ComboBoxModel model) {
            time = System.currentTimeMillis();

            //  Get the index of the currently selected item
            int size = model.getSize();
            int startIndex = -1;
            B selectedItem = (B) model.getSelectedItem();

            if (selectedItem != null) {
                for (int i = 0; i < size; i++) {
                    if (selectedItem == model.getElementAt(i)) {
                        startIndex = i;
                        break;
                    }
                }
            }

            //  Determine the "prefix" to be used when searching the model. The
            //  prefix can be a single letter or multiple letters depending on how
            //  fast the user has been typing and on which letter has been typed.
            if (time - lastTime < timeFactor) {
                if ((prefix.length() == 1) && (key == prefix.charAt(0))) {
                    // Subsequent same key presses move the keyboard focus to the next
                    // object that starts with the same letter.
                    startIndex++;
                } else {
                    prefix += key;
                }
            } else {
                startIndex++;
                prefix = "" + key;
            }

            lastTime = time;

            //  Search from the current selection and wrap when no match is found
            if (startIndex < 0 || startIndex >= size) {
                startIndex = 0;
            }

            int index = getNextMatch(prefix, startIndex, size, model);

            if (index < 0) {
                // wrap
                index = getNextMatch(prefix, 0, startIndex, model);
            }

            return index;
        }

        /*
	**  Find the index of the item in the model that starts with the prefix.
         */
        @SuppressWarnings("unchecked") // unchecked cast due to API constraints
        private int getNextMatch(String prefix, int start, int end, ComboBoxModel model) {
            for (int i = start; i < end; i++) {
                B item = (B) model.getElementAt(i);

                if (item != null) {
                    String userName = item.getUserName();

                    if (item.getSystemName().toLowerCase().startsWith(prefix)
                            || (userName != null && userName.toLowerCase().startsWith(prefix))) {
                        return i;
                    }
                }
            }

            return -1;
        }
    }

    public Set<B> getExcludedItems() {
        return excludedItems;
    }

    /**
     * Collection of named beans managed by the manager for this combo box that
     * should not be included in the combo box. This may be, for example, a list
     * of SignalHeads already in use, and therefor not available to be added to
     * a SignalMast.
     *
     * @param excludedItems items to be excluded from this combo box
     */
    public void setExcludedItems(Set<B> excludedItems) {
        this.excludedItems.clear();
        this.excludedItems.addAll(excludedItems);
        sort();
    }

}
