# Spring Boot と Spring Integration を使用して RSS を取得するサンプル

Spring Integration 4 から XML が不要になりました。
Spring Boot と Spring Integration を使用して XML を使用せずに Java Config のみを使って RSS を取得するサンプルです。

## 必須

以下のソフトウェアが必要です。あらかじめインストールしておいてください。

* JDK 6 or later
* Maven 3.0 or later

## pom.xml

```xml:pom.xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.1.0.M2</version>
        <relativePath/>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-integration</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-feed</artifactId>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>spring-milestones</id>
            <url>http://repo.spring.io/milestone/</url>
        </repository>
    </repositories>
```

## Application.java

```java:Application.java
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
```

## 実行方法＆実行例

`url` オプションに feed-url を指定して実行すると、10 秒間隔で feed-url にアクセスし、
1 アクセス当たり最大 100 件の RSS エントリーを取得します。
取得した RSS エントリーはログに出力します。

"http://search.goo.ne.jp/rss/newkw.rdf" から RSS エントリーを取得するには、
次のように実行します。

```実行例
mvn package
java -jar target/spring-boot-integration-rss-sample-1.0.jar --url=http://search.goo.ne.jp/rss/newkw.rdf
```

## Complete Source Code

see https://github.com/sunny4381/spring-boot-integration-rss-sample, and execute

```
git clone https://github.com/sunny4381/spring-boot-integration-rss-sample.git
```
