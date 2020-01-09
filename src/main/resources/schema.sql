-- drop table if exists batch_job_execution_params;
-- drop table if exists batch_job_execution_context;
-- drop table if exists batch_step_execution_context;
-- drop table if exists batch_step_execution;
-- drop table if exists batch_job_execution;
-- drop table if exists batch_job_instance;
--
-- drop sequence if exists batch_job_execution_seq;
-- drop sequence if exists batch_job_seq;
-- drop sequence if exists batch_step_execution_seq;
-- drop sequence if exists hibernate_sequence;
--
-- drop table if exists podcast_media;
-- drop table if exists podcast_link;
-- drop table if exists media;
-- drop table if exists link;
-- drop table if exists podcast;

create table if not exists link
(
    id          bigint not null
        constraint link_pkey
            primary key,
    description varchar(255),
    href        varchar(255)
);

create table if not exists media
(
    id          bigint not null
        constraint media_pkey
            primary key,
    description varchar(255),
    extension   varchar(255),
    file_name   varchar(255),
    href        varchar(255),
    type        varchar(255)
);


create table if not exists podcast
(
    id                  bigint not null
        constraint podcast_pkey
            primary key,
    date                timestamp,
    description         varchar(255),
    s3_fqn_uri          varchar(255),
    notes               varchar(255),
    s3_output_file_name varchar(255),
    title               varchar(255),
    transcript          varchar(255),
    uid                 varchar(255)
);


create table if not exists podcast_link
(
    podcast_id bigint not null
        constraint fkllcnm8ch4ses0kchqayiylv9c
            references podcast,
    link_id    bigint not null
        constraint fk2vu3w8tjdo0qb3vkpeydcc3w0
            references link
);


create table if not exists podcast_media
(
    podcast_id bigint not null
        constraint fkep89648nfax8u5t7cjle9bh77
            references podcast,
    media_id   bigint not null
        constraint fk8g18uypwfolj3nu8jew7vj6ex
            references media
);


