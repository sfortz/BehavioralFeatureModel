<?xml version="1.0" encoding="UTF-8"?>
<ts>
    <start>State_0</start>
    <states>
        <state id="State_2">
            <transition target="State_1" action="clean"></transition>
        </state>
        <state id="State_3">
            <transition target="State_9" action="goAround"></transition>
        </state>
        <state id="State_10">
            <transition target="State_4" action="charge"></transition>
        </state>
        <state id="State_4"></state>
        <state id="State_11">
            <transition target="State_6" action="charge"></transition>
        </state>
        <state id="State_5"></state>
        <state id="State_12">
            <transition target="State_3" action="caDet"></transition>
            <transition target="State_7" action="liDet"></transition>
            <transition target="State_8" action="move"></transition>
        </state>
        <state id="State_0">
            <transition target="State_12" action="map"></transition>
        </state>
        <state id="State_1">
            <transition target="State_5" action="charge"></transition>
        </state>
        <state id="State_6"></state>
        <state id="State_7">
            <transition target="State_2" action="goAround"></transition>
        </state>
        <state id="State_8">
            <transition target="State_10" action="clean"></transition>
        </state>
        <state id="State_9">
            <transition target="State_11" action="clean"></transition>
        </state>
    </states>
</ts>