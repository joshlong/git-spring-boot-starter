--
-- PostgreSQL database dump
--

-- Dumped from database version 11.5
-- Dumped by pg_dump version 11.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: orders
--

CREATE SEQUENCE  hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE  hibernate_sequence OWNER TO orders;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: link; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE  link (
    id bigint NOT NULL,
    description character varying(255),
    href character varying(255)
);


ALTER TABLE  link OWNER TO orders;

--
-- Name: media; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE  media (
    id bigint NOT NULL,
    description character varying(255),
    extension character varying(255),
    file_name character varying(255),
    href character varying(255),
    type character varying(255)
);


ALTER TABLE  media OWNER TO orders;

--
-- Name: podcast; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE  podcast (
    id bigint NOT NULL,
    date timestamp without time zone,
    description character varying(255),
    notes character varying(255),
    podbean_draft_created timestamp without time zone,
    podbean_draft_published timestamp without time zone,
    podbean_media_uri character varying(255),
    s3_audio_file_name character varying(255),
    s3_audio_uri character varying(255),
    s3_photo_file_name character varying(255),
    s3_photo_uri character varying(255),
    title character varying(255),
    transcript character varying(255),
    uid character varying(255)
);


ALTER TABLE  podcast OWNER TO orders;

--
-- Name: podcast_link; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE  podcast_link (
    podcast_id bigint NOT NULL,
    link_id bigint NOT NULL
);


ALTER TABLE  podcast_link OWNER TO orders;

--
-- Name: podcast_media; Type: TABLE; Schema: public; Owner: orders
--

CREATE TABLE  podcast_media (
    podcast_id bigint NOT NULL,
    media_id bigint NOT NULL
);


ALTER TABLE  podcast_media OWNER TO orders;


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: orders
--

SELECT pg_catalog.setval(' hibernate_sequence', 220, true);


--
-- Name: link link_pkey; Type: CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY  link
    ADD CONSTRAINT link_pkey PRIMARY KEY (id);


--
-- Name: media media_pkey; Type: CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY  media
    ADD CONSTRAINT media_pkey PRIMARY KEY (id);


--
-- Name: podcast podcast_pkey; Type: CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY  podcast
    ADD CONSTRAINT podcast_pkey PRIMARY KEY (id);


--
-- Name: podcast_link fk2vu3w8tjdo0qb3vkpeydcc3w0; Type: FK CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY  podcast_link
    ADD CONSTRAINT fk2vu3w8tjdo0qb3vkpeydcc3w0 FOREIGN KEY (link_id) REFERENCES  link(id);


--
-- Name: podcast_media fk8g18uypwfolj3nu8jew7vj6ex; Type: FK CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY  podcast_media
    ADD CONSTRAINT fk8g18uypwfolj3nu8jew7vj6ex FOREIGN KEY (media_id) REFERENCES  media(id);


--
-- Name: podcast_media fkep89648nfax8u5t7cjle9bh77; Type: FK CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY  podcast_media
    ADD CONSTRAINT fkep89648nfax8u5t7cjle9bh77 FOREIGN KEY (podcast_id) REFERENCES  podcast(id);


--
-- Name: podcast_link fkllcnm8ch4ses0kchqayiylv9c; Type: FK CONSTRAINT; Schema: public; Owner: orders
--

ALTER TABLE ONLY  podcast_link
    ADD CONSTRAINT fkllcnm8ch4ses0kchqayiylv9c FOREIGN KEY (podcast_id) REFERENCES  podcast(id);


--
-- PostgreSQL database dump complete
--

