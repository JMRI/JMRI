package jmri.util.swing;

import java.awt.FlowLayout;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;
import jmri.NamedBeanHandle;
import jmri.swing.NamedBeanComboBox;

/**
 * Create a JPanel containing a NamedBeanComboBox.  The default display option is
 * DISPLAYNAME.  JComboBoxUtil.setupComboBoxMaxRows() will be invoked.
 *
 * @param <E> the type of NamedBean
 *
 * @author Dave Sand Copyright 2021
 */
public class BeanSelectPanel<E extends NamedBean> extends JPanel {

    E _selection;
    NamedBeanComboBox<E> _beanComboBox;
    DisplayOptions _display;

    public BeanSelectPanel(@Nonnull Manager<E> manager, E selection) {
        this(manager, selection, DisplayOptions.DISPLAYNAME);
    }

    public BeanSelectPanel(@Nonnull Manager<E> manager, E selection, DisplayOptions display) {
        this(manager, selection, display, true);
    }

    /**
     * Create a JPanel that contains a named bean combo box.
     *
     * @param manager    The bean manager
     * @param selection  The bean that is selected, null for no selection.
     * @param display    The bean display option, null for default DISPLAYNAME.
     * @param maxRows    Should max rows be enabled; if false the JComboBox default of 8 will be used.
     */
    public BeanSelectPanel(@Nonnull Manager<E> manager, E selection, DisplayOptions display, boolean maxRows) {
        this(manager, selection, display, maxRows, null);
    }

    /**
     * Create a JPanel that contains a named bean combo box.
     *
     * @param manager    The bean manager
     * @param selection  The bean that is selected, null for no selection.
     * @param display    The bean display option, null for default DISPLAYNAME.
     * @param maxRows    Should max rows be enabled; if false the JComboBox default of 8 will be used.
     * @param filter     The filter or null if no filter
     */
    public BeanSelectPanel(@Nonnull Manager<E> manager, E selection, DisplayOptions display, boolean maxRows, Predicate<E> filter) {
        _selection = selection;
        _display = display == null ? DisplayOptions.DISPLAYNAME : display;

        _beanComboBox = new NamedBeanComboBox<>(manager, selection, _display, filter);
        _beanComboBox.setAllowNull(true);
        if (maxRows) JComboBoxUtil.setupComboBoxMaxRows(_beanComboBox);

        JPanel bean = new JPanel();
        bean.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        bean.add(_beanComboBox);
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        super.add(bean);
    }

    /*
     * Provide the actual combo box object to provide direct access, such as adding listeners.
     * @return the combo box
     */
    public NamedBeanComboBox<E> getBeanCombo() {
        return _beanComboBox;
    }

    /**
     * Get the named bean that has been selected.
     * @return the selected bean which may be null if the first row is selected.
     */
    public E getNamedBean() {
            return _beanComboBox.getSelectedItem();
    }

    /**
     * Set the default selected item in the combo box.
     * @param nBean the bean that is selected by default
     */
    public void setDefaultNamedBean(E nBean) {
        _selection = nBean;
        _beanComboBox.setSelectedItem(_selection);
    }

    /**
     * Set the default selected item in the combo box.
     * @param nBeanHandle the bean that is selected by default
     */
    public void setDefaultNamedBean(NamedBeanHandle<E> nBeanHandle) {
        if (nBeanHandle != null) {
            _selection = nBeanHandle.getBean();
            _beanComboBox.setSelectedItem(_selection);
        } else {
            _selection = null;
            _beanComboBox.setSelectedItem(null);
        }
    }

    /**
     * Check that the user selected something in this BeanSelectPanel.
     * @return true if nothing selected
     */
    public boolean isEmpty() {
        return _beanComboBox.getSelectedIndex() < 1;
    }

    public void dispose() {
        _beanComboBox.dispose();
    }


    //initialize logging
//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BeanSelectPanel.class);
}
