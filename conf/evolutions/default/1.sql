--public schema

-- !Ups

CREATE TABLE public.job(
id int PRIMARY KEY,
title text,
requirement text,
responsibility text,
alternate_url text,
salary_from int,
salary_to int,
salary_currency text,
salary_gross boolean,
city text,
key_word text
);

CREATE INDEX ON public.job(city);
CREATE INDEX ON public.job(key_word);

-- !Downs

DROP TABLE public.job;
