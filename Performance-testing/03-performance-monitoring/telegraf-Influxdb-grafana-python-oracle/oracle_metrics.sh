#!/bin/env bash

export LD_LIBRARY_PATH=/u01/app/oracle/product/11.2.0/dbhome_1//lib
export ORACLE_HOME=/u01/app/oracle/product/11.2.0/dbhome_1/

python /usr/local/src/tools/oracle_metrics.py -f "influx" "ALL" "-u" "system" "-p" "htsd" "-s" 172.16.14.251:1521/orcl
