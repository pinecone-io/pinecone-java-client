package io.pinecone;

import io.pinecone.proto.Core;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PineconeTranslatorTest {

    @Test
    public void translate_float2d_ndArray() {
        float[][] data = {{1F,2F,3F},{4F,5F,6F}};
        // >>> np.array([[1, 2, 3], [4, 5, 6]], np.float32).tobytes().hex(' ')
        String expectedHex = "00 00 80 3f 00 00 00 40 00 00 40 40 00 00 80 40 00 00 a0 40 00 00 c0 40";
        Core.NdArray ndArray = new PineconeTranslator().translate(data);
        assertThat(byteArrayToHex(ndArray.getBuffer().toByteArray()), equalTo(expectedHex));
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x ", b));
        return sb.toString().trim();
    }
}
