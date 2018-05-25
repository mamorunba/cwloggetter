package com.example.cwloggetter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS情報をプロパティファイルから取得するクラス。
 *
 * @author M.Asano
 *
 */
@Component
@ConfigurationProperties(prefix = "aws-property")
public class AwsProperty {
    private String cloudWatchLogsEndPoint;

    public String getCloudWatchLogsEndPoint() {
        return cloudWatchLogsEndPoint;
    }

    public void setCloudWatchLogsEndPoint(String cloudWatchLogsEndPoint) {
        this.cloudWatchLogsEndPoint = cloudWatchLogsEndPoint;
    }
}
