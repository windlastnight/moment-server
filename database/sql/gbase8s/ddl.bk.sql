CREATE TABLE momentdb:t_comment (
                                    id SERIAL NOT NULL,
                                    comment_id VARCHAR(100) NOT NULL,
    feed_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    comment_content LVARCHAR(1000),
    reply_to VARCHAR(100),
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    UNIQUE (comment_id) CONSTRAINT u107_5
    )
    in datadbs1 ;
CREATE UNIQUE INDEX "101_5" ON momentdb:t_comment (comment_id) ;
comment on column momentdb:t_comment.id is '主键';
CREATE TABLE momentdb:t_feed (
                                 id SERIAL NOT NULL,
                                 feed_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    feed_type INTEGER NOT NULL,
    feed_content TEXT NOT NULL,
    feed_poi VARCHAR(255),
    feed_status INTEGER NOT NULL DEFAULT 0,
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    update_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    UNIQUE (feed_id) CONSTRAINT u108_30
    )
    in datadbs1 ;
CREATE UNIQUE INDEX "104_30" ON momentdb:t_feed (feed_id) ;
comment on column momentdb:t_feed.id is '自增Id';
comment on column momentdb:t_feed.feed_id is '动态 Id';
comment on column momentdb:t_feed.user_id is '发布者 Id';
comment on column momentdb:t_feed.feed_type is '动态类型';
comment on column momentdb:t_feed.feed_content is '动态内容';
comment on column momentdb:t_feed.feed_poi is '动态的地理位置信息';
comment on column momentdb:t_feed.feed_status is '0 正常，1 被删除';
CREATE TABLE momentdb:t_like (
                                 id SERIAL NOT NULL,
                                 like_id VARCHAR(100) NOT NULL,
    feed_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    like_status INTEGER NOT NULL,
    UNIQUE (like_id) CONSTRAINT u109_21,
    UNIQUE (feed_id,user_id) CONSTRAINT u109_22
    )
    in datadbs1 ;
CREATE UNIQUE INDEX "103_21" ON momentdb:t_like (like_id) ;
CREATE UNIQUE INDEX "103_22" ON momentdb:t_like (feed_id,user_id) ;
comment on column momentdb:t_like.id is '主键';
CREATE TABLE momentdb:t_message (
                                    id SERIAL NOT NULL,
                                    message_id VARCHAR(64) NOT NULL,
    feed_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    message_type INTEGER NOT NULL,
    status INTEGER NOT NULL,
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    publish_user_id VARCHAR(64) NOT NULL
    )
    in datadbs1 ;
CREATE TABLE momentdb:t_timeline (
                                     id SERIAL NOT NULL,
                                     feed_id VARCHAR(32) NOT NULL,
    org_id VARCHAR(32) NOT NULL,
    create_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    feed_status INTEGER NOT NULL
    )
    in datadbs1 ;
CREATE TABLE momentdb:t_user_setting (
    user_id VARCHAR(100) NOT NULL,
    cover LVARCHAR(1024),
    update_dt DATETIME YEAR TO FRACTION(5) NOT NULL,
    PRIMARY KEY (user_id) CONSTRAINT u106_45
    )
    in datadbs1 ;

