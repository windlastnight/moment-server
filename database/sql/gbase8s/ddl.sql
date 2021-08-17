CREATE TABLE t_comment (
                                    id SERIAL NOT NULL,
                                    comment_id VARCHAR(100) NOT NULL,
    feed_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    comment_content VARCHAR(4096),
    reply_to VARCHAR(100),
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (comment_id)
    )
     ;
create index idx_comment_fid_uid on t_comment (feed_id,user_id);
CREATE TABLE t_feed (
                                 id SERIAL NOT NULL,
                                 feed_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    feed_type INTEGER NOT NULL,
    feed_content VARCHAR(4096) NOT NULL,
    feed_poi VARCHAR(255),
    feed_status INTEGER NOT NULL DEFAULT 0,
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    update_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (feed_id)
    )
     ;
CREATE TABLE t_like (
                                 id SERIAL NOT NULL,
                                 like_id VARCHAR(100) NOT NULL,
    feed_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    like_status INTEGER NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (like_id),
    UNIQUE (feed_id,user_id)
    )
     ;
CREATE TABLE t_message (
                                    id SERIAL NOT NULL,
                                    message_id VARCHAR(64) NOT NULL,
    feed_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    message_type INTEGER NOT NULL,
    status INTEGER NOT NULL,
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    publish_user_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (id)
    )
     ;
create index idx_message_user_id on  t_message(user_id);
create index idx_message_message_id on  t_message(message_id);
CREATE TABLE t_timeline (
                                     id SERIAL NOT NULL,
                                     feed_id VARCHAR(32) NOT NULL,
    org_id VARCHAR(32) NOT NULL,
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    feed_status INTEGER NOT NULL,
    PRIMARY KEY (id)
    )
     ;
create index idex_timeline_org_id on  t_timeline(org_id);
create index idex_timeline_create_dt on  t_timeline(create_dt);
CREATE TABLE t_user_setting (
    user_id VARCHAR(100) NOT NULL,
    cover VARCHAR(1024),
    update_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    PRIMARY KEY (user_id)
    )
     ;

