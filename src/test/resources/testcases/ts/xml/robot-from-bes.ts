<?xml version="1.0" encoding="UTF-8"?>
<ts>
    <start>State_0</start>
    <states>
        <state id="State_2">
            <transition target="State_7" action="goAround"></transition>
        </state>
        <state id="State_3">
            <transition target="State_5" action="clean"></transition>
        </state>
        <state id="State_10"></state>
        <state id="State_4">
            <transition target="State_6" action="goAround"></transition>
        </state>
        <state id="State_11"></state>
        <state id="State_5">
            <transition target="State_10" action="charge"></transition>
        </state>
        <state id="State_12"></state>
        <state id="State_0">
            <transition target="State_1" action="map"></transition>
        </state>
        <state id="State_1">
            <transition target="State_2" action="caDet"></transition>
            <transition target="State_3" action="move"></transition>
            <transition target="State_4" action="liDet"></transition>
        </state>
        <state id="State_6">
            <transition target="State_8" action="clean"></transition>
        </state>
        <state id="State_7">
            <transition target="State_9" action="clean"></transition>
        </state>
        <state id="State_8">
            <transition target="State_11" action="charge"></transition>
        </state>
        <state id="State_9">
            <transition target="State_12" action="charge"></transition>
        </state>
    </states>
</ts>