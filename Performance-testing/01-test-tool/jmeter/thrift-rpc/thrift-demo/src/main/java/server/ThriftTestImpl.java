package server;


import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
import thrift.service.ComputeRequest;
import thrift.service.ComputeResponse;
import thrift.service.ComputeServer;
import thrift.service.ComputeType;

/**
 * Created by zuozewei
 */
public class ThriftTestImpl implements ComputeServer.Iface {
    private static final Logger logger = LogManager.getLogger(ThriftTestImpl.class);
    public ComputeResponse getComputeResult(ComputeRequest request) {
        ComputeType computeType = request.getComputeType();
        long x = request.getX();
        long y = request.getY();
        logger.info("get compute result begin. [x:{}] [y:{}] [type:{}]", x, y, computeType.toString());
        long begin = System.currentTimeMillis();
        ComputeResponse response = new ComputeResponse();
        response.setErrorNo(0);
        try {
            long ret;
            if (computeType == ComputeType.ADD) {
                ret = add(x, y);
                response.setComputeRet(ret);
            } else if (computeType == ComputeType.SUB) {
                ret = sub(x, y);
                response.setComputeRet(ret);
            } else if (computeType == ComputeType.MUL) {
                ret = mul(x, y);
                response.setComputeRet(ret);
            } else {
                ret = div(x, y);
                response.setComputeRet(ret);
            }
        } catch (Exception e) {
            response.setErrorNo(1001);
            response.setErrorMsg(e.getMessage());
            logger.error("exception:", e);
        }
        long end = System.currentTimeMillis();
        logger.info("get compute result end. [errno:{}] cost:[{}ms]", response.getErrorNo(), (end - begin));
        return response;
    }
    private long add(long x, long y) {
        return x + y;
    }
    private long sub(long x, long y) {
        return x - y;
    }
    private long mul(long x, long y) {
        return x * y;
    }
    private long div(long x, long y) {
        return x / y;
    }
}
