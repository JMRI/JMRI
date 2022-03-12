package jmri.util.prefs;


import jmri.InstanceManagerAutoDefault;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;

/**
 * This class provides a method to create an action that launches
 * preferences.  The default implementation provides a no-op action.
 * Applications that have a preferences interface should  install a
 * version of this factory in the InstanceManager that provides the
 * desired preferences action.
 *
 * @author Paul Bender Copyright (C) 2020
 */
public class JmriPreferencesActionFactory implements InstanceManagerAutoDefault {

    public JmriAbstractAction getDefaultAction() {
        return new JmriAbstractAction("No-op"){
            @Override
            public JmriPanel makePanel() {
                return null;
            }
        };
    }

    public JmriAbstractAction getNamedAction(String name){
        return new JmriAbstractAction(name){
            @Override
            public JmriPanel makePanel() {
                return null;
            }
        };
    }

    public JmriAbstractAction getCategorizedAction(String name,String category){
        return new JmriAbstractAction(name){
            @Override
            public JmriPanel makePanel() {
                return null;
            }
        };
    }

    public JmriAbstractAction getCategorizedAction(String name,String category,String subCategory){
        return new JmriAbstractAction(name){
            @Override
            public JmriPanel makePanel() {
                return null;
            }
        };
    }

}
