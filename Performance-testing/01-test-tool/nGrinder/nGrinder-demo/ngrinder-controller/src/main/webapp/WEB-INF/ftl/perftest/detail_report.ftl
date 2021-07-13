<!DOCTYPE html>
<html>
<head>
    <#include "../common/common.ftl">
    <#include "../common/jqplot.ftl">
    <title><@spring.message "perfTest.report.title"/></title>
    <style>
        body {
            padding-top: 0;
        }

        div.row {
            margin-bottom: 50px;
        }

        div.chart {
            border: 1px solid #878988;
            height: 200px;
            min-width: 615px;
        }

        div.bigchart {
            border: 1px solid #878988;
            height: 300px;
            min-width: 615px;
        }

        h6 {
            margin-top: 20px;
        }

        td strong {
            color: #6DAFCF
        }

        .jqplot-yaxis {
            margin-right: 10px;
        }

        .jqplot-xaxis {
            margin-right: 5px;
            margin-top: 5px;
        }

        .compactpadding th {
            padding: 8px 5px;
            vertical-align: middle;
        }

        .jqplot-image-button {
            margin-top: 5px;
            margin-bottom: 5px;
        }

        div.jqplot-image-container {
            position: relative;
            z-index: 11;
            margin: auto;
            background-color: #ffffff;
            border: 1px solid #999;
            display: inline-block;
            min-width: 698px;
        }

        div.jqplot-image-container-header {
            font-size: 1.0em;
            font-weight: bold;
            padding: 5px 15px;
            background-color: #eee;
        }

        div.jqplot-image-container-content {
            background-color: #ffffff;
        }

        a.jqplot-image-container-close {
            float: right;
        }
    </style>
</head>

<#setting number_format="number">
<body>
<div id="wrap">
    <div class="navbar-inner" style="width:912px; margin-left:auto; margin-right:auto; margin-bottom:0">
        <h3><@spring.message "perfTest.report.reportPage"/> ${test.testName}</h3>
    </div>
    <div class="container">
        <form name="download_csv_form" name="download_csv_form">
            <input type="hidden" id="test_id" name="testId" value="${test.id}">
        </form>
        <div class="row">
            <div class="span3">
                <table class="table table-bordered compactpadding">
                    <colgroup>
                        <col width="120px">
                        <col>
                    </colgroup>
                    <tr>
                        <th><@spring.message "perfTest.report.totalVusers"/></th>
                        <td><strong>${test.vuserPerAgent * test.agentCount}</strong></td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.agent"/></th>
                        <td><span>${test.agentCount}</span>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.process"/><br/><@spring.message "perfTest.report.thread"/>
                        </th>
                        <td>${test.processes} / ${test.threads}</td>
                    </tr>
                    <tr>
                        <td colspan="2" class="divider"></td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.ignoreSampleCount"/></th>
                        <td><span>${test.ignoreSampleCount}</span></td>
                    </tr>
                    <tr>
                        <td colspan=2></td>
                    </tr>
                    <tr>
                        <th>TPS</th>
                        <td><strong><#if test.tps??>${(test.tps)?string(",##0.#")}</#if></strong></td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.peakTPS"/></th>
                        <td><strong>${test.peakTps!""}</strong></td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.meantime"/></th>
                        <td><span>${(test.meanTestTime!0)?string(",##0.##")}</span>&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.totalTests"/></th>
                        <td>${(test.tests + test.errors)!""}</td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.successfulTests"/></th>
                        <td>${(test.tests)!""}</td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.errors"/></th>
                        <td>${test.errors!""}</td>
                    </tr>
                    <#-- hugang -->
                    <#-- 新增 错误率，TPS标准差，TPS波动率，最小RT, 最大RT, RT 25/50/75/80/85/90/95/99百分位数 -->
                    <tr>
                        <th><@spring.message "perfTest.report.errorRate"/></th>
                        <td>${(test.errors /(test.tests + test.errors))!""}</td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.tpsStd"/></th>
                        <td>${test.tpsStd!""}</td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.tpsVix"/></th>
                        <td>${test.tpsVix!""}</td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.minRT"/></th>
                        <td>${test.minRT!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.TwentyFiveMeanTime"/></th>
                        <td>${test.twentyFiveMeanTime!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.FiftyMeanTime"/></th>
                        <td>${test.fiftyMeanTime!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.ServentyFiveMeanTime"/></th>
                        <td>${test.serventyFiveMeanTime!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.EightyMeanTime"/></th>
                        <td>${test.eightyMeanTime!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.EightyFiveMeanTime"/></th>
                        <td>${test.eightyFiveMeanTime!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.NinetyMeanTime"/></th>
                        <td>${test.ninetyMeanTime!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.NinetyFiveMeanTime"/></th>
                        <td>${test.ninetyFiveMeanTime!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.NinetyNineMeanTime"/></th>
                        <td>${test.ninetyNineMeanTime!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                    </tr>
                    <tr>
                        <th><@spring.message "perfTest.report.maxRT"/></th>
                        <td>${test.maxRT!""}&nbsp;&nbsp; <code>ms</code></td>
                    </tr>
                </table>

                <div class="well" style="max-width: 340px; padding: 8px 0;">
                    <ul class="nav nav-list">
                        <li class="active pointer-cursor perf  nav-header">
                            <a class="pointer-cursor"><@spring.message "perfTest.report.performanceReport"/></a>
                        </li>
                        <li class="nav-header"><@spring.message "perfTest.report.targetHost"/></li>

                        <@list list_items=test.targetHostIP others="no_message" ; targetIP >
                            <li class="monitor pointer-cursor" ip="${targetIP}">
                                <a class="pointer-cursor">${targetIP}</a>
                            </li>
                        </@list>

                        <li class="nav-header"><@spring.message "perfTest.report.plugins"/></li>
                        <@list list_items=plugins others="no_message" ; each >
                            <li class="plugin pointer-cursor" plugin="${each.first}" ip="${each.second}">
                                <a class="pointer-cursor">${each.first?replace("_", " ")} - ${each.second}</a>
                            </li>
                        </@list>
                    </ul>
                </div>
            </div>
            <div class="span9">
                <table class="table table-bordered" style="margin-bottom:35px">
                    <colgroup>
                        <col width="120">
                        <col width="220">
                        <col width="120">
                        <col>
                    </colgroup>
                    <tr>
                        <th><@spring.message "perfTest.report.startTime"/></th>
                        <td>${(test.startTime?string('yyyy-MM-dd HH:mm:ss'))!"&nbsp"}</td>
                        <th><@spring.message "perfTest.report.finishTime"/></th>
                        <td>${(test.finishTime?string('yyyy-MM-dd HH:mm:ss'))!"&nbsp"}</td>
                    </tr>
                    <#if test.threshold == "D">
                    <tr>
                        <th><@spring.message "perfTest.report.duration"/></th>
                        <td><span>${test.durationStr}</span> <code>HH:MM:SS</code></td>
                        <#else>
                            <th><@spring.message "perfTest.report.runCount"/></th>
                            <td><span>${test.runCount}</td>
                        </#if>
                        <th><@spring.message "perfTest.report.runtime"/></th>
                        <td><span>${test.runtimeStr}</span> <code>HH:MM:SS</code></td>
                    </tr>
                    <#if test.description?has_content>
                        <tr>
                            <th><@spring.message "common.label.description"/></th>
                            <td colspan="3">${(test.description)!?html?replace('\n', '<br>')}</td>
                        </tr>
                    </#if>
                    <#if test.testComment?has_content>
                        <tr>
                            <th><@spring.message "perfTest.report.testComment"/></th>
                            <td colspan="3">${(test.testComment)!?html?replace('\n', '<br>')}</td>
                        </tr>
                    </#if>
                </table>
                <div id="detail_panel">
                </div>
            </div>
        </div>
    </div>
</div>
<#include "../common/copyright.ftl">

<#include "../common/messages.ftl">

<script>
    $(document).ready(function () {
        var $perfMenu = $("li.perf");
        var $monitorMenu = $("li.monitor");
        var $pluginMenu = $("li.plugin");

        $perfMenu.click(function () {
            $("#detail_panel").load("${req.getContextPath()}/perftest/${(test.id)?c}/detail_report/perf");
            changActiveLink($(this));
        });

        $monitorMenu.click(function () {
            $("#detail_panel").load("${req.getContextPath()}/perftest/${(test.id)?c}/detail_report/monitor?targetIP=" + $(this).attr("ip"));
            changActiveLink($(this));
        });
        $pluginMenu.click(function () {
            $("#detail_panel").load("${req.getContextPath()}/perftest/${(test.id)?c}/detail_report/plugin/" + $(this).attr("plugin") +
                "?kind=" + $(this).attr("ip"));
            changActiveLink($(this));
        });

        $.ajaxSetup({"cache": false});
        $perfMenu.click();
    });

    function changActiveLink(obj) {
        $("li.active").removeClass("active");
        obj.addClass("active");
    }

</script>
</body>
</html>
