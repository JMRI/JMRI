package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Provide an abstract implementation of a *StreamPortController for stream
 * based ports.
 * <p>
 * Implementing classes need to implement status and configure from the
 * portAdapter interface, along with any system specific requirements.
 *
 * @author Paul Bender Copyright (C) 2014
 */
public abstract class AbstractStreamPortController extends AbstractPortController {

    protected String _name = null;
    protected DataInputStream input = null;
    protected DataOutputStream output = null;

    public AbstractStreamPortController(SystemConnectionMemo connectionMemo, DataInputStream in, DataOutputStream out, String pname) {
        super(connectionMemo);
        _name = pname;
        input = in;
        output = out;
    }

    public AbstractStreamPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    // return the InputStream from the port
    @Override
    public DataInputStream getInputStream() {
        return input;
    }

    // return the outputStream to the port
    @Override
    public DataOutputStream getOutputStream() {
        return output;
    }

    @Override
    public String getCurrentPortName() {
        return _name;
    }

    @Override
    public void recover() {
        // no recovery possible here.
    }

    // connection shouldn't require any action.
    @Override
    public void connect() {
    }

    public void connect(AbstractPortController port) {
        _name = port.getCurrentPortName();
        input = port.getInputStream();
        output = port.getOutputStream();
    }

    @Override
    public void dispose() {
        super.dispose();
        input = null;
        output = null;
    }

    // static private final Logger log = LoggerFactory.getLogger(AbstractStreamPortController.class);

}
