package jmri.jmrit.operations.rollingstock.engines.tools;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockAttributeEditFrame;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.*;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame for editing an engine attribute.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class EngineAttributeEditFrame extends RollingStockAttributeEditFrame {

    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

    // incremental attributes for this frame
    public static final String MODEL = "Model";
    public static final String CONSIST = "Consist";

    public EngineAttributeEditFrame(){
    }

    public void initComponents(String attribute) {
        initComponents(attribute, NONE);
    }

    /**
     * 
     * @param attribute One of the seven possible attributes for an engine.
     * @param name      The name of the attribute to edit.
     */
    @Override
    public void initComponents(String attribute, String name) {
        super.initComponents(attribute, name);

        setTitle(Bundle.getMessage("TitleEngineEditAtrribute", attribute));

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new EngineAttributeAction(this));
        toolMenu.add(new EngineDeleteAttributeAction(this));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_Locomotives", true); // NOI18N
    }

    @Override
    protected void deleteAttributeName(String deleteItem) {
        super.deleteAttributeName(deleteItem);
        if (_attribute.equals(MODEL)) {
            InstanceManager.getDefault(EngineModels.class).deleteName(deleteItem);
        }
        if (_attribute.equals(TYPE)) {
            InstanceManager.getDefault(EngineTypes.class).deleteName(deleteItem);
        }
        if (_attribute.equals(LENGTH)) {
            InstanceManager.getDefault(EngineLengths.class).deleteName(deleteItem);
        }
        if (_attribute.equals(CONSIST)) {
            InstanceManager.getDefault(ConsistManager.class).deleteConsist(deleteItem);
        }
    }

    @Override
    protected void addAttributeName(String addItem) {
        super.addAttributeName(addItem);
        if (_attribute.equals(MODEL)) {
            InstanceManager.getDefault(EngineModels.class).addName(addItem);
        }
        if (_attribute.equals(TYPE)) {
            InstanceManager.getDefault(EngineTypes.class).addName(addItem);
        }
        if (_attribute.equals(LENGTH)) {
            InstanceManager.getDefault(EngineLengths.class).addName(addItem);
            comboBox.setSelectedItem(addItem);
        }
        if (_attribute.equals(CONSIST)) {
            InstanceManager.getDefault(ConsistManager.class).newConsist(addItem);
        }
    }

    @Override
    protected void replaceItem(String oldItem, String newItem) {
        super.replaceItem(oldItem, newItem);
        if (_attribute.equals(MODEL)) {
            List<Engine> engines = engineManager.getList();
            for (Engine engine : engines) {
                // we need to copy the old model attributes, so find an engine.
                if (engine.getModel().equals(oldItem)) {
                    // Has this model been configured?
                    if (InstanceManager.getDefault(EngineModels.class).getModelLength(newItem) != null) {
                        engine.setModel(newItem);
                    } else {
                        // get the old configuration for this model
                        String length = engine.getLength();
                        String hp = engine.getHp();
                        String type = engine.getTypeName();
                        // now update the new model
                        engine.setModel(newItem);
                        engine.setLength(length);
                        engine.setHp(hp);
                        engine.setTypeName(type);
                    }
                }
            }
            InstanceManager.getDefault(EngineModels.class).replaceName(oldItem, newItem);
        }
        if (_attribute.equals(CONSIST)) {
            InstanceManager.getDefault(ConsistManager.class).replaceConsistName(oldItem, newItem);
        }
        if (_attribute.equals(TYPE)) {
            InstanceManager.getDefault(EngineTypes.class).replaceName(oldItem, newItem);
        }
        if (_attribute.equals(LENGTH)) {
            InstanceManager.getDefault(EngineLengths.class).replaceName(oldItem, newItem);
        }
    }

    @Override
    protected void loadCombobox() {
        super.loadCombobox();
        if (_attribute.equals(MODEL)) {
            comboBox = InstanceManager.getDefault(EngineModels.class).getComboBox();
            InstanceManager.getDefault(EngineModels.class).addPropertyChangeListener(this);
        }
        if (_attribute.equals(TYPE)) {
            comboBox = InstanceManager.getDefault(EngineTypes.class).getComboBox();
            InstanceManager.getDefault(EngineTypes.class).addPropertyChangeListener(this);
        }
        if (_attribute.equals(LENGTH)) {
            comboBox = InstanceManager.getDefault(EngineLengths.class).getComboBox();
            InstanceManager.getDefault(EngineLengths.class).addPropertyChangeListener(this);
        }
        if (_attribute.equals(CONSIST)) {
            comboBox = InstanceManager.getDefault(ConsistManager.class).getComboBox();
            InstanceManager.getDefault(ConsistManager.class).addPropertyChangeListener(this);
        }
    }
    
    @Override
    protected void updateAttributeQuanity() {
        if (!showQuanity) {
            return;
        }
        int number = 0;
        String item = (String) comboBox.getSelectedItem();
        log.debug("Selected item {}", item);
        for (Engine eng : engineManager.getList()) {
            if (_attribute.equals(ROAD)) {
                if (eng.getRoadName().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(MODEL)) {
                if (eng.getModel().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(CONSIST)) {
                if (eng.getConsistName().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(TYPE)) {
                if (eng.getTypeName().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(LENGTH)) {
                if (eng.getLength().equals(item)) {
                    number++;
                }
            }
            if (_attribute.equals(OWNER)) {
                if (eng.getOwnerName().equals(item)) {
                    number++;
                }
            }
        }
        quanity.setText(Integer.toString(number));
        // Tool to delete all attributes that haven't been assigned to a car
        if (number == 0 && deleteUnused) {
            // need to check if a car is using the road name
            if (_attribute.equals(ROAD)) {
                for (RollingStock rs : InstanceManager.getDefault(CarManager.class).getList()) {
                    if (rs.getRoadName().equals(item)) {
                        log.info("Car ({} {}) is assigned road name ({})", rs.getRoadName(), rs.getNumber(), item); // NOI18N
                        return;
                    }
                }
            }
            // need to check if a car is using the road name
            if (_attribute.equals(OWNER)) {
                for (RollingStock rs : InstanceManager.getDefault(CarManager.class).getList()) {
                    if (rs.getOwnerName().equals(item)) {
                        log.info("Car ({} {}) is assigned owner name ({})", rs.getRoadName(), rs.getNumber(), item); // NOI18N
                        return;
                    }
                }
            }
            // confirm that attribute is to be deleted
            confirmDelete(item);
        }
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(EngineModels.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineLengths.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(ConsistManager.class).removePropertyChangeListener(this);
        engineManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(EngineModels.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(EngineTypes.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(EngineLengths.class).updateComboBox(comboBox);
        }
        if (e.getPropertyName().equals(ConsistManager.LISTLENGTH_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(ConsistManager.class).updateComboBox(comboBox);
        }
        super.propertyChange(e);
    }

    private final static Logger log = LoggerFactory.getLogger(EngineAttributeEditFrame.class);
}
