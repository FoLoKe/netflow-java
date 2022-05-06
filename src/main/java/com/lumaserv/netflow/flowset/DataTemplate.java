package com.lumaserv.netflow.flowset;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(makeFinal = true)
@Getter
public class DataTemplate {
    int len;
    int templateId;
    List<TemplateField> fields = new ArrayList<>();

    public DataTemplate(byte[] data) {
        templateId = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        int count = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        int totalLen = 0;
        for(int i=0; i<count; i++) {
            int fieldLen = ((data[6 + (i * 4)] & 0xFF) << 8) | (data[7 + (i * 4)] & 0xFF);
            fields.add(new TemplateField(
                    FlowField.fromValue(((data[4 + (i * 4)] & 0xFF) << 8) | (data[5 + (i * 4)] & 0xFF)),
                    fieldLen
            ));
            totalLen += fieldLen;
        }
        len = totalLen;
    }

}
