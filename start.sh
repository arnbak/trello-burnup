#!/bin/sh

nohup ./target/universal/stage/bin/trello-burnup -Dconfig.resource=PROD.conf -DapplyDownEvolutions.default=false -Dhttp.port=9990 &

