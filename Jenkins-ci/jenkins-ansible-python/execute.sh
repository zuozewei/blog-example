#!/usr/bin/env bash
echo $Branch
echo $Hosts
echo $Package_Name

ansible-playbook /home/ansible/playbooks/serve-deploy.yaml --extra-vars "package_name=$Package_Name branch=$Branch target=$Hosts"