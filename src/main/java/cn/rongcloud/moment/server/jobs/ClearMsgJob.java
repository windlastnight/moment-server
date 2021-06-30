package cn.rongcloud.moment.server.jobs;

import cn.rongcloud.moment.server.mapper.MessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author renchaoyang
 */
@Slf4j
@Component
public class ClearMsgJob implements InitializingBean {

    @Resource
    private MessageMapper msgMapper;

    @Value("${moment.clear_msg.interval_days}")
    private Long intervalDays;

    @Scheduled(cron = "${moment.clear_msg.cron}")
    public void fillOrgInfoUserStatistic() {
        log.info("ClearMsgJob execute time:{}", LocalDateTime.now());
        handleJob();
    }

    private void handleJob() {
        LocalDate localDate = LocalDate.now();
        LocalDate delLocalDate = localDate.minusDays(intervalDays);
        log.info("del msg before day:{}", delLocalDate);
        Date delDate = Date.from(delLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        this.msgMapper.delMsgBeforeDate(delDate);
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
