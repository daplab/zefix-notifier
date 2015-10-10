Zefix Notifier -- Day 1
----

## Data Ingestion

The data is ingested daily and stored in HDFS. The files location is 
`hdfs://daplab1/shared/zefix/sogc/${yyyy}/${mm}/${dd}`, i.e. partitioned by day.

The data is stored in AVRO format, and the AVRO schema is stored in `hdfs://daplab1/shared/zefix/sogc.avsc`

## Hive table

An external Hive table is created like this

```
CREATE EXTERNAL TABLE zefix_sogc
 PARTITIONED BY (
   `year` int,
   `month` int,
   `day` int)
  ROW FORMAT SERDE
  'org.apache.hadoop.hive.serde2.avro.AvroSerDe'
  STORED as INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.avro.AvroContainerInputFormat'
  OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.avro.AvroContainerOutputFormat'
  TBLPROPERTIES (
    'avro.schema.url'='hdfs://daplab1/shared/zefix/sogc.avsc');
```

### Hive Partitions

Partitions are created on the table, for instance:

```
ALTER TABLE zefix_sogc ADD IF NOT EXISTS PARTITION(year = 2015, month = 08, day = 28) 
LOCATION 'hdfs://daplab1/shared/zefix/sogc/2015/08/28/';
```

Please note that there is no data ingested over the week-ends, so there is gap in the dates.

### First query

Now we have the table and at least one partition, we can start querying the table. First thing first,
let's show what the table looks like:

```
DESCRIBE zefix_sogc
```

And now run our first query:

```
SELECT company_name FROM zefix_sogc WHERE year = 2015 and month = 08 and day = 28
```

## User Inputs

The second part of the daily batch is to match the user inputs with the zefix SOGC table.

Let's take few shortcuts for the time being and create a user input table and manually load data 
inside. In the next iteration, we'll use sqoop to extract the data from the mysql database into Hive.

```
CREATE TABLE `zefix_notifier_input` (
  `regexp` string,
  `email` string 
) row format delimited fields terminated by '\t' stored as textfile;;
```

Let's then import test data inside this table:

```
LOAD DATA LOCAL INPATH '/home/bperroud/zefix1_input.tsv' INTO TABLE zefix_notifier_input;
```

## Matching Algorithm

We do have two tables, respectively `zefix_sogc` and `zefix_notifier_input`. 
The idea, for this iteration, is to match the user input with the company name.

In a nutshell, something like `where zefix_sogc.company_name _RLIKE_ zefix_notifier_input.regexp`.

Every records from the `zefix_sogc` table (for a given day) must be match against every records 
from the table `zefix_notifier_input`. Ouch, this looks pretty much like a cross product.
Ok, we can live with that for the time being, but we'll raise a flag here and address this
problem in a future iteration.

<aside class="notice">
As a side note, running an `EXPLAIN` on the query is actually providing a WARNING that a 
cross product will be used.
</aside>

So, joining the two tables and filtering with a `RLIKE` will give something like:

```
select company_name, email from zefix1 join zefix1_input where company_name RLIKE regexp;
```

## Storing query

Now we have the query working, we need to send email to the matches. One clean way of doing
this would be to write a Hive UDF. But the easiest is most likely to store the output
of the query in the local filesystem, read it and send email via a shell script.

To store the data in the local filesystem, the previous query will be transformed like this:

````
INSERT OVERWRITE LOCAL DIRECTORY '/tmp/zefix_notifier' 
ROW FORMAT DELIMITED 
FIELDS TERMINATED BY ','
select company_name, email from zefix1 join zefix1_input where company_name RLIKE regexp;
```

## Sending emails

The last part is to read the locally stored files, loop for each lines and send emails. 
The one-liner script doing all that will be:

```
cat /tmp/zefix_notifier/* | while read line; do email=$(echo $line | cut -d "," -f2); company=$(echo $line | cut -d "," -f1); echo "$company matches your request" | mail -s "Zefix Notifier" $email; done
```
