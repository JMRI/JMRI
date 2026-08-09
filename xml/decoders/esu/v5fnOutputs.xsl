<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:xi="http://www.w3.org/2001/XInclude">

    <!-- Copyright (C) JMRI 2026 All rights reserved -->
    
    <xsl:output method="xml" encoding="utf-8" indent="yes"/>

    <!-- Template for one function output -->

    <!-- Available output features are:

         "noSP": Output does not have the option called "Servo Power"
         "sO": Output options called "Servo output",
               "Servo output Steam engine Johnson Bar Control" and "Servo output Pantograph bouncing"
         "eS": Output option called "External controlled smoke unit"
         "sC": Output option called "Sound controlled smoke unit"
         "pa": Output option called "Pantograph"
         "rC": Output option called "Roco coupler"
         "eC": Output option called "ESU coupler"

         All other output options are the most common among all LokPilot 5 and LokSound 5 decoders.

         Determined from decoder manuals and LokProgrammer 5.2.18 -->
    
    <xsl:template name="functionOutput">
        <xsl:param name="outputLabel"/>
        <xsl:param name="outputShort"/>
        <xsl:param name="CVbase"/>
        <xsl:param name="features"/>

        <!-- all CVs mentioned in the following comments are the ones for output HL-1 -->
        <!-- output mode CV 16.0.259 -->
        <variable label="{$outputLabel} Mode" CV="16.0.{$CVbase}" item="ESU FnOut {$outputShort} Mode">
            <enumVal xmlns:xi="http://www.w3.org/2001/XInclude"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder-4-15-2.xsd">
                <enumChoice value="0">
                    <choice>(Disabled)</choice>
                    <choice xml:lang="it">Disabile</choice>
                    <choice xml:lang="de">Deaktiviert</choice>
                    <choice xml:lang="ca">Desactivat</choice>
                </enumChoice>
                <enumChoice value="1">
                    <choice>Dimmable headlight</choice>
                    <choice xml:lang="it">Luce di testa regolabile</choice>
                    <choice xml:lang="de">Dimmbares Licht</choice>
                    <choice xml:lang="ca">Intensitat de lluns</choice>
                </enumChoice>
                <enumChoice value="2">
                    <choice>Dimmable headlight (fade in/out)</choice>
                    <choice xml:lang="it">Luce di testa regolabile (sfumata in/out)</choice>
                    <choice xml:lang="de">Dimmbares Licht (Ein- und Ausblenden)</choice>
                    <choice xml:lang="ca">Intensitat llum frontal</choice>
                </enumChoice>
                <enumChoice value="3">
                    <choice>Firebox</choice>
                    <choice xml:lang="it">Fuoco Caldaia</choice>
                    <choice xml:lang="de">Feuerbüchse</choice>
                    <choice xml:lang="ca">Caixa de focs</choice>
                </enumChoice>
                <enumChoice value="4">
                    <choice>Smart firebox</choice>
                    <choice xml:lang="it">Caldaia intelligente</choice>
                    <choice xml:lang="de">Intelligente Feuerbüchse</choice>
                </enumChoice>
                <enumChoice value="5">
                    <choice>Single strobe</choice>
                    <choice xml:lang="it">Singolo Strobe</choice>
                    <choice xml:lang="ca">Estroboscòpica simple</choice>
                </enumChoice>
                <enumChoice value="6">
                    <choice>Double strobe</choice>
                    <choice xml:lang="it">Doppio Strobe</choice>
                </enumChoice>
                <enumChoice value="7">
                    <choice>Rotary beacon</choice>
                    <choice xml:lang="it">Lampeggiante rotante</choice>
                    <choice xml:lang="ca">Balisa rotatòria</choice>
                </enumChoice>
                <enumChoice value="8">
                    <choice>Strato light</choice>
                    <choice xml:lang="it">Luce Strato</choice>
                    <choice xml:lang="ca">Llum de carrer</choice>
                </enumChoice>
                <enumChoice value="9">
                    <choice>Ditch light type 1</choice>
                    <choice xml:lang="it">Luce Ditch tipo 1</choice>
                    <choice xml:lang="ca">Llum Ditch tipus 1</choice>
                </enumChoice>
                <enumChoice value="10">
                    <choice>Ditch light type 2</choice>
                    <choice xml:lang="it">Luce Ditch tipo 2</choice>
                    <choice xml:lang="ca">Llum Ditch tupus 2</choice>
                </enumChoice>
                <enumChoice value="11">
                    <choice>Oscillating headlight</choice>
                    <choice xml:lang="it">Luce di testa Oscillante</choice>
                    <choice xml:lang="ca">Llum de testera oscil·lant</choice>
                </enumChoice>
                <enumChoice value="12">
                    <choice>Flash light</choice>
                    <choice xml:lang="it">Luce a Flash</choice>
                    <choice xml:lang="de">Blinklicht</choice>
                    <choice xml:lang="ca">Llum intermitent</choice>
                </enumChoice>
                <enumChoice value="13">
                    <choice>Mars light</choice>
                    <choice xml:lang="it">Luce Mars</choice>
                    <choice xml:lang="ca">Llums Mars</choice>
                </enumChoice>
                <enumChoice value="14">
                    <choice>Gyra light</choice>
                    <choice xml:lang="it">Gyra light rotante</choice>
                    <choice xml:lang="ca">Llum Gyra Rotatòria</choice>
                </enumChoice>
                <enumChoice value="15">
                    <choice>End of train flasher</choice>
                    <choice xml:lang="it">Lampeggiante Fine Treno (FRED)</choice>
                    <choice xml:lang="ca">Llum de final de tren intermitent</choice>
                </enumChoice>
                <enumChoice value="16">
                    <choice>Neon light</choice>
                    <choice xml:lang="it">Luce Fluorescente</choice>
                    <choice xml:lang="de">Neonlicht</choice>
                    <choice xml:lang="ca">Llum fluorescent</choice>
                </enumChoice>
                <enumChoice value="17">
                    <choice>Low-energy light</choice>
                    <choice xml:lang="it">Luce a risparmio Energetico</choice>
                    <choice xml:lang="de">Energiesparlampe</choice>
                    <choice xml:lang="ca">Llum estalvi energia</choice>
                </enumChoice>
                <enumChoice value="18">
                    <choice>Single Strobe random</choice>
                </enumChoice>
                <enumChoice value="19">
                    <choice>Brake Light</choice>
                </enumChoice>
                <enumChoice value="20">
                    <choice>16 2/3 Hz flickering</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'eC')">
                    <enumChoice value="21">
                        <choice>ESU coupler</choice>
                    </enumChoice>
                </xsl:if>
                <xsl:if test="contains($features, 'sC')">
                    <enumChoice value="22">
                        <choice>Sound controlled smoke unit</choice>
                        <choice xml:lang="it">Gener.Fumo sonoro</choice>
                        <choice xml:lang="de">Raucheinheit (Soundgesteuert)</choice>
                        <choice xml:lang="ca">Fumigen controlat per llum</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="23">
                    <choice>Ventilator</choice>
                    <choice xml:lang="it">Controllo Ventole</choice>
                    <choice xml:lang="de">Ventilator</choice>
                    <choice xml:lang="ca">Ventilador</choice>
                </enumChoice>
                <enumChoice value="24">
                    <choice>Seuthe smoke unit</choice>
                    <choice xml:lang="it">Gener.Fumo Seuthe</choice>
                    <choice xml:lang="de">Seuthe Rauchgenerator</choice>
                    <choice xml:lang="ca">Fumigen Seuthe</choice>
                </enumChoice>
                <enumChoice value="25">
                    <choice>Trigger smoke chuff</choice>
                    <choice xml:lang="it">Chuff fumo Trigger</choice>
                    <choice xml:lang="de">Dampfstoß-Trigger</choice>
                    <choice xml:lang="ca">Disparador del chuff del fumigen</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'eS')">
                    <enumChoice value="26">
                        <choice>External controlled smoke unit</choice>
                        <choice xml:lang="de">externer Rauchgenerator</choice>
                    </enumChoice>
                </xsl:if>
                <xsl:if test="contains($features, 'sO')">
                    <enumChoice value="27">
                        <choice>Servo output</choice>
                        <choice xml:lang="it">Servo</choice>
                        <choice xml:lang="de">Servoausgang</choice>
                        <choice xml:lang="ca">Servo</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="28">
                    <choice>Coupler</choice>
                    <choice xml:lang="it">Gancio</choice>
                    <choice xml:lang="de">Kupplung</choice>
                    <choice xml:lang="ca">Enganxall</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'rC')">
                    <enumChoice value="29">
                        <choice>Roco coupler</choice>
                        <choice xml:lang="it">Gancio Roco</choice>
                        <choice xml:lang="de">Roco Kupplung</choice>
                        <choice xml:lang="ca">Enganxall Roco</choice>
                    </enumChoice>
                </xsl:if>
                <xsl:if test="contains($features, 'pa')">
                    <enumChoice value="30">
                        <choice>Pantograph</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="31">
                    <choice>PowerPack control</choice>
                </enumChoice>
                <xsl:if test="not(contains($features, 'noSP'))">
                    <enumChoice value="32">
                        <choice>Servo Power</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="33">
                    <choice>Autocoupler coil#2</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'sO')">
                    <enumChoice value="34">
                        <choice>Servo output Steam engine Johnson Bar Control</choice>
                        <choice xml:lang="de">Servo Dampflok Umsteuerung</choice>
                    </enumChoice>
                </xsl:if>
                <enumChoice value="35">
                    <choice>Trigger smoke chuff (Edge Toggle)</choice>
                </enumChoice>
                <xsl:if test="contains($features, 'sO')">
                    <enumChoice value="36">
                        <choice>Servo output Pantograph bouncing</choice>
                    </enumChoice>
                </xsl:if>
            </enumVal>
        </variable>

        <!-- CV 16.0.260 -->
        <variables>
	    <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>gt</relation>
                <value>0</value>
            </qualifier>
            <variable label="Function Switch On Delay" CV="16.0.{$CVbase+1}" default="0" item="ESU FnOut {$outputShort} Slider 1" tooltip="Units = 0.4096 sec" mask="XXXXVVVV">
                <decVal max="15"/>
                <label xml:lang="de">Verzögerung beim Einschalten</label>
            </variable>
            <variable label="Function Switch Off Delay" CV="16.0.{$CVbase+1}" default="0" item="ESU FnOut {$outputShort} Slider 2" tooltip="Units = 0.4096 sec" mask="VVVVXXXX">
w               <decVal max="15"/>
                <label xml:lang="de">Verzögerung beim Ausschalten</label>
            </variable>
        </variables>

        <!-- CV 16.0.261 -->
        <variable label="Function Auto Switch Off" CV="16.0.{$CVbase+2}" default="0" item="ESU FnOut {$outputShort} Slider 3" tooltip="Units = 0.4096 sec, 0 = disabled">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>gt</relation>
                <value>0</value>
            </qualifier>
            <decVal/>
            <label xml:lang="de">Ausgang automatisch Ausschalten</label>
        </variable>

        <!-- CV 16.0.262 -->
        <variable item="Brightness CV{$CVbase+3}" label="Brightness CV" CV="16.0.{$CVbase+3}" default="31" comment="Dummy to work around sheet operations/qualifiers issue">
            <decVal/>
        </variable>        

        <variable label="Brightness" CV="16.0.{$CVbase+3}" default="31" item="ESU FnOut {$outputShort} Slider 5" mask="XXXVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>gt</relation>
                <value>0</value>
            </qualifier>
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>le</relation>
                <value>20</value>
            </qualifier>
            <decVal max="31"/>
            <label xml:lang="de">Helligkeit</label>
        </variable>

        <!-- Mode 21 - ESU coupler - has no CV 16.0.262 -->
        
        <variable label="Mode" CV="16.0.{$CVbase+3}" item="ESU FnOut {$outputShort} Option 1">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>22</value>
            </qualifier>
            <enumVal>
                <enumChoice value="30">
                    <choice>Heating control</choice>
                    <choice xml:lang="de">Heizungssteuerung</choice>
                </enumChoice>
                <enumChoice value="31">
                    <choice>Fan control</choice>
                    <choice xml:lang="de">Lüftersteuerung</choice>
                </enumChoice>
            </enumVal>
            <label xml:lang="de">Modus</label>
        </variable>
        
        <variable label="Fan Speed" CV="16.0.{$CVbase+3}" default="31" item="ESU FnOut {$outputShort} Slider 11" mask="XXXVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>23</value>
            </qualifier>
            <decVal max="31"/>
            <label xml:lang="de">Geschwindigkeit</label>
        </variable>
        
        <variable label="Standing heat" CV="16.0.{$CVbase+3}" default="31" item="ESU FnOut {$outputShort} Slider 14" mask="XXXVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>24</value>
            </qualifier>
            <decVal max="31"/>
            <label xml:lang="de">Heizstufe im Stand</label>
        </variable>

        <variable label="Chuff power" CV="16.0.{$CVbase+3}" default="31" item="ESU FnOut {$outputShort} Slider 8" mask="XXXVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>25</value>
            </qualifier>
            <decVal max="31"/>
            <label xml:lang="de">Dampfstoßstärke</label>
        </variable>

        <variable label="Type of smoke unit:" CV="16.0.{$CVbase+3}" item="ESU FnOut {$outputShort} Option 2">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>26</value>
            </qualifier>
            <enumVal>
                <enumChoice value="0">
                    <choice>KM1 BR 41 / BR 44</choice>
                </enumChoice>
                <enumChoice value="1">
                    <choice>KM1 (other)</choice>
                </enumChoice>
                <enumChoice value="5">
                    <choice>KM1 BR 98.3</choice>
                </enumChoice>
                <enumChoice value="2">
                    <choice>KISS</choice>
                </enumChoice>
                <enumChoice value="3">
                    <choice>ESU Smoke Unit (Gauge 0, G)</choice>
                </enumChoice>
            </enumVal>
        </variable>

        <variable label="Duration (speed) B" CV="16.0.{$CVbase+3}" default="31" item="ESU FnOut {$outputShort} Slider 17" mask="XXVVVVVV" tooltip="Units = 0.25 sec">>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ge</relation>
            <value>27</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>28</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>29</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>30</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>31</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>32</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>33</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>le</relation>
            <value>34</value>
        </qualifier>
        <decVal max="63"/>
        </variable>

        <variable label="Coupler Force" CV="16.0.{$CVbase+3}" default="31" item="ESU FnOut {$outputShort} Slider 7" mask="XXXVVVVV">
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ge</relation>
            <value>28</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>29</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>30</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>31</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>ne</relation>
            <value>32</value>
        </qualifier>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>le</relation>
            <value>33</value>
        </qualifier>
        <decVal max="31"/>
        <label xml:lang="de">Stärke des Kupplers</label>
        </variable>

        <!-- Mode 29 - Roco Coupler - does not have CV 16.0.262 -->

        <variable label="Pantograph height" CV="16.0.{$CVbase+3}" item="ESU FnOut {$outputShort} Slider 4" mask="XXXXVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>30</value>
            </qualifier>
            <decVal max="15"/>
        </variable>

        <!-- Mode 31 and 32 - Power Pack Control and Servo Power - don't have CV 16.0.262 -->
        <!-- Mode 33 - Autocoupler coil #2 - CV 16.0.262 is the same as Mode 28 - Coupler -->
        <!-- Mode 34 - Servo output Steam engine Johnson Bar Control - is the same as Mode 27 - Servo output -->
        <!-- Mode 35 - Trigger smoke chuff "Edge Toggle" - does not have CV 16.0.262 -->

        <variable label="Duration (speed)" CV="16.0.{$CVbase+3}" default="31" item="ESU FnOut {$outputShort} Slider 18" mask="XXVVVVVV" tooltip="Units = 0.25 sec">>
        <qualifier>
            <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
            <relation>eq</relation>
            <value>36</value>
        </qualifier>
        <decVal max="63"/>
        </variable>        
        
        <!-- CV 16.0.263 -->
        <variable item="Special Function CV{$CVbase+4}" label="Special Function CV 1" CV="16.0.{$CVbase+4}" default="0" comment="Dummy to work around sheet operations/qualifiers issue">
            <decVal/>
        </variable>
        <variable label="Fan Acceleration rate" CV="16.0.{$CVbase+4}" default="0" item="ESU FnOut {$outputShort} Slider 12" mask="XXXVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>23</value>
            </qualifier>
            <decVal max="31"/>
            <label xml:lang="de">Beschleunigungszeit</label>
        </variable>

        <variable label="Minimum driving heat" CV="16.0.{$CVbase+4}" default="0" item="ESU FnOut {$outputShort} Slider 15" mask="XXXVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>24</value>
            </qualifier>
            <decVal max="31"/>
            <label xml:lang="de">Minimale Heizstufe bei Fahrt</label>
        </variable>

        <variable label="Fan power" CV="16.0.{$CVbase+4}" default="0" item="ESU FnOut {$outputShort} Slider 9" mask="XXXVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>25</value>
            </qualifier>
            <decVal max="31"/>
            <label xml:lang="de">Stärke des Bläsers</label>
        </variable>

        <variable label="Phase Reverse" CV="16.0.{$CVbase+4}" default="0" item="ESU FnOut {$outputShort} Check 6" mask="XXXXXXXV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>gt</relation>
                <value>0</value>
            </qualifier>
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>le</relation>
                <value>15</value>
            </qualifier>
            <xsl:call-template name="enum-OffOn" />
            <label xml:lang="de">Phase tauschen</label>
        </variable>

        <variable label="Grade Crossing" CV="16.0.{$CVbase+4}" default="0" item="ESU FnOut {$outputShort} Check 1" mask="XXXXXXVX">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>gt</relation>
                <value>0</value>
            </qualifier>
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>le</relation>
                <value>15</value>
            </qualifier>
            <xsl:call-template name="enum-OffOn" />
        </variable>
        <variable label="Rule 17 Fwd" CV="16.0.{$CVbase+4}" default="0" item="ESU FnOut {$outputShort} Check 2" mask="XXXXXVXX">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>gt</relation>
                <value>0</value>
            </qualifier>
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>le</relation>
                <value>15</value>
            </qualifier>
            <xsl:call-template name="enum-OffOn" />
            <label xml:lang="de">Rule 17 vorwärts</label>
        </variable>
        <variable label="Rule 17 Rev" CV="16.0.{$CVbase+4}" default="0" item="ESU FnOut {$outputShort} Check 3" mask="XXXXVXXX">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>gt</relation>
                <value>0</value>
            </qualifier>
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>le</relation>
                <value>15</value>
            </qualifier>
            <xsl:call-template name="enum-OffOn" />
            <label xml:lang="de">Rule 17 rückwärts</label>
        </variable>
        <variable label="Dimmer" CV="16.0.{$CVbase+4}" default="0" item="ESU FnOut {$outputShort} Check 4" mask="XXXVXXXX">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>gt</relation>
                <value>0</value>
            </qualifier>
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>le</relation>
                <value>15</value>
            </qualifier>
            <xsl:call-template name="enum-OffOn" />
            <label xml:lang="de">Abdimmen</label>
        </variable>
        <variable label="LED Mode" CV="16.0.{$CVbase+4}" default="0" item="ESU FnOut {$outputShort} Check 5" mask="VXXXXXXX">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>gt</relation>
                <value>0</value>
            </qualifier>
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>le</relation>
                <value>15</value>
            </qualifier>
            <xsl:call-template name="enum-OffOn" />
            <label xml:lang="de">LED Modus</label>
        </variable>

        <!-- CV 16.0.264 -->
        <variable item="Special Function CV{$CVbase+5}" label="Special Function CV 2" CV="16.0.{$CVbase+5}" default="0" comment="Dummy to work around sheet operations/qualifiers issue">
            <decVal/>
        </variable>
        <variable label="Fan Decceleration rate" CV="16.0.{$CVbase+5}" default="0" item="ESU FnOut {$outputShort} Slider 13" mask="XXXVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>23</value>
            </qualifier>
            <decVal max="31"/>
            <label xml:lang="de">Bremszeit</label>
        </variable>
        <variable label="Maximum driving heat" CV="16.0.{$CVbase+5}" default="0" item="ESU FnOut {$outputShort} Slider 16" mask="XXXVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>24</value>
            </qualifier>
            <decVal max="31"/>
            <label xml:lang="de">Maximale Heizstufe bei Fahrt</label>
        </variable>
        <variable label="Timeout" CV="16.0.{$CVbase+5}" default="0" item="ESU FnOut {$outputShort} Slider 10" tooltip="Units = 0.25 sec">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>25</value>
            </qualifier>
            <decVal/>
        </variable>
        <variable label="Startup Time" CV="16.0.{$CVbase+5}" default="0" item="ESU FnOut {$outputShort} Slider 6">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>ge</relation>
                <value>16</value>
            </qualifier>
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>le</relation>
                <value>17</value>
            </qualifier>
            <decVal/>
            <label xml:lang="de">Startzeit</label>
        </variable>
        <variable label="Level" CV="16.0.{$CVbase+5}" default="0" item="ESU FnOut {$outputShort} Slider 20" mask="XVVVVVVV">
            <qualifier>
                <variableref>ESU FnOut <xsl:value-of select="$outputShort" /> Mode</variableref>
                <relation>eq</relation>
                <value>19</value>
            </qualifier>
            <decVal max="127"/>
        </variable>

        
    </xsl:template>

    <!-- Create function output CV settings for each decoder defined in the xml file -->
    <!-- install new variables at end of variables element-->
    <xsl:template match="variables">
        <variables>
            <xsl:copy-of select="node()"/>

            <xsl:for-each select="//decoder-config/pane[name/text() = '__DecoderFnDefs']/column/display">
                <xsl:variable name="decoders" select="string(@item)"/>
                <variables include="{$decoders}">
                    <xsl:for-each select="label">
                        <xsl:variable name="features" select="."/>
                        <xsl:variable name="index" select="position()"/>
                        <xsl:variable name="outputName" select="(//decoder-config/pane[name/text() = '__functionCommonDefs']/column/display[@item='outputNames']/label)[$index]"/>
                        <xsl:variable name="outputLabel" select="(//decoder-config/pane[name/text() = '__functionCommonDefs']/column/display[@item='outputLabel']/label)[$index]"/>
                        <xsl:variable name="baseCV" select="(//decoder-config/pane[name/text() = '__functionCommonDefs']/column/display[@item='baseCV']/label)[$index]"/>
                        <xsl:if test="$features != 'N'">
                            <xsl:call-template name="functionOutput">
                                <xsl:with-param name="outputLabel" select="$outputName"/>
                                <xsl:with-param name="outputShort" select="$outputLabel"/>
                                <xsl:with-param name="CVbase" select="$baseCV"/>
                                <xsl:with-param name="features" select="$features"/>
                            </xsl:call-template>
                        </xsl:if>
                    </xsl:for-each>
                </variables>
            </xsl:for-each>
        </variables>

    </xsl:template>

    <!-- Autocreate pane for function outputs -->
    <xsl:template match="pane[1]">
        <pane include="never">
            <xsl:copy-of select="node()"/>
        </pane>
        <pane xmlns:xi="http://www.w3.org/2001/XInclude"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder-4-15-2.xsd">
            <name>Function Outputs</name>
            <name xml:lang="de">Funktionsausgänge</name>
            <column>
                <separator/>
                <xsl:for-each select="//decoder-config/pane[name/text() = '__functionCommonDefs']/column/display[@item='outputLabel']/label">
                    <xsl:variable name="outputLabel" select="."/>
                    <grid gridx="0" gridy="NEXT" weightx="1" anchor="LINE_END">
                        <griditem insets="2,0,2,0">
                            <display item="ESU FnOut {$outputLabel} Mode"/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 1"/>
                            <display item="ESU FnOut {$outputLabel} Slider 1" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 2"/>
                            <display item="ESU FnOut {$outputLabel} Slider 2" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 3"/>
                            <display item="ESU FnOut {$outputLabel} Slider 3" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 4"/>
                            <display item="ESU FnOut {$outputLabel} Slider 4" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Option 1"/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Option 2"/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 5"/>
                            <display item="ESU FnOut {$outputLabel} Slider 5" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 6"/>
                            <display item="ESU FnOut {$outputLabel} Slider 6" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 7"/>
                            <display item="ESU FnOut {$outputLabel} Slider 7" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 8"/>
                            <display item="ESU FnOut {$outputLabel} Slider 8" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 9"/>
                            <display item="ESU FnOut {$outputLabel} Slider 9" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 10"/>
                            <display item="ESU FnOut {$outputLabel} Slider 10" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 11"/>
                            <display item="ESU FnOut {$outputLabel} Slider 11" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 12"/>
                            <display item="ESU FnOut {$outputLabel} Slider 12" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 13"/>
                            <display item="ESU FnOut {$outputLabel} Slider 13" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 14"/>
                            <display item="ESU FnOut {$outputLabel} Slider 14" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 15"/>
                            <display item="ESU FnOut {$outputLabel} Slider 15" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 16"/>
                            <display item="ESU FnOut {$outputLabel} Slider 16" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 17"/>
                            <display item="ESU FnOut {$outputLabel} Slider 17" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 18"/>
                            <display item="ESU FnOut {$outputLabel} Slider 18" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 19"/>
                            <display item="ESU FnOut {$outputLabel} Slider 19" format="hslider" label=""/>
                        </griditem>
                        <griditem>
                            <display item="ESU FnOut {$outputLabel} Slider 20"/>
                            <display item="ESU FnOut {$outputLabel} Slider 20" format="hslider" label=""/>
                        </griditem>
                        <griditem insets="2,0,2,0">
                            <display item="ESU FnOut {$outputLabel} Check 1" layout="above" format="checkbox"/>
                            <separator/>
                            <display item="ESU FnOut {$outputLabel} Check 2" layout="above" format="checkbox"/>
                            <separator/>
                            <display item="ESU FnOut {$outputLabel} Check 3" layout="above" format="checkbox"/>
                            <separator/>
                            <display item="ESU FnOut {$outputLabel} Check 4" layout="above" format="checkbox"/>
                            <separator/>
                            <display item="ESU FnOut {$outputLabel} Check 5" layout="above" format="checkbox"/>
                            <separator/>
                            <display item="ESU FnOut {$outputLabel} Check 6" layout="above" format="checkbox"/>
                        </griditem>
                    </grid>
                    <separator/>
                </xsl:for-each>
            </column>
        </pane>
    </xsl:template>
    
    <!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- helper functions to reuse -->
    <!-- Replacement for the enum-OffOn.xml included in multiple places
         Can't be <xi:include>'d in xsl stylesheets because the path to the
         xsl stylesheet in the enum-OffOn.xml is relative -->

    <xsl:template name="enum-OffOn">
        <enumVal xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder-4-15-2.xsd">
            <enumChoice choice="Off">
                <choice>Off</choice>
                <choice xml:lang="it">Off</choice>
                <choice xml:lang="fr">Éteint</choice>
                <choice xml:lang="de">Aus</choice>
                <choice xml:lang="es">De</choice>
                <choice xml:lang="cs">Vypnuto</choice>
                <choice xml:lang="nl">Uit</choice>
            </enumChoice>
            <enumChoice choice="On">
                <choice>On</choice>
                <choice xml:lang="it">On</choice>
                <choice xml:lang="fr">Allumé</choice>
                <choice xml:lang="de">Ein</choice>
                <choice xml:lang="es">En</choice>
                <choice xml:lang="cs">Zapnuto</choice>
                <choice xml:lang="nl">Aan</choice>
            </enumChoice>
        </enumVal>
    </xsl:template>
    
</xsl:stylesheet>
