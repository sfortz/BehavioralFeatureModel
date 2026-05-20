<?xml version="1.0" encoding="UTF-8"?>
<ts>
    <start>state0||state0</start>
    <states>
        <state id="state0||state3">
            <transition target="state1||state3" action="a"></transition>
        </state>
        <state id="state0||state2">
            <transition target="state1||state2" action="a"></transition>
            <transition target="state0||state3" action="f"></transition>
        </state>
        <state id="state1||state1">
            <transition target="state2||state1" action="b"></transition>
            <transition target="state1||state2" action="b"></transition>
        </state>
        <state id="state1||state2">
            <transition target="state2||state2" action="b"></transition>
            <transition target="state1||state3" action="f"></transition>
        </state>
        <state id="state1||state3">
            <transition target="state2||state3" action="b"></transition>
        </state>
        <state id="state2||state0">
            <transition target="state3||state0" action="c"></transition>
            <transition target="state2||state1" action="e"></transition>
        </state>
        <state id="state2||state1">
            <transition target="state3||state1" action="c"></transition>
            <transition target="state2||state2" action="b"></transition>
        </state>
        <state id="state2||state2">
            <transition target="state3||state2" action="c"></transition>
            <transition target="state2||state3" action="f"></transition>
        </state>
        <state id="state2||state3">
            <transition target="state3||state3" action="c"></transition>
        </state>
        <state id="state3||state0">
            <transition target="state3||state1" action="e"></transition>
        </state>
        <state id="state3||state2">
            <transition target="state3||state3" action="f"></transition>
        </state>
        <state id="state3||state1">
            <transition target="state3||state2" action="b"></transition>
        </state>
        <state id="state3||state3"></state>
        <state id="state1||state0">
            <transition target="state2||state0" action="b"></transition>
            <transition target="state1||state1" action="e"></transition>
        </state>
        <state id="state0||state1">
            <transition target="state1||state1" action="a"></transition>
            <transition target="state0||state2" action="b"></transition>
        </state>
        <state id="state0||state0">
            <transition target="state1||state0" action="a"></transition>
            <transition target="state0||state1" action="e"></transition>
        </state>
    </states>
</ts>