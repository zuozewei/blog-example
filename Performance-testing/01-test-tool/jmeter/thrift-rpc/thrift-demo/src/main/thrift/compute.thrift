namespace java thrift.service

// 计算类型 - 仅限整数四则运算
enum ComputeType {
    ADD = 0;
    SUB = 1;
    MUL = 2;
    DIV = 3;
}

// 服务请求
struct ComputeRequest {
    1:required i64 x;
    2:required i64 y;
    3:required ComputeType computeType;
}

// 服务响应
struct ComputeResponse {
    1:required i32 errorNo;
    2:optional string errorMsg;
    3:required i64 computeRet;
}

service ComputeServer {
    ComputeResponse getComputeResult(1:ComputeRequest request);
}