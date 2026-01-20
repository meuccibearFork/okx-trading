#!/bin/bash
docker rmi zhuozhuang/okx-trading:latest
mvn clean package -Dmaven.test.skip=true docker:build
docker push zhuozhuang/okx-trading:latest
