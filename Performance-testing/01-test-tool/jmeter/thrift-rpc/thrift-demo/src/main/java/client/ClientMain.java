package client;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import thrift.service.ComputeRequest;
import thrift.service.ComputeResponse;
import thrift.service.ComputeServer;
import thrift.service.ComputeType;

/**
 * Created by zuozewei
 */
public class ClientMain {
    private ComputeRequest request;

    public ClientMain() {
        request = new ComputeRequest();
        request.setX(1);
        request.setY(2);
        request.setComputeType(ComputeType.MUL);
    }

    public static void main(String[] args) {
        TTransport transport = null;
        try {
            System.out.println("***********");
            long begin = System.currentTimeMillis();
            // 创建Transport，使用非阻塞方式，按块的大小进行传输
            transport = new TFramedTransport(new TSocket("127.0.0.1", 9999));
            transport.open();
            // 创建TProtocol，二进制协议序列化
            TProtocol protocol = new TBinaryProtocol(transport);
            //基于TTransport和TProtocol创建 Client
            ComputeServer.Client client = new ComputeServer.Client(protocol);
            //调用Client的相应方法
            //调用client的getComputeResult方法
            ComputeResponse response = client.getComputeResult(new ClientMain().request);
            System.out.println("cost:[" + (System.currentTimeMillis() - begin) + "ms]");
            System.out.println("***********");
            if (response != null) {
                System.out.println(response.toString());
            }
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (transport != null)
                transport.close();
        }
    }
}
