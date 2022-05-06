# netflow-java
Modified LUMASERV's netflow v9 parser and collector for "Cascade" project

## Usage
```java
NetFlowSession session = new NetFlowSession(source -> {
    source.listen((id, values) -> {
        // DO WHATEVER YOU WANT
    });
});
NetFlowCollector collector = new NetFlowCollector(session);
collector.join();
```

## Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<dependency>
    <groupId>com.github.FoLoKe</groupId>
    <artifactId>netflow-java</artifactId>
    <version>9337a8c</version>
</dependency>
```