package jmri.util.swing;

import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenu;

/**
 * Sample Pane class for tests
 *
 * @author	Bob Jacobsen Copyright 2010
 */
// sample class
public class SamplePane extends jmri.util.swing.JmriPanel {

    public SamplePane() {
    }

    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        num = ++index;

        JButton b;
        b = new JButton(new JmriNamedPaneAction("Next" + num,
                getWindowInterface(),
                jmri.util.swing.SamplePane.class.getName()));
        add(b);

        JmriNamedPaneAction act = new JmriNamedPaneAction("Extend" + num,
                getWindowInterface(),
                jmri.util.swing.SamplePane.class.getName());
        act.setHint(WindowInterface.Hint.EXTEND);
        b = new JButton(act);
        add(b);

        b = new JButton("Close" + num);
        add(b);
    }

    @Override
    public String getHelpTarget() {
        return null;
    }

    @Override
    public String getTitle() {
        return "SamplePane " + num;
    }

    @Override
    public List<JMenu> getMenus() {
        java.util.ArrayList<JMenu> list = new java.util.ArrayList<JMenu>();
        JMenu m = new JMenu("test 1");
        m.add(new JButton("sub 1"));
        m.add(new JButton("sub 2"));
        list.add(m);
        m = new JMenu("test 2");
        m.add(new JButton("sub a"));
        m.add(new JButton("sub b"));
        list.add(m);
        return list;
    }

    @Override
    public void dispose() {
        disposed.add(num);
        super.dispose();
    }

    int num;

    static public ArrayList<Integer> disposed;
    static public int index = 0;

}
