package com.techstar.activeMQByJmeter.common;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;

/**
 * 生产者类
 */
public class Producer {
    private static final Logger logger = LogManager.getLogger(Producer.class);

    public void producer(String brokerURL,String Queue,String message) {
        //连接工厂，JMS使用它创建连接
        ConnectionFactory connectionFactory;
        //Provider的连接
        Connection connection = null; //JMS客户端到JMS
        Session session; //一个发送和接受消息的线程
        Destination destination; //消息的目的地，消息发给谁
        MessageProducer producer; //消息发送者
        //构造连接工厂实例对象
        connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        try {
            //构造从工厂得到连接对象
            connection = connectionFactory.createConnection();
            //启动
            connection.start();
            //获取操作连接
            session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
            //获取session，注意指定消息队列
            destination = session.createQueue(Queue);
            //得到消息生产者（发送者）
            producer = session.createProducer(destination);
            //设置消息持久化方式
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            //构造消息，此处写死，项目就是参数，或者方法获取
            //发送消息到目的地方
            TextMessage textMessage = session.createTextMessage("ActiveMq 发送的消息" + message );
            logger.info("发送消息：" + "ActiveMq 发送的消息" + message);
            producer.send(textMessage);
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != connection)
                    connection.close();
            } catch (Throwable ignore) {
            }
        }
    }

}
