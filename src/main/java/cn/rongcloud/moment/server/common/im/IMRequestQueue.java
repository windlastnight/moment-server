package cn.rongcloud.moment.server.common.im;

import cn.rongcloud.moment.server.common.im.config.IMConfig;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import io.rong.RongCloud;
import io.rong.messages.BaseMessage;
import io.rong.models.message.PrivateMessage;
import io.rong.models.message.PrivateStatusMessage;
import io.rong.models.message.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by taoli on 2017/1/6.
 */
public class IMRequestQueue {

    static private Logger LOGGER = LoggerFactory.getLogger(IMRequestQueue.class);
    private static IMRequestQueue messenger;
    public static IMRequestQueue getInstance() {
        if (messenger == null) {
            synchronized (IMRequestQueue.class) {
                if (messenger == null) {
                    messenger = new IMRequestQueue();
                }
            }
        }
        return messenger;
    }


    public static class MessageObject {
        public static class MessageObjectBuilder {
            String fromUser;
            ConversationType conversationType;
            String[] targetIds;
            BaseMessage messageContent;
            String pushContent;
            String pushData;
            String count;
            int isPersist;
            int isIncludeSender;
            int isCount;
            int isStatus;
            int retryTimes;
            IMConfig imConfig;

            public MessageObjectBuilder() {
                this.fromUser = null;
                this.conversationType = ConversationType.PRIVATE;
                this.targetIds = null;
                this.messageContent = null;
                this.pushContent = null;
                this.pushData = null;
                count = null;
                isPersist = 0;
                isCount = 0;
                isStatus = 0;
                isIncludeSender = 1;
                retryTimes = 0;
            }

            public MessageObjectBuilder conversation(ConversationType conversationType, String targetId) {
                this.conversationType = conversationType;
                this.targetIds = new String[1];
                this.targetIds[0] = targetId;
                return this;
            }

            public MessageObjectBuilder conversations(ConversationType conversationType, List<String> targetIdList) {
                this.conversationType = conversationType;
                this.targetIds = targetIdList.toArray(new String[0]);
                return this;
            }

            public MessageObjectBuilder from(String user) {
                this.fromUser = user;
                return this;
            }

            public MessageObjectBuilder content(BaseMessage messageContent) {
                this.messageContent = messageContent;
                return this;
            }

            public MessageObjectBuilder push(String pushContent, String pushData) {
                this.pushContent = pushContent;
                this.pushData = pushData;
                return this;
            }

            public MessageObjectBuilder persist(int isPersist) {
                this.isPersist = isPersist;
                return this;
            }

            public MessageObjectBuilder isCount(int isCount) {
                this.isCount = isCount;
                return this;
            }

            public MessageObjectBuilder includeSender(int isIncludeSender) {
                this.isIncludeSender = isIncludeSender;
                return this;
            }

            public MessageObjectBuilder count(String count) {
                this.count = count;
                return this;
            }

            public MessageObject.MessageObjectBuilder status(int isStatus) {
                this.isStatus = isStatus;
                return this;
            }

            public MessageObject.MessageObjectBuilder retryTimes(int retryTimes) {
                this.retryTimes = retryTimes;
                return this;
            }

            public MessageObject.MessageObjectBuilder imConfig(IMConfig imConfig) {
                this.imConfig = imConfig;
                return this;
            }

            public void buildAndSend() {
                MessageObject object = new MessageObject();
                object.conversationType = conversationType;
                object.targetIds = targetIds;
                object.fromUser = fromUser;
                object.messageContent = messageContent;
                object.pushContent = pushContent;
                object.pushData = pushData;
                object.count = count;
                object.isPersist = isPersist;
                object.isCount = isCount;
                object.isStatus = isStatus;
                object.imConfig = imConfig;

                // 私聊，并且是自己发送给自己，那么includeSender强制置0，不然会收到两条消息
                if (conversationType == ConversationType.PRIVATE && fromUser.equals(targetIds[0])){
                    object.isIncludeSender = 0;
                }else {
                    object.isIncludeSender = isIncludeSender;
                }
                object.retryTimes = retryTimes;
                object.send();
            }

        }

        String fromUser;
        ConversationType conversationType;
        String[] targetIds;
        BaseMessage messageContent;
        String pushContent;
        String pushData;
        String count;
        int isPersist;
        int isCount;
        int isStatus;
        int isIncludeSender;
        int retryTimes;
        IMConfig imConfig;

        @Override
        public String toString() {
            return "MessageObject [fromUser=" + fromUser + ", conversationType=" + conversationType + ", targetIds="
                    + Arrays.toString(targetIds) + ", messageContent=" + messageContent + ", pushContent=" + pushContent
                    + ", pushData=" + pushData + ", count=" + count + ", isPersist=" + isPersist + ", isCount="
                    + isCount + ", isStatus=" + isStatus + ", isIncludeSender=" + isIncludeSender + ", retryTimes="
                    + retryTimes + "]";
        }

        public void send() {
            IMRequestQueue.getInstance().deliver(this);
        }
        private int getMaxTargetCount() {
            int maxCount  = 100;
            if (conversationType == ConversationType.SYSTEM) {
                maxCount = 500;
            } else if (conversationType == ConversationType.DISCUSSION) {
                maxCount = 1;
            }
            return maxCount;
        }
        public boolean isReceiverMaxCountExceed() {
            return targetIds.length > getMaxTargetCount();
        }
        public List<MessageObject> split() {
            int maxCount  = getMaxTargetCount();

            ArrayList<MessageObject> out = new ArrayList<>();
            for(int i = 0; i < (targetIds.length + maxCount - 1)/maxCount; i++) {
                int subLength;
                if (i== (targetIds.length + maxCount - 1)/maxCount - 1) {
                    subLength = targetIds.length - i * maxCount;
                } else
                    subLength = maxCount;

                String[] subIds = new String[subLength];
                for (int j = 0; j < subIds.length; j++) {
                    String id = targetIds[i*maxCount + j];
                    subIds[j] = id;
                }
                MessageObject object = new MessageObject();
                object.fromUser = fromUser;
                object.conversationType = conversationType;
                object.targetIds = subIds;
                object.messageContent = messageContent;
                object.pushContent = pushContent;
                object.pushData = pushData;
                object.isPersist = isPersist;
                object.isCount = isCount;
                object.isStatus = isStatus;
                object.isIncludeSender = isIncludeSender;
                object.retryTimes = retryTimes;
                object.imConfig = imConfig;

                out.add(object);
            }
            return out;
        }
    }

    private ExecutorService executorService;



    public static IMRequestQueue.MessageObject.MessageObjectBuilder messageBuilder(String fromUser, ConversationType conversationType, String targetId, BaseMessage messageContent) {
        return new IMRequestQueue.MessageObject.MessageObjectBuilder().conversation(conversationType, targetId)
                .content(messageContent)
                .from(fromUser);
    }

    public static IMRequestQueue.MessageObject.MessageObjectBuilder messageBuilder(String fromUser, ConversationType conversationType, List<String> targetIds, BaseMessage messageContent) {
        return new IMRequestQueue.MessageObject.MessageObjectBuilder().conversations(conversationType, targetIds)
                .content(messageContent)
                .from(fromUser);
    }

    private void deliver(final MessageObject object) {
        if (object.isReceiverMaxCountExceed()) {
            for (MessageObject subObject : object.split()) {
                deliver(subObject);
            }
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                publishMessage(object);
            }
        });
    }

    public void shutdown(){
        executorService.shutdownNow();
        executorService = null;
        messenger = null;
    }

    private IMRequestQueue() {
        executorService = new ThreadPoolExecutor(1,50,5, TimeUnit.SECONDS,new LinkedBlockingDeque<>());
    }

    private void publishMessage(MessageObject object) {
        int code = 0;
        try {
            if( object.conversationType == ConversationType.SYSTEM) {
                if (object.isStatus == 1) {
                    PrivateStatusMessage message = new PrivateStatusMessage();
                    message.setSenderId(object.fromUser);
                    message.setContent(object.messageContent);
                    message.setIsIncludeSender(object.isIncludeSender);
                    message.setObjectName(object.messageContent.getType());
                    message.setTargetId(object.targetIds);
                    message.setVerifyBlacklist(0);
                    code = RongCloud.getInstance(object.imConfig.getAppKey(), object.imConfig.getSecret(), object.imConfig.getHost()).message.msgPrivate.sendStatusMessage(message).getCode();
                    LOGGER.info("call im publish message, message:{}, code:{}", GsonUtil.toJson(message), code);
                } else {
                    PrivateMessage message = new PrivateMessage(object.fromUser, object.targetIds, object.messageContent.getType(), object.messageContent, object.pushContent, object.pushData, null, object.isPersist, object.isCount, 0, object.isIncludeSender, 0);
                    code = RongCloud.getInstance(object.imConfig.getAppKey(), object.imConfig.getSecret(), object.imConfig.getHost()).message.msgPrivate.send(message).getCode();
                    LOGGER.info("call im publish message, message:{}, code:{}", GsonUtil.toJson(message), code);
                }

            } else {
                LOGGER.error("UnSupport conversation type: {}", object.conversationType);
                return;
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Publish message:{}, IllegalArgumentException:{} ", object, e.getMessage());
            return;
        } catch (Exception e) {
            LOGGER.error("Publish message http error: {}", e.getMessage());
            e.printStackTrace();
        }
        /*
            code	描述	        		详细解释														HTTP 状态码
            404	    未找到				服务器找不到请求的地址											404
            1000	服务内部错误			服务器端内部逻辑错误,请稍后重试									500
            1001	App Secret 错误		App Key 与 App Secret 不匹配									401
            1002	参数错误				参数错误，详细的描述信息会说明									400
            1003	无 POST 数据			没有 POST 任何数据											400
            1004	验证签名错误			验证签名错误													401
            1005	参数长度超限			参数长度超限，详细的描述信息会说明								400
            1006	App 被锁定或删除		App 被锁定或删除												401
            1007	被限制调用			该方法被限制调用，详细的描述信息会说明								401
            1008	调用频率超限			调用频率超限，详细的描述信息会说明，广播消息未开通时也会返回此状态码。	429
            1009	服务未开通			未开通该服务，请到开发者管理后台开通。								430
            1015	删除的数据不存在		要删除的保活聊天室 ID 不存在。									200
            1016	设置保活聊天室个数超限	设置的保活聊天室个数超限。										403
            1050	内部服务超时			内部服务响应超时												504
            2007	测试用户数量超限		测试用户数量超限												403
         */
        if (code == 1008) {
            LOGGER.error("Message:{}, send failure with code:{}", object, code);
            if ( object.retryTimes > 0 && (code == 0 || code == 1000 || code == 1008 || code == 1050)) {
                object.retryTimes--;
                long delayTime = 500;
                LOGGER.info("Sleep {} msec, retry send message ", delayTime);
                try {
                    Thread.sleep(delayTime);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                deliver(object);
            }
        }
    }
}
