<?xml version="1.0" encoding="UTF-8"?>
<ts>
    <start>state0||state0</start>
    <states>
        <state id="state3||state3"></state>
        <state id="state1||state1">
            <transition target="state2||state1" action="b"></transition>
            <transition target="state1||state2" action="b*"></transition>
        </state>
        <state id="state1||state2">
            <transition target="state2||state2" action="b"></transition>
        </state>
        <state id="state0||state0">
            <transition target="state1||state1" action="a"></transition>
        </state>
        <state id="state2||state1">
            <transition target="state2||state2" action="b*"></transition>
        </state>
        <state id="state2||state2">
            <transition target="state3||state3" action="c"></transition>
        </state>
    </states>
</ts>