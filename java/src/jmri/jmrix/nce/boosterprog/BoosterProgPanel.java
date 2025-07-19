package jmri.jmrix.nce.boosterprog;

import java.awt.*;

import javax.swing.*;

import jmri.*;
import jmri.jmrix.nce.NceSystemConnectionMemo;

/**
 * Panel for configuring an NCE booster.
 *
 * @author ken cameron Copyright (C) 2010 Derived from BoosterProgFrame by
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class BoosterProgPanel extends jmri.jmrix.nce.swing.NcePanel {

    JTextField start = new JTextField(6);
    JTextField length = new JTextField(12);
    JLabel status = new JLabel();

    public BoosterProgPanel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof NceSystemConnectionMemo) {
            try {
                initComponents((NceSystemConnectionMemo) context);
            } catch (Exception e) {
                log.warn("BoosterProg initContext", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.nce.boosterprog.BoosterProgPanel";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("NCE_");
        }
        x.append(": ");
        x.append(Bundle.getMessage("TitleBoosterProg"));
        return x.toString();
    }

    /**
     * The minimum frame size for font size 16
     */
    @Override
    public Dimension getMinimumDimension() {
        return new Dimension(400, 200);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(NceSystemConnectionMemo m) {
        this.memo = m;

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI, one line at a time
        // box of entries
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel warn = new JLabel(Bundle.getMessage("Warn1"));
        warn.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(warn);
        warn = new JLabel(Bundle.getMessage("Warn2"));
        warn.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(warn);
        box.add(p);

        box.add(Box.createRigidArea(new Dimension(0,5)));

        p = new JPanel();
        p.setLayout(new FlowLayout());
        JButton b = new JButton(Bundle.getMessage("ButtonSet"));
        p.add(new JLabel(Bundle.getMessage("LabelStart")));
        start.setText("30");
        p.add(start);
        p.add(Box.createHorizontalGlue());
        p.add(b);
        b.addActionListener(e -> setStartPushed());
        box.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        b = new JButton(Bundle.getMessage("ButtonSet"));
        p.add(new JLabel(Bundle.getMessage("LabelDuration")));
        length.setText("420");
        p.add(length);
        p.add(Box.createHorizontalGlue());
        p.add(b);
        b.addActionListener(e -> setDurationPushed());
        box.add(p);

        add(box);
        add(Box.createVerticalGlue());

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("StatusLabel") + " "));
        p.add(status);
        status.setText(Bundle.getMessage("StatusOK"));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);
    }

    private AddressedProgrammer p = null;

    private void getProgrammer() {
        p = memo.getProgrammerManager().getAddressedProgrammer(true, 0);
    }

    private void releaseProgrammer() {
        if (p != null) {
            memo.getProgrammerManager().releaseAddressedProgrammer(p);
        }
        p = null;
    }

    void setStartPushed() {
        getProgrammer();
        status.setText(Bundle.getMessage("StatusProgramming"));
        int val = Integer.parseInt(start.getText());

        try {
            p.writeCV("255", val, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                    status.setText(Bundle.getMessage("StatusOK"));
                }
            });
        } catch (ProgrammerException e) {
            status.setText(Bundle.getMessage("StatusError") + e);
        } finally {
            releaseProgrammer();
        }
    }

    void setStart(int val) {
        getProgrammer();

        try {
            p.writeCV("255", val, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                }
            });
        } catch (ProgrammerException ignored) {
        } finally {
            releaseProgrammer();
        }
    }

    void setDuration(final int val) {
        getProgrammer();

        try {
            p.writeCV("253", val / 256, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                    synchronized (this) {
                        try {
                            wait(1500);  // needed for booster to reset
                        } catch (InterruptedException i) {
                            Thread.currentThread().interrupt(); // retain if needed later
                        }
                    }
                    try {
                        p.writeCV("254", val % 256, new ProgListener() {
                            @Override
                            public void programmingOpReply(int value, int retval) {
                            }
                        });
                    } catch (ProgrammerException ignored) {
                    } finally {
                        releaseProgrammer();
                    }
                }
            });
        } catch (ProgrammerException e) {
            releaseProgrammer();
        }
    }

    void setDurationPushed() {
        getProgrammer();
        status.setText(Bundle.getMessage("StatusProgramming"));
        int val = Integer.parseInt(length.getText()) / 256;

        try {
            p.writeCV("253", val, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                    synchronized (this) {
                        try {
                            wait(1500);  // needed for booster to reset
                        } catch (InterruptedException ignored) {
                        }
                    }
                    durationPart2();
                }
            });
        } catch (ProgrammerException e) {
            status.setText(Bundle.getMessage("StatusError") + e);
            releaseProgrammer();
        }
    }

    void durationPart2() {
        status.setText(Bundle.getMessage("StatusProgramming"));
        int val = Integer.parseInt(length.getText()) % 256;

        try {
            p.writeCV("254", val, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                    status.setText(Bundle.getMessage("StatusOK"));
                }
            });
        } catch (ProgrammerException e) {
            status.setText(Bundle.getMessage("StatusError") + e);
        } finally {
            releaseProgrammer();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BoosterProgPanel.class);

}
