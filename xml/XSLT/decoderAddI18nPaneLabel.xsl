<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE xslt [
<!ENTITY target "it">
]>

<!-- Process a JMRI pane definition file, adding a text element (with specific -->
<!-- language) based on the default text element            -->

<!-- Provide the two-character language code in the ENTITY line above.      -->

<!-- You should normalize the decoder file before using this tool.          -->

<!-- Note: Existing specific-language label elements are not replaced.      -->

<!-- xsltproc xml/XSLT/decoderAddI18nPaneLabel.xsl 0NMRA.xml | xmllint -format - | diff - 0NMRA.xml      -->

<!-- This file is part of JMRI.  Copyright 2009-2014.                       -->
<!--                                                                        -->
<!-- JMRI is free software; you can redistribute it and/or modify it under  -->
<!-- the terms of version 2 of the GNU General Public License as published  -->
<!-- by the Free Software Foundation. See the "COPYING" file for a copy     -->
<!-- of this license.                                                       -->
<!--                                                                        -->
<!-- JMRI is distributed in the hope that it will be useful, but WITHOUT    -->
<!-- ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or  -->
<!-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License  -->
<!-- for more details.                                                      -->
 
<xsl:stylesheet	version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    >

<xsl:output method="xml" encoding="utf-8"/>

<!--specific template match for lanel element with specific-language text element -->
    <xsl:template match="label[text[@xml:lang = '&target;']]" priority="5">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
    </xsl:template>

<!--specific template match for label element with text element with no language attribute present-->
    <xsl:template match="label[text[not(@xml:lang)]]" priority="4">
      <xsl:copy>
        <xsl:element name="text">
            <xsl:value-of select="./text"/>
        </xsl:element>
        <xsl:if test="not(translate(text[not(@xml:lang)], ' ', '') = '')"><!-- don't translate is empty -->
          <xsl:element name="text">
            <xsl:attribute name="xml:lang">&target;</xsl:attribute>
            <xsl:value-of select="./text"/>
          </xsl:element>
        </xsl:if>
      </xsl:copy>
    </xsl:template>

<!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
