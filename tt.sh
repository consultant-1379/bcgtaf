#!/bin/bash
#	This script is intended to replace host parameters.
#	Required parameter 1: The VAPP you wish to run on: tt will not run without this.
#	Optional parameter 2: The suite(s) you wish to run.

if [ -z $1 ]
then
	echo "Please supply a parameter for the VAPP; eg: tt.sh 123";
	exit 1
fi

VAPP=$1
#TEST_ARGS="-Dhost.RV-OSSRC.ip=atvts${VAPP}.athtem.eei.ericsson.se
#			-Dhost.RV-OSSRC.user.nmsadm.pass=nms27511
#			-Dhost.RV-OSSRC.user.nmsadm.type=oper
#			-Dhost.RV-OSSRC.user.root.pass=shroot
#			-Dhost.RV-OSSRC.user.root.type=admin
#			-Dhost.RV-OSSRC.port.ssh=2205
#			-Dhost.RV-OSSRC.type=RC "
#TEST_ARGS="${TEST_ARGS} -Dhost.RV-Netsim.ip=atvts${VAPP}.athtem.eei.ericsson.se
#			-Dhost.RV-Netsim.user.netsim.pass=netsim
#			-Dhost.RV-Netsim.port.ssh=2202
#			-Dhost.RV-Netsim.type=NETSIM"

TEST_ARGS="-Pmaven323 -Dtaf.hosts.gateway=atvts${VAPP}.athtem.eei.ericsson.se"
TEST_ARGS="${TEST_ARGS} -Dsuites=${2}"

if [ $2 ]
then
	TEST_ARGS="${TEST_ARGS} -Dsuites=${2}"
fi

#	If you need to set something specific in the test arguments for any test case, modify and uncomment these lines; eg:
#	if [ $2 == "Particular test suite" ]; then
#	TEST_ARGS="${TEST_ARGS} -Ddataprovider *"; fi
#	echo $TEST_ARGS #checks your arguments are okay, uncomment if you wish

mvn clean
mvn clean install -DskipTests
TESTSUCCESS=$?
if [ $TESTSUCCESS -eq 0 ]
then
	pushd test-pom > /dev/null
	mvn site ${TEST_ARGS}
	TESTSUCCESS=$?
	popd > /dev/null
fi
exit $TESTSUCCESS
