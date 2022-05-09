--public schema

-- !Ups

CREATE TABLE public.job(
id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
hh_id int,
title text,
requirement text,
responsibility text,
salary_id uuid,
alternate_url text,
city text,
key_word text
);

CREATE TABLE public.salary(
id uuid PRIMARY KEY,
_from int,
_to int,
currency text,
gross boolean
);

ALTER TABLE public.job ADD CONSTRAINT fk_1 FOREIGN KEY (salary_id) REFERENCES public.salary(id);
CREATE INDEX ON public.job(city);
CREATE INDEX ON public.job(key_word);

-- !Downs

DROP TABLE public.job;
DROP TABLE public.salary;
