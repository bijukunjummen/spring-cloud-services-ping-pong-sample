package org.bk.consumer.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.bk.consumer.domain.Message;
import org.bk.consumer.domain.MessageAcknowledgement;
import org.bk.consumer.feign.PongClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("hystrixPongClient")
public class HystrixWrappedPongClient implements PongClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HystrixWrappedPongClient.class);

	@Autowired
	@Qualifier("ribbonPongClient")
	private PongClient feignPongClient;

	@Override
	@HystrixCommand(commandKey = "pongClient", groupKey = "pongGroup", fallbackMethod = "fallBackCall")
	public MessageAcknowledgement sendMessage(Message message) {
		try {
			return this.feignPongClient.sendMessage(message);
		}catch(Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw  e;
		}
	}

	public MessageAcknowledgement fallBackCall(Message message) {
		MessageAcknowledgement fallback = new MessageAcknowledgement(message.getId(), message.getPayload(), "FAILED SERVICE CALL! - FALLING BACK");
		return fallback;
	}
}
