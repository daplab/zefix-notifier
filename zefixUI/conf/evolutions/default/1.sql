# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table zefix_notifier_input (
  input                    varchar(255),
  email                     varchar(255))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table zefix_notifier_input;

SET FOREIGN_KEY_CHECKS=1;

