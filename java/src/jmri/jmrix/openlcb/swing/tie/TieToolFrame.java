package jmri.jmrix.openlcb.swing.tie;

import java.awt.Container;
import java.awt.FlowLayout;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.Border;

/**
 * Frame for running assignment list.
 *
 * For historical reasons, this refers to Events as Ties.
 * That really has to change sometime soon
 *
 * @author Bob Jacobsen 2008
 * @since 2.3.7
 */
public class TieToolFrame extends jmri.util.JmriJFrame {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.openlcb.swing.tie.TieBundle");

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {

        // set the frame's initial state
        setTitle(rb.getString("WindowTitle"));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        ProducerTablePane producerPane = new ProducerTablePane();
        producerPane.initComponents();
        Border producerBorder = BorderFactory.createEtchedBorder();
        Border producerTitled = BorderFactory.createTitledBorder(producerBorder, "Producers");
        producerPane.setBorder(producerTitled);

        ConsumerTablePane consumerPane = new ConsumerTablePane();
        consumerPane.initComponents();
        Border consumerBorder = BorderFactory.createEtchedBorder();
        Border consumerTitled = BorderFactory.createTitledBorder(consumerBorder, "Consumers");
        consumerPane.setBorder(consumerTitled);

        TieTablePane tiePane = new TieTablePane();
        tiePane.initComponents();
        Border tieBorder = BorderFactory.createEtchedBorder();
        Border tieTitled = BorderFactory.createTitledBorder(tieBorder, "Events");
        tiePane.setBorder(tieTitled);

        JSplitPane upperSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, producerPane, consumerPane);

        JSplitPane wholeSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperSplit, tiePane);

        JPanel p1 = new JPanel();
        p1.add(wholeSplit);
        contentPane.add(p1);

        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(new JButton("Add"));
        p2.add(new JButton("Update"));
        p2.add(new JButton("Delete"));
        contentPane.add(p2);

        // initialize menu bar
        JMenuBar menuBar = new JMenuBar();
        // set up File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        // fileMenu.add(...);
        setJMenuBar(menuBar);

        addHelpMenu("package.jmri.jmrix.openlcb.swing.tie.TieToolFrame", true);

        // pack for display
        pack();
    }

}
