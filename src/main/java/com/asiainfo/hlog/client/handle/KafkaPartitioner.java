package com.asiainfo.hlog.client.handle;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

/**
 * Created by chenfeng on 2015/5/12.
 */
public class KafkaPartitioner implements Partitioner {
    public KafkaPartitioner (VerifiableProperties props) {

    }

    public int partition(Object key, int numPartitions) {
        try {
            int partitionNum = Integer.parseInt(key.toString());
            return Math.abs(partitionNum % numPartitions);
        } catch (Exception e) {
            return Math.abs(key.hashCode() % numPartitions);
        }
    }
}
