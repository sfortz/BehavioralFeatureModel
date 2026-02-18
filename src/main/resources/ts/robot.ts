<?xml version="1.0" encoding="UTF-8"?>
<ts xmlns:ts="http://www.unamur.be/xml/ts/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<start>state0</start>
	<states>
		<state id="state0">
			<transition action="map" target="state1" />
			<transition action="liDet" target="state2" />
			<transition action="caDet" target="state2" />
			<transition action="move" target="state3" />
		</state>
		<state id="state1">
			<transition action="liDet" target="state2" />
			<transition action="caDet" target="state2" />
			<transition action="move" target="state3" />
		</state>
		<state id="state2">
			<transition action="goAround" target="state3" />
		</state>
		<state id="state3">
			<transition action="move" target="state4" />
		</state>
		<state id="state4">
			<transition action="clean" target="state5" />
		</state>
		<state id="state5">
			<transition action="charge" target="state6" />
		</state>
	</states>
</ts>