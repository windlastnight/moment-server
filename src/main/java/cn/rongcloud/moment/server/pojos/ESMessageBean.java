package cn.rongcloud.moment.server.pojos;

import lombok.Data;
import lombok.experimental.Accessors;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import java.io.IOException;
import java.util.Date;

/**
 * ES 消息实体
 *
 * @author luke
 */
@Data
@Accessors(chain = true)
public class ESMessageBean {

    private String messageId;

    private String feedId;

    private String userId;

    private int messageType;

    private int status;

    private Date createDt;

    private String publishUserId;

    public XContentBuilder toEsContent() {
        try {
            return XContentFactory.jsonBuilder().startObject()
                .field("message_id", messageId)
                .field("feed_id", feedId)
                .field("user_id", userId)
                .field("message_type", messageType)
                .field("status", status)
                .field("create_dt", createDt)
                .field("publish_user_id", publishUserId)
                .endObject();
        } catch (IOException e) {
            throw new RuntimeException("error when build es data : ", e);
        }
    }
}
