drop table podcast_media;
drop table podcast_link;
drop table media;
drop table link;
drop table podcast;

create table if not exists podcast
(
    id serial not null
        constraint podcast_pkey
            primary key,
    description varchar(255),
    notes varchar(255),
    title varchar(255),
    s3_output_file_name varchar(255),
    transcript varchar(255),
    s3_fqn_uri varchar(255)
);

create table if not exists link
(
    id serial not null
        constraint link_pkey
            primary key,
    description varchar(255),
    href varchar(255)
);

create table if not exists media
(
    id serial not null
        constraint media_pkey
            primary key,
    description varchar(255),
    extension varchar(255),
    file_name varchar(255),
    href varchar(255),
    type varchar(255)
);

create table if not exists podcast_link
(
    podcast_id integer not null
        constraint podcast_link_podcast_id_fkey
            references podcast,
    link_id integer not null
        constraint podcast_link_link_id_fkey
            references media
);

create table if not exists podcast_media
(
    podcast_id integer not null
        constraint podcast_media_podcast_id_fkey
            references podcast,
    media_id integer not null
        constraint podcast_media_media_id_fkey
            references media
);

alter table podcast owner to orders;
alter table media owner to orders;
alter table podcast_link owner to orders;
alter table podcast_media owner to orders;
alter table link owner to orders;