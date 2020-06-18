package edu.buaa.service;

import com.alibaba.fastjson.JSONObject;
import edu.buaa.domain.Notification;
import edu.buaa.service.messaging.ShareNotiProducer;
import org.aspectj.weaver.ast.Not;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.stereotype.Service;

@Service
@EnableBinding(SendTask.class)
public class SendTask {
    private ShareNotiProducer shareNotiProducer;
    private Constant constant;
    public SendTask (ShareNotiProducer shareNotiProducer, Constant constant){
        this.shareNotiProducer = shareNotiProducer;
        this.constant = constant;
    }

    public void sendRaft(int id, JSONObject jsonObject){
        Notification notification = new Notification();
        notification.setType("back");
        notification.setOwnerId(2);
        notification.setOwner(constant.Edgename);
        notification.setTargetId(id);
        notification.setBody(jsonObject.toJSONString());
        shareNotiProducer.sendMsgToEdges(notification);
    }

    public void sendRequest(){
        Notification msg = new Notification();
        msg.setOwner(constant.Edgename);
        msg.setOwnerId(2);
        msg.setType("request");
        msg.setBody("request for leader!");
        shareNotiProducer.sendMsgToEdges(msg);
    }
}
