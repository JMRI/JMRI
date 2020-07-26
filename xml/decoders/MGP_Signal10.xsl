<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet	version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    >
<xsl:output method="xml" encoding="utf-8"/>

<!-- Copyright (C) JMRI 2002, 2005, 2007 All rights reserved -->
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
<!-- * * * * * Repeat Signals  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<xsl:template name="RepeatSignalVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Repeat Signal {$index} Type" CV="{$CV1}" mask="XXXXXVVV" default="0">
        <enumVal>
          <enumChoice choice="Not used" value="0"/>
          <enumChoice choice="2 light" value="1"/>
        </enumVal>
        <label>Repeat Signal <xsl:value-of select="$index"/> Type</label>
    </variable>
    <variable item="Repeat Signal {$index} Intensity bank number" CV="{$CV1}" mask="XXVVXXXX" default="0">
        <enumVal>
          <enumChoice choice="1" value="0"/>
          <enumChoice choice="2" value="1"/>
          <enumChoice choice="3" value="2"/>
        </enumVal>
        <label>Repeat Signal <xsl:value-of select="$index"/> Intensity bank number</label>
    </variable>
    <variable item="Repeat Signal {$index} First LED number" CV="{$CV1 +1}" default="0">
        <decVal max="64"/>
        <label>Repeat Signal <xsl:value-of select="$index"/> First LED number</label>
    </variable>
	<variable item="Repeat Signal {$index} Main signal address" CV="{$CV1 +2}" default="0">
        <splitVal highCV="{$CV1 +3}" upperMask="XXXXXVVV"/>
        <label>Repeat Signal <xsl:value-of select="$index"/> Main signal address</label>
    </variable>
</xsl:template>

<xsl:template name="AllRepeatSignalVars">
  <xsl:param name="CV1" select="500"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="5 >= $index">
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
		<label><text>Repeat Signal <xsl:value-of select="$index"/></text></label>
        <display item="Repeat Signal {$index} Type">
			<label>Type</label>
		</display>
		<display item="Repeat Signal {$index} First LED number">
			<label>First LED number</label>
		</display>
		<display item="Repeat Signal {$index} Intensity bank number">
			<label>Intensity bank number</label>
		</display>
		<display item="Repeat Signal {$index} Main signal address">
			<label>Main signal address</label>
		</display>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='RepeatSignalPane']">
	<pane>
	<name>Repeat Signals</name>
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

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Trigger rule statements   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
 <xsl:template name="TriggerRuleStmnt">
    <xsl:param name="CV1"/>
    <xsl:param name="ruleIndex"/>
    <xsl:param name="stmntIndex"/>

    <variable item="Rule {$ruleIndex} statement {$stmntIndex} Logic" CV="{$CV1}" mask="XXXXXXXV" default="0">
        <enumVal>
          <enumChoice choice="OR" value="0"/>
          <enumChoice choice="AND" value="1"/>
        </enumVal>
    </variable>

    <variable item="Rule {$ruleIndex} statement {$stmntIndex} Status" CV="{$CV1}" mask="XXXXXXVX" default="0">
        <enumVal>
          <enumChoice choice="0/Thrown/Free" value="0"/>
          <enumChoice choice="1/Closed/Occupied" value="1"/>
        </enumVal>
    </variable>
    <variable item="Rule {$ruleIndex} statement {$stmntIndex} Type" CV="{$CV1}" mask="XXXVVVXX" default="0">
        <enumVal>
          <enumChoice choice="Not used" value="0"/>
          <enumChoice choice="Sw status" value="1"/>
          <enumChoice choice="Occ sensor" value="2"/>
          <enumChoice choice="SE" value="3"/>
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
			<enumChoice choice="Not used" value='0'/>
			<enumChoice choice="Signal 1" value='1'/>
			<enumChoice choice="Signal 2" value='2'/>
			<enumChoice choice="Signal 3" value='3'/>
			<enumChoice choice="Signal 4" value='4'/>
			<enumChoice choice="Signal 5" value='5'/>
			<enumChoice choice="Signal 6" value='6'/>
			<enumChoice choice="Signal 7" value='7'/>
			<enumChoice choice="Signal 8" value='8'/>
			<enumChoice choice="Signal 9" value='9'/>
			<enumChoice choice="Signal 10" value='10'/>
        </enumVal>
    </variable>

    <variable item="Rule {$index}, Controlled status" CV="{$CV1}" mask="XXXVXXXXX" default="0">
	    <qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>2</value></qualifier>
        <enumVal>
          <enumChoice choice="Signal Stop" value="0"/>
          <enumChoice choice="Signal Go" value="1"/>
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

	<xsl:if test="5 > $index">

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
	<label><text>Rule <xsl:value-of select="$index"/></text></label>
	<display item="Rule {$index} Signal Number">
		<label>Signal Number</label>
	</display>
	<display item="Rule {$index}, Controlled status">
		<label>Controlled status</label>
	</display>

	<label><text>&#160;</text></label>
	<display item="Rule {$index} statement 1 Logic">
		<label>Stmnt 1 Logic</label>
	</display>
	<display item="Rule {$index} statement 1 Status">
		<label>Stmnt 1 Status</label>
	</display>
	<display item="Rule {$index} statement 1 Type">
		<label>Stmnt 1 Type</label>
	</display>
	<display item="Rule {$index} statement 1 Address">
		<label>Stmnt 1 Address</label>
	</display>

	<label><text>&#160;</text></label>
	<display item="Rule {$index} statement 2 Logic">
		<label>Stmnt 2 Logic</label>
	</display>
	<display item="Rule {$index} statement 2 Status">
		<label>Stmnt 2 Status</label>
	</display>
	<display item="Rule {$index} statement 2 Type">
		<label>Stmnt 2 Type</label>
	</display>
	<display item="Rule {$index} statement 2 Address">
		<label>Stmnt 2 Address</label>
	</display>

	<label><text>&#160;</text></label>
	<display item="Rule {$index} statement 3 Logic">
		<label>Stmnt 3 Logic</label>
	</display>
	<display item="Rule {$index} statement 3 Status">
		<label>Stmnt 3 Status</label>
	</display>
	<display item="Rule {$index} statement 3 Type">
		<label>Stmnt 3 Type</label>
	</display>
	<display item="Rule {$index} statement 3 Address">
		<label>Stmnt 3 Address</label>
	</display>

</xsl:template>

<!-- - - - MATCH - - - -->

<xsl:template match="pane[name='TriggerRulePane']">
	<pane>
	<name>Trigger Rules</name>
 
	<row>
		 <column>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="1"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="2"/>
			</xsl:call-template>
		</column>
	</row>
	<row>
		 <column>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="3"/>
			</xsl:call-template>
			<xsl:call-template name="TriggerRuleInPane">
			  <xsl:with-param name="index" select="4"/>
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
			<enumChoice choice="OR" value='0'/>
			<enumChoice choice="AND" value='1'/>
        </enumVal>
    </variable>

    <variable item="Extra Rule {$ruleIndex} GO {$goIndex}, Type" CV="{$CV1}" mask="VVVVXXXX" default="0">
        <enumVal>
          <enumChoice choice="Not used" value="0"/>
          <enumChoice choice="Sw status" value="1"/>
          <enumChoice choice="Occ sensor" value="2"/>
          <enumChoice choice="SE" value="3"/>
          <enumChoice choice="xRule" value="5"/>
        </enumVal>
    </variable>

	<variable item="Extra Rule {$ruleIndex} GO {$goIndex}, Address" CV="{$CV1 +1}" default="0">
        <splitVal highCV="{$CV1 +2}" upperMask="XXXXVVVV"/>
    </variable>

    <variable item="Extra Rule {$ruleIndex} GO {$goIndex}, Status" CV="{$CV1 +2}" mask="XVXXXXXX" default="0">
        <enumVal>
			<enumChoice choice="Thrown/0" value='0'/>
			<enumChoice choice="CLosed/1" value='1'/>
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

		<label><text>Rule <xsl:value-of select="$ruleIndex"/>, GO <xsl:value-of select="$goIndex"/></text></label>
		<display item="Extra Rule {$ruleIndex} GO {$goIndex}, Logic">
			<label>Logic</label>
		</display>
		<display item="Extra Rule {$ruleIndex} GO {$goIndex}, Type">
			<label>Type</label>
		</display>
		<display item="Extra Rule {$ruleIndex} GO {$goIndex}, Address">
			<label>Address</label>
		</display>
		<label><text>&#160;</text></label>
		<display item="Extra Rule {$ruleIndex} GO {$goIndex}, Status">
			<label>Status</label>
		</display>

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
		<label><text>Rule <xsl:value-of select="$index"/></text></label>

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
<!-- * * * * * Signals   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<xsl:template name="SignalVars">
	<xsl:param name="CV1" select="100"/>
	<xsl:param name="sigIndex" select="1"/>

	<!-- <SV offset="0" type="bits" start='0' length='5' name="Type" rw="1"> -->
	<variable item="Signal {$sigIndex} Type" CV="{$CV1}" mask="XXXVVVVV" default="0">
		<enumVal>
			<enumChoice choice="Not used" value='0'/>
			<enumChoice choice="Hsi 2" value='1'/>
			<enumChoice choice="Hsi 3" value='3'/>
			<enumChoice choice="Hsi 4" value='4'/>
			<enumChoice choice="Hsi 5" value='5'/>
			<enumChoice choice="HdvM" value='6'/>
			<enumChoice choice="Dvsi" value='7'/>
			<enumChoice choice="Fsi 2" value='8'/>
			<enumChoice choice="Fsi 3" value='9'/>
			<enumChoice choice="Repeat" value='10'/>
			<enumChoice choice="VSI 2" value='11'/>
			<enumChoice choice="VSI rgb" value='12'/>
			<enumChoice choice="VFSI" value='13'/>
			<enumChoice choice="VTSI" value='14'/>
		</enumVal>
	</variable>
	
	<!-- <SV offset="0" type="bits" start='7' length='1' name="Default start" rw="1" minVersion='2'> -->
	<variable item="Signal {$sigIndex} Default start" CV="{$CV1}" mask="VXXXXXXX" default="1">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>2</value></qualifier>
		<enumVal>
			<enumChoice choice="Stop" value='0'/>
			<enumChoice choice="Go" value='1'/>
		</enumVal>
	</variable>

	<!-- <SV offset="1" type="int1" minValue='1'  maxValue='64' name="First LED number" rw="1"/> -->
	<variable item="Signal {$sigIndex} First LED number" CV="{$CV1+1}" default="0">
		<decVal min="1" max="64"/>
	</variable>

	<!-- <SV offset="2" type="bits" start='0' length='1' name="Short way" rw="1" minVersion='3'> -->
	<variable item="Signal {$sigIndex} Short way" CV="{$CV1+2}" mask="XXXXXXXV" default="0">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>3</value></qualifier>
		<enumVal>
			<enumChoice choice="No" value='0'/>
			<enumChoice choice="Yes" value='1'/>
		</enumVal>
	</variable>
	
	<!-- <SV offset="2" type="bits" start='1' length='3' name="Direction Control" rw="1" minVersion='1'> -->
	<variable item="Signal {$sigIndex} Direction Control" CV="{$CV1+2}" mask="XXXXVVVX" default="0">
		<enumVal>
			<enumChoice choice="Not used" value='0'/>
			<enumChoice choice="1 - West to East" value='2'/>
			<enumChoice choice="1 - East to West" value='3'/>
			<enumChoice choice="2 - West to East" value='4'/>
			<enumChoice choice="2 - East to West" value='5'/>
			<enumChoice choice="3 - West to East" value='6'/>
			<enumChoice choice="3 - East to West" value='7'/>
		</enumVal>
	</variable>

	<!-- <SV offset="2" type="bits" start='4' length='2' name="Intensity bank number" rw="1"> -->
	<variable item="Signal {$sigIndex} Intensity bank number" CV="{$CV1+2}" mask="XXVVXXXX" default="0">
		<enumVal>
			<enumChoice choice="1" value='0'/>
			<enumChoice choice="2" value='1'/>
			<enumChoice choice="3" value='2'/>
		</enumVal>
	</variable>

	<!-- <SV offset="2" type="bits" start='6' length='1' name="Combine with next" rw="1" minVersion='1'> -->
	<variable item="Signal {$sigIndex} Combine with next" CV="{$CV1+2}" mask="XVXXXXXX" default="0">
		<enumVal>
			<enumChoice choice="No" value='0'/>
			<enumChoice choice="Yes" value='1'/>
		</enumVal>
	</variable>

	<!-- <SV offset="2" type="bits" start='7' length='1' name="Combined with previous" rw="0" minVersion='1'> -->
	<variable item="Signal {$sigIndex} Combined with previous" CV="{$CV1+2}" mask="VXXXXXXX" default="0">
		<enumVal>
			<enumChoice choice="No" value='0'/>
			<enumChoice choice="Yes" value='1'/>
		</enumVal>
	</variable>

	<!-- <SV offset="3" type="int2" minValue='0'  maxValue='4096' name="Next signal address" rw="1"/> -->
	<variable item="Signal {$sigIndex} Next signal address" CV="{$CV1 +3}" default="0">
		<splitVal highCV="{$CV1 +4}" upperMask="XXXXVVVV"/>
	</variable>

	<!-- <SV offset="5" type="bits" start='0' length='12' name="Set Diverging 1, Address" minValue='0' maxValue='2048' rw="1"/> -->
	<variable item="Signal {$sigIndex} Set Diverging 1, Address" CV="{$CV1 +5}" default="0">
		<splitVal highCV="{$CV1 +6}" upperMask="XXXXXVVV"/>
	</variable>
	<!-- <SV offset="6" type="bits" start='7' length='1' name="Set Diverging 1, Use switch order" advanced="1" minVersion='3' rw="1"> -->
	<variable item="Signal {$sigIndex} Set Diverging 1, Use switch order" CV="{$CV1+6}" mask="VXXXXXXX" default="0">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>3</value></qualifier>
		<enumVal>
			<enumChoice choice="No, react on normal feedback" value='0'/>
			<enumChoice choice="Yes, handle feedback" value='1'/>
		</enumVal>
	</variable>
	
	<!-- <SV offset="7" type="bits" start='0' length='12' name="Set Diverging 2, Address" minValue='0' maxValue='2048' rw="1"/> -->
	<variable item="Signal {$sigIndex} Set Diverging 2, Address" CV="{$CV1 +7}" default="0">
		<splitVal highCV="{$CV1 +8}" upperMask="XXXXXVVV"/>
	</variable>
	<!-- <SV offset="8" type="bits" start='7' length='1' name="Set Diverging 2, Direct order" advanced="1" minVersion='3' rw="1"> -->
	<variable item="Signal {$sigIndex} Set Diverging 2, Use switch order" CV="{$CV1+8}" mask="VXXXXXXX" default="0">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>3</value></qualifier>
		<enumVal>
			<enumChoice choice="No, react on normal feedback" value='0'/>
			<enumChoice choice="Yes, handle feedback" value='1'/>
		</enumVal>
	</variable>
	
	<!-- <SV offset="9" type="bits" start='0' length='12' name="Set Diverging 3, Address" minValue='0' maxValue='2048' rw="1"/> -->
	<variable item="Signal {$sigIndex} Set Diverging 3, Address" CV="{$CV1 +9}" default="0">
		<splitVal highCV="{$CV1 +10}" upperMask="XXXXXVVV"/>
	</variable>
	<!-- <SV offset="10" type="bits" start='7' length='1' name="Set Diverging 3, Direct order" advanced="1" minVersion='3' rw="1"> -->
	<variable item="Signal {$sigIndex} Set Diverging 3, Use switch order" CV="{$CV1+10}" mask="VXXXXXXX" default="0">
		<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>3</value></qualifier>
		<enumVal>
			<enumChoice choice="No, react on normal feedback" value='0'/>
			<enumChoice choice="Yes, handle feedback" value='1'/>
		</enumVal>
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
				<enumChoice choice="OR" value='0'/>
				<enumChoice choice="AND" value='1'/>
			</enumVal>
		</variable>

		<!--	<SV offset="11" type="bits" start='4' length='4' name="GO 1, Type" rw="1"> -->
		<variable item="Signal {$sigIndex} GO {$goIndex}, Type" CV="{$CV1}" mask="VVVVXXXX" default="0">
			<enumVal>
			  <enumChoice choice="Not used" value="0"/>
			  <enumChoice choice="Sw status" value="1"/>
			  <enumChoice choice="Occ sensor" value="2"/>
			  <enumChoice choice="SE" value="3"/>
			  <enumChoice choice="xRule" value="5"/>
			</enumVal>
		</variable>

		<!-- <SV offset="12" type="bits" start='0' length='12' name="GO 1, Address" minValue='0' maxValue='4095' rw="1"/> -->
		<variable item="Signal {$sigIndex} GO {$goIndex}, Address" CV="{$CV1 +1}" default="0">
			<splitVal highCV="{$CV1 +2}" upperMask="XXXXVVVV"/>
		</variable>

		<!-- <SV offset="13" type="bits" start='6' length='1' name="GO 1, Status" rw="1" minVersion='1'> -->
		<variable item="Signal {$sigIndex} GO {$goIndex}, Status" CV="{$CV1 +2}" mask="XVXXXXXX" default="0">
			<enumVal>
				<enumChoice choice="Thrown/0" value='0'/>
				<enumChoice choice="CLosed/1" value='1'/>
			</enumVal>
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
	
		<label><text>Signal <xsl:value-of select="$sigIndex"/>, Go rule <xsl:value-of select="$goIndex"/></text></label>
		<display item="Signal {$sigIndex} GO {$goIndex}, Logic">
			<label>Logic</label>
		</display>
		<display item="Signal {$sigIndex} GO {$goIndex}, Type">
			<label>Type</label>
		</display>
		<display item="Signal {$sigIndex} GO {$goIndex}, Address">
			<label>Address</label>
		</display>
		<display item="Signal {$sigIndex} GO {$goIndex}, Status">
			<label>Status</label>
		</display>

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
		<label><text>Signal <xsl:value-of select="$sigIndex"/></text></label>

		<display item="Signal {$sigIndex} Type">
			<label>Type</label>
		</display>
		<display item="Signal {$sigIndex} Default start">
			<label>Default start</label>
		</display>
		<display item="Signal {$sigIndex} First LED number">
			<label>First LED number</label>
		</display>
		<display item="Signal {$sigIndex} Short way">
			<label>Short way</label>
		</display>
		<display item="Signal {$sigIndex} Direction Control">
			<label>Direction Control</label>
		</display>
		<display item="Signal {$sigIndex} Intensity bank number">
			<label>Intensity bank number</label>
		</display>
		<display item="Signal {$sigIndex} Combine with next">
			<label>Combine with next</label>
		</display>
		<display item="Signal {$sigIndex} Combined with previous">
			<label>Combined with previous</label>
		</display>
		<display item="Signal {$sigIndex} Next signal address">
			<label>Next signal address</label>
		</display>

		<label><text>&#160;</text></label>
		<display item="Signal {$sigIndex} Set Diverging 1, Address">
			<label>Set Diverging 1, Address</label>
		</display>
		<display item="Signal {$sigIndex} Set Diverging 1, Use switch order">
			<label>Set Diverging 1, Use switch order</label>
		</display>
		<display item="Signal {$sigIndex} Set Diverging 2, Address">
			<label>Set Diverging 2, Address</label>
		</display>
		<display item="Signal {$sigIndex} Set Diverging 2, Use switch order">
			<label>Set Diverging 2, Use switch order</label>
		</display>
		<display item="Signal {$sigIndex} Set Diverging 3, Address">
			<label>Set Diverging 3, Address</label>
		</display>
		<display item="Signal {$sigIndex} Set Diverging 3, Use switch order">
			<label>Set Diverging 3, Use switch order</label>
		</display>

		<label><text>&#160;</text></label>
		<xsl:call-template name="SigGoInPane">
			<xsl:with-param name="sigIndex" select="$sigIndex"/>
			<xsl:with-param name="goIndex" select="1"/>
		</xsl:call-template>

	</xsl:if>

</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='SignalsPane']">
	<pane>
	<name>Signals</name>
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
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->



<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
<xsl:template match="pane[name='TestPane']">
	<pane>
	<name>Just for testing</name>
		<column>
			<row>
				<label><text>R1 C1</text></label>
			</row>
			<row>
				<label><text>R2 C1</text></label>
			</row>
		</column>
		<column>
			<row>
				<label><text>R1 C2</text></label>
			</row>
			<row>
				<label><text>R2 C2</text></label>
			</row>
		</column>
   </pane>
</xsl:template>
-->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Run All   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- - - - MATCH - - - -->
<!-- install new variables at end of variables element-->
 <xsl:template match="variables">
   <variables>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="AllSignalVars"/>
     <xsl:call-template name="AllExtraRuleVar"/>
     <xsl:call-template name="AllRepeatSignalVars"/>
	 <xsl:call-template name="AllTriggerRuleVars"/>
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
