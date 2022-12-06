-- Adminer 4.8.1 PostgreSQL 15.1 (Debian 15.1-1.pgdg110+1) dump

DROP TABLE IF EXISTS "ARTICLES";
DROP SEQUENCE IF EXISTS "ARTICLES_ID_seq";
CREATE SEQUENCE "ARTICLES_ID_seq" START 1;

CREATE TABLE "public"."ARTICLES" (
    "ID" integer DEFAULT nextval('"ARTICLES_ID_seq"') NOT NULL,
    "TITLE" character varying NOT NULL,
    "AUTHOR_ID" integer NOT NULL,
    CONSTRAINT "ARTICLES_pkey" PRIMARY KEY ("ID")
) WITH (oids = false);

INSERT INTO "ARTICLES" ("ID", "TITLE", "AUTHOR_ID") VALUES
(1,	'First',	1),
(2,	'Second',	1),
(3,	'First',	2);

DROP TABLE IF EXISTS "USERS";
DROP SEQUENCE IF EXISTS "USERS_ID_seq";
CREATE SEQUENCE "USERS_ID_seq" START 1;

CREATE TABLE "public"."USERS" (
    "ID" integer DEFAULT nextval('"USERS_ID_seq"') NOT NULL,
    "NAME" character varying NOT NULL,
    CONSTRAINT "USERS_pkey" PRIMARY KEY ("ID")
) WITH (oids = false);

INSERT INTO "USERS" ("ID", "NAME") VALUES
(1,	'Marco'),
(2,	'Enrico');

-- 2022-11-18 11:43:19.559226+00