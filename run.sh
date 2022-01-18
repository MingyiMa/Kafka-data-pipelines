#!/bin/bash

#start docker
systemctl status docker || systemctl start docker || sleep 5

#run docker compose
docker-compose -f docker-compose.yml up

