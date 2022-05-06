package com.lumaserv.netflow;

import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@FieldDefaults(makeFinal = true)
public class NetFlowSession implements Consumer<NetFlowPacket> {

    // MAP : IP->SOURCES
    Map<Integer, Map<Integer, NetFlowSource>> devices = new HashMap<>();
    Consumer<NetFlowSource> consumer;

    public NetFlowSession(Consumer<NetFlowSource> consumer) {
        this.consumer = consumer;
    }

    public void accept(NetFlowPacket packet) {

        Map<Integer, NetFlowSource> deviceSources = devices.computeIfAbsent(packet.getSourceAddress(), k -> new HashMap<>());

        NetFlowSource source = deviceSources.get(packet.getSourceId());

        if(source == null) {
            source = new NetFlowSource(packet.getSourceAddress(), packet.getSourceId());
            deviceSources.put(source.getId(), source);
            consumer.accept(source);
        }

        source.accept(packet);
    }

}
