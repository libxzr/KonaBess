package xzr.konabess.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetsUtil {
    public static void exportFiles(Context context, String src, String out )throws IOException {
        String fileNames[] = context.getAssets().list(src);
        if (fileNames.length > 0) {
            File file = new File(out);
            file.mkdirs();
            for (String fileName : fileNames) {
                exportFiles(context,src + "/" + fileName,out+"/"+fileName);
            }
        } else {
            InputStream is = context.getAssets().open(src);
            FileOutputStream fos = new FileOutputStream(new File(out));
            byte[] buffer = new byte[1024];
            int byteCount=0;
            while((byteCount=is.read(buffer))!=-1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
        }

    }
}
