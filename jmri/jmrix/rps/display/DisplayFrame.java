 // DisplayFrame.java
 
 package jmri.jmrix.rps.display;

import jmri.jmrix.rps.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.vecmath.Point3d;
import java.io.*;

/**
 * Frame for manual operation and debugging of the RPS system
 *
 * @author	   Bob Jacobsen   Copyright (C) 2006
 * @version   $Revision: 1.2 $
 */


public class DisplayFrame extends jmri.util.JmriJFrame 
            implements ReadingListener, MeasurementListener {

    public DisplayFrame() {
        super();
        
        times = new JTextField[NUMSENSORS];
        residuals = new JLabel[NUMSENSORS];
        sensorx = new JTextField[NUMSENSORS];
        sensory = new JTextField[NUMSENSORS];
        sensorz = new JTextField[NUMSENSORS];
        
        for (int i = 0; i < NUMSENSORS; i++) {
            times[i] = new JTextField(10);
            times[i].setText("0");
            residuals[i] = new JLabel("          ");
            sensorx[i] = new JTextField(7);
            sensory[i] = new JTextField(7);
            sensorz[i] = new JTextField(7);
        }
    }

    protected String title() { return "RPS"; }  // product name, not translated

    public void dispose() {
        // separate from data source
        Distributor.getInstance().removeReadingListener(this);
        Distributor.getInstance().removeMeasurementListener(this);
        // and unwind swing
        super.dispose();
    }

    java.text.NumberFormat nf;

    JComboBox mode;
    JComboBox algorithm;
    JButton doButton;
    
    static final int NUMSENSORS = 6;
    
    JTextField[] times;
    JLabel[] residuals;
    
    
    JTextField vs = new JTextField(18);
    JTextField offset = new JTextField(10);

    JTextField sensorx[];
    JTextField sensory[];
    JTextField sensorz[];
    
    JTextField x = new JTextField(18);
    JTextField y = new JTextField(18);
    JTextField z = new JTextField(18);
    JLabel code = new JLabel();
    
    JTextField id = new JTextField(5);
    
    public void initComponents() {
        nf = java.text.NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // add panes in the middle
        JPanel p, p1, p2;
        
        // positions
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(new JLabel("Receiver positions: "));
        for (int i = 0; i< NUMSENSORS; i++) {
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("r"+(i+1)+":"));
            p1.add(sensorx[i]);
            p1.add(sensory[i]);
            p1.add(sensorz[i]);
            p.add(p1);
        }

        getContentPane().add(p);

         // file load, store
        p = new JPanel();
        JButton b1;
        b1 = new JButton("Store...");
        b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                        store();
                }
        });
        p.add(b1);
        b1 = new JButton("Load...");
        b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                        load();
                }
        });
        p.add(b1);
        getContentPane().add(p);
        getContentPane().add(new JSeparator());

        p = new JPanel();
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("Vsound:"));
            p1.add(vs);
        p.add(p1);
        getContentPane().add(p);
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("Offset:"));
            p1.add(offset);
        p.add(p1);
        getContentPane().add(p);
        getContentPane().add(new JSeparator());

        // load values for initial positions
        loadInitialSpacePoints();
        
        // Time inputs
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        p.add(new JLabel("Time measurements: "));
        
        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.GridLayout(NUMSENSORS, 2));
        
        for (int i = 0; i< NUMSENSORS; i++) {
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("r"+(i+1)+":"));
            p1.add(times[i]);   
            p3.add(p1);
            p1 = new JPanel();
            p1.add(new JLabel("r-t: "));
            p1.add(residuals[i]);
            p3.add(p1);         
        }
        p.add(p3);

        // add id field at bottom
        JPanel p5 = new JPanel();
        p5.setLayout(new FlowLayout());
        p5.add(new JLabel("Id: "));
        p5.add(id);
        p.add(p5);

        getContentPane().add(p);

        getContentPane().add(new JSeparator());

        // algorithm selection        
        algorithm = Algorithms.algorithmBox();
        getContentPane().add(algorithm);
    
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
        
        mode = new JComboBox(new String[]{"From time fields", "from X,Y,Z fields", "from time file", "from X,Y,Z file"});
        p.add(mode);
        p.setLayout(new FlowLayout());
        
        doButton = new JButton("Do Once");
        doButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doOnce();
                }
            });
        p.add(doButton);
        getContentPane().add(p);
        
        // start working
        Distributor.getInstance().addReadingListener(this);
        Distributor.getInstance().addMeasurementListener(this);
        
        // prepare for display
        pack();
    }
    
    JFileChooser fci = jmri.jmrit.XmlFile.userFileChooser();
    void load() {
        try {
            // request the filename from an open dialog
            fci.rescanCurrentDirectory();
            int retVal = fci.showOpenDialog(this);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fci.getSelectedFile();
                if (log.isInfoEnabled()) log.info("located file "+file+" for load");
                // handle the file
                PositionFile pf = new PositionFile();
                pf.loadFile(file);
                Point3d p;
                for (int i = 0; i<NUMSENSORS; i++) {
                    p = pf.getReceiverPosition(i);
                    if (p == null) break;
                    sensorx[i].setText(""+p.x); sensory[i].setText(""+p.y); sensorz[i].setText(""+p.z);
                }
            }
            else log.info("load cancelled in open dialog");
        } catch (Exception e) {
            log.error("exception during load: "+e);
        }
    }
    
    void store() {
        try {
            // request the filename from an open dialog
            fci.rescanCurrentDirectory();
            int retVal = fci.showSaveDialog(this);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fci.getSelectedFile();
                if (log.isInfoEnabled()) log.info("located file "+file+" for store");
                // handle the file
                PositionFile pf = new PositionFile();
                pf.prepare();
                for (int i = 0; i<NUMSENSORS; i++) {
                    Point3d p = getPoint(i);
                    if (p==null) break;
                    pf.setReceiver(p);
                }
                pf.store(file);
            }
            else log.info("load cancelled in open dialog");
        } catch (Exception e) {
            log.error("exception during load: "+e);
        }
    }
        
    void loadInitialSpacePoints() {
        // r1x.setText("0");r1y.setText("0");r1z.setText("0");
        // r2x.setText("100");r2y.setText("0");r2z.setText("0");
        // r3x.setText("50");r3y.setText("50");r3z.setText("100");
        // r4x.setText("100");r4y.setText("100");r4z.setText("0");

/*         r1x.setText(""+44*2.54);r1y.setText(""+53*2.54);r1z.setText(""+15*2.54); */
/*         r2x.setText(""+44*2.54);r2y.setText(""+0*2.54);r2z.setText(""+15*2.54); */
/*         r3x.setText(""+(-12*2.54));r3y.setText(""+26.5*2.54);r3z.setText(""+15*2.54); */
/*         r4x.setText(""+30*2.54);r4y.setText(""+26.5*2.54);r4z.setText(""+25*2.54); */

/*         sensorx[0].setText("83.3");sensory[0].setText("18.5");sensorz[0].setText("12.2"); */
/*         sensorx[1].setText("25");sensory[1].setText("1.3");sensorz[1].setText("14.1"); */
/*         sensorx[2].setText("1.5");sensory[2].setText("35.9");sensorz[2].setText("10.6"); */
/*         sensorx[3].setText("21.5");sensory[3].setText("57.1");sensorz[3].setText("13.8"); */
        
/*         sensorx[0].setText("75.2");sensory[0].setText("0");sensorz[0].setText("73.4"); */
/*         sensorx[1].setText("0");sensory[1].setText("0");sensorz[1].setText("73.4"); */
/*         sensorx[2].setText("0");sensory[2].setText("158.5");sensorz[2].setText("73.4"); */
/*         sensorx[3].setText("75.2");sensory[3].setText("158.5");sensorz[3].setText("73.4"); */

/*         sensorx[0].setText("0");sensory[0].setText("158.5");sensorz[0].setText("73.4"); */
/*         sensorx[1].setText("75.2");sensory[1].setText("158.5");sensorz[1].setText("73.4"); */
/*         sensorx[2].setText("75.2");sensory[2].setText("0");sensorz[2].setText("73.4"); */
/*         sensorx[3].setText("0");sensory[3].setText("0");sensorz[3].setText("73.4"); */

        // RPS demo in inches
        sensorx[0].setText("0");sensory[0].setText("0");sensorz[0].setText("30");
        sensorx[1].setText("0");sensory[1].setText("31.5");sensorz[1].setText("30");
        sensorx[2].setText("0");sensory[2].setText("62");sensorz[2].setText("30");
        sensorx[3].setText("29.5");sensory[3].setText("62");sensorz[3].setText("30");
        sensorx[4].setText("29.5");sensory[4].setText("31.5");sensorz[4].setText("30");
        sensorx[5].setText("29.5");sensory[5].setText("0");sensorz[5].setText("30");
        
        vs.setText("0.01354");  // inches/sec
/*         vs.setText("0.0344");  // cm/sec */

        offset.setText("0");

    }
    
    double getVSound() {
        try {
            return Double.valueOf(vs.getText()).doubleValue();
        } catch (Exception e) {
            vs.setText("0.0344");
            return 0.0344;
        }
    }
    
    int getOffset() {
        try {
            return Integer.valueOf(offset.getText()).intValue();
        } catch (Exception e) {
            offset.setText("0");
            return 0;
        }
    }
    
    /**
     * Invoked by button to do one cycle
     */
    void doOnce() {
        switch (mode.getSelectedIndex()) {
        default: // should not happen
            log.error("Did not expect selected mode "+mode.getSelectedIndex());
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
            } catch (java.io.IOException e) {log.error("exception "+e);}
            return;
        case 3: // From X,Y,Z file
            try {
                doLoadMeasurementFromFile();
            } catch (java.io.IOException e) {log.error("exception "+e);}
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
            if (!readingInput.readRecord()) throw new java.io.IOException("no valid file");
        }
        // item 0 is the ID, not used right now
        for (int i = 0; i< Math.min(NUMSENSORS, readingInput.getColumnCount()+1); i++) {
            times[i].setText(readingInput.get(i+1));
        }
    }
    
    void setupReadingFile() throws java.io.IOException {
        // get file
        readingInput = null;
        
        readingFileChooser.rescanCurrentDirectory();
        int retVal = readingFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
    
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
            if (!measurementInput.readRecord()) throw new java.io.IOException("no valid file");
        }

        // item 0 is the ID, not used right now
        Measurement m = new Measurement(null, 
                            Double.valueOf(measurementInput.get(1)).doubleValue(),
                            Double.valueOf(measurementInput.get(2)).doubleValue(),
                            Double.valueOf(measurementInput.get(3)).doubleValue(),
                            getVSound(),
                            0,
                            "Data File"
                        );
        
        lastPoint = m;
        Distributor.getInstance().submitMeasurement(m);
    }
    
    void setupMeasurementFile() throws java.io.IOException {
        // get file
        measurementInput = null;
        
        measurementFileChooser.rescanCurrentDirectory();
        int retVal = measurementFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
    
        // create and keep reader
        java.io.Reader reader = new java.io.FileReader(
                                        measurementFileChooser.getSelectedFile());
        measurementInput = new com.csvreader.CsvReader(reader);
    }

    void setResidual(int i, Measurement m) {
        Point3d p = getPoint(i);
        Point3d x = new Point3d((float)m.getX(), (float)m.getY(), (float)m.getZ());
        
        double rt = p.distance(x)/getVSound();
        int res = (int) (rt-m.getReading().getValue(i));
        residuals[i].setText(""+res);
    }
    
    Measurement lastPoint = null;
    
    Reading getReadingFromTimeFields() {
        // parse input
        int count = 0;
        for (int i = 0; i<NUMSENSORS; i++) {
            if (!times[i].getText().equals("")) count++;
        }
        
        double[] values = new double[count];
        
        int index = 0;
        for (int i = 0; i<NUMSENSORS; i++) {
            if (!times[i].getText().equals("")) {
                values[index] = Double.valueOf(times[i].getText()).doubleValue();
                index++;
            }
        }

        // get the id number
        int idnum = 21;
        try {
            idnum = Integer.valueOf(id.getText()).intValue();
        } catch (Exception e) {}

        Reading r = new Reading(idnum, values);
        return r;
    }
        
    void doReadingFromTimeFields() {
        // get the reading
        Reading r = getReadingFromTimeFields();

        // and forward
        Distributor.getInstance().submitReading(r);
    }
    
    public void notify(Reading r) {
        // This implementation creates a new Calculator
        // each time to ensure that the most recent
        // receiver positions are used; this should be
        // replaced with some notification system
        // to reduce the work used.

        // Display this set of time values
        for (int i = 0; i<Math.min(r.getNSample(), times.length); i++) {
            times[i].setText(nf.format(r.getValue(i)));
        }
        
        // create a set of device locations
        int count = 0;
        for (int i = 0; i<NUMSENSORS; i++) {
            if (getPoint(i)!=null) count++;
        }
        
        int index = 0;
        Point3d list[] = new Point3d[count];
        for (int i = 0; i<NUMSENSORS; i++) {
            Point3d p = getPoint(i);
            if ( p != null ) {
                list[index] = p;
                index++;
            }
        }
        
        Calculator c = Algorithms.newCalculator(list, getVSound(), getOffset(), (String)algorithm.getSelectedItem());

        Measurement m = c.convert(r, lastPoint);

        lastPoint = m;
        Distributor.getInstance().submitMeasurement(m);
    }

    void doMeasurementFromPositionFields() {
        // contain dummy Reading
        int idnum = 21;
        try {
            idnum = Integer.valueOf(id.getText()).intValue();
        } catch (Exception e) {}
        
        Reading r = new Reading(idnum, new double[]{0.,0.,0.,0.});
        
        Measurement m = new Measurement(r, 
                            Double.valueOf(x.getText()).doubleValue(),
                            Double.valueOf(y.getText()).doubleValue(),
                            Double.valueOf(z.getText()).doubleValue(),
                            getVSound(),
                            0,
                            "Position Data"
                        );

        lastPoint = m;
        Distributor.getInstance().submitMeasurement(m);
    }
    
    public void notify(Measurement m) {
        // show result
        x.setText(nf.format(m.getX()));
        y.setText(nf.format(m.getY()));
        z.setText(nf.format(m.getZ()));
        code.setText(""+m.getCode());
        try {
            for (int i=0; i<NUMSENSORS; i++) 
                setResidual(i, m);
        } catch (Exception e) {}
    }
    
    // to find and remember the input files
    com.csvreader.CsvReader readingInput = null;
    final javax.swing.JFileChooser readingFileChooser = new JFileChooser("rps/readings.csv");

    com.csvreader.CsvReader measurementInput = null;
    final javax.swing.JFileChooser measurementFileChooser = new JFileChooser("rps/positions.csv");

    /**
     * Service routine for finding a Point3d from input fields
     */
    Point3d getPoint(int index) {
        try {
            float xval = Float.valueOf(sensorx[index].getText()).floatValue();
            float yval = Float.valueOf(sensory[index].getText()).floatValue();
            float zval = Float.valueOf(sensorz[index].getText()).floatValue();
            return new Point3d(xval, yval, zval);
        } catch (java.lang.NumberFormatException e) { return null; }
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DisplayFrame.class.getName());
}
