package jmri.swing;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import com.alexandriasoftware.swing.JInputValidatorPreferences;
import com.alexandriasoftware.swing.JInputValidator;
import com.alexandriasoftware.swing.Validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Manager;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.NamedBean.DisplayOptions;
import jmri.util.NamedBeanComparator;
import jmri.util.NamedBeanUserNameComparator;
import jmri.util.ThreadingPropertyChangeListener;

/**
 * A {@link javax.swing.JComboBox} for {@link jmri.NamedBean}s.
 * <p>
 * When editable, this will create a new NamedBean if backed by a
 * {@link jmri.ProvidingManager} if {@link #getSelectedItem()} is called and the
 * current text is neither the system name nor user name of an existing
 * NamedBean. This will also validate input when editable, showing an
 * Information (blue I in circle) icon to indicate a name will be used to create
 * a new Named Bean, an Error (red X in circle) icon to indicate a typed in name
 * cannot be used (either because it would not be valid as a user name or system
 * name or because the name of an existing NamedBean not usable in the current
 * context has been entered, or no icon to indicate the name of an existing
 * Named Bean has been entered.
 * <p>
 * When not editable, this will allow (but may not actively show) continual
 * typing of a system name or a user name by a user to match a NamedBean even if
 * only the system name or user name or both are displayed (e.g. if a list of
 * turnouts is shown by user name only, a user may type in the system name of
 * the turnout and the turnout will be selected correctly). If the typing speed
 * is slower than the {@link javax.swing.UIManager}'s
 * {@code ComboBox.timeFactor} setting, keyboard input acts like a normal
 * JComboBox, with only the first character displayed matching the user input.
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
    private boolean providing = true;
    private boolean validatingInput = true;
    private String beanInUse = "NamedBeanComboBoxBeanInUse";
    private String invalidNameFormat = "NamedBeanComboBoxInvalidNameFormat";
    private String noMatchingBean = "NamedBeanComboBoxNoMatchingBean";
    private String willCreateBean = "NamedBeanComboBoxWillCreateBean";
    private final Set<B> excludedItems = new HashSet<>();
    private final PropertyChangeListener managerListener = ThreadingPropertyChangeListener.guiListener(evt -> sort());
    private final static Logger log = LoggerFactory.getLogger(NamedBeanComboBox.class);

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
        super();
        this.manager = manager;
        setToolTipText(Bundle.getMessage("NamedBeanComboBoxDefaultToolTipText", this.manager.getBeanTypeHandled(true)));
        setDisplayOrder(displayOrder);
        setEditable(false);
        NamedBeanRenderer namedBeanRenderer = new NamedBeanRenderer(getRenderer());
        setRenderer(namedBeanRenderer);
        setKeySelectionManager(namedBeanRenderer);
        Component ec = getEditor().getEditorComponent();
        if (ec instanceof JComponent) {
            JComponent jc = (JComponent) ec;
            jc.setInputVerifier(new JInputValidator(jc, true, false) {
                @Override
                protected Validation getValidation(JComponent component, JInputValidatorPreferences preferences) {
                    if (component instanceof JTextComponent) {
                        JTextComponent jtc = (JTextComponent) component;
                        String text = jtc.getText();
                        if (text != null && !text.isEmpty()) {
                            B bean = manager.getNamedBean(text);
                            if (bean != null) {
                                setSelectedItem(bean); // won't change if bean is not in model
                                if (!bean.equals(getSelectedItem())) {
                                    jtc.setText(text);
                                    if (validatingInput) {
                                        return new Validation(Validation.Type.DANGER, Bundle.getMessage(beanInUse,
                                                manager.getBeanTypeHandled(), bean.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
                                    }
                                }
                            } else {
                                if (validatingInput) {
                                    if (providing) {
                                        if (!manager.isValidSystemNameFormat(text) && !text.equals(NamedBean.normalizeUserName(text))) {
                                            return new Validation(Validation.Type.DANGER,
                                                    Bundle.getMessage(invalidNameFormat, manager.getBeanTypeHandled(), text), preferences);
                                        }
                                        return new Validation(Validation.Type.INFORMATION,
                                                Bundle.getMessage(willCreateBean, manager.getBeanTypeHandled(), text), preferences);
                                    } else {
                                        return new Validation(Validation.Type.WARNING,
                                                Bundle.getMessage(noMatchingBean, manager.getBeanTypeHandled(), text), preferences);
                                    }
                                }
                            }
                        }
                    }
                    return getNoneValidation();
                }
            });
        }
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

    public final void setDisplayOrder(DisplayOptions displayOrder) {
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
     * @return the selected item as the supported type of NamedBean, creating a
     *         new NamedBean as needed if {@link #isEditable()} and
     *         {@link #isProviding()} are true, or null if there is no
     *         selection, or {@link #isAllowNull()} is true and the null object
     *         is selected
     */
    @Override
    public B getSelectedItem() {
        B item = getItemAt(getSelectedIndex());
        if (isEditable() && providing && item == null) {
            Component ec = getEditor().getEditorComponent();
            if (ec instanceof JTextComponent && manager instanceof ProvidingManager) {
                JTextComponent jtc = (JTextComponent) ec;
                String text = jtc.getText();
                if (text != null && !text.isEmpty()) {
                    if ((manager.isValidSystemNameFormat(text)) || text.equals(NamedBean.normalizeUserName(text))) {
                        ProvidingManager<B> pm = (ProvidingManager<B>) manager;
                        item = pm.provide(jtc.getText());
                    }
                }
            }
        }
        return item;
    }

    /**
     * Check if new NamedBeans can be provided by a
     * {@link jmri.ProvidingManager} when {@link #isEditable} returns
     * {@code true}.
     *
     * @return {@code true} is allowing new NamedBeans to be provided;
     *         {@code false} otherwise
     */
    public boolean isProviding() {
        return providing;
    }

    /**
     * Set if new NamedBeans can be provided by a {@link jmri.ProvidingManager}
     * when {@link #isEditable()} returns {@code true}.
     *
     * @param providing {@code true} to allow new NamedBeans to be provided;
     *                  {@code false} otherwise
     */
    public void setProviding(boolean providing) {
        this.providing = providing;
    }

    @Override
    public void setEditable(boolean editable) {
        if (editable && !(manager instanceof ProvidingManager)) {
            log.error("Unable to set editable to true because not backed by editable manager");
            return; // refuse to allow editing if unable to accept user input
        }
        if (editable && !providing) {
            log.error("Refusing to set editable if not allowing new NamedBeans to be created");
            return; // refuse to allow editing if not allowing user input to be accepted
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
        if (displayOptions != DisplayOptions.SYSTEMNAME &&
                displayOptions != DisplayOptions.QUOTED_SYSTEMNAME) {
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

    /**
     * Set the translation key to be used when a typed in bean name matches a
     * named bean has been included in a call to
     * {@link #setExcludedItems(java.util.Set)} and {@link #isValidatingInput()}
     * is {@code true}.
     *
     * @param beanInUseKey a translatable bundle key where {@code {0}} is the
     *                     result of {@link jmri.Manager#getBeanTypeHandled()}
     *                     and {1} is the result of
     *                     {@link jmri.NamedBean#getDisplayName(DisplayOptions)}
     *                     with {@link DisplayOptions#QUOTED_DISPLAYNAME} for
     *                     the matching bean
     */
    public void setNoMatchingToolTipBeanInUse(String beanInUseKey) {
        beanInUse = beanInUseKey;
    }

    /**
     * Set the translation key to be used when a typed in bean name does not
     * match a named bean, {@link #isValidatingInput()} is {@code true} and
     * {@link #isProviding()} is {@code false}.
     *
     * @param noMatchingBeanKey a translatable bundle key where {@code {0}} is
     *                          the result of
     *                          {@link jmri.Manager#getBeanTypeHandled()} and
     *                          {1} is the typed input
     */
    public void setNoMatchingToolTipNoMatchingBean(String noMatchingBeanKey) {
        noMatchingBean = noMatchingBeanKey;
    }

    /**
     * Set the translation key to be used when a typed in bean name does not
     * match a named bean, {@link #isValidatingInput()} is {@code true} and
     * {@link #isProviding()} is {@code true}.
     *
     * @param willCreateBeanKey a translatable bundle key where {@code {0}} is
     *                          the result of
     *                          {@link jmri.Manager#getBeanTypeHandled()} and
     *                          {1} is the typed input
     */
    public void setNoMatchingToolTipWillCreateBean(String willCreateBeanKey) {
        willCreateBean = willCreateBeanKey;
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

    private class NamedBeanRenderer implements ListCellRenderer<B>, JComboBox.KeySelectionManager {

        private final ListCellRenderer<? super B> renderer;
        private final long timeFactor;
        private long lastTime;
        private long time;
        private String prefix = "";

        public NamedBeanRenderer(ListCellRenderer<? super B> renderer) {
            this.renderer = renderer;
            Long l = (Long) UIManager.get("ComboBox.timeFactor");
            timeFactor = l != null ? l : 1000;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends B> list, B value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                label.setText(value.getDisplayName(displayOptions));
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

        /**
         * Find the index of the item in the model that starts with the prefix.
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

}
