Unit and Functional Test Scripts for JMRI Releases

See jmri_bindings_test.py  for sample test, including error reporting

These are run by java/test/jmri/jmrit/jython/SampleScriptTest.java

Setting the jmri.skipjythontests property to "true" will omit defining and running these tests

Notes: 

- These run in a single (e.g. hot) interpreter.  As we add tests, we may start seeing interference, and will have to deal with it.  But for now, this is a basic, quick approach.

- When errors do happen, the reporting is ugly.  This can be improved (of course, we don't want errors in the first place)


