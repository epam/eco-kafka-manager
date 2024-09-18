package com.epam.eco.kafkamanager;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Mikhail_Vershkov
 */

public class KafkaExtendedAvroDeserializerTest {

    private final byte[] BYTES_SHORT = new byte[4];
    private final byte[] BYTES_NORMAL = new byte[20];
    private final static byte BYTE_FILL = 1;
    private final static byte BYTE_ZERO = 0;
    private final KafkaExtendedAvroDeserializer kafkaSchemaIdAwareAvroDeserializer =
            Mockito.spy(KafkaExtendedAvroDeserializer.class);

    @Test
    public void illegalLengthTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                kafkaSchemaIdAwareAvroDeserializer.getSchemaId(BYTES_SHORT)
        );
    }

    @Test
    public void extractSchemaIdWithNoAvroSchemaTest() {
        Arrays.fill(BYTES_NORMAL, BYTE_FILL);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                kafkaSchemaIdAwareAvroDeserializer.getSchemaId(BYTES_NORMAL)
        );
    }
    @Test
    public void extractSchemaIdWithTest() {
        Arrays.fill(BYTES_NORMAL, BYTE_FILL);
        Arrays.fill(BYTES_NORMAL,0,3, BYTE_ZERO);
        long schemaId = kafkaSchemaIdAwareAvroDeserializer.getSchemaId(BYTES_NORMAL);
        Assertions.assertEquals(schemaId, 257L);
    }
}
