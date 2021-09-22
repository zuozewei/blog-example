set oracle_sid=orcl
set nls_lang=simplified chinese_china.zhs16gbk
md D:\rmanbackup\oracle10g\log\%date:~0,4%%date:~5,2%%date:~8,2%
cd /d D:\oracle\product\10.2.0\db_1\bin
rman cmdfile=D:\Rmanbackup\oracle10g\数据库备份脚本\inc0.txt >> D:\rmanbackup\oracle10g\log\%date:~0,4%%date:~5,2%%date:~8,2%\%date:~0,4%%date:~5,2%%date:~8,2%.log