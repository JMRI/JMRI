package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;

import jmri.*;
import jmri.jmrit.beantable.light.LightControlPane;
import jmri.jmrit.beantable.light.LightIntensityPane;

/**
 * Provides an edit panel for a Light object.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Steve Young Copyright (C) 2021
 */
public class LightEditAction extends BeanEditAction<Light> {

    private LightControlPane lcp;
    private LightIntensityPane lip;

    @Override
    protected void initPanels() {
        if (InstanceManager.getNullableDefault(LightManager.class) == null) {
            setEnabled(false);
        }
        super.initPanels();
        lightControlPanel();
        lightIntensityPanel();
        
        applyBut.setToolTipText(Bundle.getMessage("LightUpdateButtonHint"));
    }

    @Override
    public String helpTarget() {
        return "package.jmri.jmrit.beantable.LightAddEdit";
    } // NOI18N
    
    @Override
    public Light getByUserName(String name) {
        return InstanceManager.lightManagerInstance().getByUserName(name);
    }
    
    @Override
    protected void cancelButtonAction(ActionEvent e) {
        if (lcp!=null) {
            lcp.dispose(); // ensures add / edit single LightControl Frame is closed
        }
        super.cancelButtonAction(e);
    }
    
    /**
     * Hide the Bean Properties Tab.
     * @return null
     */
    @Override
    BeanItemPanel propertiesDetails() {
        return null;
    }
    
    BeanItemPanel lightControlPanel() {
        BeanItemPanel lcPanel = new BeanItemPanel();
        lcPanel.setName(Bundle.getMessage("LightControllerTitlePlural"));

        lcp = new LightControlPane(bean);        
        lcPanel.add(lcp);
        
        lcPanel.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lcp.setToLight(bean);
            }
        });

        lcPanel.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lcp.setLightFromControlTable(bean);
            }
        });
        bei.add(lcPanel);
        return lcPanel;
    }

    BeanItemPanel lightIntensityPanel() {
        BeanItemPanel liPanel = new BeanItemPanel();
        liPanel.setName(Bundle.getMessage("LightVariableBorder"));
        lip = new LightIntensityPane(true);
        lip.setToLight(bean);
        liPanel.add(lip);
        
        liPanel.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lip.setToLight(bean);
            }
        });

        liPanel.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bean instanceof VariableLight) {
                    lip.setLightFromPane((VariableLight)bean);
                }
            }
        });
        bei.add(liPanel);

        if (!(bean instanceof VariableLight)) {
            liPanel.setEnabled(false);
            liPanel.setToolTipText(Bundle.getMessage("NoLightIntensityForHardware",getBeanManagerSystemUserName()));
        }
        return liPanel;
    }
    
    /**
     * Get Bean Manager System UserName.
     * Human readable form of System Username.
     * e.g. "Internal", "MERG"
     * 
     * @return Manager UserName, else empty String.
     */
    @Nonnull
    private String getBeanManagerSystemUserName(){
        Manager<Light> lm = InstanceManager.getDefault(LightManager.class);
        if (lm instanceof ProxyManager){
            ProxyManager<Light> plm = (ProxyManager<Light>)lm;
            for (Manager<Light> m : plm.getManagerList()) {
                if (m.getBySystemName(bean.getSystemName())!=null) {
                    return m.getMemo().getUserName();
                }
            }
        }
        return "";
    }
    
}
