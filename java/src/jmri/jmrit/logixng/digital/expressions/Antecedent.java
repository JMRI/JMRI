package jmri.jmrit.logixng.digital.expressions;

import static jmri.Conditional.OPERATOR_AND;
import static jmri.Conditional.OPERATOR_NONE;
import static jmri.Conditional.OPERATOR_OR;

import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates to True if the antecedent evaluates to true
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class Antecedent extends AbstractDigitalExpression implements FemaleSocketListener {

    static final java.util.ResourceBundle rbx = java.util.ResourceBundle.getBundle("jmri.jmrit.conditional.ConditionalBundle");  // NOI18N
    
    private String _antecedent = "";
    private final List<ExpressionEntry> _expressionEntries = new ArrayList<>();
    
    /**
     * Create a new instance of Antecedent with system name and user name.
     */
    public Antecedent(@Nonnull String sys, @CheckForNull String user) {
        super(sys, user);
        init();
    }

    public Antecedent(@Nonnull String sys, @CheckForNull String user,
            List<Map.Entry<String, String>> expressionSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        setExpressionSystemNames(expressionSystemNames);
    }

    private void init() {
        _expressionEntries
                .add(new ExpressionEntry(InstanceManager.getDefault(DigitalExpressionManager.class)
                        .createFemaleSocket(this, this, getNewSocketName())));
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws Exception {
        
        if (_antecedent.isEmpty()) {
            return false;
        }
        
        boolean result;
        
        char[] ch = _antecedent.toCharArray();
        int n = 0;
        for (int j = 0; j < ch.length; j++) {
            if (ch[j] != ' ') {
                if (ch[j] == '{' || ch[j] == '[') {
                    ch[j] = '(';
                } else if (ch[j] == '}' || ch[j] == ']') {
                    ch[j] = ')';
                }
                ch[n++] = ch[j];
            }
        }
        try {
            List<ExpressionEntry> list = new ArrayList<>();
            for (ExpressionEntry e : _expressionEntries) {
                list.add(e);
            }
            DataPair dp = parseCalculate(new String(ch, 0, n), list);
            result = dp.result;
        } catch (NumberFormatException | IndexOutOfBoundsException | JmriException nfe) {
            result = false;
            log.error(getDisplayName() + " parseCalculation error antecedent= " + _antecedent + ", ex= " + nfe);  // NOI18N
        }
        
        return result;
    }
    
    /** {@inheritDoc} */
    @Override
    public void reset() {
        for (ExpressionEntry e : _expressionEntries) {
            e._socket.reset();
        }
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _expressionEntries.get(index)._socket;
    }
    
    @Override
    public int getChildCount() {
        return _expressionEntries.size();
    }
    
    public void setChildCount(int count) {
        // Is there too many children?
        while (_expressionEntries.size() > count) {
            int childNo = _expressionEntries.size()-1;
            FemaleSocket socket = _expressionEntries.get(childNo)._socket;
            if (socket.isConnected()) {
                socket.disconnect();
            }
            _expressionEntries.remove(childNo);
        }
        
        // Is there not enough children?
        while (_expressionEntries.size() < count) {
            _expressionEntries
                    .add(new ExpressionEntry(
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .createFemaleSocket(this, this, getNewSocketName())));
        }
    }
    
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Antecedent_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        if (_antecedent.isEmpty()) {
            return Bundle.getMessage(locale, "Antecedent_Long_Empty");
        } else {
            return Bundle.getMessage(locale, "Antecedent_Long", _antecedent);
        }
    }

    private void setExpressionSystemNames(List<Map.Entry<String, String>> systemNames) {
        if (!_expressionEntries.isEmpty()) {
            throw new RuntimeException("expression system names cannot be set more than once");
        }
        
        for (Map.Entry<String, String> entry : systemNames) {
            FemaleDigitalExpressionSocket socket =
                    InstanceManager.getDefault(DigitalExpressionManager.class)
                            .createFemaleSocket(this, this, entry.getKey());
            
            _expressionEntries.add(new ExpressionEntry(socket, entry.getValue()));
        }
    }
    
    public String getExpressionSystemName(int index) {
        return _expressionEntries.get(index)._socketSystemName;
    }

    public String getAntecedent() {
        return _antecedent;
    }

    public final void setAntecedent(String antecedent) throws Exception {
//        String result = validateAntecedent(antecedent, _expressionEntries);
//        if (result != null) System.out.format("DANIEL: Exception: %s%n", result);
//        if (result != null) throw new IllegalArgumentException(result);
        _antecedent = antecedent;
    }
    
    @Override
    public void connected(FemaleSocket socket) {
        boolean hasFreeSocket = false;
        for (ExpressionEntry entry : _expressionEntries) {
            hasFreeSocket = !entry._socket.isConnected();
            if (socket == entry._socket) {
                entry._socketSystemName =
                        socket.getConnectedSocket().getSystemName();
            }
        }
        if (!hasFreeSocket) {
            _expressionEntries
                    .add(new ExpressionEntry(
                            InstanceManager.getDefault(DigitalExpressionManager.class)
                                    .createFemaleSocket(this, this, getNewSocketName())));
        }
        firePropertyChange(Base.PROPERTY_SOCKET_CONNECTED, null, socket);
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        for (ExpressionEntry entry : _expressionEntries) {
            if (socket == entry._socket) {
                entry._socketSystemName = null;
                break;
            }
        }
        firePropertyChange(Base.PROPERTY_SOCKET_DISCONNECTED, null, socket);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        for (ExpressionEntry ee : _expressionEntries) {
            try {
                if ( !ee._socket.isConnected()
                        || !ee._socket.getConnectedSocket().getSystemName()
                                .equals(ee._socketSystemName)) {

                    String socketSystemName = ee._socketSystemName;
                    ee._socket.disconnect();
                    if (socketSystemName != null) {
                        MaleSocket maleSocket =
                                InstanceManager.getDefault(DigitalExpressionManager.class)
                                        .getBySystemName(socketSystemName);
                        if (maleSocket != null) {
                            ee._socket.connect(maleSocket);
                            maleSocket.setup();
                        } else {
                            log.error("cannot load digital action " + socketSystemName);
                        }
                    }
                } else {
                    ee._socket.getConnectedSocket().setup();
                }
            } catch (SocketAlreadyConnectedException ex) {
                // This shouldn't happen and is a runtime error if it does.
                throw new RuntimeException("socket is already connected");
            }
        }
    }



    /**
     * Check that an antecedent is well formed.
     *
     * @param ant the antecedent string description
     * @param expressionEntryList arraylist of existing ExpressionEntries
     * @return error message string if not well formed
     */
    public String validateAntecedent(String ant, List<ExpressionEntry> expressionEntryList) throws Exception {
        char[] ch = ant.toCharArray();
        int n = 0;
        for (int j = 0; j < ch.length; j++) {
            if (ch[j] != ' ') {
                if (ch[j] == '{' || ch[j] == '[') {
                    ch[j] = '(';
                } else if (ch[j] == '}' || ch[j] == ']') {
                    ch[j] = ')';
                }
                ch[n++] = ch[j];
            }
        }
        int count = 0;
        for (int j = 0; j < n; j++) {
            if (ch[j] == '(') {
                count++;
            }
            if (ch[j] == ')') {
                count--;
            }
        }
        if (count > 0) {
            return java.text.MessageFormat.format(
                    rbx.getString("ParseError7"), new Object[]{')'});  // NOI18N
        }
        if (count < 0) {
            return java.text.MessageFormat.format(
                    rbx.getString("ParseError7"), new Object[]{'('});  // NOI18N
        }
        try {
            DataPair dp = parseCalculate(new String(ch, 0, n), expressionEntryList);
            if (n != dp.indexCount) {
                return java.text.MessageFormat.format(
                        rbx.getString("ParseError4"), new Object[]{ch[dp.indexCount - 1]});  // NOI18N
            }
            int index = dp.argsUsed.nextClearBit(0);
            if (index >= 0 && index < expressionEntryList.size()) {
//                System.out.format("Daniel: ant: %s%n", ant);
                return java.text.MessageFormat.format(
                        rbx.getString("ParseError5"),  // NOI18N
                        new Object[]{expressionEntryList.size(), index + 1});
            }
        } catch (NumberFormatException | IndexOutOfBoundsException | JmriException nfe) {
            return rbx.getString("ParseError6") + nfe.getMessage();  // NOI18N
        }
        return null;
    }

    /**
     * Parses and computes one parenthesis level of a boolean statement.
     * <p>
     * Recursively calls inner parentheses levels. Note that all logic operators
     * are detected by the parsing, therefore the internal negation of a
     * variable is washed.
     *
     * @param s            The expression to be parsed
     * @param expressionEntryList ExpressionEntries for R1, R2, etc
     * @return a data pair consisting of the truth value of the level a count of
     *         the indices consumed to parse the level and a bitmap of the
     *         variable indices used.
     * @throws jmri.JmriException if unable to compute the logic
     */
    DataPair parseCalculate(String s, List<ExpressionEntry> expressionEntryList)
            throws Exception {

        // for simplicity, we force the string to upper case before scanning
        s = s.toUpperCase();

        BitSet argsUsed = new BitSet(expressionEntryList.size());
        DataPair dp = null;
        boolean leftArg = false;
        boolean rightArg = false;
        int oper = OPERATOR_NONE;
        int k = -1;
        int i = 0;      // index of String s
        //int numArgs = 0;
        if (s.charAt(i) == '(') {
            dp = parseCalculate(s.substring(++i), expressionEntryList);
            leftArg = dp.result;
            i += dp.indexCount;
            argsUsed.or(dp.argsUsed);
        } else // cannot be '('.  must be either leftArg or notleftArg
        {
            if (s.charAt(i) == 'R') {  // NOI18N
                try {
                    k = Integer.parseInt(String.valueOf(s.substring(i + 1, i + 3)));
                    i += 2;
                } catch (NumberFormatException | IndexOutOfBoundsException nfe) {
                    k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                }
                leftArg = expressionEntryList.get(k - 1)._socket.evaluate();
                i++;
                argsUsed.set(k - 1);
            } else if ("NOT".equals(s.substring(i, i + 3))) {  // NOI18N
                i += 3;

                // not leftArg
                if (s.charAt(i) == '(') {
                    dp = parseCalculate(s.substring(++i), expressionEntryList);
                    leftArg = dp.result;
                    i += dp.indexCount;
                    argsUsed.or(dp.argsUsed);
                } else if (s.charAt(i) == 'R') {  // NOI18N
                    try {
                        k = Integer.parseInt(String.valueOf(s.substring(i + 1, i + 3)));
                        i += 2;
                    } catch (NumberFormatException | IndexOutOfBoundsException nfe) {
                        k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                    }
                    leftArg = expressionEntryList.get(k - 1)._socket.evaluate();
                    i++;
                    argsUsed.set(k - 1);
                } else {
                    throw new JmriException(java.text.MessageFormat.format(
                            rbx.getString("ParseError1"), new Object[]{s.substring(i)}));  // NOI18N
                }
                leftArg = !leftArg;
            } else {
                throw new JmriException(java.text.MessageFormat.format(
                        rbx.getString("ParseError9"), new Object[]{s}));  // NOI18N
            }
        }
        // crank away to the right until a matching parent is reached
        while (i < s.length()) {
            if (s.charAt(i) != ')') {
                // must be either AND or OR
                if ("AND".equals(s.substring(i, i + 3))) {  // NOI18N
                    i += 3;
                    oper = OPERATOR_AND;
                } else if ("OR".equals(s.substring(i, i + 2))) {  // NOI18N
                    i += 2;
                    oper = OPERATOR_OR;
                } else {
                    throw new JmriException(java.text.MessageFormat.format(
                            rbx.getString("ParseError2"), new Object[]{s.substring(i)}));  // NOI18N
                }
                if (s.charAt(i) == '(') {
                    dp = parseCalculate(s.substring(++i), expressionEntryList);
                    rightArg = dp.result;
                    i += dp.indexCount;
                    argsUsed.or(dp.argsUsed);
                } else // cannot be '('.  must be either rightArg or notRightArg
                {
                    if (s.charAt(i) == 'R') {  // NOI18N
                        try {
                            k = Integer.parseInt(String.valueOf(s.substring(i + 1, i + 3)));
                            i += 2;
                        } catch (NumberFormatException | IndexOutOfBoundsException nfe) {
                            k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                        }
                        rightArg = expressionEntryList.get(k - 1)._socket.evaluate();
                        i++;
                        argsUsed.set(k - 1);
                    } else if ("NOT".equals(s.substring(i, i + 3))) {  // NOI18N
                        i += 3;
                        // not rightArg
                        if (s.charAt(i) == '(') {
                            dp = parseCalculate(s.substring(++i), expressionEntryList);
                            rightArg = dp.result;
                            i += dp.indexCount;
                            argsUsed.or(dp.argsUsed);
                        } else if (s.charAt(i) == 'R') {  // NOI18N
                            try {
                                k = Integer.parseInt(String.valueOf(s.substring(i + 1, i + 3)));
                                i += 2;
                            } catch (NumberFormatException | IndexOutOfBoundsException nfe) {
                                k = Integer.parseInt(String.valueOf(s.charAt(++i)));
                            }
                            rightArg = expressionEntryList.get(k - 1)._socket.evaluate();
                            i++;
                            argsUsed.set(k - 1);
                        } else {
                            throw new JmriException(java.text.MessageFormat.format(
                                    rbx.getString("ParseError3"), new Object[]{s.substring(i)}));  // NOI18N
                        }
                        rightArg = !rightArg;
                    } else {
                        throw new JmriException(java.text.MessageFormat.format(
                                rbx.getString("ParseError9"), new Object[]{s.substring(i)}));  // NOI18N
                    }
                }
                if (oper == OPERATOR_AND) {
                    leftArg = (leftArg && rightArg);
                } else if (oper == OPERATOR_OR) {
                    leftArg = (leftArg || rightArg);
                }
            } else {  // This level done, pop recursion
                i++;
                break;
            }
        }
        dp = new DataPair();
        dp.result = leftArg;
        dp.indexCount = i;
        dp.argsUsed = argsUsed;
        return dp;
    }


    static class DataPair {
        boolean result = false;
        int indexCount = 0;         // index reached when parsing completed
        BitSet argsUsed = null;     // error detection for missing arguments
    }

    /* This class is public since ExpressionAntecedentXml needs to access it. */
    public static class ExpressionEntry {
        private String _socketSystemName;
        private final FemaleDigitalExpressionSocket _socket;
        
        public ExpressionEntry(FemaleDigitalExpressionSocket socket, String socketSystemName) {
            _socketSystemName = socketSystemName;
            _socket = socket;
        }
        
        private ExpressionEntry(FemaleDigitalExpressionSocket socket) {
            this._socket = socket;
        }
        
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    private final static Logger log = LoggerFactory.getLogger(Antecedent.class);
}
