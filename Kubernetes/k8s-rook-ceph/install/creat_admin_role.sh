#!/usr/bin/env bash

#创建内置管理员角色的新副本，从该角色中删除 iscsi 权限，然后将此新角色分配给管理员。

ceph dashboard ac-role-create admin-no-iscsi

for scope in dashboard-settings log rgw prometheus grafana nfs-ganesha manager hosts rbd-image config-opt rbd-mirroring cephfs user osd pool monitor; do
ceph dashboard ac-role-add-scope-perms admin-no-iscsi ${scope} create delete read update;
done
ceph dashboard ac-user-set-roles admin admin-no-iscsi
