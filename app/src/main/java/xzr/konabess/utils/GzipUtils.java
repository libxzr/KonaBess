package xzr.konabess.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

    public static String compress(byte[] bytes) throws IOException {
        if(bytes==null||bytes.length==0)
            throw new IOException();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        gzip = new GZIPOutputStream(out);
        gzip.write(bytes);
        gzip.close();
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    public static String uncompress(String compressedStr) throws IOException {
        if (compressedStr == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream byteArrayInputStream;
        GZIPInputStream gzipInputStream;
        byte[] compressed;
        String decompressed;
        compressed = Base64.getDecoder().decode(compressedStr);
        byteArrayInputStream = new ByteArrayInputStream(compressed);
        gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        byte[] buffer = new byte[1024];
        int offset;
        while ((offset = gzipInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, offset);
        }
        decompressed = out.toString(String.valueOf(StandardCharsets.UTF_8));
        gzipInputStream.close();
        byteArrayInputStream.close();
        out.close();
        return decompressed;
    }
}

