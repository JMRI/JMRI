/*
 * Copyright (c) 2018, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package jmri.script.jsr223graalpython;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.graalvm.nativeimage.ImageInfo;
import org.graalvm.polyglot.Engine;

import org.openide.util.lookup.ServiceProvider;
@ServiceProvider(service = javax.script.ScriptEngineFactory.class)

public final class GraalJSEngineFactory implements ScriptEngineFactory {

    private static final String ENGINE_NAME = "Graal.python";
    private static final String NAME = "python";
    private static final String LANGUAGE = "Python";
    private static final String LANGUAGE_VERSION = "3";

    private static final String[] NAMES = {"py3", "Python", "python", "Python3", "python3",
                    "Graal.python", "graal.python", "Graal-python", "graal-python",
                    "Graal.Python", "Graal-Python", "GraalPython", "GraalPythonPolyglot"};
    private static final String[] MIME_TYPES = {"application/javascript", "application/ecmascript", "text/javascript", "text/ecmascript"};
    private static final String[] EXTENSIONS = {"py3"};

    private static final String NASHORN_ENGINE_NAME = "Oracle Nashorn";
    private static final List<String> names;
    private static final List<String> mimeTypes;
    private static final List<String> extensions;

    public static final boolean RegisterAsNashornScriptEngineFactory = Boolean.getBoolean("graaljs.RegisterGraalJSAsNashorn");

    static {
        List<String> nameList = Arrays.asList(NAMES);
        List<String> mimeTypeList = Arrays.asList(MIME_TYPES);
        List<String> extensionList = Arrays.asList(EXTENSIONS);

        // Needed on Java 8 only: ensure Graal.js is consistently picked as the js engine.
        // Skipped if the class is initialized at run time in a native image since likely,
        // the image was not built with a reflection config that would allow patching.
        boolean java8 = System.getProperty("java.specification.version").compareTo("1.9") < 0;
        if (java8 && !ImageInfo.inImageRuntimeCode()) {
            ScriptEngineFactory nashornFactory = getNashornEngineFactory();
            if (nashornFactory != null) {
                if (RegisterAsNashornScriptEngineFactory) {
                    nameList = new ArrayList<>(nameList);
                    nameList.removeAll(nashornFactory.getNames());
                    nameList.addAll(nashornFactory.getNames());
                    mimeTypeList = new ArrayList<>(mimeTypeList);
                    mimeTypeList.removeAll(nashornFactory.getMimeTypes());
                    mimeTypeList.addAll(nashornFactory.getMimeTypes());
                    extensionList = new ArrayList<>(extensionList);
                    extensionList.removeAll(nashornFactory.getExtensions());
                    extensionList.addAll(nashornFactory.getExtensions());
                }
                clearEngineFactory(nashornFactory);
            }
        }

        names = Collections.unmodifiableList(nameList);
        mimeTypes = Collections.unmodifiableList(mimeTypeList);
        extensions = Collections.unmodifiableList(extensionList);
    }

    private WeakReference<Engine> defaultEngine;
    private final Engine userDefinedEngine;

    public GraalJSEngineFactory() {
        log.trace("ctor() invoked");
        this.defaultEngine = null; // lazy
        this.userDefinedEngine = null;
    }

    GraalJSEngineFactory(Engine engine) {
        log.trace("ctor(Engine) invoked");
        this.userDefinedEngine = engine;
    }

    private static Engine createDefaultEngine() {
        return Engine.newBuilder()
            .allowExperimentalOptions(true)
            //.allowAllAccess(true) // no such method
            //.option("polyglot.python.allowHostAccess", "true") // option not found
            .option("python.EmulateJython", "true") // Jython-compatible class structure
            .build();
    }

    /**
     * Returns the underlying polyglot engine.
     * @return the underlying polyglot engine.
     */
    public Engine getPolyglotEngine() {
        log.trace("getPolyglotEngine() invoked");
        if (userDefinedEngine != null) {
            return userDefinedEngine;
        } else {
            Engine engine = defaultEngine == null ? null : defaultEngine.get();
            if (engine == null) {
                engine = createDefaultEngine();
                defaultEngine = new WeakReference<>(engine);
            }
            return engine;
        }
    }

    @Override
    public String getEngineName() {
        log.trace("getEngineName() invoked, returns {}", ENGINE_NAME);
        return ENGINE_NAME;
    }

    @Override
    public String getEngineVersion() {
        try {
            log.trace("getEngineVersion() invoked");
            return getPolyglotEngine().getVersion();
        } catch (NoClassDefFoundError e) {
            log.debug("returning null due to no class found");
            return null;
        }
    }

    @Override
    public List<String> getExtensions() {
        log.trace("getExtensions() invoked");
        return extensions;
    }

    @Override
    public List<String> getMimeTypes() {
        log.trace("getMimeTypes() invoked");
        return mimeTypes;
    }

    @Override
    public List<String> getNames() {
        log.trace("getNames() invoked");
        return names;
    }

    @Override
    public String getLanguageName() {
        log.trace("getLanguageName() invoked");
        return LANGUAGE;
    }

    @Override
    public String getLanguageVersion() {
        log.trace("getLanguageVersion() invoked");
        return LANGUAGE_VERSION;
    }

    @Override
    public Object getParameter(String key) {
        log.trace("getParameter('{}') invoked", key);
        switch (key) {
            case ScriptEngine.NAME:
                return NAME;
            case ScriptEngine.ENGINE:
                return getEngineName();
            case ScriptEngine.ENGINE_VERSION:
                return getEngineVersion();
            case ScriptEngine.LANGUAGE:
                return getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION:
                return getLanguageVersion();
            default:
                return null;
        }
    }

    @Override
    public GraalJSScriptEngine getScriptEngine() {
        log.trace("getEngineName() invoked");
        return new GraalJSScriptEngine(this);
    }

    @Override
    public String getMethodCallSyntax(final String obj, final String method, final String... args) {
        log.trace("getEngineName() invoked");
        Objects.requireNonNull(obj);
        Objects.requireNonNull(method);
        final StringBuilder sb = new StringBuilder().append(obj).append('.').append(method).append('(');
        final int len = args.length;

        if (len > 0) {
            Objects.requireNonNull(args[0]);
            sb.append(args[0]);
        }
        for (int i = 1; i < len; i++) {
            Objects.requireNonNull(args[i]);
            sb.append(',').append(args[i]);
        }
        sb.append(')');

        return sb.toString();
    }

    @Override
    public String getOutputStatement(final String toDisplay) {
        log.trace("getEngineName() invoked");
        return "print(" + toDisplay + ")";
    }

    @Override
    public String getProgram(final String... statements) {
        log.trace("getEngineName() invoked");
        final StringBuilder sb = new StringBuilder();

        for (final String statement : statements) {
            Objects.requireNonNull(statement);
            sb.append(statement).append(';');
        }

        return sb.toString();
    }

    private static ScriptEngineFactory getNashornEngineFactory() {
        for (ScriptEngineFactory factory : new ScriptEngineManager().getEngineFactories()) {
            if (NASHORN_ENGINE_NAME.equals(factory.getEngineName())) {
                return factory;
            }
        }
        return null;
    }

    private static void clearEngineFactory(ScriptEngineFactory factory) {
        assert factory != null;

        try {
            Class<?> clazz = factory.getClass();
            for (String immutableListFieldName : new String[]{"names", "mimeTypes", "extensions"}) {
                Field immutableListField = clazz.getDeclaredField(immutableListFieldName);
                immutableListField.setAccessible(true);
                Object immutableList = immutableListField.get(null);

                Class<?> unmodifiableListClazz = Class.forName("java.util.Collections$UnmodifiableList");
                Field unmodifiableListField = unmodifiableListClazz.getDeclaredField("list");
                unmodifiableListField.setAccessible(true);

                Class<?> unmodifiableCollectionClazz = Class.forName("java.util.Collections$UnmodifiableCollection");
                Field unmodifiableCField = unmodifiableCollectionClazz.getDeclaredField("c");
                unmodifiableCField.setAccessible(true);

                List<?> list = (List<?>) unmodifiableListField.get(immutableList);
                List<Object> filteredList = new ArrayList<>();

                for (Object item : list) {
                    if (!RegisterAsNashornScriptEngineFactory && item.toString().toLowerCase().equals("nashorn")) {
                        filteredList.add(item);
                    }
                }

                unmodifiableListField.set(immutableList, filteredList);
                unmodifiableCField.set(immutableList, filteredList);
            }
        } catch (NullPointerException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            System.err.println("Failed to clear engine names [" + factory.getEngineName() + "]");
            e.printStackTrace();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GraalJSEngineFactory.class);
}
