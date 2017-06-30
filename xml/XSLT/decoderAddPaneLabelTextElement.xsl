<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Process a JMRI decoder file, adding a text element (with default       -->
<!-- language) based on the label attribute in a pane element               -->

<!-- Note: An existing default-language label element is not replaced.      -->

<!-- xsltproc decoderAddPaneLabelTextElement.xsl 0NMRA.xml | diff - 0NMRA.xml      -->

<!-- The sequence of operations to normalize a file is                      -->
<!--  decoderPaneLabelToText.xsl                                            -->
<!--  decoderAddPaneLabelTextElement.xsl                                    -->
<!--  decoderAddI18nPaneLabel.xsl             (if translating)              -->
<!--  decoderPaneSuppressLabelAttribute.xsl                                 -->
<!--  xmllint -format                                                       -->


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

<!--specific template match for variable element with default-language label element -->
    <xsl:template match="label[text[not(@xml:lang)]]" priority="5">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
    </xsl:template>

<!--specific template match for variable element with no label element but with label attribute present-->
    <xsl:template match="label[@label]" priority="4">
      <xsl:copy>
        <xsl:apply-templates select="@*" /><!-- copy attributes before adding elements-->
        <xsl:element name="text"><xsl:value-of select="@label"/></xsl:element>
        <xsl:apply-templates select="*[not(self::label[not(@xml:lang)])]" />
      </xsl:copy>
    </xsl:template>

<!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
