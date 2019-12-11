--创建表
CREATE TABLE emp
(empno  MEDIUMINT UNSIGNED  NOT NULL  DEFAULT 0 COMMENT '编号',
ename VARCHAR(20) NOT NULL DEFAULT "" COMMENT '名字',
job VARCHAR(9) NOT NULL DEFAULT "" COMMENT '工作',
mgr MEDIUMINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级编号',
hiredate DATE NOT NULL COMMENT '入职时间',
sal DECIMAL(7,2)  NOT NULL COMMENT '薪水',
comm DECIMAL(7,2) NOT NULL COMMENT '红利',
deptno MEDIUMINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '部门编号'
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


--返回一个长度为参数 n 的随机字符串
delimiter $$

create function rand_string(n INT)
returns varchar(255) #该函数会返回一个字符串
begin
    declare chars_str varchar(100) default 'abcdefghijklmnopqrstuvwxyzABCDEFJHIJKLMNOPQRSTUVWXYZ';
    declare return_str varchar(255) default '';
    declare i int default 0;
    while i < n do
        set return_str =concat(return_str,substring(chars_str,floor(1+rand()*52),1));
        set i = i + 1;
    end while;
    return return_str;
end $$

delimiter ;

--返回一个随机 int 值
delimiter $$

create function rand_num( )
returns int(5)
begin
 declare i int default 0;
 set i = floor(10+rand()*500);
return i;
  end $$

delimiter ;

--插入数据表 emp 的数据

delimiter $$

create procedure insert_emp(in max_num int(10))
begin
declare i int default 0;
 set autocommit = 0;
 repeat
 set i = i + 1;
 insert into emp values (i ,rand_string(6),'SALESMAN',0001,curdate(),2000,400,rand_num());
  until i = max_num
 end repeat;
   commit;
 end $$

delimiter ;