CREATE TABLE `t_transactional_message`
(
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edit_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator             VARCHAR(20)     NOT NULL DEFAULT 'admin',
    editor              VARCHAR(20)     NOT NULL DEFAULT 'admin',
    deleted             TINYINT         NOT NULL DEFAULT 0,
    current_retry_times TINYINT         NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    max_retry_times     TINYINT         NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    queue_name          VARCHAR(255)    NOT NULL COMMENT '队列名',
    exchange_name       VARCHAR(255)    NOT NULL COMMENT '交换器名',
    exchange_type       VARCHAR(8)      NOT NULL COMMENT '交换类型',
    routing_key         VARCHAR(255) COMMENT '路由键',
    business_module     VARCHAR(32)     NOT NULL COMMENT '业务模块',
    business_key        VARCHAR(255)    NOT NULL COMMENT '业务键',
    next_schedule_time  DATETIME        NOT NULL COMMENT '下一次调度时间',
    message_status      TINYINT         NOT NULL DEFAULT 0 COMMENT '消息状态',
    init_backoff        BIGINT UNSIGNED NOT NULL DEFAULT 10 COMMENT '退避初始化值,单位为秒',
    backoff_factor      TINYINT         NOT NULL DEFAULT 2 COMMENT '退避因子(也就是指数)',
    INDEX idx_queue_name (queue_name),
    INDEX idx_create_time (create_time),
    INDEX idx_next_schedule_time (next_schedule_time),
    INDEX idx_business_key (business_key)
) COMMENT '事务消息表';

CREATE TABLE `t_transactional_message_content`
(
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT UNSIGNED NOT NULL COMMENT '事务消息记录ID',
    content    TEXT COMMENT '消息内容'
) COMMENT '事务消息内容表';

/*因为此模块有可能扩展出一个后台管理模块，所以要把消息的管理和状态相关字段和大体积的消息内容分别存放在两个表，
  从而避免大批量查询消息记录的时候 MySQL 服务 IO 使用率过高的问题。
  预留了两个业务字段 business_module 和 business_key 用于标识业务模块和业务键（一般是唯一识别号，例如订单号）。

  一般情况下，如果服务通过配置自行提前声明队列和交换器的绑定关系，那么发送 RabbitMQ 消息的时候其实只依赖于 exchangeName 和 routingKey 两个字段），
  考虑到服务可能会遗漏声明操作，发送消息的时候会基于队列进行首次绑定声明并且缓存相关的信息（RabbitMQ 中的队列-交换器绑定声明只要每次声明绑定关系的参数一致，则不会抛出异常）。
  */