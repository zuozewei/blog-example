var entities = require('v1/entities');
var http = require('v1/http');
var workflow = require('v1/workflow');
var DINGDING_ROBOT_URL = 'https://oapi.dingtalk.com';
var DINGDING_ROBOT_URL_DIRECTION = '/robot/send?access_token=xxxxxxxxx';

exports.rule = entities.Issue.onChange({

    title: 'cpjf-state-changed-notify-dingtalk',
    guard: function(ctx) {
        var issue = ctx.issue;
        return ctx.issue.fields.isChanged(ctx.State) && (ctx.issue.fields.State.name === 'F-已选择' || ctx.issue.fields.State.name === 'F-待测试' || ctx.issue.fields.State.name === 'F-待开发');
    },

    action: function(ctx) {
        var issue = ctx.issue;
        var issueId = issue.id;
        var issueName = issue.summary;
        var issueDescription = issue.description;
        var issueLink = issue.url;
        var userName = ctx.currentUser.fullName;
        var nowState = issue.fields.State.name;
        var contextStr = "";
        var payload = "";
        var nowStateArr=nowState.split("-");

        var result = {
            '魏xx': '178xxxx9463',
            '沈xx': '136xxxx0845',
        };

        var atPersons = "";
        var atPersonsPhone = "";
        var notifyArr = [];
        if (issue.fields.指派人 !== null && issue.fields.指派人.size > 0) {
            for (var i = 0; i < issue.fields.指派人.size; i++) {
                var person = issue.fields.指派人.get(i).fullName;
                var phone = result[person];
                atPersons += "、 " + person;
                atPersonsPhone += "、 @" + phone;
                notifyArr.push(phone);
            }
        }
        atPersons = atPersons.substring(1);
        atPersonsPhone = atPersonsPhone.substring(1);

        contextStr = " #### 产品交付 # " + issueId + " \n" +
            " ##### **名称**：" + issueName + "\n\n" +
            " ##### **状态**：" + nowState + " \n\n" +
            " ##### **链接**：[查看详情](" + issueLink + ") \n" +
            " ##### **操作**：" +"["  + userName + "] 同学将任务指向" + atPersonsPhone + "，请尽快处理！ \n\n" +
            " > ###### 技术团队 \n";

        payload =  {
            "msgtype": "markdown",
            "markdown": {
                "title": "产品交付通知",
                "text": contextStr
            },
            "at": {
                "atMobiles": notifyArr,
                "isAtAll": false
            }
        };

        var connection = new http.Connection(DINGDING_ROBOT_URL);

        connection.addHeader({
            name: "Content-Type",
            value: "application/json; charset=utf-8"
        });

        connection.postSync(DINGDING_ROBOT_URL_DIRECTION, [], JSON.stringify(payload));

    },
    requirements: {
        State: {
            type: entities.State.fieldType,
        },
        指派人: {
            type: entities.User.fieldType,
            multi: true,
        }
    }
});
