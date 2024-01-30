package jmri.jmrit.etcs.dmi.swing;

import org.apiguardian.api.API;

/**
 * Class to represent an ERTMS ETCS DMI Screen within a JFrame.
 * @author Steve Young Copyright (C) 2024
 * @since 5.7.4
 */
@API(status=API.Status.EXPERIMENTAL)
public class DmiFrame extends jmri.util.JmriJFrame {

    private final DmiPanel main;

    /**
     * Create a new DmiFrame with a default title.
     */
    public DmiFrame(){
        this("ETCS DMI Frame");
    }

    /**
     * Create a new DmiFrame with a given title.
     * @param frameName the Frame Title.
     */
    public DmiFrame(String frameName){
        super(frameName, false, true);
        main = new DmiPanel();
        add(main);
        getRootPane().setDefaultButton(null);
        pack();
    }

    /**
     * Get the main DmiPanel which controls the DMI Objects.
     * @return the DmiPanel.
     */
    public DmiPanel getDmiPanel(){
        return main;
    }

    @Override
    public void setVisible(boolean visible){
        jmri.util.ThreadingUtil.runOnGUI( () -> super.setVisible(visible));
    }

    @Override
    public void dispose(){
        main.dispose();
        super.dispose();
    }

}
