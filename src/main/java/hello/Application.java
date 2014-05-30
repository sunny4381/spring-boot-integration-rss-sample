/*
 * Copyright 2014 NAKANO Hideo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hello;

import com.sun.syndication.feed.synd.SyndEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.feed.inbound.FeedEntryMessageSource;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {
	private static final Logger LOG = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
		System.in.read();
		Runtime.getRuntime().exit(SpringApplication.exit(ctx));
	}

	@Autowired
	private Environment env;

	@Bean
	@InboundChannelAdapter(value = "feedChannel",
			poller = @Poller(maxMessagesPerPoll = "100", fixedRate = "10000"))
	public MessageSource<SyndEntry> feedAdapter() throws MalformedURLException {
		return new FeedEntryMessageSource(new URL(env.getProperty("url")), "feedAdapter");
	}

	@MessageEndpoint
	public static class Endpoint {
		@ServiceActivator(inputChannel = "feedChannel")
		public void log(Message<SyndEntry> message) {
			SyndEntry payload = message.getPayload();
			LOG.info(payload.getPublishedDate() + " - " + payload.getTitle());
		}
	}

	@Bean
	public MessageChannel feedChannel() {
		return new QueueChannel(500);
	}

	// <int:poller id="poller" default="true" fixed-rate="10"/>
	@Bean(name = PollerMetadata.DEFAULT_POLLER)
	public PollerMetadata poller() {
		PeriodicTrigger trigger = new PeriodicTrigger(10);
		trigger.setFixedRate(true);
		PollerMetadata pollerMetadata = new PollerMetadata();
		pollerMetadata.setTrigger(trigger);
		return pollerMetadata;
	}
}
