create table photography_contest (
  photography_contest_name varchar(20) not null,
  year smallint not null,
  location varchar(20),
  from_date timestamp not null,
  to_date timestamp not null,
  constraint photography_contest_pk primary key (photography_contest_name, year)
);
create table sponsors (
  sponsor_name varchar(20) not null,
  sponsor_type varchar(20) not null,
  constraint sponsors_pk primary key (sponsor_name)
);
create table support (
  level varchar(20) not null,
  sponsor_name varchar(20) not null,
  photography_contest_name varchar(20) not null,
  year smallint not null,
  constraint support_pk primary key (level, sponsor_name, photography_contest_name),
  constraint fk_photography_contest foreign key (photography_contest_name, year) references photography_contest,
  constraint fk_sponsor foreign key (sponsor_name) references sponsors
);
create table members (
  member_id varchar(20) not null,
  email varchar(20) not null,
  name varchar(20) not null,
  country varchar(20) not null,
  expertise varchar(20) not null,
  constraint members_pk primary key (member_id)
);
create table nominators (
  member_id varchar(20) not null,
  photography_contest_name varchar(20) not null,
  year smallint not null,
  constraint nominators_pk primary key (member_id, photography_contest_name, year),
  constraint fk_photography_contest1 foreign key (photography_contest_name, year) references photography_contest,
  constraint fk_members foreign key (member_id) references members
);
create table category (
  name varchar(20) not null,
  description varchar(20) not null,
  constraint category_pk primary key (name)
);
create table photographer (
  r_id varchar(20) not null,
  name varchar(20) not null,
  email varchar(20) not null,
  country varchar(20) not null,
  dob timestamp not null,
  constraint photographer_pk primary key (r_id)
);
create table submissions (
  caption varchar(20) not null,
  format varchar(20) not null,
  size varchar(20) not null,
  r_id varchar(20) not null,
  submission_date timestamp not null,
  name varchar(20) not null,
  constraint submissions_pk primary key (caption),
  constraint fk_photographer foreign key (r_id) references photographer,
  constraint fk_category foreign key (name) references category
);
create table open_call_exhibition(
  caption varchar(20) not null,
  constraint open_call_exhibition_pk primary key (caption),
  constraint fk_submissions_oce foreign key (caption) references submissions
);
create table nominate(
  nominate_id varchar(20) not null,
  member_id varchar(20) not null,
  photography_contest_name varchar(20) not null,
  year smallint not null,
  score decimal(5, 2) not null,
  comment varchar(20) not null,
  caption varchar(20) not null,
  constraint nominate_pk primary key (nominate_id),
  constraint fk_nominators foreign key (member_id, photography_contest_name, year) references nominators,
  constraint fk_submissions foreign key (caption) references submissions
);
create table nominee(
  nominee_id varchar(20) not null,
  caption varchar(20) not null,
  nominate_id varchar(20) not null,
  constraint nominee_pk primary key (nominee_id),
  constraint fk_open_call_exhibition foreign key (caption) references open_call_exhibition,
  constraint fk_nominate foreign key (nominate_id) references nominate
);
create table awards(
  award_name varchar(20) not null,
  prize varchar(20) not null,
  photography_contest_name varchar(20) not null,
  year smallint not null,
  nominee_id varchar(20) not null,
  constraint awards_pk primary key (award_name),
  constraint fk_photography_contest foreign key (photography_contest_name, year) references photography_contest,
  constraint fk_nominee1 foreign key (nominee_id) references nominee
);
create table feature_exhibition(
  caption varchar(20) not null,
  constraint feature_exhibition_pk primary key (caption),
  constraint fk_submissions foreign key (caption) references submissions
);
create table photo_magazine (
  title varchar(20) not null,
  editorInChief varchar(20) not null,
  url varchar(20) not null,
  year smallint not null,
  caption varchar(20) not null,
  constraint fk_feature_exhibition foreign key (caption) references feature_exhibition
);