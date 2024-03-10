package jmri.jmrit.logixng.util.parser.functions;

import java.time.Instant;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.parser.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of clock functions.
 *
 * @author Daniel Bergqvist 2020
 */
@ServiceProvider(service = FunctionFactory.class)
public class ClockFunctions implements FunctionFactory {

    private final Timebase _fastClock = InstanceManager.getDefault(jmri.Timebase.class);

    @Override
    public String getModule() {
        return "Clock";
    }

    @Override
    public Set<Function> getFunctions() {
        Set<Function> functionClasses = new HashSet<>();

        addSystemClockFunction(functionClasses);
        addFastClockFunction(functionClasses);
        addFastClockRateFunction(functionClasses);
        addFastClockRunningFunction(functionClasses);

        return functionClasses;
    }

    @Override
    public Set<Constant> getConstants() {
        return new HashSet<>();
    }

    @Override
    public String getConstantDescription() {
        // This module doesn't define any constants
        return null;
    }

    private void addSystemClockFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "systemClock", Bundle.getMessage("Clock.systemClock_Descr")) {
            @Override
            @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {

                Date currentTime = Date.from(Instant.now());

                if (parameterList.isEmpty()) {  // Num minutes since midnight
                    return (currentTime.getHours() * 60) + currentTime.getMinutes();
                } else if (parameterList.size() == 1) {
                    Object param = parameterList.get(0).calculate(symbolTable);
                    if (param instanceof String) {
                        switch ((String)param) {
                            case "hour":
                                return currentTime.getHours();
                            case "min":
                                return currentTime.getMinutes();
                            case "sec":
                                return currentTime.getSeconds();
                            case "minOfDay":
                                return (currentTime.getHours() * 60) + currentTime.getMinutes();
                            case "secOfDay":
                                return ((currentTime.getHours() * 60) + currentTime.getMinutes()) * 60 + currentTime.getSeconds();
                            default:
                                throw new CalculateException(Bundle.getMessage("IllegalParameter", 1, param, getName()));
                        }
                    } else {
                        throw new CalculateException(Bundle.getMessage("IllegalParameter", 1, param, getName()));
                    }
                }
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
            }
        });
    }

    private void addFastClockFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "fastClock", Bundle.getMessage("Clock.fastClock_Descr")) {
            @Override
            @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                Date currentTime = _fastClock.getTime();

                if (parameterList.isEmpty()) {  // Num minutes since midnight
                    return (currentTime.getHours() * 60) + currentTime.getMinutes();
                } else if (parameterList.size() == 1) {
                    Object param = parameterList.get(0).calculate(symbolTable);
                    if (param instanceof String) {
                        switch ((String)param) {
                            case "hour":
                                return currentTime.getHours();
                            case "min":
                                return currentTime.getMinutes();
                            case "minOfDay":
                                return (currentTime.getHours() * 60) + currentTime.getMinutes();
                            default:
                                throw new CalculateException(Bundle.getMessage("IllegalParameter", 1, param, getName()));
                        }
                    } else {
                        throw new CalculateException(Bundle.getMessage("IllegalParameter", 1, param, getName()));
                    }
                }
                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
            }
        });
    }

    private void addFastClockRateFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "fastClockRate", Bundle.getMessage("Clock.fastClockRate_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                double rate = _fastClock.userGetRate();

                if (parameterList.isEmpty()) return rate;

                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
            }
        });
    }

    private void addFastClockRunningFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "isFastClockRunning", Bundle.getMessage("Clock.isFastClockRunning_Descr")) {
            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws JmriException {

                boolean rate = _fastClock.getRun();

                if (parameterList.isEmpty()) return rate;

                throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName()));
            }
        });
    }

}
