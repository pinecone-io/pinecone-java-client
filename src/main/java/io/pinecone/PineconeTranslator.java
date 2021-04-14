package io.pinecone;

import com.google.protobuf.ByteString;
import io.pinecone.proto.Core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class PineconeTranslator {

    public Core.NdArray translate(float[][] data) {
        int rows = data.length, cols = data[0].length;

        ByteBuffer bbuf = ByteBuffer.allocate(rows * cols * Float.BYTES);
        bbuf.order(ByteOrder.LITTLE_ENDIAN); // to match numpy ndarry.tobytes()
        for (float[] row : data) {
            for (int i = 0; i < data[0].length; i++) {
                bbuf.putFloat(row[i]);
            }
        }

        return Core.NdArray.newBuilder()
                .setDtype(PineconeRequest.DTYPE.FLOAT32.dtypeName())
                .addShape(rows)
                .addShape(cols)
                .setBuffer(ByteString.copyFrom(bbuf.array()))
                .build();
    }

    public float[][] translate(Core.NdArray ndArray) {
        List<Integer> shapeList = ndArray.getShapeList();
        if(shapeList.size() == 0)
            return null;
        if(!PineconeRequest.DTYPE.FLOAT32.dtypeName().equals(ndArray.getDtype()))
            throw new PineconeValidationException("Unexpected ndArray.dtype: " + ndArray.getDtype());
        int rows = shapeList.get(0), cols = shapeList.get(1);
        ByteBuffer buffer = ByteBuffer.allocate(rows * cols * Float.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        ndArray.getBuffer().copyTo(buffer);
        buffer.flip();

        float[][] data = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                data[i][j] = buffer.getFloat();
            }
        }
        
        return data;
    }
}
