package com.messagebus.managesystem.action.monitor;

import com.messagebus.managesystem.action.other.BaseAction;
import com.messagebus.managesystem.pojo.rabbitHTTP.Queue;
import com.messagebus.managesystem.service.IQueueService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * monitor for queue
 */
public class QueueAction extends BaseAction {

    @Autowired
    private IQueueService queueService;

    public String index() {
        super.index();
        ServletActionContext.getRequest().setAttribute("pageName", "monitor/queue");
        return "index";
    }

    public void list() {
        HttpServletRequest req = ServletActionContext.getRequest();
        HttpServletResponse resp = ServletActionContext.getResponse();
        Queue[] queues = queueService.list();

        String jsonArr = gson.toJson(queues);
        jsonArr = generateListSuccessJSONStr(jsonArr);
        responseJTableData(resp, jsonArr);
    }

}
