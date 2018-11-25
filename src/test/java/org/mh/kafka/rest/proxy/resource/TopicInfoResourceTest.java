/*
 *  Copyright 2016, 2018 Markus Helbig
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.mh.kafka.rest.proxy.resource;

import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mh.kafka.rest.proxy.AbstractMvcTest;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class TopicInfoResourceTest extends AbstractMvcTest {

    @Test
    public void testListTopics() throws Exception {
        when(topicListInfo.getTopcis()).thenReturn(List.of("test"));
        mvc.perform(get("/topicslist"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"test\"]"));
    }

    @Test
    public void testGetTopicInfo() throws Exception {
        when(kafkaTemplate.partitionsFor("test")).thenAnswer((Answer<List<PartitionInfo>>) invocation -> List.of(new PartitionInfo("test", 1, new Node(1, "localhost", 1), new Node[]{}, new Node[]{})));
        mvc.perform(get("/topicsinfo/test"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"{topic: test, partition: 1, leader: {id: 1 }}\"]"));
    }

    @Test
    public void testMetrics() throws Exception {
        when(kafkaTemplate.metrics()).thenAnswer(new Answer<Map<MetricName, ? extends Metric>>() {
            @Override
            public Map<MetricName, ? extends Metric> answer(InvocationOnMock invocation) throws Throwable {
                Map<MetricName, Metric> metrics = new HashMap<>();
                MetricName metricName = new MetricName("name", "group", "description", new HashMap<>());
                metrics.put(metricName, new Metric() {
                    @Override
                    public MetricName metricName() {
                        return metricName;
                    }

                    @Override
                    public double value() {
                        return 0;
                    }

                    @Override
                    public Object metricValue() {
                        return 0;
                    }
                });
                return metrics;
            }
        });
        mvc.perform(get("/metrics"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"MetricName [name=name, group=group, description=description, tags={}]\":{}}"));
    }
}