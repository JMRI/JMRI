package jmri.jmrit.beantable.light;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmri.*;
import jmri.jmrit.beantable.BeanTableDataModel;
import static jmri.jmrit.beantable.LightTableAction.getDescriptionText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data model for a Light Table.
 * Code originally within LightTableAction.
 * 
 * @author Dave Duchamp Copyright (C) 2004
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class LightTableDataModel extends BeanTableDataModel<Light> {
    
    static public final int ENABLECOL = BeanTableDataModel.NUMCOLUMN;
    static public final int INTENSITYCOL = ENABLECOL + 1;
    static public final int EDITCOL = INTENSITYCOL + 1;
    static public final int CONTROLCOL = EDITCOL + 1;

    // for icon state col
    protected boolean _graphicState = false; // updated from prefs
    
    public LightTableDataModel(){
        super();
        initTable();
    }

    public LightTableDataModel(Manager<Light> mgr){
        super();
        setManager(mgr);
        initTable();
    }
    
    private void initTable() {

        _graphicState = InstanceManager.getDefault(jmri.util.gui.GuiLafPreferencesManager.class).isGraphicTableState();
        
    }
    
    private Manager<Light> lightManager;
    
    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Manager<Light> getManager() {
        if (lightManager == null) {
            lightManager = InstanceManager.getDefault(LightManager.class);
        }
        return lightManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void setManager(@Nonnull Manager<Light> manager) {
        if (!(manager instanceof LightManager)) {
            return;
        }
        getManager().removePropertyChangeListener(this);
        if (sysNameList != null) {
            for (int i = 0; i < sysNameList.size(); i++) {
                // if object has been deleted, it's not here; ignore it
                NamedBean b = getBySystemName(sysNameList.get(i));
                if (b != null) {
                    b.removePropertyChangeListener(this);
                }
            }
        }
        lightManager = manager;
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return CONTROLCOL + 1 + getPropertyColumnCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case EDITCOL:
                return ""; // no heading on "Edit"
            case INTENSITYCOL:
                return Bundle.getMessage("ColumnHeadIntensity");
            case ENABLECOL:
                return Bundle.getMessage("ColumnHeadEnabled");
            case CONTROLCOL:
                return Bundle.getMessage("LightControllerTitlePlural");
            default:
                return super.getColumnName(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case EDITCOL:
                return JButton.class;
            case INTENSITYCOL:
                return Double.class;
            case ENABLECOL:
                return Boolean.class;
            case CONTROLCOL:
                return String.class;
            case VALUECOL:  // may use an image to show light state
                return ( _graphicState ? JLabel.class : JButton.class );
            default:
                return super.getColumnClass(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case USERNAMECOL: // override default value for UserName column
            case CONTROLCOL:
                return new JTextField(16).getPreferredSize().width;
            case EDITCOL:
                return new JButton(Bundle.getMessage("ButtonEdit")).getPreferredSize().width+4;
            case INTENSITYCOL:
            case ENABLECOL:
                return new JTextField(6).getPreferredSize().width;
            default:
                return super.getPreferredWidth(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case INTENSITYCOL:
                return getValueAt(row, SYSNAMECOL) instanceof VariableLight;
            case EDITCOL:
            case ENABLECOL:
                return true;
            case CONTROLCOL:
                return false;
            default:
                return super.isCellEditable(row, col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(String name) {
        Light l = lightManager.getBySystemName(name);
        return (l==null ? ("Failed to find " + name) : l.describeState(l.getState()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int row, int col) {
        Light l = (Light) super.getValueAt(row, SYSNAMECOL);
        if (l == null){
            return null;
        }
        switch (col) {
            case EDITCOL:
                return Bundle.getMessage("ButtonEdit");
            case INTENSITYCOL:
                if (l instanceof VariableLight) {
                    return ((VariableLight)l).getTargetIntensity();
                } else {
                    return 0.0;
                }
            case ENABLECOL:
                return l.getEnabled();
            case CONTROLCOL:
                StringBuilder sb = new StringBuilder();
                for (LightControl lc : l.getLightControlList()) {
                    sb.append(getDescriptionText(lc, lc.getControlType()));
                    sb.append(" ");
                }
                return sb.toString();
            default:
                return super.getValueAt(row, col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        Light l = (Light) getValueAt(row, SYSNAMECOL);
        if (l == null){
            return;
        }
        switch (col) {
            case EDITCOL:
                // Use separate Runnable so window is created on top
                javax.swing.SwingUtilities.invokeLater(() -> {
                    editButton(l);
                });
                break;
            case INTENSITYCOL:
                // alternate
                try {
                    if (l instanceof VariableLight) {
                        double intensity = Math.max(0, Math.min(1.0, (Double) value));
                        ((VariableLight)l).setTargetIntensity(intensity);
                    } else {
                        double intensity = ((Double) value);
                        l.setCommandedState( intensity > 0.5 ? Light.ON : Light.OFF);
                    }
                } catch (IllegalArgumentException e1) {
                    JOptionPane.showMessageDialog(null,  e1.getMessage());
                }
                break;
            case ENABLECOL:
                l.setEnabled(!l.getEnabled());
                break;
            case VALUECOL:
                clickOn(l);
                fireTableRowsUpdated(row, row);
                break;
            default:
                super.setValueAt(value, row, col);
                break;
        }
    }
    
    private void editButton(Light bean){
        jmri.jmrit.beantable.beanedit.LightEditAction beanEdit = new jmri.jmrit.beantable.beanedit.LightEditAction();
        beanEdit.setBean(bean);
        beanEdit.actionPerformed(null);
    }

    /**
     * Delete the bean after all the checking has been done.
     * <p>
     * Deactivate the light, then use the superclass to delete it.
     */
    @Override
    protected void doDelete(Light bean) {
        bean.deactivateLight();
        super.doDelete(bean);
    }

    // all properties update for now
    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Light getBySystemName(@Nonnull String name) {
        return lightManager.getBySystemName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Light getByUserName(@Nonnull String name) {
        return InstanceManager.getDefault(LightManager.class).getByUserName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getMasterClassName() {
        return jmri.jmrit.beantable.LightTableAction.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clickOn(Light t) {
        t.setState( t.getState()==Light.OFF ? Light.ON : Light.OFF );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JButton configureButton() {
        return new JButton(" " + Bundle.getMessage("StateOff") + " ");
    }

    /**
     * Customize the light table Value (State) column to show an
     * appropriate graphic for the light state if _graphicState = true,
     * or (default) just show the localized state text when the
     * TableDataModel is being called from ListedTableAction.
     *
     * @param table a JTable of Lights
     */
    @Override
    protected void configValueColumn(JTable table) {
        // have the value column hold a JPanel (icon)
        //setColumnToHoldButton(table, VALUECOL, new JLabel("123456")); // for small round icon, but cannot be converted to JButton
        // add extras, override BeanTableDataModel
        log.debug("Light configValueColumn (I am {})", this);
        if (_graphicState) { // load icons, only once
            table.setDefaultEditor(JLabel.class, new ImageIconRenderer()); // editor
            table.setDefaultRenderer(JLabel.class, new ImageIconRenderer()); // item class copied from SwitchboardEditor panel
        } else {
            super.configValueColumn(table); // classic text style state indication
        }
    }

    /**
     * Visualize state in table as a graphic, customized for Lights (2
     * states + ... for transitioning). Renderer and Editor are
     * identical, as the cell contents are not actually edited, only
     * used to toggle state using {@link #clickOn(Light)}.
     *
     */
    static class ImageIconRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

        protected JLabel label;
        protected String rootPath = "resources/icons/misc/switchboard/"; // also used in display.switchboardEditor
        protected char beanTypeChar = 'L'; // for Light
        protected String onIconPath = rootPath + beanTypeChar + "-on-s.png";
        protected String offIconPath = rootPath + beanTypeChar + "-off-s.png";
        protected BufferedImage onImage;
        protected BufferedImage offImage;
        protected ImageIcon onIcon;
        protected ImageIcon offIcon;
        protected int iconHeight = -1;

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            log.debug("Renderer Item = {}, State = {}", row, value);
            if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                loadIcons();
                log.debug("icons loaded");
            }
            return updateLabel((String) value, row);
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column) {
            log.debug("Renderer Item = {}, State = {}", row, value);
            if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                loadIcons();
                log.debug("icons loaded");
            }
            return updateLabel((String) value, row);
        }

        public JLabel updateLabel(String value, int row) {
            if (iconHeight > 0) { // if necessary, increase row height;
                log.debug("TODO adjust table row height for Lights?");
                //table.setRowHeight(row, Math.max(table.getRowHeight(), iconHeight - 5));
            }
            if (value.equals(Bundle.getMessage("StateOff")) && offIcon != null) {
                label = new JLabel(offIcon);
                label.setVerticalAlignment(JLabel.BOTTOM);
                log.debug("offIcon set");
            } else if (value.equals(Bundle.getMessage("StateOn")) && onIcon != null) {
                label = new JLabel(onIcon);
                label.setVerticalAlignment(JLabel.BOTTOM);
                log.debug("onIcon set");
            } else if (value.equals(Bundle.getMessage("BeanStateInconsistent"))) {
                label = new JLabel("X", JLabel.CENTER); // centered text alignment
                label.setForeground(Color.red);
                log.debug("Light state inconsistent");
                iconHeight = 0;
            } else if (value.equals(Bundle.getMessage("LightStateIntermediate"))) {
                label = new JLabel("...", JLabel.CENTER); // centered text alignment
                log.debug("Light state in transition");
                iconHeight = 0;
            } else { // failed to load icon
                label = new JLabel(value, JLabel.CENTER); // centered text alignment
                log.warn("Error reading icons for LightTable");
                iconHeight = 0;
            }
            label.setToolTipText(value);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public final void mousePressed(MouseEvent evt) {
                    log.debug("Clicked on icon in row {}", row);
                    stopCellEditing();
                }
            });
            return label;
        }

        @Override
        public Object getCellEditorValue() {
            log.debug("getCellEditorValue, me = {})", this);
            return this.toString();
        }

        /**
         * Read and buffer graphics. Only called once for this table.
         *
         * @see #getTableCellEditorComponent(JTable, Object, boolean,
         * int, int)
         */
        protected void loadIcons() {
            try {
                onImage = ImageIO.read(new File(onIconPath));
                offImage = ImageIO.read(new File(offIconPath));
            } catch (IOException ex) {
                log.error("error reading image from {} or {}", onIconPath, offIconPath, ex);
            }
            log.debug("Success reading images");
            int imageWidth = onImage.getWidth();
            int imageHeight = onImage.getHeight();
            // scale icons 50% to fit in table rows
            Image smallOnImage = onImage.getScaledInstance(imageWidth / 2, imageHeight / 2, Image.SCALE_DEFAULT);
            Image smallOffImage = offImage.getScaledInstance(imageWidth / 2, imageHeight / 2, Image.SCALE_DEFAULT);
            onIcon = new ImageIcon(smallOnImage);
            offIcon = new ImageIcon(smallOffImage);
            iconHeight = onIcon.getIconHeight();
        }

    }
    
    private final static Logger log = LoggerFactory.getLogger(LightTableDataModel.class);
            
}
