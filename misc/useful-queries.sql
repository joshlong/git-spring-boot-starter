--  delete everything to reset to pristine
delete from podcast_link; delete from podcast_media; delete from podcast; delete from media; delete from link; delete from mappings;

-- drop all the tables
drop table podcast_media cascade ; drop table podcast_link cascade ; drop table media cascade; drop table link cascade; drop table podcast cascade; drop table mappings; drop sequence hibernate_sequence;

-- change the date of a podcast
update podcast set date = TIMESTAMP '2019-10-19 10:23:54+02' where id = 4;
