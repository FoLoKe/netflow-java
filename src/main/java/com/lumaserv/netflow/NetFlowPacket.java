package com.lumaserv.netflow;

import com.lumaserv.netflow.flowset.FlowData;
import com.lumaserv.netflow.flowset.DataTemplate;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(makeFinal = true)
public class NetFlowPacket {
    // The Source ID field is a 32-bit value that is used to guarantee uniqueness for all flows
    // exported from a PARTICULAR device. (The Source ID field is the equivalent of the engine
    // type and engine ID fields found in the NetFlow Version 5 and Version 8 headers).
    // ~~~~
    // Collector devices should use the combination of the SOURCE IP ADDRESS
    // plus the Source ID field to associate an incoming NetFlow export packet
    // with a unique instance of NetFlow on a particular device. - CISCO
    int sourceAddress;
    int sourceId;

    int version;
    long sysUptime;
    long timestamp;
    int flowSequence;
    List<DataTemplate> dataTemplates = new ArrayList<>();
    List<FlowData> data = new ArrayList<>();

    public NetFlowPacket(InetAddress address, byte[] packet) {
        this.sourceAddress = ByteBuffer.wrap(address.getAddress()).getInt();

        version = ((packet[0] & 0xFF) << 8) | (packet[1] & 0xFF);
        int count = ((packet[2] & 0xFF) << 8) | (packet[3] & 0xFF);
        sysUptime = ((packet[4] & 0xFFL) << 24) |
                ((packet[5] & 0xFFL) << 16) |
                ((packet[6] & 0xFFL) << 8) |
                (packet[7] & 0xFFL);
        timestamp = ((packet[8] & 0xFFL) << 24) |
                ((packet[9] & 0xFFL) << 16) |
                ((packet[10] & 0xFFL) << 8) |
                (packet[11] & 0xFFL);
        flowSequence = ((packet[12] & 0xFF) << 24) |
                ((packet[13] & 0xFF) << 16) |
                ((packet[14] & 0xFF) << 8) |
                (packet[15] & 0xFF);
        sourceId = ((packet[16] & 0xFF) << 24) |
                ((packet[17] & 0xFF) << 16) |
                ((packet[18] & 0xFF) << 8) |
                (packet[19] & 0xFF);
        int offset = 20;

        // This is actually works wrong: Netflow v9 contains FlowSets with len and id(template) values, BUT, this function works only with first flow set
        // and WAS copying first set entry over and over, loosing data about actual flows in set.
        for(int i=0; i<count; i++) {
            int id = ((packet[offset] & 0xFF) << 8) | (packet[offset+1] & 0xFF);
            int length = ((packet[offset+2] & 0xFF) << 8) | (packet[offset+3] & 0xFF);
            // offset + 4 + length / count * i = flow set info offset + "i"'s set entry offset
            if (id == 0) {
                byte[] data = new byte[length];
                System.arraycopy(packet, offset + 4, data, 0, data.length);
                dataTemplates.add(new DataTemplate(data));
            } else {
                byte[] data = new byte[length / count];
                System.arraycopy(packet, offset + 4 + ((length - 1) / count) * i, data, 0, data.length);
                this.data.add(new FlowData(id, data));
            }
        }
    }
}
