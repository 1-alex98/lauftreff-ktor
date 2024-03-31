create table if not exists "user"(
    id SERIAL PRIMARY KEY,
    email varchar(255) not null unique,
    name varchar(255) not null
);

alter table "user" add column trainer bool default false not null;

create table if not exists "semester"(
    id SERIAL PRIMARY KEY,
    name varchar(255) not null unique ,
    beginning date not null,
    "end" date not null
);

create table if not exists "course"(
    id SERIAL PRIMARY KEY,
    name varchar(255) not null,
    "first_date" timestamp not null,
    weekly bool not null,
    semester integer not null references semester
);

create table if not exists "course_happening"(
    id SERIAL PRIMARY KEY,
    "date" timestamp not null,
    sent_email bool not null default false,
    course integer not null references course
);

create table if not exists "course_participation"(
    id SERIAL PRIMARY KEY,
    "user" integer not null references "user",
    course_happening integer not null references course_happening
);

create table if not exists "global_value"(
    id SERIAL PRIMARY KEY,
    "key" varchar(255) unique not null,
    "value" varchar(8000) not null
);

