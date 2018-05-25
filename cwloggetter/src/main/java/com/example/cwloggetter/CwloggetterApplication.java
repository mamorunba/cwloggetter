package com.example.cwloggetter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.model.FilterLogEventsRequest;
import com.amazonaws.services.logs.model.FilterLogEventsResult;

/**
 * CloudWatchLogsのログをログファイルに出力するクラス。
 *
 * @author M.Asano
 *
 */
@SpringBootApplication
public class CwloggetterApplication implements CommandLineRunner {
    static Log log = LogFactory.getLog(CwloggetterApplication.class);

    // use the role of the lambda to determine policy.
    protected static final AWSCredentialsProvider CREDENTIALS_PROVIDER =
            new DefaultAWSCredentialsProviderChain();

    @Autowired
    private AwsProperty awsProp;


	public static void main(String[] args) {
        SpringApplication application = new SpringApplication(CwloggetterApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
	}

    /**
     * CloudWatchLogsのログをログファイルに出力する。
     * @param args args[0]：ロググループ、args[1]：ログ取得開始日時（yyyyMMdd HH:mm:ss）、args[2]：ログ取得終了日時（yyyyMMdd HH:mm:ss）※省略可。省略した場合、現在時刻で取得。
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("===== RUN =====");

        if (args.length > 2) {
            log.info("logGroup=[" + args[0] + "]  startTime=[" + args[1] + "]  endTime=[" + args[2] + "]");
        } else if (args.length == 2) {
            log.info("logGroup=[" + args[0] + "]  startTime=[" + args[1] + "]  endTime=[null]");
        } else {
            throw new IllegalArgumentException("args length invalid(necessary 2 or 3)");
        }

        String logGroup = args[0];
        String startTime = args[1];
        String endTime = null;
        if (args.length > 2) {
            endTime = args[2];
        }

        outputCloudWatchLog(logGroup, startTime, endTime);
    }


    private void outputCloudWatchLog(String logGroup, String startTimeStr, String endTimeStr) throws ParseException{

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Date st = sdf.parse(startTimeStr);
        long startTime = st.getTime();

        long endTime = 0;
        if (endTimeStr == null) {
            endTime = new Date().getTime();
        } else {
            Date ed = sdf.parse(endTimeStr);
            endTime = ed.getTime();
        }


        ClientConfiguration logsClientCfg = new ClientConfiguration();
        Protocol logsProtocolType = Protocol.HTTPS;
        logsClientCfg.setProtocol(logsProtocolType);

        AWSLogsClient cloudWatchLogs = new AWSLogsClient(CREDENTIALS_PROVIDER, logsClientCfg)
                .withEndpoint(awsProp.getCloudWatchLogsEndPoint())
                .withRegion(Region.getRegion(Regions.AP_NORTHEAST_1));

        try {

            FilterLogEventsRequest request = new FilterLogEventsRequest();
            request.setStartTime(startTime);
            request.setEndTime(endTime);
            request.setLogGroupName(logGroup);
            log.info(request.toString());

            FilterLogEventsResult result = cloudWatchLogs.filterLogEvents(request);
            result.getNextToken();
            log.info(result.toString());
            log.info("events found:"+result.getEvents().size());
            result.getEvents().forEach(event->{
                log.info(event.toString());
            });

        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
