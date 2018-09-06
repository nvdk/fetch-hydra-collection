#!/bin/bash
ENDPOINT=`sed -n 1p $1 | xargs`
echo $ENDPOINT > /tmp/fetch.log
exec java -jar /opt/unifiedviews/scripts/fetch-paged-collection.jar --endpoint $ENDPOINT --path $2 >> /tmp/fetch.log 2>>/tmp/fetch.log
