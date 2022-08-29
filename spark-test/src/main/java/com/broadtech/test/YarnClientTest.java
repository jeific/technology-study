package com.broadtech.test;

import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.IOException;
import java.util.*;

public class YarnClientTest {

    public static void main(String[] args) throws IOException, YarnException {
        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(new YarnConfiguration());
        yarnClient.start();

        Set<String> appTypes = new HashSet<>();
        EnumSet<YarnApplicationState> appStates = EnumSet.noneOf(YarnApplicationState.class);
        appStates.addAll(Arrays.asList(YarnApplicationState.RUNNING, YarnApplicationState.ACCEPTED));
        List<ApplicationReport> ret = yarnClient.getApplications(appTypes, appStates);
        for (ApplicationReport app : ret) {
            System.out.println(app.getApplicationId()
                    + "\t" + app.getQueue()
                    + "\t" + app.getName());
        }

        List<QueueInfo> queues = yarnClient.getAllQueues();
        for (QueueInfo queue : queues) {
            System.out.println(queue.getQueueName()
                    + "\t" + queue.getQueueState()
                    + "\t" + queue.getApplications().size()
                    + "\t" + queue.getAccessibleNodeLabels());
        }
        QueueInfo searchInfo = yarnClient.getQueueInfo("root.users.hdfs");
        System.out.println(searchInfo);

        yarnClient.close();
    }
}
