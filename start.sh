#!/bin/sh

nohup ./target/universal/stage/bin/trello-burnup -Dconfig.resource=PROD.conf -Dhttp.port=9990 &

