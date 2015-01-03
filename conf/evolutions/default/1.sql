# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table sensor_data (
  id                        bigint not null,
  log_date                  timestamp not null,
  i_hour_minute             integer not null,
  pir_count                 integer not null,
  data_owner_id             bigint not null,
  created_date              timestamp not null,
  updated_date              timestamp not null,
  constraint uq_sensor_data_1 unique (i_hour_minute,data_owner_id),
  constraint pk_sensor_data primary key (id))
;

create table user (
  id                        bigint not null,
  email                     varchar(255) not null,
  password                  varchar(255) not null,
  name                      varchar(255) not null,
  created_date              timestamp not null,
  updated_date              timestamp not null,
  constraint uq_user_1 unique (email),
  constraint pk_user primary key (id))
;

create sequence sensor_data_seq;

create sequence user_seq;

alter table sensor_data add constraint fk_sensor_data_dataOwner_1 foreign key (data_owner_id) references user (id) on delete restrict on update restrict;
create index ix_sensor_data_dataOwner_1 on sensor_data (data_owner_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists sensor_data;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists sensor_data_seq;

drop sequence if exists user_seq;

