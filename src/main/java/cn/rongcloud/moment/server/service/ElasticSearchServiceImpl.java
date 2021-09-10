package cn.rongcloud.moment.server.service;

import cn.rongcloud.moment.server.common.CustomerConstant;
import cn.rongcloud.moment.server.common.elasticsearch.EsIndexHelper;
import cn.rongcloud.moment.server.common.utils.DateTimeUtils;
import cn.rongcloud.moment.server.common.utils.GsonUtil;
import cn.rongcloud.moment.server.common.utils.UserHolder;
import cn.rongcloud.moment.server.model.Message;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
@Service
public class ElasticSearchServiceImpl implements ElasticSearchService{

    private static final String RCE_ES_INDEX_PREFIX = "cid_" + CustomerConstant.SERVICE_NAME.toLowerCase() + "_";
    private static final String MSG_STAT_INDEX_PREFIX = RCE_ES_INDEX_PREFIX.concat("message_storage_");
    public static final String DEFAULT_TYPE = "default";

    @Autowired
    private EsIndexHelper esIndexHelper;

    @Resource
    private TransportClient esTransportClient;

    @Value("${moment.message.query_time}")
    private int messageQueryTime;

    @Scheduled(cron = "0 0 1 * * ?")
    @PostConstruct
    public void createIndex() {
        log.info("init index");
        esIndexHelper.initDailyIndex(MSG_STAT_INDEX_PREFIX, dataMapping(),
                ImmutableMap.of("index.refresh_interval", "300s", "index.translog.sync_interval", "300s"));
    }

    @Override
    public void saveMessage(Message message) {
        String index = MSG_STAT_INDEX_PREFIX.concat(DateTimeUtils.getDateTimeStringWithoutSplitByMillis(message.getCreateDt().getTime()));
        esTransportClient.prepareIndex(index, DEFAULT_TYPE).setId(message.getMessageId() + message.getUserId()).setSource(toEsContent(message))
                .setRefreshPolicy(WriteRequest.RefreshPolicy.NONE)
                .execute();
    }

    @Override
    public void batchSaveMessage(List<Message> messages) {
        // bulk批量操作
        BulkRequestBuilder bulk = esTransportClient.prepareBulk();
        IndexRequestBuilder requestBuilder = null;
        for (Message message : messages) {
            String index = getIndices(message.getCreateDt());
            requestBuilder = esTransportClient.prepareIndex(index, DEFAULT_TYPE).setId(message.getMessageId() + message.getUserId()).setSource(toEsContent(message));
            // 加入bulk批量操作
            bulk.add(requestBuilder);
        }
        try {
            // 请求过来即刷新
            // BulkResponse response = bulk.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL).get();
            // 执行
            BulkResponse response = bulk.get();
            log.info("save message by messageIds from elastic, status:{}", response.status().name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteByMessageIds(Map<String, Date> messageMap) {
        // bulk批量操作
        BulkRequestBuilder bulk = esTransportClient.prepareBulk();
        // 删除请求
        DeleteRequestBuilder delete = null;
        String userId = UserHolder.getUid();
        for (String messageId : messageMap.keySet()) {
            delete = esTransportClient.prepareDelete(getIndices(messageMap.get(messageId)), DEFAULT_TYPE, messageId + userId);
            // 加入bulk批量操作
            bulk.add(delete);
        }
        try {
            // 请求过来即刷新
            //BulkResponse response = bulk.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL).get();
            // 执行
            BulkResponse response = bulk.get();
            log.info("delete message by messageIds from elastic, status:{}", response.status().name());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long deleteByUserId(String userId) {
        // match 查询
        QueryBuilder build = QueryBuilders.matchQuery("userId", userId);
        // 删除
        BulkByScrollResponse delete = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esTransportClient)
                .source(getIndices())
                .filter(build)
                .get();
        return delete.getDeleted();
    }

    @Override
    public List<Message> getMessageByPage(String userId, Integer size, Message fromMessage) {
        SearchRequestBuilder requestBuilder = esTransportClient.prepareSearch(getIndices());

        BoolQueryBuilder boolQueryBuild = QueryBuilders.boolQuery();
        QueryBuilder matchQuery = QueryBuilders.matchQuery("userId", userId);
        boolQueryBuild.filter(matchQuery);

        // 构建请求  设置查询条件 排序条件
        requestBuilder.setQuery(boolQueryBuild);
        requestBuilder.addSort(SortBuilders.fieldSort("createDt").order(SortOrder.DESC));
        requestBuilder.addSort(SortBuilders.fieldSort("messageId").order(SortOrder.ASC));
        requestBuilder.setSize(size);
        if (fromMessage != null) {
            Object[] sortValues = new Object[]{fromMessage.getCreateDt().getTime(), fromMessage.getMessageId()};
            requestBuilder.searchAfter(sortValues);
        }
        // 请求 es
        SearchResponse response = requestBuilder.get();
        SearchHit[] hits = response.getHits().getHits();

        List<Message> messages = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, Object> hitSource = hit.getSource();
            Message message = (Message) GsonUtil.fromJson(GsonUtil.toJson(hitSource), Message.class);
            messages.add(message);
        }
        return messages;
    }

    @Override
    public Message getMessage(String messageId, String userId) {

        SearchRequestBuilder requestBuilder = esTransportClient.prepareSearch(getIndices());
        BoolQueryBuilder boolQueryBuild = QueryBuilders.boolQuery();
        boolQueryBuild.must(QueryBuilders.matchQuery("messageId", messageId));
        boolQueryBuild.must(QueryBuilders.matchQuery("userId", userId));

        requestBuilder.setQuery(boolQueryBuild);
        requestBuilder.setSize(1);

        // 构建请求  设置查询条件 排序条件
        requestBuilder.setQuery(boolQueryBuild);

        SearchResponse response = requestBuilder.get();
        SearchHit[] hits = response.getHits().getHits();

        for (SearchHit hit : hits) {
            Map<String, Object> hitSource = hit.getSource();
            return (Message) GsonUtil.fromJson(GsonUtil.toJson(hitSource), Message.class);
        }
        return null;
    }

    @Override
    public List<String> getLikeAlreadyNotifyUser(String messageId, String publishUserId) {

        SearchRequestBuilder requestBuilder = esTransportClient.prepareSearch(getIndices());
        BoolQueryBuilder boolQueryBuild = QueryBuilders.boolQuery();
        boolQueryBuild.must(QueryBuilders.matchQuery("messageId", messageId));
        boolQueryBuild.must(QueryBuilders.matchQuery("publishUserId", publishUserId));

        requestBuilder.setQuery(boolQueryBuild);
        requestBuilder.addDocValueField("userId");
        requestBuilder.setSize(10000);

        // 构建请求  设置查询条件 排序条件
        requestBuilder.setQuery(boolQueryBuild);

        SearchResponse response = requestBuilder.get();
        SearchHit[] hits = response.getHits().getHits();

        List<String> userIds = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, Object> hitSource = hit.getSource();
            Message message = (Message) GsonUtil.fromJson(GsonUtil.toJson(hitSource), Message.class);
            userIds.add(message.getUserId());
        }
        return userIds;
    }

    private String[] getIndices(){
        String[] indices = new String[messageQueryTime];
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(EsIndexHelper.defaultPattern);
        c.add(Calendar.MONTH, -messageQueryTime);
        for (int i = 0; i < messageQueryTime; i++) {
            c.add(Calendar.MONTH, 1);
            String ds = sdf.format(c.getTime());
            indices[i] = MSG_STAT_INDEX_PREFIX + ds;
        }
        return indices;
    }

    private String getIndices(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(EsIndexHelper.defaultPattern);
        return MSG_STAT_INDEX_PREFIX + sdf.format(date.getTime());
    }


    public XContentBuilder toEsContent(Message message) {
        try {
            return XContentFactory.jsonBuilder().startObject()
                    .field("messageId", message.getMessageId())
                    .field("feedId", message.getFeedId())
                    .field("userId", message.getUserId())
                    .field("messageType", message.getMessageType())
                    .field("status", message.getStatus())
                    .field("createDt", message.getCreateDt())
                    .field("publishUserId", message.getPublishUserId())
                    .endObject();
        } catch (IOException e) {
            throw new RuntimeException("error when build es data : ", e);
        }
    }

    private XContentBuilder dataMapping() {

        try {
            return XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject(DEFAULT_TYPE)
                    .startObject("properties")
                    .startObject("messageId")
                    .field("type", "keyword")
                    .endObject()
                    .startObject("feedId")
                    .field("type", "keyword")
                    .endObject()
                    .startObject("userId")
                    .field("type", "keyword")
                    .endObject()
                    .startObject("messageType")
                    .field("type", "keyword")
                    .endObject()
                    .startObject("status")
                    .field("type", "keyword")
                    .endObject()
                    .startObject("createDt")
                    .field("type", "date")
                    .endObject()
                    .startObject("publishUserId")
                    .field("type", "keyword")
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject();
        } catch (IOException e) {
            log.info("error builder dataMapping, {} ", e.getMessage());
            throw new RuntimeException("error builder dataMapping ");
        }
    }

}
