<?xml version="1.0" encoding="UTF-8"?>
<ts>
    <start>State_0</start>
    <states>
        <state id="State_2">
            <transition target="State_8" action="clean"></transition>
        </state>
        <state id="State_20">
            <transition target="State_23" action="charge"></transition>
        </state>
        <state id="State_3">
            <transition target="State_10" action="goAround"></transition>
        </state>
        <state id="State_10">
            <transition target="State_14" action="clean"></transition>
        </state>
        <state id="State_21"></state>
        <state id="State_4">
            <transition target="State_5" action="move_0"></transition>
            <transition target="State_6" action="liDet_0"></transition>
            <transition target="State_7" action="caDet_0"></transition>
        </state>
        <state id="State_11">
            <transition target="State_20" action="clean"></transition>
        </state>
        <state id="State_22"></state>
        <state id="State_5">
            <transition target="State_13" action="clean"></transition>
        </state>
        <state id="State_12"></state>
        <state id="State_23"></state>
        <state id="State_0">
            <transition target="State_1" action="liDet_1"></transition>
            <transition target="State_2" action="move_1"></transition>
            <transition target="State_3" action="caDet_1"></transition>
            <transition target="State_4" action="map"></transition>
        </state>
        <state id="State_1">
            <transition target="State_9" action="goAround"></transition>
        </state>
        <state id="State_6">
            <transition target="State_16" action="goAround"></transition>
        </state>
        <state id="State_7">
            <transition target="State_11" action="goAround"></transition>
        </state>
        <state id="State_8">
            <transition target="State_12" action="charge"></transition>
        </state>
        <state id="State_9">
            <transition target="State_15" action="clean"></transition>
        </state>
        <state id="State_17"></state>
        <state id="State_18">
            <transition target="State_22" action="charge"></transition>
        </state>
        <state id="State_19"></state>
        <state id="State_13">
            <transition target="State_17" action="charge"></transition>
        </state>
        <state id="State_14">
            <transition target="State_19" action="charge"></transition>
        </state>
        <state id="State_15">
            <transition target="State_21" action="charge"></transition>
        </state>
        <state id="State_16">
            <transition target="State_18" action="clean"></transition>
        </state>
    </states>
</ts>