package edu.buaa.service.messaging;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import edu.buaa.domain.Info;
import edu.buaa.domain.Notification;
import edu.buaa.repository.InfoRepository;
import edu.buaa.service.Constant;
import edu.buaa.service.InfoService;
import edu.buaa.service.RaftTask;
import edu.buaa.service.SendTask;
import edu.buaa.service.messaging.channel.GameChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
public class GameNotiConsumer {
    private final Logger log = LoggerFactory.getLogger(GameNotiConsumer.class);

    private Constant constant;
    private InfoService infoService;

    public GameNotiConsumer(Constant constant,InfoService infoService) {
        this.constant = constant;
        this.infoService  = infoService;
    }

    @StreamListener(GameChannel.CHANNELIN)
    public void listen(Notification msg) {
        if(!msg.getOwner().equals("edge2")) {   // 来自其余边缘节点的消息
            if(msg.getType().equals("gameintial") && constant.leader.equals(constant.Edgename)){
                System.err.println(msg.getBody());
            }
            if(msg.getType().equals("translateFile") && msg.getTarget().equals(constant.Edgename)){
                System.err.println("Get File from " + msg.getOwner() );
                // 处理接收到的文件信息并存储
                JSONArray files = JSONArray.parseArray(msg.getBody());
                for(Object file: files) {
                    System.err.println( file.toString() );
                    JSONObject one = (JSONObject) file;
                    Info info = new Info();
                    info.setFile_type(one.getString("filetype"));
                    info.setNote(one.getString("note"));
                    info.setFile_size(one.getLong("filesize"));
                    info.setFile_name(one.getString("filename"));
                    info.setFile_body(one.getBytes("filebody"));
                    info.setFile_bodyContentType(one.getString("filebodyContentType"));
                    if(!infoService.existsbyname(info.getFile_name())){   // 不存在重复的name 就存储
                        infoService.save(info);
                    }
                }
            }
        }
    }
}
