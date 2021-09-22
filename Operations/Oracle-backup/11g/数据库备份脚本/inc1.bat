set oracle_sid=visu11g
set nls_lang=simplified chinese_china.zhs16gbk
md D:\rmanbackup\oracle11g\log\%date:~0,4%%date:~5,2%%date:~8,2%
cd /d D:\oracle11\product\11.2.0\dbhome_1\BIN
rman cmdfile=D:\Rmanbackup\oracle11g\数据库备份脚本\inc1.txt >> D:\rmanbackup\oracle11g\log\%date:~0,4%%date:~5,2%%date:~8,2%\%date:~0,4%%date:~5,2%%date:~8,2%.log