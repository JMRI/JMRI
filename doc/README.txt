JMRI Documentation Sources
--------------------------

The JMRI documentation is (or will eventually be) written using
[DocBook](http://docbook.org) 5.0 to provide a single semantic markup that can
be published in HTML, JavaHelp, PDF, and other formats using standard ant tasks.

# Structure

The documentation sources are organized using the following structure:

* `README.txt` - this file
* `build.xml` - ant build configuration
* `docbook-xsl-VERSION` - DocBook XML stylesheets for transforming docbook into
  other formats. This is here instead of in `svn-root/xml/XSLT` since these are
  build-time only artifacts.

# DocBook

DocBook is an XML schema, so it can be edited directly within most Java IDEs or
any other XML editor.

JMRI ships with the DocBook schema definitions since JMRI uses certain DocBook
elements internally.