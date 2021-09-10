package cn.rongcloud.moment.server.common.elasticsearch.config;

import com.floragunn.searchguard.ssl.SearchGuardSSLPlugin;
import com.floragunn.searchguard.ssl.util.SSLConfigConstants;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Configuration
public class ESClientConfig {

    @Value("${elasticsearch.searchguard.pemcert_filepath}")
    private String pemCertFilepath;
    @Value("${elasticsearch.searchguard.pemkey_filepath}")
    private String pemKeyFilepath;
    @Value("${elasticsearch.searchguard.pemtrustedcas_filepath}")
    private String pemTrustedCasFilepath;
    @Value("${elasticsearch.searchguard.pemkey_password}")
    private String pemKeyPassword;
    @Value("${elasticsearch.cluster_name}")
    private String clusterName;
    @Value("${elasticsearch.hosts}")
    private String hosts;

    @Bean(name = "esTransportClient")
    public TransportClient createTransportClient() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        Settings settings;
        TransportClient client;
        if (StringUtils.isEmpty(pemCertFilepath) || StringUtils.isEmpty(pemKeyFilepath)) {
            settings = Settings.builder().put("client.transport.ignore_cluster_name", true).build();
            client = new PreBuiltTransportClient(settings);
        } else {
            settings = Settings.builder()
                    .put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_PEMCERT_FILEPATH, pemCertFilepath)
                    .put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_PEMKEY_FILEPATH, pemKeyFilepath)
                    .put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_PEMTRUSTEDCAS_FILEPATH, pemTrustedCasFilepath)
                    .put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_PEMKEY_PASSWORD, pemKeyPassword)
                    .put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_ENFORCE_HOSTNAME_VERIFICATION, false)
                    .put("cluster.name", clusterName)
                    .put("client.transport.sniff",false)
                    .build();
            client = new PreBuiltTransportClient(settings, Lists.newArrayList(SearchGuardSSLPlugin.class));
        }

        try {
            for (int i = 0; i <= getNodesNum() - 1; i++) {
                String address = getNodeHost(i);
                String port = getNodePort(i);
                if (address != null && port != null) {
                    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address), Integer.parseInt(port)));
                }
            }
        } catch (UnknownHostException e) {
            log.error("UnknownHostException error, e:{}", e.getMessage());
        }
        return client;
    }

    private int getNodesNum() {
        String[] nodes = hosts.split(",");
        return nodes.length;
    }
    private String getNodeHost(int num){
        String[] nodes = hosts.split(",");
        String node = nodes[num];
        return node.split(":")[0];
    }
    private String getNodePort(int num){
        String[] nodes = hosts.split(",");
        String node = nodes[num];
        return node.split(":")[1];
    }
}