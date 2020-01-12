package jmri.jmrit.mastbuilder;

import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import jmri.util.FileUtil;

/**
 * Pane for building Signal Mast definitions.
 * Demo, not in any JMRI menu or tool as of 4.9.6
 *
 * TODO: add code behind buttons, add to jmri.jmrit.ToolsMenu using
 * {@literal add(new jmri.jmrit.mastbuilder.MastBuilderAction(Bundle.getMessage("MastBuilderTitle") + "..."));}
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class MastBuilderPane extends jmri.util.JmriJFrame {

    public MastBuilderPane() {
        super();
    }

    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("MastBuilderTitle"));

        JPanel builderPane = new JPanel();
        builderPane.setLayout(new BoxLayout(builderPane, BoxLayout.Y_AXIS));

        String head = Bundle.getMessage("BeanNameSignalHead");
        String turnout = Bundle.getMessage("BeanNameTurnout");
        String closed = Bundle.getMessage("TurnoutStateClosed");
        String thrown = Bundle.getMessage("TurnoutStateThrown");
        String delete = Bundle.getMessage("ButtonDelete");
        String aspclear = Bundle.getMessage("AspectClear");
        String aspadvappmed = Bundle.getMessage("AspectAdvApprMed");
        String aspapprlim = Bundle.getMessage("AspectApprLim");
        String asplimclear = Bundle.getMessage("AspectLimClear");

        JPanel p = new JPanel();
        p.setLayout(new jmri.util.javaworld.GridLayout2(1, 3));
        builderPane.add(p);

        p.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("SigSys"))));
        p.add(new JComboBox<String>(new String[]{
                Bundle.getMessage("OptionBasic"),
                Bundle.getMessage("OptionAAR"),
                Bundle.getMessage("OptionNYCS")
        }));

        JPanel p2 = new JPanel();
        p2.add(new JLabel(Bundle.getMessage("NumOutputsLabel")));
        p2.add(new JSpinner(new SpinnerNumberModel(4, 1, 10,1)));
        p.add(p2);

        builderPane.add(new JSeparator());

        p = new JPanel();
        p.setLayout(new jmri.util.javaworld.GridLayout2(1 + 4, 5));
        builderPane.add(p);

        // first how is titles
        p.add(new JLabel(Bundle.getMessage("NameTypeLabel")));
        p.add(new JComboBox<String>(new String[]{head, turnout}));
        p.add(new JComboBox<String>(new String[]{head, turnout}));
        p.add(new JComboBox<String>(new String[]{head, turnout}));
        p.add(new JLabel(""));

        p.add(new JComboBox<String>(new String[]{aspclear, aspadvappmed, aspapprlim, asplimclear}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JComboBox<String>(new String[]{closed, thrown}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JButton(delete));

        p.add(new JComboBox<String>(new String[]{aspclear, aspadvappmed, aspapprlim, asplimclear}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JComboBox<String>(new String[]{closed, thrown}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JButton(delete));

        p.add(new JComboBox<String>(new String[]{aspclear, aspadvappmed, aspapprlim, asplimclear}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JComboBox<String>(new String[]{closed, thrown}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JButton(delete));

        p.add(new JComboBox<String>(new String[]{aspclear, aspadvappmed, aspapprlim, asplimclear}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JComboBox<String>(new String[]{closed, thrown}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JButton(delete));

        p = new JPanel();
        p.setLayout(new FlowLayout());
        builderPane.add(p);
        p.add(new JButton(Bundle.getMessage("AddAspectButton")));

        builderPane.add(new JSeparator());

        p = new JPanel();
        p.setLayout(new FlowLayout());
        builderPane.add(p);
        p.add(new JLabel(Bundle.getMessage("NewAppTableLabel")));
        p.add(new JTextField(30));
        p.add(new JButton(Bundle.getMessage("ButtonSave")));

        builderPane.add(Box.createVerticalGlue());
        getContentPane().add(builderPane);
    }

    Object[] icons() {
        Object[] list = new Object[3];
        list[0] = new ImageIcon(FileUtil.findURL("resources/icons/smallschematics/aspects/AAR-1946/SL-1-low/rule-287.gif"));
        list[1] = new ImageIcon(FileUtil.findURL("resources/icons/smallschematics/aspects/AAR-1946/SL-1-low/rule-290.gif"));
        list[2] = new ImageIcon(FileUtil.findURL("resources/icons/smallschematics/aspects/AAR-1946/SL-1-low/rule-292.gif"));
        return list;
    }

}
