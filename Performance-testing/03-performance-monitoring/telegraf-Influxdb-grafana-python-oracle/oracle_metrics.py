import socket,argparse,subprocess,re,cx_Oracle

fqdn = socket.getfqdn()

class OraStats():

    def __init__(self, user, passwd, sid):
        self.user = user
        self.passwd = passwd
        self.sid = sid
        self.delengine = "none"
        connstr=self.user+'/'+self.passwd+'@'+self.sid
        self.connection = cx_Oracle.connect(connstr)
        cursor = self.connection.cursor()
        cursor.execute("select distinct(SVRNAME)  from v$dnfs_servers")
        rows = cursor.fetchall()

        for i in range(0, cursor.rowcount):
            self.dengine_ip = rows[i][0]
            proc = subprocess.Popen(["nslookup", self.dengine_ip], stdout=subprocess.PIPE)
            lookupresult = proc.communicate()[0].split('\n')

            for line in lookupresult:
                if 'name=' in re.sub(r'\s', '', line):
                    self.delengine = re.sub('\..*$', '', re.sub(r'^.*name=', '', re.sub(r'\s', '', re.sub(r'.$', '', line))))

    # 等待类别
    def waitclassstats(self, user, passwd, sid, format):
        cursor = self.connection.cursor()
        cursor.execute("""
        select n.wait_class, round(m.time_waited/m.INTSIZE_CSEC,3) AAS
        from   v$waitclassmetric  m, v$system_wait_class n
        where m.wait_class_id=n.wait_class_id and n.wait_class != 'Idle'
        union
        select  'CPU', round(value/100,3) AAS
        from v$sysmetric where metric_name='CPU Usage Per Sec' and group_id=2
        union select 'CPU_OS', round((prcnt.busy*parameter.cpu_count)/100,3) - aas.cpu
        from
        ( select value busy
        from v$sysmetric
        where metric_name='Host CPU Utilization (%)'
         and group_id=2 ) prcnt,
        ( select value cpu_count from v$parameter where name='cpu_count' )  parameter,
        ( select  'CPU', round(value/100,3) cpu from v$sysmetric where metric_name='CPU Usage Per Sec' and group_id=2) aas
        """)
        for wait in cursor:
            wait_name = wait[0]
            wait_value = wait[1]
            print ("oracle_wait_class,fqdn={0},delphix={1},db={2},wait_class={3} wait_value={4}".format(fqdn, self.delengine, sid, re.sub(' ', '_', wait_name), wait_value))

    # 系统指标
    def sysmetrics(self, user, passwd, sid, format):
        cursor = self.connection.cursor()
        cursor.execute("""
        select METRIC_NAME,VALUE,METRIC_UNIT from v$sysmetric where group_id=2
        """)
        for metric in cursor:
            metric_name = metric[0]
            metric_value = metric[1]
            print ("oracle_sysmetric,fqdn={0},delphix={1},db={2},metric_name={3} metric_value={4}".format(fqdn,self.delengine,sid,re.sub(' ', '_', metric_name),metric_value))

    # 在闪回恢复区中有关磁盘配额和当前磁盘使用情况
    def fraused(self, user, passwd, sid, format):
        cursor = self.connection.cursor()
        cursor.execute("""
        select round((SPACE_USED-SPACE_RECLAIMABLE)*100/SPACE_LIMIT,1) from  V$RECOVERY_FILE_DEST
        """)
        for frau in cursor:
            fra_used = frau[0]
            print ("oracle_fra_pctused,fqdn={0},delphix={1},db={2} fra_pctused={3}".format(fqdn,self.delengine,sid,fra_used))

    # 磁盘使用状态
    def fsused(self):
     fss = ['/oracle', '/data']
     for fs in fss:
            df = subprocess.Popen(["df","-P",fs], stdout=subprocess.PIPE)
            output = df.communicate()[0]
            total = re.sub('%','',output.split("\n")[1].split()[1])
            used = re.sub('%','',output.split("\n")[1].split()[2])
            pctused = re.sub('%','',output.split("\n")[1].split()[4])
            print("oracle_fs_pctused,fqdn={0},fs_name={1} oraclefs_pctused={2},oraclefs_alloc={3},oraclefs_used={4}".format(fqdn,fs,pctused,total,used))

    # 等待状态
    def waitstats(self, user, passwd, sid, format):
        cursor = self.connection.cursor()
        cursor.execute("""
        select /*+ ordered use_hash(n) */
        n.wait_class wait_class,
        n.name wait_name,
        m.wait_count  cnt,
        nvl(round(10*m.time_waited/nullif(m.wait_count,0),3) ,0) avg_ms
        from v$eventmetric m,
        v$event_name n
        where m.event_id=n.event_id
        and n.wait_class <> 'Idle' and m.wait_count > 0 order by 1""")
        for wait in cursor:
            wait_class = wait[0]
            wait_name = wait[1]
            wait_cnt = wait[2]
            wait_avgms = wait[3]
            print ("oracle_wait_event,fqdn={0},delphix={1},db={2},wait_class={3},wait_event={4} count={5},latency={6}".format(fqdn, self.delengine,sid,re.sub(' ', '_', wait_class), re.sub(' ','_',wait_name),wait_cnt,wait_avgms))

    # 表空间使用状态
    def tbsstats(self, user, passwd, sid, format):
        cursor = self.connection.cursor()
        cursor.execute("""
        select /*+ ordered */ tablespace_name,
            round(used_space),
            round(max_size-used_space) free_space,
            round(max_size),
            round(used_space*100/max_size,2) percent_used
            from (
                select m.tablespace_name,
                m.used_space*t.block_size/1024/1024 used_space,
                (case when t.bigfile='YES' then power(2,32)*t.block_size/1024/1024
                        else tablespace_size*t.block_size/1024/1024 end) max_size
            from dba_tablespace_usage_metrics m, dba_tablespaces t
        where m.tablespace_name=t.tablespace_name)
        """)
        for tbs in cursor:
            tbs_name = tbs[0]
            used_space_mb = tbs[1]
            free_space_mb = tbs[2]
            max_size_mb = tbs[3]
            percent_used = tbs[4]
            print ("oracle_tablespaces,fqdn={0},delphix={1},db={2},tbs_name={3} used_space_mb={4},free_space_mb={5},percent_used={6},max_size_mb={7}".format(fqdn, self.delengine, sid, re.sub(' ', '_', tbs_name), used_space_mb,free_space_mb,percent_used,max_size_mb))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('-f', '--format', help="Output format, default influx", choices=['kafka', 'influx'], default='influx')
    subparsers = parser.add_subparsers(dest='stat')
    parser_all = subparsers.add_parser('ALL', help="Get all database stats")
    parser_all.add_argument('-u', '--user', help="Username with sys views grant", required=True)
    parser_all.add_argument('-p', '--passwd', required=True)
    parser_all.add_argument('-s', '--sid', help="tnsnames SID to connect", required=True)

    args = parser.parse_args()

    if args.stat == "ALL":
        stats = OraStats(args.user, args.passwd, args.sid)
        stats.waitclassstats(args.user, args.passwd, args.sid, args.format)
        stats.waitstats(args.user, args.passwd, args.sid, args.format)
        stats.sysmetrics(args.user, args.passwd, args.sid, args.format)
        stats.tbsstats(args.user, args.passwd, args.sid, args.format)
        stats.fraused(args.user, args.passwd, args.sid, args.format)
        stats.fsused()
