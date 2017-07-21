package jmri.jmrix.loconet.soundloader;

import java.awt.FlowLayout;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.jmrix.loconet.spjfile.SpjFile;
import jmri.util.FileUtil;

/**
 * Pane for downloading .hex files
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
public class LoaderPane extends jmri.jmrix.loconet.swing.LnPanel {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.soundloader.Loader");

    JLabel inputFileName = new JLabel("");

    JButton readButton;
    JButton loadButton;

    JTextField comment = new JTextField(32);

    JProgressBar bar;
    JLabel status = new JLabel("");
    String statusText = "";

    SpjFile file;
    LoaderEngine engine;

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.soundloader.LoaderFrame"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemSoundload"));
    }

    public LoaderPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JButton b = new JButton(Bundle.getMessage("ButtonSelect")); // is in jmri.NBBundle
            b.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectInputFile();
                }
            });
            p.add(b);
            p.add(new JLabel(res.getString("LabelInpFile")));
            p.add(inputFileName);

            add(p);
        }

        add(new JSeparator());

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            readButton = new JButton(res.getString("ButtonRead"));
            readButton.setEnabled(false);
            readButton.setToolTipText(res.getString("TipReadDisabled"));
            p.add(readButton);
            readButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doRead();
                }
            });

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            p.add(new JLabel(res.getString("LabelFileComment")));
            comment.setEditable(false);
            p.add(comment);
            add(p);
        }

        add(new JSeparator());

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            loadButton = new JButton(res.getString("ButtonLoad"));
            loadButton.setEnabled(false);
            loadButton.setToolTipText(res.getString("TipLoadDisabled"));
            p.add(loadButton);
            loadButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doLoad();
                }
            });

            add(p);

            add(new JSeparator());

            bar = new JProgressBar();
            add(bar);

            add(new JSeparator());

            {
                p = new JPanel();
                p.setLayout(new FlowLayout());
                status.setText(res.getString("StatusSelectFile"));
                status.setAlignmentX(JLabel.LEFT_ALIGNMENT);
                p.add(status);
                add(p);
            }

        }
    }

    JFileChooser chooser;

    void selectInputFile() {
        String name = inputFileName.getText();
        if (name.equals("")) {
            name = FileUtil.getUserFilesPath();
        }
        if (chooser == null) {
            chooser = new JFileChooser(name);
        }
        inputFileName.setText("");  // clear out in case of failure
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }
        inputFileName.setText(chooser.getSelectedFile().getName());

        readButton.setEnabled(true);
        readButton.setToolTipText(res.getString("TipReadEnabled"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipLoadDisabled"));
        status.setText(res.getString("StatusReadFile"));
    }

    void doRead() {
        if (inputFileName.getText().equals("")) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorNoInputFile"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // force load, verify disabled in case read fails
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipLoadDisabled"));

        try {
            file = new SpjFile(chooser.getSelectedFile());
            file.read();
        } catch (FileNotFoundException f) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorFileNotFound"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException f) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorIOError"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // display contents
        comment.setText(file.getComment());

        // set up for next step
        loadButton.setEnabled(true);
        loadButton.setToolTipText(res.getString("TipLoadEnabled"));
        status.setText(res.getString("StatusDoDownload"));

    }

    void doLoad() {
        status.setText(res.getString("StatusDownloading"));
        readButton.setEnabled(false);
        readButton.setToolTipText(res.getString("TipDisabledDownload"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipDisabledDownload"));

        // Create a loader to run in a separate thread
        // Override notify() method to do a swing-thread update of status field
        if (engine == null) {
            engine = new LoaderEngine(memo) {
                @Override
                public void notify(String s) {
                    javax.swing.SwingUtilities.invokeLater(new Notifier(s));
                }
            };
        }

        // start the download itself
        new Thread() {
            @Override
            public void run() {
                engine.runDownload(file);
            }
        }.start();

    }

    /**
     * Define objects to update status JLabel in pane
     */
    private class Notifier implements Runnable {

        public Notifier(String msg) {
            this.msg = msg;
        }
        String msg;

        @Override
        public void run() {
            status.setText(msg);
        }
    }

    /**
     * Get rid of any held resources
     */
    @Override
    public void dispose() {
        if (file != null) {
            file.dispose();
        }
        file = null;  // not for GC, this flags need to reinit

        if (engine != null) {
            engine.dispose();
        }
        engine = null;  // not for GC, this flags need to reinit
    }

}
