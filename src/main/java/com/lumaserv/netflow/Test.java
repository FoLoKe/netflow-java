package com.lumaserv.netflow;

import com.lumaserv.netflow.flowset.FlowField;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Test {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static void main(String[] args) throws SocketException {

        NetFlowSession session = new NetFlowSession(source -> {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                byteBuffer.putInt(source.getDeviceIP());
                System.out.println("Device: " + toAddress(byteBuffer.array()) + " source: " + source.getId());

                source.listen((id, values) -> {
                    System.out.println(values.get(FlowField.LAST_SWITCHED).asInt());
                    System.out.println(values.get(FlowField.FIRST_SWITCHED).asInt());
                    System.out.println(sdf.format(Calendar.getInstance().getTime())
                            + " " + values.get(FlowField.L4_DST_PORT).asUShort()
                            + " " + toAddress(values.get(FlowField.IPV4_DST_ADDR).asBytes())
                            + " " + toAddress(values.get(FlowField.IPV4_SRC_ADDR).asBytes())
                            + " " + values.get(FlowField.L4_SRC_PORT).asUShort());

                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        NetFlowCollector collector = new NetFlowCollector(session, 9996);
        Thread netFlowThread = new Thread(collector);
        netFlowThread.setDaemon(true);
        netFlowThread.start();
    }

    private static String toAddress(byte[] bytes) {

        return "" + (bytes[0] > 0 ? bytes[0] : 256 + bytes[0]) + '.' +
                (bytes[1] > 0 ? bytes[1] : 256 + bytes[1]) + '.' +
                (bytes[2] > 0 ? bytes[2] : 256 + bytes[2]) + '.' +
                (bytes[3] > 0 ? bytes[3] : 256 + bytes[3]);
    }

}
