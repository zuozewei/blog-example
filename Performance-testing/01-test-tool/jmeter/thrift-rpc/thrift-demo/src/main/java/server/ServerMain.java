package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import thrift.service.ComputeServer;

/**
 * Created by zuozewei
 */

public class ServerMain {
    private static final Logger logger = LogManager.getLogger(ServerMain.class);

    public static void main(String[] args) {
        try {
            //实现服务处理接口impl
            ThriftTestImpl workImpl = new ThriftTestImpl();
            //创建TProcessor
            TProcessor tProcessor = new ComputeServer.Processor<ComputeServer.Iface>(workImpl);
            //创建TServerTransport,非阻塞式 I/O，服务端和客户端需要指定 TFramedTransport 数据传输的方式
            final TNonblockingServerTransport transport = new TNonblockingServerSocket(9998);
            //创建TProtocol
            TThreadedSelectorServer.Args ttpsArgs = new TThreadedSelectorServer.Args(transport);
            ttpsArgs.transportFactory(new TFramedTransport.Factory());
            //二进制格式反序列化
            ttpsArgs.protocolFactory(new TBinaryProtocol.Factory());
            ttpsArgs.processor(tProcessor);
            ttpsArgs.selectorThreads(16);
            ttpsArgs.workerThreads(32);
            logger.info("compute service server on port :" + 9998);
            //创建TServer
            TServer server = new TThreadedSelectorServer(ttpsArgs);
            //启动Server
            server.serve();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
