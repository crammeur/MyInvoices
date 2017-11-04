package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-06-15.
 */

public final class Serialize {

    public static class Buffer {
        public static final byte[] RAM = new byte[8192];
        public static final byte[] DISK = new byte[4096];
     }

    public static byte[] fileToBytes(@NotNull File pFile) {
        byte[] buffer = Buffer.DISK;
        int len;
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(pFile));
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            while ((len = buf.read(buffer)) >= 0) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static byte[] serialize(@NotNull Serializable pObject) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os;
        byte[] result;
        try {
            os = new ObjectOutputStream(out);
            os.writeObject(pObject);
            result = out.toByteArray();
            os.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }

    public static <T> T deserialize(@NotNull byte[] pData) {
        ByteArrayInputStream in = new ByteArrayInputStream(pData);
        ObjectInputStream is;
        try {
            is = new ObjectInputStream(in);
            T result = (T) is.readObject();
            is.close();
            in.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
