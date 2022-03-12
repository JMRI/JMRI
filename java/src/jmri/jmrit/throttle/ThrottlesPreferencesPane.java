package jmri.jmrit.throttle;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;

import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A preferences panel to display and edit JMRI throttle keyboard shortcuts
 * 
 * @author Lionel Jeanson - 2009-2021
 * 
 */
@ServiceProvider(service = PreferencesPanel.class)
public class ThrottlesPreferencesPane extends JPanel implements PropertyChangeListener, PreferencesPanel {

    private ThrottlesPreferencesUISettingsPane uiSettingsPane;
    private ThrottlesPreferencesControlsSettingsPane ctrlSettingsPane;

    /**
     * Creates new form ThrottlesPreferencesPane
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ThrottlesPreferencesPane() {
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        initComponents();
    }

    private void initComponents() {
        if (InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        ThrottlesPreferences tp = InstanceManager.getDefault(ThrottlesPreferences.class);
        tp.addPropertyChangeListener(this);
                       
        uiSettingsPane = new ThrottlesPreferencesUISettingsPane(tp);
        ctrlSettingsPane = new ThrottlesPreferencesControlsSettingsPane(tp);
               
        JScrollPane scrollPane1 = new JScrollPane(uiSettingsPane);
        JScrollPane scrollPane2 = new JScrollPane(ctrlSettingsPane);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Bundle.getMessage("UISettingsPane"),scrollPane1);
        tabbedPane.addTab(Bundle.getMessage("ControlsSettingsPane"),scrollPane2);
        
        this.setLayout(new BorderLayout());
        this.add(tabbedPane, BorderLayout.CENTER);       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt == null) || (evt.getPropertyName() == null)) {
            return;
        }
        if (evt.getPropertyName().compareTo("ThrottlePreferences") == 0) {
            if ((evt.getNewValue() == null) || (!(evt.getNewValue() instanceof ThrottlesPreferences))) {
                return;
            }
            uiSettingsPane.resetComponents((ThrottlesPreferences) evt.getNewValue());
            ctrlSettingsPane.resetComponents((ThrottlesPreferences) evt.getNewValue());
        }
    }
    
    public void resetComponents() {
        ThrottlesPreferences tp = InstanceManager.getDefault(ThrottlesPreferences.class);
        uiSettingsPane.resetComponents(tp);
        ctrlSettingsPane.resetComponents(tp);
    }

    @Override
    public String getPreferencesItem() {
        return "THROTTLE";
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuThrottle");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return null;
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        applyPreferences();
        InstanceManager.getDefault(ThrottlesPreferences.class).save();    
    }
    
    void applyPreferences() {
        ThrottlesPreferences tp = InstanceManager.getDefault(ThrottlesPreferences.class);
        uiSettingsPane.updateThrottlesPreferences(tp);
        ctrlSettingsPane.updateThrottlesPreferences(tp);
        InstanceManager.getDefault(ThrottlesPreferences.class).set(tp);
        InstanceManager.getDefault(ThrottleFrameManager.class).applyPreferences();
    }

    @Override
    public boolean isDirty() {
        return uiSettingsPane.isDirty() && ctrlSettingsPane.isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return false;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottlesPreferencesPane.class);        
}
