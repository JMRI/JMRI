<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE xslt [
<!ENTITY target "it">
]>

<!-- Process a JMRI decoder file, adding a choice element (with specific    -->
<!-- language) based on the existing choice attribute                         -->

<!-- Provide the two-character language code in the ENTITY line above.      -->

<!-- You should normalize the decoder file before using this tool.          -->

<!-- Note: Existing specific-language choice elements are not replaced.     -->

<!-- xsltproc xml/XSLT/decoderAddI18nChoice.xsl 0NMRA.xml | xmllint -format - | diff - 0NMRA.xml      -->

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

<!--specific template match for enumChoice element with specific-language choice element -->
    <xsl:template match="enumChoice[choice[@xml:lang = '&target;']]" priority="6">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
    </xsl:template>

<!--specific template match for enumChoice element with default-language choice element -->
    <xsl:template match="enumChoice[choice]" priority="5">
      <xsl:copy>
        <xsl:apply-templates select="@*|*[not(self::choice[@xml:lang = '&target;'])]" />
        <xsl:element name="choice">
          <xsl:attribute name="xml:lang">&target;</xsl:attribute>
          <xsl:value-of select="choice"/>
        </xsl:element>
      </xsl:copy>
    </xsl:template>

<!--specific template match for enumChoice element with no choice element but with choice attribute present-->
    <xsl:template match="enumChoice[@choice]" priority="4">
      <xsl:copy>
        <xsl:apply-templates select="@*|*[not(self::choice[@xml:lang = '&target;'])]" />
        <xsl:element name="choice">
          <xsl:attribute name="xml:lang">&target;</xsl:attribute>
          <xsl:value-of select="@choice"/>
        </xsl:element>
      </xsl:copy>
    </xsl:template>

<!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
