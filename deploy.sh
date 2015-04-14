#!/bin/bash

REMOTE=lars@arnbak.com
REMOTE_APP=/home/lars/trello-burnup/

sbt stage || exit 1;
rsync -va start.sh stop.sh ${REMOTE}:${REMOTE_APP};
rsync -va target/ ${REMOTE}:${REMOTE_APP}/target;
ssh ${REMOTE} "cd $REMOTE_APP; ./stop.sh";
ssh ${REMOTE} "cd $REMOTE_APP; ./start.sh";