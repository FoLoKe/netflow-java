package com.lumaserv.netflow;

import com.lumaserv.netflow.flowset.DataTemplate;
import com.lumaserv.netflow.flowset.FlowSet;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.net.DatagramPacket;
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
    List<FlowSet> flowSets = new ArrayList<>();

    public NetFlowPacket(DatagramPacket datagram) {
        this.sourceAddress = ByteBuffer.wrap(datagram.getAddress().getAddress()).getInt();
        byte[] packet = datagram.getData(); // contains mix of flowSets and flowTemplates

        version = ((packet[0] & 0xFF) << 8) | (packet[1] & 0xFF);
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

        // https://www.cisco.com/en/US/technologies/tk648/tk362/technologies_white_paper09186a00800a3db9.html
        while(offset < datagram.getLength()) {
            int id = ((packet[offset] & 0xFF) << 8) | (packet[offset+1] & 0xFF); // template id (0 for templates itself)
            int flowSetLen = ((packet[offset+2] & 0xFF) << 8) | (packet[offset+3] & 0xFF); // warning: contains padding
            offset += 4;
            flowSetLen -= 4;

            // trying to parse out flow sets
            // idk 8 could contain flow set with 1 flow and 1 attribute everything less could be set's padding

            if (id == 0) {
                while(flowSetLen >= 8) {
                    byte[] flow = new byte[flowSetLen]; // contains templates set
                    System.arraycopy(packet, offset, flow, 0, flowSetLen);

                    DataTemplate template = new DataTemplate(flow);
                    dataTemplates.add(template);
                    offset += 4 + template.getFields().size() * 4;      // moving offset for the next template
                    flowSetLen -= 4 + template.getFields().size() * 4; // 4 bytes for template info, other for fields
                }
                offset += flowSetLen; // set's padding
            } else {
                byte[] flowSetData = new byte[flowSetLen]; // contains flows set
                System.arraycopy(packet, offset, flowSetData, 0, flowSetLen);
                FlowSet flowSet = new FlowSet(flowSetData, id);
                offset += flowSetLen;       // moving offset for the next set
                flowSets.add(flowSet);
            }
        }
    }
}
