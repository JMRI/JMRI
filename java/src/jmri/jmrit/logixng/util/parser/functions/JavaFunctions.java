package jmri.jmrit.logixng.util.parser.functions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TypeConversionUtil;

import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of string functions.
 *
 * @author Daniel Bergqvist 2020
 */
@ServiceProvider(service = FunctionFactory.class)
public class JavaFunctions implements FunctionFactory {

    @Override
    public String getModule() {
        return "Java";
    }

    @Override
    public Set<Function> getFunctions() {
        Set<Function> functionClasses = new HashSet<>();

        addNewFunction(functionClasses);

        return functionClasses;
    }

    @Override
    public Set<Constant> getConstants() {
        Set<Constant> constantClasses = new HashSet<>();
        constantClasses.add(new Constant(getModule(), "null", null));
        constantClasses.add(new Constant(getModule(), "None", null));
        return constantClasses;
    }

    @Override
    public String getConstantDescription() {
        return Bundle.getMessage("Java.ConstantDescriptions");
    }

    private void addNewFunction(Set<Function> functionClasses) {
        functionClasses.add(new AbstractFunction(this, "new", Bundle.getMessage("Java.new_Descr")) {
            private boolean isAssignableFrom(Class<?> type, Object param) {
                if (param == null) return true;
                if (type.isAssignableFrom(param.getClass())) return true;
                if ((type == Byte.TYPE) && (param instanceof Long)) return true;
                if ((type == Short.TYPE) && (param instanceof Long)) return true;
                if ((type == Integer.TYPE) && (param instanceof Long)) return true;
                return ((type == Float.TYPE) && (param instanceof Double));
            }

            private boolean canCall(Constructor<?> constructor, Object[] params) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (paramTypes.length != params.length) return false;
                for (int i=0; i < paramTypes.length; i++) {
                    if (!isAssignableFrom(paramTypes[i], params[i])) return false;
                }
                return true;
            }

            private Object callConstructor(Constructor<?> constructor, Object[] params)
                    throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {

                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] newParams = new Object[params.length];
                for (int i=0; i < params.length; i++) {
                    Object newParam;
                    if ((params[i] == null) || (paramTypes[i].isAssignableFrom(params[i].getClass()))) newParam = params[i];
                    else if ((paramTypes[i] == Byte.TYPE) && (params[i] instanceof Long)) newParam = (byte)(long)params[i];
                    else if ((paramTypes[i] == Short.TYPE) && (params[i] instanceof Long)) newParam = (short)(long)params[i];
                    else if ((paramTypes[i] == Integer.TYPE) && (params[i] instanceof Long)) newParam = (int)(long)params[i];
                    else if ((paramTypes[i] == Float.TYPE) && (params[i] instanceof Double)) newParam = (float)(double)params[i];
                    else throw new RuntimeException(String.format("%s cannot be assigned to %s", params[i].getClass().getName(), paramTypes[i].getName()));
                    newParams[i] = newParam;
                }
                return constructor.newInstance(newParams);
            }

            public Object createInstance(String className, List<Object> parameters)
                    throws JmriException, ClassNotFoundException, InstantiationException {

                if (className == null) throw new NullPointerException("Class name is null");

                Class<?> clazz = Class.forName(className);
                Constructor<?>[] constructors = clazz.getConstructors();
                Object[] params = parameters.toArray();

                Exception exception = null;
                for (Constructor<?> c : constructors) {
                    try {
                        if (canCall(c, params)) return callConstructor(c, params);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        exception = ex;
                    }
                }

                if (exception != null) throw new ReflectionException("Reflection exception", exception);

                List<String> paramList = new ArrayList<>();
                for (Object o : params) paramList.add(String.format("%s:%s", o, o != null ? o.getClass().getName() : "null"));
                throw new CannotCreateInstanceException(String.format("Can not create new instance of class %s with parameters %s", clazz.getName(), String.join(", ", paramList)), clazz.getName());
            }

            @Override
            public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
                    throws CalculateException, JmriException {
                if (parameterList.isEmpty()) {
                    throw new WrongNumberOfParametersException(Bundle.getMessage("WrongNumberOfParameters1", getName(), 1));
                }

                String className = TypeConversionUtil.convertToString(
                        parameterList.get(0).calculate(symbolTable), false);

                List<Object> list = new ArrayList<>();
                for (int i=1; i < parameterList.size(); i++) {
                    list.add(parameterList.get(i).calculate(symbolTable));
                }

                try {
                    return createInstance(className, list);
                } catch (ClassNotFoundException e) {
                    throw new ClassIsNotFoundException(String.format("The class %s is not found", className), className);
                } catch (InstantiationException e) {
                    throw new ReflectionException("Reflection exception", e);
                }
            }
        });
    }

}
