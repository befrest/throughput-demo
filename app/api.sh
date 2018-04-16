#!/usr/bin/env bash

mvn clean compile

if [ -f "$1.$2.pid" ]; then
    kill -9 $(cat "$1.$2.pid")
fi

export MAVEN_OPTS='-Xms128m -Xmx2056m -javaagent:newrelic/newrelic.jar'

nohup mvn exec:java -Dexec.mainClass=rest.bef.demo.API -Dexec.args="$1 $2" 1>$1.$2.out 2>$1.$2.err &
