package com.techstar.activeMQ;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 消费者
 */
public class Receiver {
    private static final Logger logger = LogManager.getLogger(Receiver.class);

    public static void main(String[] args) {
        //连接工厂，JMS用它创建连接
        ConnectionFactory connectionFactory;
        
        //JMS client到JMS Provider的连接
        Connection connection = null;
        
        //一个发送或接受消息的线程
        Session session;
        
        //消息的目的地，消息发送谁
        Destination destination;
        
        //消费者，消息接受者
        MessageConsumer consumer;
        connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.1.5:61616");
        try {
            //构造从工厂得到连接对象
            connection = connectionFactory.createConnection();
            
            //启动
            connection.start();
            
            //获取操作连接
            session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            
            //获取session的注意参数值，7DQueue是一个消息服务器的队列
            destination = session.createQueue("7DQueue");
            consumer = session.createConsumer(destination);
            
            while (true) {
                //设置接收者接收消息的时间
                TextMessage message = (TextMessage) consumer.receive(500000);
                if (null != message) {
                    //System.out.println("收到消息：" + message.getText());
                    logger.info("收到消息：" + message.getText());

                } else {
                    break;
                }
            }
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