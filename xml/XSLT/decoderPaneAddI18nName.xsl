<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE xslt [
<!ENTITY target "it">
]>

<!-- Process a JMRI decoder or programmer file, adding a name element (with specific     -->
<!-- language) based on the name attribute             -->

<!-- Provide the two-character language code in the ENTITY line above.      -->

<!-- You should normalize the decoder file before using this tool.          -->

<!-- Note: Existing specific-language name elements are not replaced.      -->

<!-- xsltproc xml/XSLT/decoderAddI18nName.xsl 0NMRA.xml | xmllint -format - | diff - 0NMRA.xml      -->

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

<!--specific template match for pane element with specific-language name element -->
    <xsl:template match="pane[name[@xml:lang = '&target;']]" priority="5">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
    </xsl:template>

<!--specific template match for pane element with default language name element -->
    <xsl:template match="pane[name]" priority="4">
      <xsl:copy>
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="name" />
        <xsl:element name="name">
          <xsl:attribute name="xml:lang">&target;</xsl:attribute>
          <xsl:value-of select="name"/>
        </xsl:element>
        <xsl:apply-templates select="*[not(self::name)]" />
      </xsl:copy>
    </xsl:template>

<!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
