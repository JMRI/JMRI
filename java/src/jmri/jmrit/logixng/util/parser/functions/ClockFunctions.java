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

    @Override
    public String getModule() {
        return "Clock";
    }
    
    @Override
    public Set<Function> getFunctions() {
        Set<Function> functionClasses = new HashSet<>();
        functionClasses.add(new SystemClockFunction());
        functionClasses.add(new FastClockFunction());
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
    
    
    
    public static class SystemClockFunction implements Function {
        
        @Override
        public String getModule() {
            return new ClockFunctions().getModule();
        }
        
        @Override
        public String getConstantDescriptions() {
            return new ClockFunctions().getConstantDescription();
        }
        
        @Override
        public String getName() {
            return "systemClock";
        }
        
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
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Clock.systemClock_Descr");
        }
        
    }
    
    public static class FastClockFunction implements Function {
        
        private final Timebase _fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        
        @Override
        public String getModule() {
            return new ClockFunctions().getModule();
        }
        
        @Override
        public String getConstantDescriptions() {
            return new ClockFunctions().getConstantDescription();
        }
        
        @Override
        public String getName() {
            return "fastClock";
        }
        
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
        
        @Override
        public String getDescription() {
            return Bundle.getMessage("Clock.fastClock_Descr");
        }
        
    }
    
}
