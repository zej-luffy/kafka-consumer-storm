package com.homework3.ylc.kafka_consumer_storm;

import java.util.UUID;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;

import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;


public class KafkaProcessing {
    private final String BOLT_ID = MyBolt.class.getName();  
    private final String SPOUT_ID = KafkaSpout.class.getName();
    
    private final TopologyBuilder builder;
    private final Config config;
    
    private LocalCluster localCluster = null;
    
    
    public KafkaProcessing()
    {
		builder = new TopologyBuilder();
		// zookeeper地址 
		String zkConnString = "172.31.42.237:2181,172.31.43.30:2181,172.31.42.86:2181,172.31.42.215:2181";
		String brokerZkStr = "/brokers";
		ZkHosts zkHosts = new ZkHosts(zkConnString,brokerZkStr);  
		// topic  
		String topic = "mykafka";
		// zookeeper的根目录
		String zkRootPath = "/brokers/topics";  
		String id = UUID.randomUUID().toString();  
		SpoutConfig spoutconf  = new SpoutConfig(zkHosts, topic, zkRootPath, id);
		spoutconf.ignoreZkOffsets = true;
		builder.setSpout(SPOUT_ID , new KafkaSpout(spoutconf));
		builder.setBolt(BOLT_ID,new MyBolt()).shuffleGrouping(SPOUT_ID);
		
		config = new Config();
		config.setDebug(false);
		// 设置worker的数量
		config.setNumWorkers(4);
		// 最大并行的数量
		config.setMaxTaskParallelism(3);
//		config.put(Config.NIMBUS_SEEDS, Arrays.asList(new String[] {"172.31.42.237"}));
		config.put(Config.NIMBUS_THRIFT_PORT, 6627);
    }
    
    public void runLocalCluster()
    {
    	localCluster = new LocalCluster(); 
    	localCluster.submitTopology(KafkaProcessing.class.getSimpleName(), config,builder.createTopology());
    }
    
    public void shutdown()
    {
    	if(localCluster != null)
    	{
    		localCluster.shutdown();
    	}
    }
    
    public void runCluster()
    {
        try {
        	StormSubmitter.submitTopology(KafkaProcessing.class.getSimpleName(), 
					config, builder.createTopology());
		} catch (AlreadyAliveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTopologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }
    
	public static void main(String[] args)
	{
		KafkaProcessing kp = new KafkaProcessing();
		kp.runLocalCluster();
//		kp.shutdown();
    }  
}
