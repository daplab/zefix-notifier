Zefix Notifier -- Day 1
----

## Mysql and web app

The [play framework](https://www.playframework.com/) used for the webapp part is awesome. 
A [handful lines of code](https://github.com/daplab/zefix-notifier/commit/829c0d9630d2a3a411e0edf5214fd9586a286ec6)
were enough to full fill the requirements

The only issue we faced was with the `regexp` keyword, reserved in MySQL and not properly
escaped with backticks by [javaEbean](https://www.playframework.com/documentation/2.2.x/JavaEbean)

### Creating database and user

Well, this is pretty unsecure as the test user has password-less access from everywhere.
It's still safe in our context because MySQL is not reachable from outside.

```
CREATE DATABASE test;
CREATE USER 'test'@'%';
GRANT ALL PRIVILEGES ON test.* TO 'test'@'%';
FLUSH PRIVILEGES;
```

### Creating the table

```
USE test;
CREATE TABLE `zefix_notifier_input` ( `input` VARCHAR(256), `email` VARCHAR(256) );
```

### Inserting test data

```
INSERT INTO `zefix_notifier_input` ( `email`, `input` )
VALUES 
  ('email1@daplab.ch', 'bank'), 
  ('email2@daplab.ch', 'foo.*bar');
```

## Hive

As the database schema has been changed, the Hive table will be changed too (well, re-created :))

```
DROP TABLE IF EXISTS `zefix_notifier_input`;
```

```
CREATE TABLE `zefix_notifier_input` (
  `input` string,
  `email` string 
) row format delimited fields terminated by '\t' stored as textfile;
```

## Sqoop

There're plenty of [Sqoop tutorials](https://www.google.ch/webhp?ie=UTF-8#q=sqoop%20tutorial) 
in the net, this short document does not aim at competing with them.

The main resource used to play with Sqoop is the official documentation:  
- [https://sqoop.apache.org/docs/1.4.2/SqoopUserGuide.html](https://sqoop.apache.org/docs/1.4.2/SqoopUserGuide.html)
and more specially the section speaking about importing in Hive 
- [https://sqoop.apache.org/docs/1.4.2/SqoopUserGuide.html#_importing_data_into_hive](https://sqoop.apache.org/docs/1.4.2/SqoopUserGuide.html#_importing_data_into_hive)

Sqoop allows to explore the jdbc source before importing the data. For instance listing 
all the available tables

```
sqoop list-tables --connect jdbc:mysql://localhost/test --username test
```

Ane executing custom SQL queries

```
sqoop eval --connect jdbc:mysql://localhost/test --username test --query "SELECT * FROM zefix_notifier_input"
```

### Importing data

By default Sqoop creates a file in HDFS. `--hive-import` is required to import _directly_ in Hive.
This is actually done in two steps: from jdbc source to HDFS, from HDFS to Hive.

There is one trick in our table, we don't have primary key. `-m 1` is required to Hive to 
work properly.

The complete sqoop line to read from our table and import in the Hive table finally is:

```
sqoop import --connect jdbc:mysql://10.10.10.3/test \
  --username test --table zefix_notifier_input \
  --target-dir /tmp/test-$(date +%s) \
  --hive-import --hive-overwrite --hive-table zefix_notifier_input \
  -m 1 --fields-terminated-by '\t'
```

And, that's it!

Now we have all the building blocks to tight together to have a complete -- even under-efficient --
Zefix Notifier. It took us, in total, 2 times 3 hours sessions -- including beers :)




