package cn.rongcloud.moment.server.common.elasticsearch;

import cn.rongcloud.moment.server.common.utils.ApplicationUtil;
import cn.rongcloud.moment.server.common.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * 索引创建工具
 *
 * @author luke
 */
@Slf4j
@Component
public class EsIndexHelper {

    @Resource
    private TransportClient esTransportClient;

    @Value("${moment.message.query_time}")
    private int messageQueryTime;

    public static final String defaultPattern = "yyyyMM";

    public static final String DEFAULT_TYPE = "default";

    public void initDailyIndex(String prefix, XContentBuilder dataMapping, Map setting) {
        this.initDailyIndex(prefix, defaultPattern, DEFAULT_TYPE, dataMapping, setting);
    }

    public void initSingleIndex(String name, String type ,XContentBuilder dataMapping ){
        if (!indexExist(name)) {
            createIndex(name, type, dataMapping);
        }
    }

    public void deleteIndexWithPrefix(String prefix){
        esTransportClient.admin().indices().prepareDelete(prefix.concat("*")).get();
    }


    public void initDailyIndex(String prefix, String pattern, String type,
        XContentBuilder dataMapping, Map settings) {

        int nextMonthOffSet = 6 + messageQueryTime;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        c.add(Calendar.MONTH, -messageQueryTime);
        for (int i = 0; i < nextMonthOffSet; i++) {
            c.add(Calendar.MONTH, 1);
            String ds = sdf.format(c.getTime());
            String index = prefix.concat(ds);
            if (indexExist(index)) {
                continue;
            }

            createIndex(index, type, dataMapping);

            Optional.ofNullable(settings).ifPresent(idxSetting -> this.dailyIndexSetting(index, idxSetting));
        }
    }

    private boolean indexExist(String indexDaily) {
        return esTransportClient.admin().indices().prepareExists(indexDaily)
            .execute().actionGet()
            .isExists();
    }

    public void dailyIndexSetting(String index, Map settings) {

        log.info("index {} create with setting : {} " ,index, settings);
        UpdateSettingsRequest request = new UpdateSettingsRequest();
        request.indices(index).settings(settings);
    }

    public void createIndex(String index, String type, XContentBuilder dataMapping) {
        log.info("init index {} ", index);
        esTransportClient.admin().indices().prepareCreate(index).get();
        try {
            esTransportClient.admin().indices().putMapping(
                Requests.putMappingRequest(index).type(type).source(dataMapping)).get();
        } catch (Exception e) {
            log.error("init index datamapping {} error ", e);
        }
    }

    public List<String> getDailyDateStringIndexName(final String prefix, final String begin,
        final String end) {
        String datePattern = "yyyyMMdd";
        return DateTimeUtils.dailyListWithStartAndEnd(begin, end, datePattern).stream().map(s -> prefix.concat(s)).collect(toList());
    }


    public void deleteIndex(String indexName){
        ApplicationUtil.getBean(TransportClient.class).admin().indices().prepareDelete(indexName).get();
    }

}
