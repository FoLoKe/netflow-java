package com.lumaserv.netflow.flowset;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class FlowSet {
    public byte[] data;
    public int id;

    public List<Map<FlowField, FlowValue>> parse(DataTemplate template) {
        int count = data.length / template.getLen();
        List<Map<FlowField, FlowValue>> set = new ArrayList<>();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                byte[] flowDataArray = new byte[template.getLen()];
                System.arraycopy(data, i * template.getLen(), flowDataArray, 0, flowDataArray.length);
                FlowData flowData = new FlowData(id, flowDataArray);
                set.add(flowData.parse(template));
            }
        }

        return set;
    }
}
