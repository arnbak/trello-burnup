#!/bin/sh

RUNNING_PID=./target/universal/stage/RUNNING_PID

test -f ${RUNNING_PID} && kill `cat ${RUNNING_PID}` && sleep 5;
