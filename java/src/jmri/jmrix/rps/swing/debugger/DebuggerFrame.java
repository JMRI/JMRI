package jmri.jmrix.rps.swing.debugger;

import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.ReadingListener;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for manual operation and debugging of the RPS system.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class DebuggerFrame extends jmri.util.JmriJFrame
        implements ReadingListener, MeasurementListener {

    RpsSystemConnectionMemo memo = null;

    public DebuggerFrame(RpsSystemConnectionMemo _memo) {
        super();
        memo = _memo;

        NUMSENSORS = Engine.instance().getMaxReceiverNumber();

        setTitle(title());
    }

    protected String title() {
        return "RPS Debugger";
    }  // product name, not translated

    @Override
    public void dispose() {
        // separate from data source
        Distributor.instance().removeReadingListener(this);
        Distributor.instance().removeMeasurementListener(this);
        // and unwind swing
        super.dispose();
    }

    java.text.NumberFormat nf;

    JComboBox<String> mode;
    JButton doButton;

    JTextField vs = new JTextField(18);
    JTextField offset = new JTextField(10);

    JTextField x = new JTextField(18);
    JTextField y = new JTextField(18);
    JTextField z = new JTextField(18);
    JLabel code = new JLabel();

    JTextField id = new JTextField(5);

    DebuggerTimePane timep = new DebuggerTimePane();

    int NUMSENSORS;

    @Override
    public void initComponents() {

        nf = java.text.NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add panes in the middle
        JPanel p, p1;

        // Time inputs
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(new JLabel("Time measurements: "));

        timep.initComponents();
        JScrollPane sc = new JScrollPane(timep);
        p.add(sc);

        // add id field at bottom
        JPanel p5 = new JPanel();
        p5.setLayout(new FlowLayout());
        p5.add(new JLabel("Id: "));
        p5.add(id);
        p.add(p5);

        getContentPane().add(p);

        getContentPane().add(new JSeparator());

        // x, y, z results
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel("Results:"));
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("X:"));
        p1.add(x);
        p.add(p1);
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("Y:"));
        p1.add(y);
        p.add(p1);
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("Z:"));
        p1.add(z);
        p.add(p1);
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("Code:"));
        p1.add(code);
        p.add(p1);
        getContentPane().add(p);

        getContentPane().add(new JSeparator());

        // add controls at bottom
        p = new JPanel();

        mode = new JComboBox<String>(new String[]{"From time fields", "from X,Y,Z fields", "from time file", "from X,Y,Z file"});
        p.add(mode);
        p.setLayout(new FlowLayout());

        doButton = new JButton("Do Once");
        doButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doOnce();
            }
        });
        p.add(doButton);
        getContentPane().add(p);

        // start working
        Distributor.instance().addReadingListener(this);
        Distributor.instance().addMeasurementListener(this);

        // add file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrix.rps.swing.CsvExportAction("Export Readings as CSV...", memo));
        fileMenu.add(new jmri.jmrix.rps.swing.CsvExportMeasurementAction("Export Measurements as CSV...", memo));
        setJMenuBar(menuBar);

        // add help
        addHelpMenu("package.jmri.jmrix.rps.swing.debugger.DebuggerFrame", true);

        // prepare for display
        pack();
    }

    /**
     * Invoked by button to do one cycle
     */
    void doOnce() {
        switch (mode.getSelectedIndex()) {
            default: // should not happen
                log.error("Did not expect selected mode {}", mode.getSelectedIndex());
                return;
            case 0: // From time fields
                doReadingFromTimeFields();
                return;
            case 1: // From X,Y,Z fields
                doMeasurementFromPositionFields();
                return;
            case 2: // From time file
                try {
                    doLoadReadingFromFile();
                    doReadingFromTimeFields();
                } catch (java.io.IOException e) {
                    log.error("exception ", e);
                }
                return;
            case 3: // From X,Y,Z file
                try {
                    doLoadMeasurementFromFile();
                } catch (java.io.IOException e) {
                    log.error("exception ", e);
                }
                return;

        }
        // Should not actually get here
    }

    void doLoadReadingFromFile() throws java.io.IOException {
        if (readingInput == null) {
            setupReadingFile();
        }

        // get and load a line
        if (!readingInput.readRecord()) {
            // read failed, try once to get another file
            setupReadingFile();
            if (!readingInput.readRecord()) {
                throw new java.io.IOException("no valid file");
            }
        }
        // item 0 is the ID, not used right now
        for (int i = 0; i < Math.min(NUMSENSORS, readingInput.getColumnCount() + 1); i++) {
            timep.times[i].setText(readingInput.get(i + 1));
        }
    }

    void setupReadingFile() throws java.io.IOException {
        // get file
        readingInput = null;

        readingFileChooser.rescanCurrentDirectory();
        int retVal = readingFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }
        // create and keep reader
        java.io.Reader reader = new java.io.FileReader(
                readingFileChooser.getSelectedFile());
        readingInput = new com.csvreader.CsvReader(reader);
    }

    void doLoadMeasurementFromFile() throws java.io.IOException {
        if (measurementInput == null) {
            setupMeasurementFile();
        }

        // get and load a line
        if (!measurementInput.readRecord()) {
            // read failed, try once to get another file
            setupMeasurementFile();
            if (!measurementInput.readRecord()) {
                throw new java.io.IOException("no valid file");
            }
        }

        // item 0 is the ID, not used right now
        Measurement m = new Measurement(null,
                Double.valueOf(measurementInput.get(1)).doubleValue(),
                Double.valueOf(measurementInput.get(2)).doubleValue(),
                Double.valueOf(measurementInput.get(3)).doubleValue(),
                Engine.instance().getVSound(),
                0,
                "Data File"
        );

        lastPoint = m;
        Distributor.instance().submitMeasurement(m);
    }

    void setupMeasurementFile() throws java.io.IOException {
        // get file
        measurementInput = null;

        measurementFileChooser.rescanCurrentDirectory();
        int retVal = measurementFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }
        // create and keep reader
        java.io.Reader reader = new java.io.FileReader(
                measurementFileChooser.getSelectedFile());
        measurementInput = new com.csvreader.CsvReader(reader);
    }

    Measurement lastPoint = null;

    Reading getReadingFromTimeFields() {

        double[] values = new double[NUMSENSORS + 1];

        // parse input
        for (int i = 0; i <= NUMSENSORS; i++) {
            values[i] = 0.;
            if ((timep.times[i] != null) && !timep.times[i].getText().equals("")) {
                values[i] = Double.valueOf(timep.times[i].getText()).doubleValue();
            }
        }

        // get the id number and make reading
        Reading r = new Reading(id.getText(), values);
        return r;
    }

    void doReadingFromTimeFields() {
        // get the reading
        Reading r = getReadingFromTimeFields();

        // and forward
        Distributor.instance().submitReading(r);
    }

    @Override
    public void notify(Reading r) {
        // This implementation creates a new Calculator
        // each time to ensure that the most recent
        // receiver positions are used; this should be
        // replaced with some notification system
        // to reduce the work used.

        id.setText("" + r.getId());
        timep.notify(r);
    }

    void doMeasurementFromPositionFields() {
        // contain dummy Reading
        Reading r = new Reading(id.getText(), new double[]{0., 0., 0., 0.});

        Measurement m = new Measurement(r,
                Double.valueOf(x.getText()).doubleValue(),
                Double.valueOf(y.getText()).doubleValue(),
                Double.valueOf(z.getText()).doubleValue(),
                Engine.instance().getVSound(),
                0,
                "Position Data"
        );

        lastPoint = m;
        Distributor.instance().submitMeasurement(m);
    }

    @Override
    public void notify(Measurement m) {
        // show result
        x.setText(nf.format(m.getX()));
        y.setText(nf.format(m.getY()));
        z.setText(nf.format(m.getZ()));
        code.setText(m.textCode());

        timep.notify(m);
    }

    // to find and remember the input files
    com.csvreader.CsvReader readingInput = null;
    final javax.swing.JFileChooser readingFileChooser = new JFileChooser("rps/readings.csv");

    com.csvreader.CsvReader measurementInput = null;
    final javax.swing.JFileChooser measurementFileChooser = new JFileChooser("rps/positions.csv");

    private final static Logger log = LoggerFactory.getLogger(DebuggerFrame.class);

}
