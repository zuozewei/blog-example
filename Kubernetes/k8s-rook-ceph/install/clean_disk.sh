#!/usr/bin/env bash
rm -rf /var/lib/rook
/dev/mapper/ceph-*
dmsetup ls
dmsetup remove_all
dd if=/dev/zero of=/dev/vda4 bs=512k count=1
wipefs -af /dev/vda4
