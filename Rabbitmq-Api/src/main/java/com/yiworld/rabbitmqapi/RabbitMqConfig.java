package com.yiworld.rabbitmqapi;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Value("${rabbitmq.connection.channel_cache_size}")
    private String rabbitmqConnectionChannelCacheSize;

    @Value("${rabbitmq.connection.requested_heart_beat}")
    private String rabbitmqConnectionRequestedHeartBeat;

    @Value("${rabbitmq.connection.timeout}")
    private String rabbitmqConnectionTimeout;

    @Value("${rabbit.listener.container.concurrency}")
    private String rabbitListenerContainerConcurrency;

    @Value("${rabbit.listener.container.max_concurrency}")
    private String rabbitListenerContainerMaxConcurrency;

    @Value("${rabbitmq.retry.back_off_policy.initial_interval}")
    private String rabbitmqRetryBackOffPolicyInitialInterval;

    @Value("${rabbitmq.retry.back_off_policy.max_interval}")
    private String rabbitmqRetryBackOffPolicyMaxInterval;

    @Value("${rabbitmq.retry.back_off_policy.multiplier}")
    private String rabbitmqRetryBackOffPolicyMultiplier;

    @Value("${rabbitmq.retry.retry_policy.max_attempts}")
    private String rabbitmqRetryRetryPolicyMaxAttempts;


    @Bean(name = "connectionFactory")
    public ConnectionFactory connectionFactory(@Value("${rabbitmq.connection.addresses}") String addresses,
                                               @Value("${rabbitmq.connection.username}") String username,
                                               @Value("${rabbitmq.connection.password}") String password) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(addresses);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setChannelCacheSize(Integer.valueOf(rabbitmqConnectionChannelCacheSize));
        connectionFactory.setRequestedHeartBeat(Integer.valueOf(rabbitmqConnectionRequestedHeartBeat));
        connectionFactory.setCloseTimeout(Integer.valueOf(rabbitmqConnectionTimeout));
        return connectionFactory;
    }

//    @Bean
//    public FanoutExchange fanoutExchange(@Value("${rabbitmq.exchangeName}")String exchangeName)
//    {
//        return new FanoutExchange(exchangeName,true,false);
//    }

    @Bean(name = "rabbitAdmin")
    public RabbitAdmin rabbitAdmin(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("", true, false);
    }

    @Bean(name = "captureQueue")
    public Queue queue(@Value("${rabbitmq.queue.name}") String queueName, @Qualifier("rabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = new Queue(queueName, true, false, false);
        queue.setShouldDeclare(true);
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean
    public Binding binding(@Qualifier("captureQueue") Queue queue, @Value("${rabbitmq.queue.name}") String queueName) {
        return BindingBuilder.bind(queue).to(directExchange()).with(queueName);
    }

    @Bean(name = "accessPlatformMsgQueue")
    public Queue accessPlatformMsgQueue(@Value("${rabbitmq.queue.name.accessPlatform}") String queueName,
                                        @Qualifier("rabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = new Queue(queueName, true, false, false);
        queue.setShouldDeclare(true);
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean(name = "accessPlatformMsgExchange")
    public DirectExchange accessPlatformMsgExchange(@Value("${rabbitmq.exchange.name.accessPlatform}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding accessPlatformMsgQueueBinding(@Qualifier("accessPlatformMsgExchange") DirectExchange exchange,
                                                 @Qualifier("accessPlatformMsgQueue") Queue queue,
                                                 @Value("${rabbitmq.queue.name.accessPlatform}") String queueName) {
        return BindingBuilder.bind(queue).to(exchange).with("");
    }

    @Bean(name = "gpuFacelibMsgQueue")
    public Queue gpuFacelibMsgQueue(@Value("${rabbitmq.queue.name.gpuFacelib}") String queueName,
                                    @Qualifier("rabbitAdmin") RabbitAdmin rabbitAdmin) {
        Queue queue = new Queue(queueName, true, false, false);
        queue.setShouldDeclare(true);
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean(name = "gpuFacelibMsgExchange")
    public DirectExchange gpuFacelibMsgExchange(@Value("${rabbitmq.exchange.name.gpuFacelib}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding gpuFacelibMsgQueueBinding(@Qualifier("gpuFacelibMsgExchange") DirectExchange exchange,
                                             @Qualifier("gpuFacelibMsgQueue") Queue queue,
                                             @Value("${rabbitmq.queue.name.gpuFacelib}") String queueName) {
        return BindingBuilder.bind(queue).to(exchange).with("");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(@Qualifier("connectionFactory") ConnectionFactory connectionFactory, @Qualifier("retryTemplate") RetryTemplate retryTemplate) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setRetryTemplate(retryTemplate);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean(name = "retryTemplate")
    public RetryTemplate retryTemplate(@Qualifier("backOffPolicy") ExponentialBackOffPolicy exponentialBackOffPolicy, @Qualifier("retryPolicy") SimpleRetryPolicy simpleRetryPolicy) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        return retryTemplate;
    }

    @Bean(name = "backOffPolicy")
    public ExponentialBackOffPolicy exponentialBackOffPolicy() {
        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(Long.valueOf(rabbitmqRetryBackOffPolicyInitialInterval));
        exponentialBackOffPolicy.setMaxInterval(Long.valueOf(rabbitmqRetryBackOffPolicyMaxInterval));
        exponentialBackOffPolicy.setMultiplier(Double.valueOf(rabbitmqRetryBackOffPolicyMultiplier));
        return exponentialBackOffPolicy;
    }

    @Bean(name = "retryPolicy")
    public SimpleRetryPolicy simpleRetryPolicy() {
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(Integer.valueOf(rabbitmqRetryRetryPolicyMaxAttempts));
        return simpleRetryPolicy;
    }

    @Bean
    @Primary
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory = new SimpleRabbitListenerContainerFactory();
        simpleRabbitListenerContainerFactory.setConnectionFactory(connectionFactory);
        simpleRabbitListenerContainerFactory.setMessageConverter(new SimpleMessageConverter());
        simpleRabbitListenerContainerFactory.setConcurrentConsumers(Integer.valueOf(rabbitListenerContainerConcurrency));
        simpleRabbitListenerContainerFactory.setMaxConcurrentConsumers(Integer.valueOf(rabbitListenerContainerMaxConcurrency));
        simpleRabbitListenerContainerFactory.setChannelTransacted(true);
		simpleRabbitListenerContainerFactory.setTaskExecutor(new SimpleAsyncTaskExecutor("MqConsumer-"));
        return simpleRabbitListenerContainerFactory;
    }

}
