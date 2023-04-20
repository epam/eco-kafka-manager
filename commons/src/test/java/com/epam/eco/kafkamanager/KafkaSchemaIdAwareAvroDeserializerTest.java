package com.epam.eco.kafkamanager;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mikhail_Vershkov
 */

public class KafkaSchemaIdAwareAvroDeserializerTest {

    private final byte[] BYTES_SHORT = new byte[4];
    private final byte[] BYTES_NORMAL = new byte[20];
    private final byte BYTE_FILL = 1;
    private final byte BYTE_ZERO = 0;
    private final KafkaSchemaIdAwareAvroDeserializer kafkaSchemaIdAwareAvroDeserializer =
            Mockito.spy(KafkaSchemaIdAwareAvroDeserializer.class);

    @Test(expected = IllegalArgumentException.class)
    public void illegalLengthTest() {
        kafkaSchemaIdAwareAvroDeserializer.getSchemaId(BYTES_SHORT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractSchemaIdWithNoAvroSchemaTest() {
        Arrays.fill(BYTES_NORMAL, BYTE_FILL);
        kafkaSchemaIdAwareAvroDeserializer.getSchemaId(BYTES_NORMAL);
    }
    @Test
    public void extractSchemaIdWithTest() {
        Arrays.fill(BYTES_NORMAL, BYTE_FILL);
        Arrays.fill(BYTES_NORMAL,0,3, BYTE_ZERO);
        long schemaId = kafkaSchemaIdAwareAvroDeserializer.getSchemaId(BYTES_NORMAL);
        Assert.assertEquals(schemaId,257L);
    }
}
