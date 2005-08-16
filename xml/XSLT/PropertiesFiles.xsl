<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: PropertiesFiles.xsl,v 1.1 2005-08-16 06:14:31 jacobsen Exp $ -->

<!-- Stylesheet to convert a JMRI decoder definition to a properties file -->

<!-- This made from the readme2html.xsl file of TestXSLT 2.7 -->

<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Need to instruct the XSLT processor to use text output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="text" encoding="ISO-8859-1" 
        indent="no"
        omit-xml-declaration="yes"
        standalone="no" />

<!-- avoid producing lots of blank lines -->
<xsl:strip-space elements="*" />

<!-- This first template matches our root element in the input file.
     This will trigger the generation of the first property line
     Then we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
-->     
<xsl:template match='decoder-config'>

<xsl:text>familyname=</xsl:text><xsl:value-of select="decoder/family/@name"/><xsl:text>
</xsl:text>  <!-- thats a newline with no whitespace -->
<xsl:text>manufacturer=</xsl:text><xsl:value-of select="decoder/family/@mfg"/><xsl:text>
</xsl:text>

<xsl:if test="not( string-length(decoder/family/@lowVersionID)=0 )" >
<xsl:text>lowVersionID=</xsl:text><xsl:value-of select="decoder/family/@lowVersionID"/><xsl:text>
</xsl:text></xsl:if>
<xsl:if test="not( string-length(decoder/family/@highVersionID)=0 )" >
<xsl:text>highVersionID=</xsl:text><xsl:value-of select="decoder/family/@highVersionID"/><xsl:text>
</xsl:text></xsl:if>
<xsl:text>definitionSource=JMRI
</xsl:text>
<xsl:apply-templates/>  <!-- descend into definition -->
</xsl:template>

<!-- Match a variable definition. Produce a line listing the CV
     number and name
-->     
<xsl:template match='variable'>

<xsl:text>CV=</xsl:text><xsl:value-of select="@CV"/><xsl:text>,"</xsl:text><xsl:value-of select="@label"/><xsl:apply-templates/><xsl:text>"
</xsl:text>  <!-- thats a newline with no whitespace -->
<!-- note we descended into nested definitions before terminating the line-->
</xsl:template>

<!-- Match an enum definition within an variable definition.  If matched, produce
     a name/value pair.
-->     
<xsl:template match='enumChoice'>

<xsl:text>,"</xsl:text><xsl:value-of select="@choice"/><xsl:text>","</xsl:text><xsl:value-of select="@value"/><xsl:apply-templates/><xsl:text>"</xsl:text>
<!-- note we descended into nested definitions before terminating the line-->
</xsl:template>


</xsl:stylesheet>
