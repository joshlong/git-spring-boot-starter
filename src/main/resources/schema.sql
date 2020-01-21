drop sequence if exists hibernate_sequence;
drop table if exists podcast_media cascade;
drop table if exists podcast_link cascade;
drop table if exists link cascade;
drop table if exists media cascade;
drop table if exists podcast cascade;

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: link; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE link
(
    id          bigint NOT NULL,
    description character varying(255),
    href        character varying(255)
);



--
-- Name: media; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE media
(
    id          bigint NOT NULL,
    description character varying(255),
    extension   character varying(255),
    file_name   character varying(255),
    href        character varying(255),
    type        character varying(255)
);



--
-- Name: podcast; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE podcast
(
    id                      bigint NOT NULL,
    date                    timestamp without time zone,
    description             character varying(255),
    notes                   character varying(255),
    podbean_draft_created   timestamp without time zone,
    podbean_draft_published timestamp without time zone,
    podbean_media_uri       character varying(255),
    s3_audio_file_name      character varying(255),
    s3_audio_uri            character varying(255),
    s3_photo_file_name      character varying(255),
    s3_photo_uri            character varying(255),
    title                   character varying(255),
    transcript              character varying(255),
    uid                     character varying(255)
);



--
-- Name: podcast_link; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE podcast_link
(
    podcast_id bigint NOT NULL,
    link_id    bigint NOT NULL
);



--
-- Name: podcast_media; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE podcast_media
(
    podcast_id bigint NOT NULL,
    media_id   bigint NOT NULL
);



--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: orders
--

-- SELECT pg_catalog.setval(' hibernate_sequence', 220, true);


--
-- Name: link link_pkey; Type: CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY link
    ADD CONSTRAINT link_pkey PRIMARY KEY (id);


--
-- Name: media media_pkey; Type: CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY media
    ADD CONSTRAINT media_pkey PRIMARY KEY (id);


--
-- Name: podcast podcast_pkey; Type: CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY podcast
    ADD CONSTRAINT podcast_pkey PRIMARY KEY (id);


--
-- Name: podcast_link fk2vu3w8tjdo0qb3vkpeydcc3w0; Type: FK CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY podcast_link
    add CONSTRAINT fk2vu3w8tjdo0qb3vkpeydcc3w0 FOREIGN KEY (link_id) REFERENCES link (id);


--
-- Name: podcast_media fk8g18uypwfolj3nu8jew7vj6ex; Type: FK CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY podcast_media
    ADD CONSTRAINT fk8g18uypwfolj3nu8jew7vj6ex FOREIGN KEY (media_id) REFERENCES media (id);


--
-- Name: podcast_media fkep89648nfax8u5t7cjle9bh77; Type: FK CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY podcast_media
    ADD CONSTRAINT fkep89648nfax8u5t7cjle9bh77 FOREIGN KEY (podcast_id) REFERENCES podcast (id);


--
-- Name: podcast_link fkllcnm8ch4ses0kchqayiylv9c; Type: FK CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY podcast_link
    ADD CONSTRAINT fkllcnm8ch4ses0kchqayiylv9c FOREIGN KEY (podcast_id) REFERENCES podcast (id);


--
-- PostgreSQL database dump complete
--

