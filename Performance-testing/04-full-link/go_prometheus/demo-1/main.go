package main

import (
	"net/http"

	"github.com/prometheus/client_golang/prometheus/promhttp"
)

func main() {
	//提供 /metrics HTTP 端点
	http.Handle("/metrics", promhttp.Handler())
	//端口号
	http.ListenAndServe(":2112", nil)
}
