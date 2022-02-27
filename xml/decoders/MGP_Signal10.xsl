<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet	version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    >
<xsl:output method="xml" encoding="utf-8"/>

<!-- Copyright (C) JMRI 2002, 2005, 2007 All rights reserved -->
<!-- $Id:$ -->
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

<!--for MGP Signal 10 -->
<!-- * * * * *  Variables * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Short Signals  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<xsl:template name="ShortSignalVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Short Signal {$index} Rule" CV="{$CV1}" mask="XXXXXVVV" default="0">
        <enumVal>
            <enumChoice choice="Not used" value="0">
                <choice>Not used</choice>
                <choice xml:lang="cs">Nepoužito</choice>
            </enumChoice>
            <enumChoice choice="Use extra GO rule 1" value="1">
                <choice>Use extra GO rule 1</choice>
                <choice xml:lang="cs">Použít další pravidlo pro volnoznak 1</choice>
            </enumChoice>
            <enumChoice choice="Use extra GO rule 2" value="2">
                <choice>Use extra GO rule 2</choice>
                <choice xml:lang="cs">Použít další pravidlo pro volnoznak 2</choice>
            </enumChoice>
            <enumChoice choice="Use extra GO rule 3" value="3">
                <choice>Use extra GO rule 3</choice>
                <choice xml:lang="cs">Použít další pravidlo pro volnoznak 3</choice>
            </enumChoice>
            <enumChoice choice="Use extra GO rule 4" value="4">
                <choice>Use extra GO rule 4</choice>
                <choice xml:lang="cs">Použít další pravidlo pro volnoznak 4</choice>
            </enumChoice>
            <enumChoice choice="Use extra GO rule 5" value="5">
                <choice>Use extra GO rule 5</choice>
                <choice xml:lang="cs">Použít další pravidlo pro volnoznak 5</choice>
            </enumChoice>
        </enumVal>
        <label>Rule</label>
        <label xml:lang="cs">Pravidlo</label>
    </variable>
</xsl:template>

<xsl:template name="AllShortSignalVars">
  <xsl:param name="CV1" select="390"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="10 >= $index">
    <xsl:call-template name="ShortSignalVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="AllShortSignalVars">
      <xsl:with-param name="CV1" select="$CV1 +1"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** ShortSignal Pane  ********************************************************************** -->
<xsl:template name="ShortSignalInPane">
  <xsl:param name="index"/>
  
		<label><text>&#160;</text></label>
		<label>
                    <text>Signal <xsl:value-of select="$index"/></text>
                    <text xml:lang="cs">Návěstidlo <xsl:value-of select="$index"/></text>
                </label>
       		  <display item="Short Signal {$index} Rule"/>
</xsl:template>


<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='ShortSignalPane']">
	<pane>
	<name>Rules Short</name>
	<name xml:lang="cs">Pravidla pro nedostatečnou zábrzdnou vzdálenost</name>
    <column>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="1"/>
		</xsl:call-template>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="2"/>
		</xsl:call-template>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="3"/>
		</xsl:call-template>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="4"/>
		</xsl:call-template>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="5"/>
		</xsl:call-template>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="6"/>
		</xsl:call-template>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="7"/>
		</xsl:call-template>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="8"/>
		</xsl:call-template>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="9"/>
		</xsl:call-template>
		<xsl:call-template name="ShortSignalInPane">
		  <xsl:with-param name="index" select="10"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Repeat Signals  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<xsl:template name="RepeatSignalVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Repeat Signal {$index} Type" CV="{$CV1}" mask="XXXXXVVV" default="0">
        <enumVal>
          <enumChoice choice="Not used" value="0">
            <choice>Not used</choice>
            <choice xml:lang="cs">Nepoužito</choice>
          </enumChoice>
          <enumChoice choice="2 light" value="1">
            <choice>2 light</choice>
            <choice xml:lang="cs">2 světla</choice>
          </enumChoice>
        </enumVal>
        <label>Type</label>
        <label xml:lang="cs">Typ</label>
    </variable>
    <variable item="Repeat Signal {$index} Intensity bank number" CV="{$CV1}" mask="XXVVXXXX" default="0">
        <enumVal>
          <enumChoice choice="1" value="0"/>
          <enumChoice choice="2" value="1"/>
          <enumChoice choice="3" value="2"/>
        </enumVal>
        <label>Intensity bank number</label>
        <label xml:lang="cs">Intenzita banka číslo</label>
    </variable>
    <variable item="Repeat Signal {$index} First LED number" CV="{$CV1 +1}" default="0">
        <decVal max="64"/>
        <label>First LED number</label>
        <label xml:lang="cs">Číslo první LED</label>
    </variable>
	<variable item="Repeat Signal {$index} Main signal address" CV="{$CV1 +2}" default="0">
        <splitVal highCV="{$CV1 +3}" upperMask="XXXXXVVV"/>
        <label>Main signal address</label>
        <label xml:lang="cs">Adresa hlavního návěstidla</label>
    </variable>
</xsl:template>

<xsl:template name="AllRepeatSignalVars">
  <xsl:param name="CV1" select="500"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="4 >= $index">
    <xsl:call-template name="RepeatSignalVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="AllRepeatSignalVars">
      <xsl:with-param name="CV1" select="$CV1 +4"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** RepeatSignal Pane  ********************************************************************** -->
<xsl:template name="RepeatSignalInPane">
  <xsl:param name="index"/>
  
		<label><text>&#160;</text></label>
		<label>
                    <text>Repeat Signal <xsl:value-of select="$index"/></text>
                    <text xml:lang="cs">Opakovací návěstidlo <xsl:value-of select="$index"/></text>
                </label>
        	<display item="Repeat Signal {$index} Type"/>
		<display item="Repeat Signal {$index} First LED number"/>
		<display item="Repeat Signal {$index} Intensity bank number"/>
		<display item="Repeat Signal {$index} Main signal address"/>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='RepeatSignalPane']">
	<pane>
	<name>Repeat Signals</name>
	<name xml:lang="cs">Opakovací návěstidla</name>
    <column>
		<xsl:call-template name="RepeatSignalInPane">
		  <xsl:with-param name="index" select="1"/>
		</xsl:call-template>
		<xsl:call-template name="RepeatSignalInPane">
		  <xsl:with-param name="index" select="2"/>
		</xsl:call-template>
		<xsl:call-template name="RepeatSignalInPane">
		  <xsl:with-param name="index" select="3"/>
		</xsl:call-template>
		<xsl:call-template name="RepeatSignalInPane">
		  <xsl:with-param name="index" select="4"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>


<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Direction Detection  * * * * * * *  * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<xsl:template name="DirectionDetectionVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Direction Detection {$index} First Address" CV="{$CV1}" default="0">
        <splitVal highCV="{$CV1 +1}" upperMask="XXXXXVVV"/>
        <label>First Address</label>
        <label xml:lang="cs">První adresa</label>
    </variable>
    <variable item="Direction Detection {$index} Delay Free Status" CV="{$CV1 +2}" default="0">
        <decVal max="255"/>
        <label>Delay Free Status</label>
        <label xml:lang="cs">Zpoždění stavu neobsazeno</label>
    </variable>
    <variable item="Direction Detection {$index} Address A" CV="{$CV1 + 3}" default="0">
        <splitVal highCV="{$CV1 +4}" upperMask="XXXXXVVV"/>
        <label>Address A</label>
        <label xml:lang="cs">Adresa A</label>
    </variable>
    <variable item="Direction Detection {$index} Address B" CV="{$CV1 + 5}" default="0">
        <splitVal highCV="{$CV1 +6}" upperMask="XXXXXVVV"/>
        <label>Address B</label>
        <label xml:lang="cs">Adresa B</label>
    </variable>
</xsl:template>

<xsl:template name="AllDirectionDetectionVars">
  <xsl:param name="CV1" select="550"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="4 >= $index">
    <xsl:call-template name="DirectionDetectionVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="AllDirectionDetectionVars">
      <xsl:with-param name="CV1" select="$CV1 +7"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** DirectionDetection Pane  ********************************************************************** -->
<xsl:template name="DirectionDetectionInPane">
  <xsl:param name="index"/>
  
		<label><text>&#160;</text></label>
		<label>
                    <text>Direction Detection <xsl:value-of select="$index"/></text>
                    <text xml:lang="cs">Detekce směru <xsl:value-of select="$index"/></text>
                </label>
                <display item="Direction Detection {$index} First Address"/>
		<display item="Direction Detection {$index} Delay Free Status"/>
		<display item="Direction Detection {$index} Address A"/>
		<display item="Direction Detection {$index} Address B"/>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='DirectionDetectionPane']">
	<pane>
	<name>Direction Detection</name>
	<name xml:lang="cs">Detekce směru</name>
    <column>
		<xsl:call-template name="DirectionDetectionInPane">
		  <xsl:with-param name="index" select="1"/>
		</xsl:call-template>
		<xsl:call-template name="DirectionDetectionInPane">
		  <xsl:with-param name="index" select="2"/>
		</xsl:call-template>
		<xsl:call-template name="DirectionDetectionInPane">
		  <xsl:with-param name="index" select="3"/>
		</xsl:call-template>
		<xsl:call-template name="DirectionDetectionInPane">
		  <xsl:with-param name="index" select="4"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>


<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Trigger rule statements   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
 <xsl:template name="TriggerRuleStmnt">
    <xsl:param name="CV1"/>
    <xsl:param name="ruleIndex"/>
    <xsl:param name="stmntIndex"/>

    <variable item="Rule {$ruleIndex} statement {$stmntIndex} Logic" CV="{$CV1}" mask="XXXXXXXV" default="0">
        <enumVal>
          <enumChoice choice="OR" value="0">
            <choice>OR</choice>
            <choice xml:lang="cs">NEBO</choice>
          </enumChoice>
          <enumChoice choice="AND" value="1">
            <choice>AND</choice>
            <choice xml:lang="cs">A ZÁROVEŇ</choice>
          </enumChoice>
        </enumVal>
    </variable>

    <variable item="Rule {$ruleIndex} statement {$stmntIndex} Status" CV="{$CV1}" mask="XXXXXXVX" default="0">
        <enumVal>
          <enumChoice choice="0/Thrown/Free" value="0">
              <choice>0/Thrown/Free</choice>
              <choice xml:lang="cs">0 / Do odbočky / Neobsazeno</choice>
          </enumChoice>
          <enumChoice choice="1/Closed/Occupied" value="1">
              <choice>1/Closed/Occupied</choice>
              <choice xml:lang="cs">1 / Přímo / Obsazeno</choice>
          </enumChoice>
        </enumVal>
    </variable>
    <variable item="Rule {$ruleIndex} statement {$stmntIndex} Type" CV="{$CV1}" mask="XXXVVVXX" default="0">
        <enumVal>
          <enumChoice choice="Not used" value="0">
              <choice>Not used</choice>
              <choice xml:lang="cs">Nepoužito</choice>
          </enumChoice>
          <enumChoice choice="Sw status" value="1">
              <choice>Sw status</choice>
              <choice xml:lang="cs">Stav výhybky</choice>
          </enumChoice>
          <enumChoice choice="Occ sensor" value="2">
              <choice>Occ sensor</choice>
              <choice xml:lang="cs">Detekce obsazení</choice>
          </enumChoice>
          <enumChoice choice="SE" value="3">
              <choice>SE</choice>
              <choice xml:lang="cs">Změna návěsti</choice>
          </enumChoice>
        </enumVal>
    </variable>
	<variable item="Rule {$ruleIndex} statement {$stmntIndex} Address" CV="{$CV1 +1}" default="0">
        <splitVal highCV="{$CV1 +2}" upperMask="XXXXXVVV"/>
    </variable>
 </xsl:template>
 
 <xsl:template name="TriggerRuleVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Rule {$index} Signal Number" CV="{$CV1}" mask="XXXVVVVX" default="0">
	    <qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>2</value></qualifier>
        <enumVal>
			<enumChoice choice="Not used" value='0'>
                            <choice>Not used</choice>
                            <choice xml:lang="cs">Nepoužito</choice>
                        </enumChoice>
			<enumChoice choice="Signal 1" value='1'>
                            <choice>Signal 1</choice>
                            <choice xml:lang="cs">Návěstidlo 1</choice>
                        </enumChoice>
			<enumChoice choice="Signal 2" value='2'>
                            <choice>Signal 2</choice>
                            <choice xml:lang="cs">Návěstidlo 2</choice>
                        </enumChoice>
			<enumChoice choice="Signal 3" value='3'>
                            <choice>Signal 3</choice>
                            <choice xml:lang="cs">Návěstidlo 3</choice>
                        </enumChoice>
			<enumChoice choice="Signal 4" value='4'>
                            <choice>Signal 4</choice>
                            <choice xml:lang="cs">Návěstidlo 4</choice>
                        </enumChoice>
			<enumChoice choice="Signal 5" value='5'>
                            <choice>Signal 5</choice>
                            <choice xml:lang="cs">Návěstidlo 5</choice>
                        </enumChoice>
			<enumChoice choice="Signal 6" value='6'>
                            <choice>Signal 6</choice>
                            <choice xml:lang="cs">Návěstidlo 6</choice>
                        </enumChoice>
			<enumChoice choice="Signal 7" value='7'>
                            <choice>Signal 7</choice>
                            <choice xml:lang="cs">Návěstidlo 7</choice>
                        </enumChoice>
			<enumChoice choice="Signal 8" value='8'>
                            <choice>Signal 8</choice>
                            <choice xml:lang="cs">Návěstidlo 8</choice>
                        </enumChoice>
			<enumChoice choice="Signal 9" value='9'>
                            <choice>Signal 9</choice>
                            <choice xml:lang="cs">Návěstidlo 9</choice>
                        </enumChoice>
			<enumChoice choice="Signal 10" value='10'>
                            <choice>Signal 10</choice>
                            <choice xml:lang="cs">Návěstidlo 10</choice>
                        </enumChoice>
        </enumVal>
    </variable>

    <variable item="Rule {$index}, Controlled status" CV="{$CV1}" mask="XXXVXXXXX" default="0">
	    <qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>2</value></qualifier>
        <enumVal>
          <enumChoice choice="Signal Stop" value="0">
            <choice>Signal Stop</choice>
            <choice xml:lang="cs">Návěst Stůj</choice>
          </enumChoice>
          <enumChoice choice="Signal Go" value="1">
            <choice>Signal Go</choice>
            <choice xml:lang="cs">Návěst Volnoznak</choice>
          </enumChoice>
        </enumVal>
    </variable>

	<xsl:call-template name="TriggerRuleStmnt">
	  <xsl:with-param name="CV1" select="$CV1 +1"/>
	  <xsl:with-param name="ruleIndex" select="$index"/>
	  <xsl:with-param name="stmntIndex" select="1"/>
	</xsl:call-template>
	<xsl:call-template name="TriggerRuleStmnt">
	  <xsl:with-param name="CV1" select="$CV1 +4"/>
	  <xsl:with-param name="ruleIndex" select="$index"/>
	  <xsl:with-param name="stmntIndex" select="2"/>
	</xsl:call-template>
	<xsl:call-template name="TriggerRuleStmnt">
	  <xsl:with-param name="CV1" select="$CV1 +7"/>
	  <xsl:with-param name="ruleIndex" select="$index"/>
	  <xsl:with-param name="stmntIndex" select="3"/>
	</xsl:call-template>
</xsl:template>

<xsl:template name="AllTriggerRuleVars">
	<xsl:param name="CV1" select="600"/>
	<xsl:param name="index" select="1"/>

	<xsl:if test="11 > $index">

		<xsl:call-template name="TriggerRuleVar">
			<xsl:with-param name="CV1" select="$CV1"/>
			<xsl:with-param name="index" select="$index"/>
		</xsl:call-template>

		<xsl:call-template name="AllTriggerRuleVars">
			<xsl:with-param name="CV1" select="$CV1 +10"/>
			<xsl:with-param name="index" select="$index+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<xsl:template name="TriggerRuleInPane">
	<xsl:param name="index"/>

	<label><text>&#160;</text></label>
	<label>
            <text>Rule <xsl:value-of select="$index"/></text>
            <text xml:lang="cs">Pravidlo <xsl:value-of select="$index"/></text>
        </label>
	<display item="Rule {$index} Signal Number">
		<label>Signal Number</label>
		<label xml:lang="cs">Návěstidlo číslo</label>
	</display>
	<display item="Rule {$index}, Controlled status">
		<label>Controlled status</label>
                <label xml:lang="cs">Řízený stav</label>
	</display>

	<label><text>&#160;</text></label>
	<display item="Rule {$index} statement 1 Logic">
            <label>Stmnt 1 Logic</label>
            <label xml:lang="cs">Výrok 1 Logika</label>
	</display>
	<display item="Rule {$index} statement 1 Status">
            <label>Stmnt 1 Status</label>
            <label xml:lang="cs">Výrok 1 Stav</label>
	</display>
	<display item="Rule {$index} statement 1 Type">
            <label>Stmnt 1 Type</label>
            <label xml:lang="cs">Výrok 1 Typ</label>
	</display>
	<display item="Rule {$index} statement 1 Address">
            <label>Stmnt 1 Address</label>
            <label xml:lang="cs">Výrok 1 Adresa</label>
	</display>

	<label><text>&#160;</text></label>
	<display item="Rule {$index} statement 2 Logic">
		<label>Stmnt 2 Logic</label>
            <label xml:lang="cs">Výrok 2 Logika</label>
	</display>
	<display item="Rule {$index} statement 2 Status">
		<label>Stmnt 2 Status</label>
            <label xml:lang="cs">Výrok 2 Stav</label>
	</display>
	<display item="Rule {$index} statement 2 Type">
		<label>Stmnt 2 Type</label>
            <label xml:lang="cs">Výrok 2 Typ</label>
	</display>
	<display item="Rule {$index} statement 2 Address">
		<label>Stmnt 2 Address</label>
            <label xml:lang="cs">Výrok 2 Adresa</label>
	</display>

	<label><text>&#160;</text></label>
	<display item="Rule {$index} statement 3 Logic">
		<label>Stmnt 3 Logic</label>
            <label xml:lang="cs">Výrok 3 Logika</label>
	</display>
	<display item="Rule {$index} statement 3 Status">
		<label>Stmnt 3 Status</label>
            <label xml:lang="cs">Výrok 3 Stav</label>
	</display>
	<display item="Rule {$index} statement 3 Type">
		<label>Stmnt 3 Type</label>
            <label xml:lang="cs">Výrok 3 Typ</label>
	</display>
	<display item="Rule {$index} statement 3 Address">
		<label>Stmnt 3 Address</label>
            <label xml:lang="cs">Výrok 3 Adresa</label>
	</display>

</xsl:template>

<!-- - - - MATCH - - - -->

<xsl:template match="pane[name='TriggerRulePane']">
	<pane>
	<name>Trigger Rules</name>
	<name xml:lang="cs">Pravidla spouštění</name>
 
	<row>
		 <column>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="1"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="2"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="3"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="4"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="5"/>
			</xsl:call-template>
		</column>
	</row>
	<row>
		 <column>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="6"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="7"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="8"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="9"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="10"/>
			</xsl:call-template>
		</column>
	</row>
   </pane>
</xsl:template>

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Extra rule statements   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
 <xsl:template name="ExtraRuleVar">
    <xsl:param name="CV1"/>
    <xsl:param name="ruleIndex"/>
    <xsl:param name="goIndex"/>

    <variable item="Extra Rule {$ruleIndex} GO {$goIndex}, Logic" CV="{$CV1}" mask="XXXXXXXV" default="0">
        <enumVal>
          <enumChoice choice="OR" value="0">
            <choice>OR</choice>
            <choice xml:lang="cs">NEBO</choice>
          </enumChoice>
          <enumChoice choice="AND" value="1">
            <choice>AND</choice>
            <choice xml:lang="cs">A ZÁROVEŇ</choice>
          </enumChoice>
        </enumVal>
    </variable>

    <variable item="Extra Rule {$ruleIndex} GO {$goIndex}, Type" CV="{$CV1}" mask="VVVVXXXX" default="0">
        <enumVal>
          <enumChoice choice="Not used" value="0">
              <choice>Not used</choice>
              <choice xml:lang="cs">Nepoužito</choice>
          </enumChoice>
          <enumChoice choice="Sw status" value="1">
              <choice>Sw status</choice>
              <choice xml:lang="cs">Stav výhybky</choice>
          </enumChoice>
          <enumChoice choice="Occ sensor" value="2">
              <choice>Occ sensor</choice>
              <choice xml:lang="cs">Detekce obsazení</choice>
          </enumChoice>
          <enumChoice choice="SE" value="3">
              <choice>SE</choice>
              <choice xml:lang="cs">Změna návěsti</choice>
          </enumChoice>
          <enumChoice choice="xRule" value="5">
              <choice>xRule</choice>
              <choice xml:lang="cs">Další pravidlo</choice>
          </enumChoice>          
        </enumVal>
    </variable>

	<variable item="Extra Rule {$ruleIndex} GO {$goIndex}, Address" CV="{$CV1 +1}" default="0">
        <splitVal highCV="{$CV1 +2}" upperMask="XXXXVVVV"/>
    </variable>

    <variable item="Extra Rule {$ruleIndex} GO {$goIndex}, Status" CV="{$CV1 +2}" mask="XVXXXXXX" default="0">
        <enumVal>
         <enumChoice choice="Thrown/0" value="0">
              <choice>Thrown/0</choice>
              <choice xml:lang="cs">Do odbočky / 0</choice>
          </enumChoice>
          <enumChoice choice="Closed/1" value="1">
              <choice>Closed/1</choice>
              <choice xml:lang="cs">Přímo / 1</choice>
          </enumChoice>
        </enumVal>
    </variable>

</xsl:template>

<xsl:template name="AllGoExtraRuleVar">
	<xsl:param name="CV1"/>
	<xsl:param name="ruleIndex"/>
	<xsl:param name="goIndex" select="1"/>

	<xsl:if test="7 > $goIndex">

		<xsl:call-template name="ExtraRuleVar">
			<xsl:with-param name="CV1" select="$CV1"/>
			<xsl:with-param name="ruleIndex" select="$ruleIndex"/>
			<xsl:with-param name="goIndex" select="$goIndex"/>
		</xsl:call-template>
		<xsl:call-template name="AllGoExtraRuleVar">
			<xsl:with-param name="CV1" select="$CV1 +3"/>
			<xsl:with-param name="ruleIndex" select="$ruleIndex"/>
			<xsl:with-param name="goIndex" select="$goIndex+1"/>
		</xsl:call-template>
	</xsl:if>
</xsl:template>

<xsl:template name="AllExtraRuleVar">
	<xsl:param name="CV1" select="400"/>
	<xsl:param name="index" select="1"/>

	<xsl:if test="6 > $index">

		<xsl:call-template name="AllGoExtraRuleVar">
			<xsl:with-param name="CV1" select="$CV1"/>
			<xsl:with-param name="ruleIndex" select="$index"/>
			<xsl:with-param name="goIndex" select="1"/>
		</xsl:call-template>
		<xsl:call-template name="AllExtraRuleVar">
			<xsl:with-param name="CV1" select="$CV1 +18"/>
			<xsl:with-param name="index" select="$index +1"/>
		</xsl:call-template>
	</xsl:if>
</xsl:template>

<!-- ExtraRulesPane -->
<xsl:template name="ExtraRuleGoInPane">
	<xsl:param name="ruleIndex"/>
	<xsl:param name="goIndex"/>

	<xsl:if test="7 > $goIndex">

		<label>
                    <text>Rule <xsl:value-of select="$ruleIndex"/>, GO <xsl:value-of select="$goIndex"/></text>
                    <text xml:lang="cs">Pravidlo <xsl:value-of select="$ruleIndex"/>, Volnoznak <xsl:value-of select="$goIndex"/></text>
                </label>
		<display item="Extra Rule {$ruleIndex} GO {$goIndex}, Logic">
			<label>Logic</label>
			<label xml:lang="cs">Logika</label>
		</display>
		<display item="Extra Rule {$ruleIndex} GO {$goIndex}, Type">
			<label>Type</label>
			<label xml:lang="cs">Typ</label>
		</display>
		<display item="Extra Rule {$ruleIndex} GO {$goIndex}, Address">
			<label>Address</label>
			<label xml:lang="cs">Adresa</label>
		</display>
		<display item="Extra Rule {$ruleIndex} GO {$goIndex}, Status">
			<label>Status</label>
			<label xml:lang="cs">Stav</label>
		</display>
		<label><text>&#160;</text></label>

		<xsl:call-template name="ExtraRuleGoInPane">
			<xsl:with-param name="ruleIndex" select="$ruleIndex"/>
			<xsl:with-param name="goIndex" select="$goIndex +1"/>
		</xsl:call-template>
	</xsl:if>
	
</xsl:template>

<xsl:template name="AllExtraRuleInPane">
	<xsl:param name="index"/>

<!--	<xsl:if test="6 > $index"> -->
		<label><text>&#160;</text></label>
		<label>
                    <text>Rule <xsl:value-of select="$index"/></text>
                    <text xml:lang="cs">Pravidlo <xsl:value-of select="$index"/></text>
                </label>

		<xsl:call-template name="ExtraRuleGoInPane">
			<xsl:with-param name="ruleIndex" select="$index"/>
			<xsl:with-param name="goIndex" select="1"/>
		</xsl:call-template>
<!--		<xsl:call-template name="AllExtraRuleInPane">
			<xsl:with-param name="index" select="$index +1"/>
		</xsl:call-template>
		
	</xsl:if>
-->
</xsl:template>

<!-- - - - MATCH - - - -->

<xsl:template match="pane[name='ExtraRulesPane']">
	<pane>
	<name>Extra GO Rules</name>
	<name xml:lang="cs">Další pravidla pro volnoznak</name>
	<column>
				<xsl:call-template name="AllExtraRuleInPane">
				  <xsl:with-param name="index" select="1"/>
				</xsl:call-template>
	</column>
	<column>
				<xsl:call-template name="AllExtraRuleInPane">
				  <xsl:with-param name="index" select="2"/>
				</xsl:call-template>
	</column>
	<column>
				<xsl:call-template name="AllExtraRuleInPane">
				  <xsl:with-param name="index" select="3"/>
				</xsl:call-template>
	</column>
	<column>
				<xsl:call-template name="AllExtraRuleInPane">
				  <xsl:with-param name="index" select="4"/>
				</xsl:call-template>
	</column>
	<column>
				<xsl:call-template name="AllExtraRuleInPane">
				  <xsl:with-param name="index" select="5"/>
				</xsl:call-template>
	</column>
   </pane>
</xsl:template>

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Signal Selectors  * * * * * * *  * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<xsl:template name="SignalSelectorsVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Signal Selectors {$index} Switch Address" CV="{$CV1}" default="0">
        <splitVal highCV="{$CV1 +1}" upperMask="XXXXXVVV"/>
    </variable>
    <variable item="Signal Selectors {$index} Thrown Signal Address" CV="{$CV1 + 2}" default="0">
        <splitVal highCV="{$CV1 +3}" upperMask="XXXXXVVV"/>
    </variable>
    <variable item="Signal Selectors {$index} Closed Signal Address" CV="{$CV1 + 4}" default="0">
        <splitVal highCV="{$CV1 +5}" upperMask="XXXXXVVV"/>
    </variable>
</xsl:template>

<xsl:template name="AllSignalSelectorsVars">
  <xsl:param name="CV1" select="702"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="4 >= $index">
    <xsl:call-template name="SignalSelectorsVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="AllSignalSelectorsVars">
      <xsl:with-param name="CV1" select="$CV1 +6"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** Signal Selectors Pane  ********************************************************************** -->
<xsl:template name="SignalSelectorsInPane">
  <xsl:param name="index"/>
  
		<label><text>&#160;</text></label>
		<label>
                    <text>Signal Selectors <xsl:value-of select="$index"/></text>
                    <text xml:lang="cs">Volič návěstidel <xsl:value-of select="$index"/></text>
                </label>
                <display item="Signal Selectors {$index} Switch Address">
			<label>Switch Address</label>
			<label xml:lang="cs">Adresa výhybky</label>
		</display>
		<display item="Signal Selectors {$index} Thrown Signal Address">
			<label>Thrown Signal Address</label>
			<label xml:lang="cs">Adresa návěstidla Do odbočky</label>
		</display>
		<display item="Signal Selectors {$index} Closed Signal Address">
			<label>Closed Signal Address</label>
			<label xml:lang="cs">Adresa návěstidla Přímo</label>
		</display>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='SignalSelectorsPane']">
	<pane>
	<name>Signal Selectors</name>
	<name xml:lang="cs">Voliče návěstidel</name>
    <column>
		<display item="Start Address"/>
		<xsl:call-template name="SignalSelectorsInPane">
		  <xsl:with-param name="index" select="1"/>
		</xsl:call-template>
		<xsl:call-template name="SignalSelectorsInPane">
		  <xsl:with-param name="index" select="2"/>
		</xsl:call-template>
		<xsl:call-template name="SignalSelectorsInPane">
		  <xsl:with-param name="index" select="3"/>
		</xsl:call-template>
		<xsl:call-template name="SignalSelectorsInPane">
		  <xsl:with-param name="index" select="4"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>



<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Signals   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<xsl:template name="SignalVars">
	<xsl:param name="CV1" select="100"/>
	<xsl:param name="sigIndex" select="1"/>

	<variable item="SignalSE {$sigIndex} Type" CV="{$CV1}" mask="XXXVVVVV" default="0">
	  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>6</value></qualifier>
        <enumVal>
		    <enumChoice choice="Not used" value='0'/>
            <enumChoice choice="Hsi 2" value='1'/>
            <enumChoice choice="Hsi 3" value='3'/>
            <enumChoice choice="Hsi 4" value='4'/>
            <enumChoice choice="Hsi 5" value='5'/>
            <enumChoice choice="Hdvsi" value='16'/>                  <!-- note, this one is valid from decoder version 12 -->
            <enumChoice choice="Dvsi" value='7'/>
            <enumChoice choice="Fsi 2" value='8'/>
            <enumChoice choice="Fsi 3" value='9'/>
            <enumChoice choice="Repeat" value='10'/>
            <enumChoice choice="VSI mgp" value='17'/>                <!-- note, this one is valid from decoder version 13 -->
            <enumChoice choice="VSI 2" value='11'/>
            <enumChoice choice="VSI rgb" value='12'/>
            <enumChoice choice="VFSI" value='13'/>
            <enumChoice choice="VTSI" value='14'/>
            <enumChoice choice="Sl" value='15'/>                     <!-- note, this one is valid from decoder version 9 -->
            <enumChoice choice="TGOJ UT 3" value='20'/>
            <enumChoice choice="HdvM" value='6'/>
        </enumVal>
	</variable>
	<variable item="SignalDE {$sigIndex} Type" CV="{$CV1}" mask="XXXVVVVV" default="0">
	  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>7</value></qualifier>
        <enumVal>
            <enumChoice choice="Not used" value='0'/>
            <enumChoice choice="Hp Block 2" value='1'/>
            <enumChoice choice="Hp Einf 3" value='2'/>
            <enumChoice choice="Hp Ausf 6" value='3'/>
            <enumChoice choice="V 4" value='4'/>
            <enumChoice choice="Hp/V 10" value='5'/>
            <enumChoice choice="Hp Schutz alt" value='18'/>
            <enumChoice choice="Hp Schutz neu" value='17'/>
            <enumChoice choice="Ks Vor" value='10'/>                 <!-- note, this one is valid from decoder version 9 -->
            <enumChoice choice="Ks Haupt" value='11'/>               <!-- note, this one is valid from decoder version 9 -->
            <enumChoice choice="Ks Voll" value='12'/>                <!-- note, this one is valid from decoder version 9 -->
            <enumChoice choice="Ks 'Buchstabe'" value='13'/>         <!-- note, this one is valid from decoder version 9 -->
        </enumVal>
	</variable>	
	<variable item="SignalDK {$sigIndex} Type" CV="{$CV1}" mask="XXXVVVVV" default="0">
	  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>8</value></qualifier>
        <enumVal>
            <enumChoice choice="Not used" value='0'/>
			<enumChoice choice="I 2" value='2'/>
            <enumChoice choice="I 3" value='3'/>
            <enumChoice choice="I 4" value='4'/>
            <enumChoice choice="U 2" value='5'/>
            <enumChoice choice="U 3" value='6'/>
            <enumChoice choice="F 2" value='7'/>
            <enumChoice choice="F 3" value='8'/>
            <enumChoice choice="AM 3" value='11'/>
            <enumChoice choice="PU" value='14'/>
            <enumChoice choice="Dv" value='15'/>
            <enumChoice choice="Stop" value='10'/>                   <!-- note, this one is valid from decoder version 12 -->
        </enumVal>
	</variable>
	<variable item="SignalCZ {$sigIndex} Type" CV="{$CV1}" mask="XXXVVVVV" default="0">
	  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>11</value></qualifier>
        <enumVal>
            <enumChoice choice="Not used" value='0'>
                <choice>Not used</choice>
                <choice xml:lang="cs">Nepoužito</choice>
            </enumChoice>
            <enumChoice choice="Cz V2" value='2'>
                <choice>Cz V2</choice>
                <choice xml:lang="cs">V2 zč</choice>
            </enumChoice>
            <enumChoice choice="Cz V3" value='3'>
                <choice>Cz V3</choice>
                <choice xml:lang="cs">V3 žzč</choice>
            </enumChoice>
            <enumChoice choice="Cz V4" value='4'>
                <choice>Cz V4</choice>
                <choice xml:lang="cs">V4 žzčž</choice>
            </enumChoice>
            <enumChoice choice="Cz V5" value='5'>
                <choice>Cz V5</choice>
                <choice xml:lang="cs">V5 žzčbž</choice>
            </enumChoice>
            <enumChoice choice="Cz O2" value='12'>
                <choice>Cz O2</choice>
                <choice xml:lang="cs">O2 zč</choice>
            </enumChoice>
            <enumChoice choice="Cz O3" value='13'>
                <choice>Cz O3</choice>
                <choice xml:lang="cs">O3 zčž</choice>
            </enumChoice>
            <enumChoice choice="Cz O3GRW" value='16'>
                <choice>Cz O3GRW</choice>
                <choice xml:lang="cs">O3 zčb</choice>
            </enumChoice>
            <enumChoice choice="Cz O4" value='14'>
                <choice>Cz O4</choice>
                <choice xml:lang="cs">O4 zčbž</choice>
            </enumChoice>
            <enumChoice choice="Cz P" value='6'>
                <choice>Cz P</choice>
                <choice xml:lang="cs">P seřaďovací</choice>
            </enumChoice>
            <enumChoice choice="Cz Distant" value='10'>
                <choice>Cz Distant</choice>
                <choice xml:lang="cs">D předvěst</choice>
            </enumChoice>
            <enumChoice choice="Cz RoadCrossing" value='20'>        <!-- note, this one is valid from decoder version 11 -->
                <choice>Cz RoadCrossing</choice>
                <choice xml:lang="cs">Přejezd</choice>
            </enumChoice>
            <enumChoice choice="Cz StopLight" value='27'>           <!-- note, this one is valid from decoder version 11 -->
                <choice>Cz StopLight</choice>
                <choice xml:lang="cs">Indikátor</choice>
            </enumChoice>
        </enumVal>
            <label>Type</label>
            <label xml:lang="cs">Typ</label>
	</variable>
	<variable item="SignalUS {$sigIndex} Type" CV="{$CV1}" mask="XXXVVVVV" default="0">
	  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>12</value></qualifier>
        <enumVal>
            <enumChoice choice="Not used" value='0'/>
            <enumChoice choice="SigPic Us stop" value='1'/>
            <enumChoice choice="SigPic Us BN shGYR" value='2'/>
            <enumChoice choice="SigPic Us BN dhGYR" value='3'/>
            <enumChoice choice="SigPic Us BN shSL" value='4'/>
            <enumChoice choice="SigPic Us BN dhSL" value='5'/>
            <enumChoice choice="SigPic Us BN thGYR" value='6'/>
            <enumChoice choice="SigPic Us BN thSL" value='7'/>
        </enumVal>
	</variable>
	<variable item="SignalNO {$sigIndex} Type" CV="{$CV1}" mask="XXXVVVVV" default="0">
	  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>13</value></qualifier>
        <enumVal>
            <enumChoice choice="Not used" value='0'/>
            <enumChoice choice="No Hsi2Ytter" value='2'/>
            <enumChoice choice="No Hsi2Indre" value='3'/>
            <enumChoice choice="No Hsi3Ytter" value='4'/>
            <enumChoice choice="No Hsi3Indre" value='5'/>
            <enumChoice choice="No Hsi4Ytter" value="12" minVersion='11'/>           <!-- note, this one is valid from decoder version 11 -->
            <enumChoice choice="No Hsi4Indre" value="13" minVersion='11'/>           <!-- note, this one is valid from decoder version 11 -->
            <enumChoice choice="No Hsi5Ytter" value="10" minVersion='11'/>           <!-- note, this one is valid from decoder version 11 -->
            <enumChoice choice="No Hsi5Indre" value="11" minVersion='11'/>           <!-- note, this one is valid from decoder version 11 -->
            <enumChoice choice="No Fsi2" value='6'/>
            <enumChoice choice="No Dvsi" value='7'/>
            <enumChoice choice="No Rep1" value='8'/>
            <enumChoice choice="No Rep2" value='9'/>
            <enumChoice choice="No SL" value='14' minVersion='12'/>  <!-- note, this one is valid from decoder version 12 -->
        </enumVal>
	</variable>		
	<variable item="SignalNL {$sigIndex} Type" CV="{$CV1}" mask="XXXVVVVV" default="0">
	  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>14</value></qualifier>
        <enumVal>
            <enumChoice choice="Not used" value='0'/>
            <enumChoice choice="NL 55 3" value='2'/>
            <enumChoice choice="NL 55 3 w Display" value='3'/>
            <enumChoice choice="NL 55 4" value='4'/>
            <enumChoice choice="NL 55 Dwarf" value='5'/>
        </enumVal>
	</variable>		

	<!-- <SV offset="0" type="bits" start='7' length='1' name="Default start" rw="1" minVersion='2'> -->
	<variable item="Signal {$sigIndex} Default start" CV="{$CV1}" mask="VXXXXXXX" default="1">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>2</value></qualifier>
		<enumVal>
                    <enumChoice choice="Stop" value='0'>
                        <choice>Stop</choice>
                        <choice xml:lang="cs">Stůj</choice>
                    </enumChoice>
                    <enumChoice choice="Go" value='1'>
                        <choice>Go</choice>
                        <choice xml:lang="cs">Volnoznak</choice>
                    </enumChoice>
		</enumVal>
            <label>Default start</label>
            <label xml:lang="cs">Výchozí návěst po startu</label>
	</variable>

	<!-- <SV offset="1" type="int1" minValue='1'  maxValue='64' name="First LED number" rw="1"/> -->
	<variable item="Signal {$sigIndex} First LED number" CV="{$CV1+1}" default="0">
		<decVal min="1" max="64"/>
            <label>First LED number</label>
            <label xml:lang="cs">Číslo první LED</label>
	</variable>

	<!-- <SV offset="2" type="bits" start='0' length='1' name="Short way" rw="1" minVersion='3'> -->
	<variable item="Signal {$sigIndex} Short way" CV="{$CV1+2}" mask="XXXXXXXV" default="0">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>3</value></qualifier>
		<enumVal>
                    <enumChoice choice="No" value='0'>
                        <choice>No</choice>
                        <choice xml:lang="cs">Ne</choice>
                    </enumChoice>
                    <enumChoice choice="Yes" value='1'>
                        <choice>Yes</choice>
                        <choice xml:lang="cs">Ano</choice>
                    </enumChoice>
		</enumVal>
            <label>Short way</label>
            <label xml:lang="cs">Nedostatečná zábrzdná vzdálenost</label>
	</variable>
	
	<!-- <SV offset="2" type="bits" start='1' length='3' name="Direction Control" rw="1" minVersion='1'> -->
	<variable item="Signal {$sigIndex} Direction Control" CV="{$CV1+2}" mask="XXXXVVVX" default="0">
		<enumVal>
                    <enumChoice choice="Not used" value='0'>
                        <choice>Not used</choice>
                        <choice xml:lang="cs">Nepoužito</choice>
                    </enumChoice>
                    <enumChoice choice="1 - West to East" value='2'>
                        <choice>1 - West to East</choice>
                        <choice xml:lang="cs">1 – od Západu na Východ</choice>
                    </enumChoice>
                    <enumChoice choice="1 - East to West" value='3'>
                        <choice>1 - East to West</choice>
                        <choice xml:lang="cs">1 – od Východu na Západ</choice>
                    </enumChoice>
                    <enumChoice choice="2 - West to East" value='4'>
                        <choice>2 - West to East</choice>
                        <choice xml:lang="cs">2 – od Západu na Východ</choice>
                    </enumChoice>
                    <enumChoice choice="2 - East to West" value='5'>
                        <choice>2 - East to West</choice>
                        <choice xml:lang="cs">2 – od Východu na Západ</choice>
                    </enumChoice>
                    <enumChoice choice="3 - West to East" value='6'>
                        <choice>3 - West to East</choice>
                        <choice xml:lang="cs">3 – od Západu na Východ</choice>
                    </enumChoice>
                    <enumChoice choice="3 - East to West" value='7'>
                        <choice>3 - East to West</choice>
                        <choice xml:lang="cs">3 – od Východu na Západ</choice>
                    </enumChoice>
		</enumVal>
            <label>Direction Control</label>
            <label xml:lang="cs">Řízení směru</label>
	</variable>

	<!-- <SV offset="2" type="bits" start='4' length='2' name="Intensity bank number" rw="1"> -->
	<variable item="Signal {$sigIndex} Intensity bank number" CV="{$CV1+2}" mask="XXVVXXXX" default="0">
		<enumVal>
			<enumChoice choice="1" value='0'/>
			<enumChoice choice="2" value='1'/>
			<enumChoice choice="3" value='2'/>
		</enumVal>
            <label>Intensity bank number</label>
            <label xml:lang="cs">Intenzita banka číslo</label>
	</variable>

	<!-- <SV offset="3" type="int2" minValue='0'  maxValue='4096' name="Next signal address" rw="1"/> -->
	<variable item="Signal {$sigIndex} Next signal address" CV="{$CV1 +3}" default="0">
		<splitVal highCV="{$CV1 +4}" upperMask="XXXXVVVV"/>
            <label>Next signal address</label>
            <label xml:lang="cs">Adresa následujícího návěstidla</label>
	</variable>

	<!-- <SV offset="2" type="bits" start='6' length='1' name="Combine with next" rw="1" minVersion='1'> -->
	<variable item="Signal {$sigIndex} Combine with next" CV="{$CV1+2}" mask="XVXXXXXX" default="0">
		<enumVal>
                    <enumChoice choice="No" value='0'>
                        <choice>No</choice>
                        <choice xml:lang="cs">Ne</choice>
                    </enumChoice>
                    <enumChoice choice="Yes" value='1'>
                        <choice>Yes</choice>
                        <choice xml:lang="cs">Ano</choice>
                    </enumChoice>
		</enumVal>
            <label>Combine rule space with next</label>
            <label xml:lang="cs">Spojit pravidlo oddílu s následujícím</label>
	</variable>

	<!-- <SV offset="2" type="bits" start='7' length='1' name="Combined with previous" rw="0" minVersion='1'> -->
	<variable item="Signal {$sigIndex} Combined with previous" CV="{$CV1+2}" mask="VXXXXXXX" default="0">
		<enumVal>
                    <enumChoice choice="No" value='0'>
                        <choice>No</choice>
                        <choice xml:lang="cs">Ne</choice>
                    </enumChoice>
                    <enumChoice choice="Yes" value='1'>
                        <choice>Yes</choice>
                        <choice xml:lang="cs">Ano</choice>
                    </enumChoice>
		</enumVal>
                <label>Combined rule space with previous</label>
            <label xml:lang="cs">Spojeno pravidlo oddílu s předcházejícím</label>
	</variable>

	<!-- <SV offset="5" type="bits" start='0' length='12' name="Set Diverging 1, Address" minValue='0' maxValue='2048' rw="1"/> -->
	<variable item="Signal {$sigIndex} Set Diverging 1, Address" CV="{$CV1 +5}" default="0">
		<splitVal highCV="{$CV1 +6}" upperMask="XXXXXVVV"/>
            <label>Set Diverging 1, Address</label>
            <label xml:lang="cs">Nastavuje Do odbočky 1, Adresa</label>
	</variable>
	<!-- <SV offset="6" type="bits" start='7' length='1' name="Set Diverging 1, Use switch order" advanced="1" minVersion='3' rw="1"> -->
	<variable item="Signal {$sigIndex} Set Diverging 1, Use switch order" CV="{$CV1+6}" mask="VXXXXXXX" default="0">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>3</value></qualifier>
		<enumVal>
                    <enumChoice choice="No, react on normal feedback" value='0'>
                        <choice>No, react on normal feedback</choice>
                        <choice xml:lang="cs">Ne, reagovat na normální ohlas</choice>
                    </enumChoice>
                    <enumChoice choice="Yes, handle feedback" value='1'>
                        <choice>Yes, handle feedback</choice>
                        <choice xml:lang="cs">Ano, zpracovat ohlas</choice>
                    </enumChoice>
		</enumVal>
            <label>Set Diverging 1, Use switch order</label>
            <label xml:lang="cs">Nastavuje do odbočky 1, Použít příkaz výhybky</label>
	</variable>
	
	<!-- <SV offset="7" type="bits" start='0' length='12' name="Set Diverging 2, Address" minValue='0' maxValue='2048' rw="1"/> -->
	<variable item="Signal {$sigIndex} Set Diverging 2, Address" CV="{$CV1 +7}" default="0">
		<splitVal highCV="{$CV1 +8}" upperMask="XXXXXVVV"/>
            <label>Set Diverging 2, Address</label>
            <label xml:lang="cs">Nastavuje Do odbočky 2, Adresa</label>
	</variable>
	<!-- <SV offset="8" type="bits" start='7' length='1' name="Set Diverging 2, Direct order" advanced="1" minVersion='3' rw="1"> -->
	<variable item="Signal {$sigIndex} Set Diverging 2, Use switch order" CV="{$CV1+8}" mask="VXXXXXXX" default="0">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>3</value></qualifier>
		<enumVal>
                    <enumChoice choice="No, react on normal feedback" value='0'>
                        <choice>No, react on normal feedback</choice>
                        <choice xml:lang="cs">Ne, reagovat na normální ohlas</choice>
                    </enumChoice>
                    <enumChoice choice="Yes, handle feedback" value='1'>
                        <choice>Yes, handle feedback</choice>
                        <choice xml:lang="cs">Ano, zpracovat ohlas</choice>
                    </enumChoice>
		</enumVal>
            <label>Set Diverging 2, Use switch order</label>
            <label xml:lang="cs">Nastavuje do odbočky 2, Použít příkaz výhybky</label>
	</variable>
	
	<!-- <SV offset="9" type="bits" start='0' length='12' name="Set Diverging 3, Address" minValue='0' maxValue='2048' rw="1"/> -->
	<variable item="Signal {$sigIndex} Set Diverging 3, Address" CV="{$CV1 +9}" default="0">
		<splitVal highCV="{$CV1 +10}" upperMask="XXXXXVVV"/>
            <label>Set Diverging 3, Address</label>
            <label xml:lang="cs">Nastavuje Do odbočky 3, Adresa</label>
	</variable>
	<!-- <SV offset="10" type="bits" start='7' length='1' name="Set Diverging 3, Direct order" advanced="1" minVersion='3' rw="1"> -->
	<variable item="Signal {$sigIndex} Set Diverging 3, Use switch order" CV="{$CV1+10}" mask="VXXXXXXX" default="0">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>3</value></qualifier>
		<enumVal>
                    <enumChoice choice="No, react on normal feedback" value='0'>
                        <choice>No, react on normal feedback</choice>
                        <choice xml:lang="cs">Ne, reagovat na normální ohlas</choice>
                    </enumChoice>
                    <enumChoice choice="Yes, handle feedback" value='1'>
                        <choice>Yes, handle feedback</choice>
                        <choice xml:lang="cs">Ano, zpracovat ohlas</choice>
                    </enumChoice>
		</enumVal>
            <label>Set Diverging 3, Use switch order</label>
            <label xml:lang="cs">Nastavuje do odbočky 3, Použít příkaz výhybky</label>
	</variable>
	
	
	<xsl:call-template name="SignalGoRulesVar">
		<xsl:with-param name="CV1" select="$CV1+11"/>
		<xsl:with-param name="sigIndex" select="$sigIndex"/>
		<xsl:with-param name="goIndex" select="1"/>
	</xsl:call-template>

</xsl:template>

 <xsl:template name="SignalGoRulesVar">
    <xsl:param name="CV1"/>
    <xsl:param name="sigIndex"/>
    <xsl:param name="goIndex"/>

	<xsl:if test="6 >= $goIndex">

		<!-- <SV offset="11" type="bits" start='0' length='1' name="GO 1, Logic" rw="1" minVersion='1'> -->
		<variable item="Signal {$sigIndex} GO {$goIndex}, Logic" CV="{$CV1}" mask="XXXXXXXV" default="0">
			<enumVal>
                        <enumChoice choice="OR" value="0">
                          <choice>OR</choice>
                          <choice xml:lang="cs">NEBO</choice>
                        </enumChoice>
                        <enumChoice choice="AND" value="1">
                          <choice>AND</choice>
                          <choice xml:lang="cs">A ZÁROVEŇ</choice>
                        </enumChoice>
			</enumVal>
			<label>Logic</label>
			<label xml:lang="cs">Logika</label>
		</variable>

		<!--	<SV offset="11" type="bits" start='4' length='4' name="GO 1, Type" rw="1"> -->
		<variable item="Signal {$sigIndex} GO {$goIndex}, Type" CV="{$CV1}" mask="VVVVXXXX" default="0">
			<enumVal>
                            <enumChoice choice="Not used" value="0">
                                <choice>Not used</choice>
                                <choice xml:lang="cs">Nepoužito</choice>
                            </enumChoice>
                            <enumChoice choice="Sw status" value="1">
                                <choice>Sw status</choice>
                                <choice xml:lang="cs">Stav výhybky</choice>
                            </enumChoice>
                            <enumChoice choice="Occ sensor" value="2">
                                <choice>Occ sensor</choice>
                                <choice xml:lang="cs">Detekce obsazení</choice>
                            </enumChoice>
                            <enumChoice choice="SE" value="3">
                                <choice>SE</choice>
                                <choice xml:lang="cs">Změna návěsti</choice>
                            </enumChoice>
                            <enumChoice choice="xRule" value="5">
                                <choice>xRule</choice>
                                <choice xml:lang="cs">Další pravidlo</choice>
                            </enumChoice>
			</enumVal>
			<label>Type</label>
			<label xml:lang="cs">Typ</label>
		</variable>

		<!-- <SV offset="12" type="bits" start='0' length='12' name="GO 1, Address" minValue='0' maxValue='4095' rw="1"/> -->
		<variable item="Signal {$sigIndex} GO {$goIndex}, Address" CV="{$CV1 +1}" default="0">
			<splitVal highCV="{$CV1 +2}" upperMask="XXXXVVVV"/>
			<label>Address</label>
			<label xml:lang="cs">Adresa</label>
		</variable>

		<!-- <SV offset="13" type="bits" start='6' length='1' name="GO 1, Status" rw="1" minVersion='1'> -->
		<variable item="Signal {$sigIndex} GO {$goIndex}, Status" CV="{$CV1 +2}" mask="XVXXXXXX" default="0">
			<enumVal>
                            <enumChoice choice="Thrown/0" value="0">
                                 <choice>Thrown/0</choice>
                                 <choice xml:lang="cs">Do odbočky / 0</choice>
                             </enumChoice>
                             <enumChoice choice="Closed/1" value="1">
                                 <choice>Closed/1</choice>
                                 <choice xml:lang="cs">Přímo / 1</choice>
                             </enumChoice>
			</enumVal>
			<label>Status</label>
			<label xml:lang="cs">Stav</label>
		</variable>

		<xsl:call-template name="SignalGoRulesVar">
			<xsl:with-param name="CV1" select="$CV1+3"/>
			<xsl:with-param name="sigIndex" select="$sigIndex"/>
			<xsl:with-param name="goIndex" select="$goIndex+1"/>
		</xsl:call-template>

	</xsl:if>

</xsl:template>

<xsl:template name="AllSignalVars">
	<xsl:param name="CV1" select="100"/>
	<xsl:param name="sigIndex" select="1"/>

	<xsl:if test="10 >= $sigIndex">

		<xsl:call-template name="SignalVars">
			<xsl:with-param name="CV1" select="$CV1"/>
			<xsl:with-param name="sigIndex" select="$sigIndex"/>
		</xsl:call-template>

		<xsl:call-template name="AllSignalVars">
			<xsl:with-param name="CV1" select="$CV1 +29"/>
			<xsl:with-param name="sigIndex" select="$sigIndex+1"/>
		</xsl:call-template>
	</xsl:if>
</xsl:template>

<!-- panes -->

<xsl:template name="SigGoInPane">
	<xsl:param name="sigIndex" select="1"/>
	<xsl:param name="goIndex" select="1"/>

	<xsl:if test="6 >= $goIndex">
	
		<label>
                    <text>Signal <xsl:value-of select="$sigIndex"/>, Go rule <xsl:value-of select="$goIndex"/></text>
                    <text xml:lang="cs">Návěstidlo <xsl:value-of select="$sigIndex"/>, Pravidlo Volnoznak <xsl:value-of select="$goIndex"/></text>
                </label>
		<display item="Signal {$sigIndex} GO {$goIndex}, Logic"/>
		<display item="Signal {$sigIndex} GO {$goIndex}, Type"/>
		<display item="Signal {$sigIndex} GO {$goIndex}, Address"/>
		<display item="Signal {$sigIndex} GO {$goIndex}, Status"/>

		<xsl:call-template name="SigGoInPane">
			<xsl:with-param name="sigIndex" select="$sigIndex"/>
			<xsl:with-param name="goIndex" select="$goIndex +1"/>
		</xsl:call-template>

	</xsl:if>
	
</xsl:template>

<xsl:template name="AllSignalsInPane">
	<xsl:param name="sigIndex" select="1"/>

	<xsl:if test="11 > $sigIndex">

		<label><text>&#160;</text></label>
		<separator/> 
		<label>
                    <text>Signal <xsl:value-of select="$sigIndex"/></text>
                    <text xml:lang="cs">Návěstidlo <xsl:value-of select="$sigIndex"/></text>
                </label>

			<label>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>6</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>7</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>8</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>11</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>12</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>13</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>14</value></qualifier>
				<text layout="right">Type indetermined</text>
			</label>
		

		<display item="SignalSE {$sigIndex} Type">
		  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>6</value></qualifier>
		    <label>Type</label>
		</display>
		<display item="SignalDE {$sigIndex} Type">
		  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>7</value></qualifier>
		    <label>Type</label>
		</display>
		<display item="SignalDK {$sigIndex} Type">
		  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>8</value></qualifier>
		    <label>Type</label>
		</display>
		<display item="SignalCZ {$sigIndex} Type">
		  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>11</value></qualifier>
		</display>
		<display item="SignalUS {$sigIndex} Type">
		  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>12</value></qualifier>
		    <label>Type</label>
		</display>
		<display item="SignalNO {$sigIndex} Type">
		  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>13</value></qualifier>
		    <label>Type</label>
		</display>
		<display item="SignalNL {$sigIndex} Type">
		  <qualifier><variableref>Product Id</variableref><relation>eq</relation><value>14</value></qualifier>
		    <label>Type</label>
		</display>
		<display item="Signal {$sigIndex} Default start"/>
		<display item="Signal {$sigIndex} First LED number"/>
		<display item="Signal {$sigIndex} Short way"/>
		<display item="Signal {$sigIndex} Direction Control"/>
		<display item="Signal {$sigIndex} Intensity bank number"/>
		<display item="Signal {$sigIndex} Combine with next"/>
		<display item="Signal {$sigIndex} Combined with previous"/>

		<label><text>&#160;</text></label>
		<label>
                    <text>Distant signalling</text>
                    <text xml:lang="cs">Předvěstění</text>
                </label>
		<display item="Signal {$sigIndex} Next signal address"/>

		<label><text>&#160;</text></label>
		<label>
                    <text>Divering switches</text>
                    <text xml:lang="cs">Odbočující výhybky</text>
                </label>

		<display item="Signal {$sigIndex} Set Diverging 1, Address"/>
		<display item="Signal {$sigIndex} Set Diverging 1, Use switch order"/>
		<display item="Signal {$sigIndex} Set Diverging 2, Address"/>
		<display item="Signal {$sigIndex} Set Diverging 2, Use switch order"/>
		<display item="Signal {$sigIndex} Set Diverging 3, Address"/>
		<display item="Signal {$sigIndex} Set Diverging 3, Use switch order"/>

		<label><text>&#160;</text></label>
		<xsl:call-template name="SigGoInPane">
			<xsl:with-param name="sigIndex" select="$sigIndex"/>
			<xsl:with-param name="goIndex" select="1"/>
		</xsl:call-template>
		
		<label><text>&#160;</text></label>

	</xsl:if>

</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='SignalsPane']">
	<pane>
	<name>Signals</name>
	<name xml:lang="cs">Návěstidla</name>

	<column>
		<row>
			<display item="Product Id"/>
			<label><text>&#160;&#160;&#160;</text></label>
			<label>
				<qualifier><variableref>Product Id</variableref><relation>eq</relation><value>6</value></qualifier>
				<text>Swedish signal decoder</text>
				<text xml:lang="cs">Švédský návěstní dekodér</text>
			</label>
			<label>
				<qualifier><variableref>Product Id</variableref><relation>eq</relation><value>7</value></qualifier>
				<text>German signal decoder</text>
				<text xml:lang="cs">Německý návěstní dekodér</text>
			</label>
			<label>
				<qualifier><variableref>Product Id</variableref><relation>eq</relation><value>8</value></qualifier>
				<text>Danish signal decoder</text>
				<text xml:lang="cs">Dánský návěstní dekodér</text>
			</label>
			<label>
				<qualifier><variableref>Product Id</variableref><relation>eq</relation><value>11</value></qualifier>
				<text>Czech signal decoder</text>
				<text xml:lang="cs">Český návěstní dekodér</text>
			</label>
			<label>
				<qualifier><variableref>Product Id</variableref><relation>eq</relation><value>12</value></qualifier>
				<text>US signal decoder</text>
				<text xml:lang="cs">USA návěstní dekodér</text>
			</label>
			<label>
				<qualifier><variableref>Product Id</variableref><relation>eq</relation><value>13</value></qualifier>
				<text>Norweigen signal decoder</text>
				<text xml:lang="cs">Norský návěstní dekodér</text>
			</label>
			<label>
				<qualifier><variableref>Product Id</variableref><relation>eq</relation><value>14</value></qualifier>
				<text>Dutch signal decoder</text>
				<text xml:lang="cs">Holandský návěstní dekodér</text>
			</label>
		</row>
		<row>
			<label>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>6</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>7</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>8</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>11</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>12</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>13</value></qualifier>
				<qualifier><variableref>Product Id</variableref><relation>ne</relation><value>14</value></qualifier>
				<text>Country for Signal decoder is not defined, please read data from decoder</text>
				<text xml:lang="cs">Není definovaná země návěstního dekodéru, prosím přečtěte data z dekodéru</text>
			</label>
		</row>

		<row>
		<column>

			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="1"/>
			</xsl:call-template>
			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="3"/>
			</xsl:call-template>
			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="5"/>
			</xsl:call-template>
			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="7"/>
			</xsl:call-template>
			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="9"/>
			</xsl:call-template>
		</column>
		<column>
			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="2"/>
			</xsl:call-template>
			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="4"/>
			</xsl:call-template>
			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="6"/>
			</xsl:call-template>
			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="8"/>
			</xsl:call-template>
			<xsl:call-template name="AllSignalsInPane">
				<xsl:with-param name="sigIndex" select="10"/>
			</xsl:call-template>
		</column>
		</row>
		</column>
	   </pane>
</xsl:template>


<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Special Signal Controls * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<xsl:template name="SpecialSignalVars">
	<xsl:param name="CV1" select="800"/>
	<xsl:param name="sigIndex" select="1"/>
	<xsl:param name="goIndex" select="1"/>


	<xsl:call-template name="SpecialSignalControls">
		<xsl:with-param name="CV1" select="$CV1"/>
		<xsl:with-param name="sigIndex" select="$sigIndex"/>
	</xsl:call-template>

</xsl:template>

 <xsl:template name="SpecialSignalControls">
    <xsl:param name="CV1"/>
    <xsl:param name="sigIndex"/>
    <xsl:param name="goIndex" select="1"/>

	<xsl:if test="4 >= $goIndex">

		<!-- <SV offset="0" type="int2" minValue='0'  maxValue='4096' name="Control Address" rw="1"/> -->
		<variable item="Signal {$sigIndex} Control {$goIndex} Control Address" CV="{$CV1}" default="0">
			<splitVal highCV="{$CV1 +1}" upperMask="XXXXVVVV"/>
                    <label>Control Address</label>
                    <label xml:lang="cs">Řídí adresu</label>
		</variable>

		<!-- <SV offset="1" type="bits" start='0' length='1' name="Feedback" rw="1" minVersion='12'> -->
		<variable item="Signal {$sigIndex} Control {$goIndex} Default State" CV="{$CV1+1}" mask="XXXVXXXX" default="0">
			<enumVal>
                            <enumChoice choice="Thrown at start" value='0'>
                                <choice>Thrown at start</choice>
                                <choice xml:lang="cs">Do odbočky po startu</choice>
                            </enumChoice>
                            <enumChoice choice="Closed at start" value='1'>
                                <choice>Closed at start</choice>
                                <choice xml:lang="cs">Přímo po startu</choice>
                            </enumChoice>
			</enumVal>
                    <label>Default State</label>
                    <label xml:lang="cs">Výchozí stav</label>
		</variable>

		<!-- <SV offset="1" type="bits" start='0' length='1' name="Feedback" rw="1" minVersion='9'> -->
		<variable item="Signal {$sigIndex} Control {$goIndex} Feedback" CV="{$CV1+1}" mask="XXVXXXXX" default="0">
			<enumVal>
                            <enumChoice choice="No, react on normal feedback" value='0'>
                                <choice>No, react on normal feedback</choice>
                                <choice xml:lang="cs">Ne, reagovat na normální ohlas</choice>
                            </enumChoice>
                            <enumChoice choice="Yes, handle feedback" value='1'>
                                <choice>Yes, handle feedback</choice>
                                <choice xml:lang="cs">Ano, zpracovat ohlas</choice>
                            </enumChoice>
			</enumVal>
                    <label>Feedback</label>
                    <label xml:lang="cs">Ohlas</label>
		</variable>
		
		<!-- <SV offset="1" type="bits" start='0' length='1' name="Special State" rw="1" minVersion='9'> -->
		<variable item="Signal {$sigIndex} Control {$goIndex} Special State" CV="{$CV1+1}" mask="XVXXXXXX" default="0">
			<enumVal>
                            <enumChoice choice="Active at thrown" value='0'>
                                <choice>Active at thrown</choice>
                                <choice xml:lang="cs">Aktivní při Do odbočky</choice>
                            </enumChoice>
                            <enumChoice choice="Active at closed" value='1'>
                                <choice>Active at closed</choice>
                                <choice xml:lang="cs">Aktivní při Přímo</choice>
                            </enumChoice>
			</enumVal>
			<label>Special State</label>
			<label xml:lang="cs">Zvláštní stav</label>
		</variable>
		
		<!-- <SV offset="1" type="bits" start='0' length='1' name="Force View" rw="1" minVersion='9'> -->
		<variable item="Signal {$sigIndex} Control {$goIndex} Force View" CV="{$CV1+1}" mask="VXXXXXXX" default="0">
			<enumVal>
                            <enumChoice choice="No,view when alloved" value='0'>
                                <choice>No,view when alloved</choice>
                                <choice xml:lang="cs">Ne, zobrazení je povoleno</choice>
                            </enumChoice>
                            <enumChoice choice="Yes, force this view" value='1'>
                                <choice>Yes, force this view</choice>
                                <choice xml:lang="cs">Ano, vynutit toto zobrazení</choice>
                            </enumChoice>
			</enumVal>
                    <label>Force View</label>
                    <label xml:lang="cs">Vynutit zobrazení</label>
		</variable>

		<xsl:call-template name="SpecialSignalControls">
			<xsl:with-param name="CV1" select="$CV1+2"/>
			<xsl:with-param name="sigIndex" select="$sigIndex"/>
			<xsl:with-param name="goIndex" select="$goIndex+1"/>
		</xsl:call-template>

	</xsl:if>

</xsl:template>

<xsl:template name="AllSpecialSignalVars">
	<xsl:param name="CV1" select="800"/>
	<xsl:param name="sigIndex" select="1"/>

	<xsl:if test="10 >= $sigIndex">
	
		<xsl:call-template name="SpecialSignalVars">
			<xsl:with-param name="CV1" select="$CV1"/>
			<xsl:with-param name="sigIndex" select="$sigIndex"/>
		</xsl:call-template>
		
	<!-- <SV offset="8" type="bits" start='0' length='4' name="Control 1 Address Type" rw="1" minVersion='11'> -->
	<variable item="Signal {$sigIndex} Control 1 Address Type" CV="{$CV1+8}" mask="XXXXVVVV" default="0">
		<enumVal>
                    <enumChoice choice="Not used" value="0">
                        <choice>Not used</choice>
                        <choice xml:lang="cs">Nepoužito</choice>
                    </enumChoice>
                    <enumChoice choice="Switch status" value="1">
                        <choice>Switch status</choice>
                        <choice xml:lang="cs">Stav výhybky</choice>
                    </enumChoice>
                    <enumChoice choice="Track Sensor" value="2">
                        <choice>Track Sensor</choice>
                        <choice xml:lang="cs">Detekce obsazení</choice>
                    </enumChoice>
                    <enumChoice choice="Signal Status" value="3">
                        <choice>Signal Status</choice>
                        <choice xml:lang="cs">Stav návěstidla</choice>
                    </enumChoice>
                    <enumChoice choice="Extra Rule" value='4'>
                        <choice>Extra Rule</choice>
                        <choice xml:lang="cs">Další pravidlo</choice>
                    </enumChoice>
                    <enumChoice choice="Fixed Setting" value='5'>
                        <choice>Fixed Setting</choice>
                        <choice xml:lang="cs">Pevné nastavení</choice>
                    </enumChoice>
                </enumVal>
            <label>Control 1 Address Type</label>
            <label xml:lang="cs">Řídí 1 Typ adresy</label>
	</variable>
	
	<!-- <SV offset="8" type="bits" start='4' length='4' name="Control 2 Address Type" rw="1" minVersion='11'> -->
	<variable item="Signal {$sigIndex} Control 2 Address Type" CV="{$CV1+8}" mask="VVVVXXXX" default="0">
		<enumVal>
                    <enumChoice choice="Not used" value="0">
                        <choice>Not used</choice>
                        <choice xml:lang="cs">Nepoužito</choice>
                    </enumChoice>
                    <enumChoice choice="Switch status" value="1">
                        <choice>Switch status</choice>
                        <choice xml:lang="cs">Stav výhybky</choice>
                    </enumChoice>
                    <enumChoice choice="Track Sensor" value="2">
                        <choice>Track Sensor</choice>
                        <choice xml:lang="cs">Detekce obsazení</choice>
                    </enumChoice>
                    <enumChoice choice="Signal Status" value="3">
                        <choice>Signal Status</choice>
                        <choice xml:lang="cs">Stav návěstidla</choice>
                    </enumChoice>
                    <enumChoice choice="Extra Rule" value='4'>
                        <choice>Extra Rule</choice>
                        <choice xml:lang="cs">Další pravidlo</choice>
                    </enumChoice>
                    <enumChoice choice="Fixed Setting" value='5'>
                        <choice>Fixed Setting</choice>
                        <choice xml:lang="cs">Pevné nastavení</choice>
                    </enumChoice>
		</enumVal>
            <label>Control 2 Address Type</label>
            <label xml:lang="cs">Řídí 2 Typ adresy</label>
	</variable>
	
		
	<!-- <SV offset="9" type="bits" start='0' length='4' name="Control 3 Address Type" rw="1" minVersion='11'> -->
	<variable item="Signal {$sigIndex} Control 3 Address Type" CV="{$CV1+9}" mask="XXXXVVVV" default="0">
		<enumVal>
                    <enumChoice choice="Not used" value="0">
                        <choice>Not used</choice>
                        <choice xml:lang="cs">Nepoužito</choice>
                    </enumChoice>
                    <enumChoice choice="Switch status" value="1">
                        <choice>Switch status</choice>
                        <choice xml:lang="cs">Stav výhybky</choice>
                    </enumChoice>
                    <enumChoice choice="Track Sensor" value="2">
                        <choice>Track Sensor</choice>
                        <choice xml:lang="cs">Detekce obsazení</choice>
                    </enumChoice>
                    <enumChoice choice="Signal Status" value="3">
                        <choice>Signal Status</choice>
                        <choice xml:lang="cs">Stav návěstidla</choice>
                    </enumChoice>
                    <enumChoice choice="Extra Rule" value='4'>
                        <choice>Extra Rule</choice>
                        <choice xml:lang="cs">Další pravidlo</choice>
                    </enumChoice>
                    <enumChoice choice="Fixed Setting" value='5'>
                        <choice>Fixed Setting</choice>
                        <choice xml:lang="cs">Pevné nastavení</choice>
                    </enumChoice>
		</enumVal>
            <label>Control 3 Address Type</label>
            <label xml:lang="cs">Řídí 3 Typ adresy</label>
	</variable>
	
	<!-- <SV offset="9" type="bits" start='4' length='4' name="Control 4 Address Type" rw="1" minVersion='11'> -->
	<variable item="Signal {$sigIndex} Control 4 Address Type" CV="{$CV1+9}" mask="VVVVXXXX" default="0">
		<enumVal>
                    <enumChoice choice="Not used" value="0">
                        <choice>Not used</choice>
                        <choice xml:lang="cs">Nepoužito</choice>
                    </enumChoice>
                    <enumChoice choice="Switch status" value="1">
                        <choice>Switch status</choice>
                        <choice xml:lang="cs">Stav výhybky</choice>
                    </enumChoice>
                    <enumChoice choice="Track Sensor" value="2">
                        <choice>Track Sensor</choice>
                        <choice xml:lang="cs">Detekce obsazení</choice>
                    </enumChoice>
                    <enumChoice choice="Signal Status" value="3">
                        <choice>Signal Status</choice>
                        <choice xml:lang="cs">Stav návěstidla</choice>
                    </enumChoice>
                    <enumChoice choice="Extra Rule" value='4'>
                        <choice>Extra Rule</choice>
                        <choice xml:lang="cs">Další pravidlo</choice>
                    </enumChoice>
                    <enumChoice choice="Fixed Setting" value='5'>
                        <choice>Fixed Setting</choice>
                        <choice xml:lang="cs">Pevné nastavení</choice>
                    </enumChoice>
		</enumVal>
            <label>Control 4 Address Type</label>
            <label xml:lang="cs">Řídí 4 Typ adresy</label>
	</variable>
	

	<xsl:call-template name="AllSpecialSignalVars">
			<xsl:with-param name="CV1" select="$CV1+10"/>
			<xsl:with-param name="sigIndex" select="$sigIndex+1"/>
		</xsl:call-template>
	
	</xsl:if>
</xsl:template>

<!-- panes -->

<xsl:template name="SigSpecialInPane">
	<xsl:param name="sigIndex" select="1"/>
	<xsl:param name="goIndex" select="1"/>

	<xsl:if test="4 >= $goIndex">
	
		<label>
                    <text>Signal <xsl:value-of select="$sigIndex"/>, Control <xsl:value-of select="$goIndex"/></text>
                    <text xml:lang="cs">Návěstidlo <xsl:value-of select="$sigIndex"/>, Řídí <xsl:value-of select="$goIndex"/></text>
                </label>

		<display item="Signal {$sigIndex} Control {$goIndex} Control Address"/>
		<display item="Signal {$sigIndex} Control {$goIndex} Default State"/>
		<display item="Signal {$sigIndex} Control {$goIndex} Feedback"/>
		<display item="Signal {$sigIndex} Control {$goIndex} Special State"/>
		<display item="Signal {$sigIndex} Control {$goIndex} Force View"/>

		<xsl:call-template name="SigSpecialInPane">
			<xsl:with-param name="sigIndex" select="$sigIndex"/>
			<xsl:with-param name="goIndex" select="$goIndex +1"/>
		</xsl:call-template>

	</xsl:if>
	
</xsl:template>

<xsl:template name="AllSpecialSignalsInPane">
	<xsl:param name="sigIndex" select="1"/>

	<xsl:if test="11 > $sigIndex">

-		<label><text>&#160;</text></label>
		<label>
                    <text>Signal <xsl:value-of select="$sigIndex"/></text>
                    <text xml:lang="cs">Návěstidlo <xsl:value-of select="$sigIndex"/></text>
                </label>
		<label><text>&#160;</text></label>

		<display item="Signal {$sigIndex} Control 1 Address Type"/>
		<display item="Signal {$sigIndex} Control 2 Address Type"/>
		<display item="Signal {$sigIndex} Control 3 Address Type"/>
		<display item="Signal {$sigIndex} Control 4 Address Type"/>
		
		<label><text>&#160;</text></label>
		<xsl:call-template name="SigSpecialInPane">
			<xsl:with-param name="sigIndex" select="$sigIndex"/>
			<xsl:with-param name="goIndex" select="1"/>
		</xsl:call-template>

	</xsl:if>

</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='SigSpecialsPane']">
	<pane>
	<name>Special Signal Controls</name>
	<name xml:lang="cs">Zvláštní řízení návěstidla</name>
		<column>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="1"/>
			</xsl:call-template>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="3"/>
			</xsl:call-template>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="5"/>
			</xsl:call-template>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="7"/>
			</xsl:call-template>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="9"/>
			</xsl:call-template>
		</column>
		<column>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="2"/>
			</xsl:call-template>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="4"/>
			</xsl:call-template>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="6"/>
			</xsl:call-template>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="8"/>
			</xsl:call-template>
			<xsl:call-template name="AllSpecialSignalsInPane">
				<xsl:with-param name="sigIndex" select="10"/>
			</xsl:call-template>
		</column>
   </pane>
</xsl:template>

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Run All   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- - - - MATCH - - - -->
<!-- install new variables at end of variables element-->
 <xsl:template match="variables">
   <variables>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="AllSignalVars"/>
     <xsl:call-template name="AllSpecialSignalVars"/>	 
     <xsl:call-template name="AllExtraRuleVar"/>
     <xsl:call-template name="AllShortSignalVars"/>
     <xsl:call-template name="AllRepeatSignalVars"/>
     <xsl:call-template name="AllDirectionDetectionVars"/>
     <xsl:call-template name="AllTriggerRuleVars"/>
     <xsl:call-template name="AllSignalSelectorsVars"/>	 
   </variables>
 </xsl:template>


<!--Identity template copies content forward -->
<!-- - - - MATCH - - - -->
 <xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
 </xsl:template>
 
</xsl:stylesheet>
