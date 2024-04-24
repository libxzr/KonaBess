package xzr.konabess;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.SystemProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import xzr.konabess.utils.AssetsUtil;

public class KonaBessCore {
    public static String dts_path;
    private static int dtb_num;
    public static String boot_name;

    private enum dtb_types {
        dtb,
        kernel_dtb,
        both
    }

    private static dtb_types dtb_type;

    public static void cleanEnv(Context context) throws IOException {
        Process process = new ProcessBuilder("sh").start();
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
        outputStreamWriter.write("rm -rf " + context.getFilesDir().getAbsolutePath() + "/*\nexit" +
                "\n");
        outputStreamWriter.flush();
        while (bufferedReader.readLine() != null) {
        }
        bufferedReader.close();
        process.destroy();
    }

    private static String[] fileList = {"dtc", "magiskboot"};

    public static void setupEnv(Context context) throws IOException {
        for (String s : fileList) {
            AssetsUtil.exportFiles(context, s, context.getFilesDir().getAbsolutePath() + "/" + s);
            File file = new File(context.getFilesDir().getAbsolutePath() + "/" + s);
            file.setExecutable(true);
            if (!file.canExecute())
                throw new IOException();
        }
    }

    public static void reboot() throws IOException {
        Process process = new ProcessBuilder("su").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("svc power reboot\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        while (bufferedReader.readLine() != null) {
        }
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
    }

    static String getCurrent(String name) {
        switch (name.toLowerCase()) {
            case "brand":
                return SystemProperties.get("ro.product.brand", "");
            case "name":
                return SystemProperties.get("ro.product.name", "");
            case "model":
                return SystemProperties.get("ro.product.model", "");
            case "board":
                return SystemProperties.get("ro.product.board", "");
            case "id":
                return SystemProperties.get("ro.product.build.id", "");
            case "version":
                return SystemProperties.get("ro.product.build.version.release", "");
            case "fingerprint":
                return SystemProperties.get("ro.product.build.fingerprint", "");
            case "manufacturer":
                return SystemProperties.get("ro.product.manufacturer", "");
            case "device":
                return SystemProperties.get("ro.product.device", "");
            case "slot":
                return SystemProperties.get("ro.boot.slot_suffix", "");
            default:
                return null;
        }
    }

    public static void getBootImage(Context context) throws IOException {
        try {
            getVendorBootImage(context);
            boot_name = "vendor_boot";
        } catch (Exception e) {
            getRealBootImage(context);
            boot_name = "boot";
        }
    }

    private static void getRealBootImage(Context context) throws IOException {
        Process process = new ProcessBuilder("su").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("dd if=/dev/block/bootdevice/by-name/boot" + getCurrent("slot") + " of=" + context.getFilesDir().getAbsolutePath() + "/boot.img\n");
        outputStreamWriter.write("chmod 644 " + context.getFilesDir().getAbsolutePath() + "/boot" +
                ".img\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        while (bufferedReader.readLine() != null) {
        }
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();

        File target = new File(context.getFilesDir().getAbsolutePath() + "/boot.img");
        if (!target.exists() || !target.canRead()) {
            target.delete();
            throw new IOException();
        }
    }

    private static void getVendorBootImage(Context context) throws IOException {
        Process process = new ProcessBuilder("su").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("dd if=/dev/block/bootdevice/by-name/vendor_boot" + getCurrent(
                "slot") + " of=" + context.getFilesDir().getAbsolutePath() + "/boot.img\n");
        outputStreamWriter.write("chmod 644 " + context.getFilesDir().getAbsolutePath() + "/boot" +
                ".img\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        while (bufferedReader.readLine() != null) {
        }
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();

        File target = new File(context.getFilesDir().getAbsolutePath() + "/boot.img");
        if (!target.exists() || !target.canRead()) {
            target.delete();
            throw new IOException();
        }
    }

    public static void writeBootImage(Context context) throws IOException {
        Process process = new ProcessBuilder("su").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("dd if=" + context.getFilesDir().getAbsolutePath() + "/boot_new" +
                ".img of=/dev/block/bootdevice/by-name/" + boot_name + getCurrent("slot") + "\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        while (bufferedReader.readLine() != null) {
        }
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
    }

    public static void backupBootImage(Context context) throws IOException {
        Process process = new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("cp -f " + context.getFilesDir().getAbsolutePath() + "/boot.img "
                + Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + boot_name + ".img\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        while (bufferedReader.readLine() != null) {
        }
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
    }

    static class dtb {
        int id;
        ChipInfo.type type;
    }

    public static ArrayList<dtb> dtbs;

    public static void checkDevice(Context context) throws IOException {
        dtbs = new ArrayList<>();
        for (int i = 0; i < dtb_num; i++) {
            if (checkChip(context, i, "kona v2.1")
                    || KonaBessCore.getCurrent("device").equals("OP4A79") && checkChip(context, i
                    , "kona v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = checkSingleBin(context, i) ? ChipInfo.type.kona_singleBin :
                        ChipInfo.type.kona;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "SM8150 v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = checkSingleBin(context, i) ? ChipInfo.type.msmnile_singleBin :
                        ChipInfo.type.msmnile;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lahaina V2.1")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = checkSingleBin(context, i) ? ChipInfo.type.lahaina_singleBin :
                        ChipInfo.type.lahaina;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lahaina v2.1")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = checkSingleBin(context, i) ? ChipInfo.type.lahaina_singleBin :
                        ChipInfo.type.lahaina;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lito")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.lito_v1;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lito v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.lito_v2;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lagoon")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.lagoon;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Shima")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.shima;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Yupik")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.yupik;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Waipio")
                    || checkChip(context, i, "Waipio v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.waipio_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Cape")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.cape_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Kalama v2")
                    || checkChip(context, i, "KalamaP v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.kalama;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Diwali")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.diwali;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Ukee")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.ukee_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Pineapple v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.pineapple;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Cliffs SoC")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.cliffs_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Cliffs 7 SoC")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.cliffs_7_singleBin;
                dtbs.add(dtb);
            }
        }
    }

    public static void chooseTarget(dtb dtb, Activity activity) {
        dts_path = activity.getFilesDir().getAbsolutePath() + "/" + dtb.id + ".dts";
        ChipInfo.which = dtb.type;
    }

    private static boolean checkSingleBin(Context context, int index) throws IOException {
        Process process = new ProcessBuilder("sh").start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("cat " + context.getFilesDir().getAbsolutePath() + "/" + index + ".dts | grep 'qcom,gpu-pwrlevels {'\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        boolean is = false;
        String s = bufferedReader.readLine();
        if (s != null)
            is = true;
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
        return is;
    }

    private static boolean checkChip(Context context, int index, String chip) throws IOException {
        boolean result = false;
        Process process = new ProcessBuilder("sh").start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("cat " + context.getFilesDir().getAbsolutePath() + "/" + index + ".dts | grep model | grep '" + chip + "'\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        String s = bufferedReader.readLine();
        if (s != null)
            result = true;
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
        return result;
    }

    private static void unpackBootImage(Context context) throws IOException {
        Process process = new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
        BufferedReader bufferedReader =
                new BufferedReader((new InputStreamReader(process.getInputStream())));
        outputStreamWriter.write("cd " + context.getFilesDir().getAbsolutePath() + "\n");
        outputStreamWriter.write("./magiskboot unpack boot.img\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        StringBuilder log = new StringBuilder();
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            log.append(s).append("\n");
        }
        bufferedReader.close();
        outputStreamWriter.close();
        process.destroy();

        File kdtb_file = new File(context.getFilesDir().getAbsolutePath() + "/kernel_dtb");
        File dtb_file = new File(context.getFilesDir().getAbsolutePath() + "/dtb");

        if (kdtb_file.exists() && dtb_file.exists()) {
            dtb_type = dtb_types.both;
            return;
        }

        if (kdtb_file.exists()) {
            dtb_type = dtb_types.kernel_dtb;
            return;
        }

        if (dtb_file.exists()) {
            dtb_type = dtb_types.dtb;
            return;
        }

        throw new IOException();
    }

    private static void dtb2dts(Context context, int index) throws IOException {
        Process process = new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
        BufferedReader bufferedReader =
                new BufferedReader((new InputStreamReader(process.getInputStream())));
        outputStreamWriter.write("cd " + context.getFilesDir().getAbsolutePath() + "\n");
        outputStreamWriter.write("./dtc -I dtb -O dts " + index + ".dtb -o " + index + ".dts\n");
        outputStreamWriter.write("rm -f " + index + ".dtb\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        StringBuilder log = new StringBuilder();
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            log.append(s).append("\n");
        }
        bufferedReader.close();
        outputStreamWriter.close();
        process.destroy();
        if (!new File(context.getFilesDir().getAbsolutePath() + "/" + index + ".dts").exists())
            throw new IOException(log.toString());
    }

    public static void bootImage2dts(Context context) throws IOException {
        unpackBootImage(context);
        dtb_num = dtb_split(context);
        for (int i = 0; i < dtb_num; i++) {
            dtb2dts(context, i);
        }
    }

    private static int toUnsignedByte(byte in) {
        return (int) in & 0xFF;
    }

    public static int dtb_split(Context context) throws IOException {
        File dtb = null;
        if (dtb_type == dtb_types.dtb)
            dtb = new File(context.getFilesDir().getAbsolutePath() + "/dtb");
        else if (dtb_type == dtb_types.kernel_dtb)
            dtb = new File(context.getFilesDir().getAbsolutePath() + "/kernel_dtb");
        else if (dtb_type == dtb_types.both) {
            dtb = new File(context.getFilesDir().getAbsolutePath() + "/dtb");
            if (!new File(context.getFilesDir().getAbsolutePath() + "/kernel_dtb").delete())
                throw new IOException();
        } else {
            throw new IOException();
        }

        byte[] dtb_bytes = new byte[(int) dtb.length()];
        FileInputStream fileInputStream = new FileInputStream(dtb);
        if (fileInputStream.read(dtb_bytes) != dtb.length())
            throw new IOException();
        fileInputStream.close();

        int i = 0;
        ArrayList<Integer> cut = new ArrayList<>();
        while (i + 8 < dtb.length()) {
            if (dtb_bytes[i] == (byte) 0xD0 && dtb_bytes[i + 1] == (byte) 0x0D
                    && dtb_bytes[i + 2] == (byte) 0xFE && dtb_bytes[i + 3] == (byte) 0xED) {
                cut.add(i);
                int size = (int) (toUnsignedByte(dtb_bytes[i + 4]) * Math.pow(256, 3)
                        + toUnsignedByte(dtb_bytes[i + 5]) * Math.pow(256, 2)
                        + toUnsignedByte(dtb_bytes[i + 6]) * Math.pow(256, 1)
                        + toUnsignedByte(dtb_bytes[i + 7]));
                i += size > 0 ? size : 1;
                continue;
            }
            i++;
        }

        for (i = 0; i < cut.size(); i++) {
            File out = new File(context.getFilesDir().getAbsolutePath() + "/" + i + ".dtb");
            FileOutputStream fileOutputStream = new FileOutputStream(out);
            int end = (int) dtb.length();
            try {
                end = cut.get(i + 1);
            } catch (Exception ignored) {
            }

            fileOutputStream.write(dtb_bytes, cut.get(i), end - cut.get(i));
            fileInputStream.close();
        }

        if (!dtb.delete())
            throw new IOException();

        return cut.size();
    }

    private static void dts2dtb(Context context, int index) throws IOException {
        Process process = new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
        BufferedReader bufferedReader =
                new BufferedReader((new InputStreamReader(process.getInputStream())));
        outputStreamWriter.write("cd " + context.getFilesDir().getAbsolutePath() + "\n");
        outputStreamWriter.write("./dtc -I dts -O dtb " + index + ".dts -o " + index + ".dtb\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        StringBuilder log = new StringBuilder();
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            log.append(s).append("\n");
        }
        bufferedReader.close();
        outputStreamWriter.close();
        process.destroy();
        if (!new File(context.getFilesDir().getAbsolutePath() + "/" + index + ".dtb").exists())
            throw new IOException(log.toString());
    }


    private static void dtb2bootImage(Context context) throws IOException {
        Process process = new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
        BufferedReader bufferedReader =
                new BufferedReader((new InputStreamReader(process.getInputStream())));
        outputStreamWriter.write("cd " + context.getFilesDir().getAbsolutePath() + "\n");
        if (dtb_type == dtb_types.both)
            outputStreamWriter.write("cp dtb kernel_dtb\n");
        outputStreamWriter.write("./magiskboot repack boot.img boot_new.img\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        StringBuilder log = new StringBuilder();
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            log.append(s).append("\n");
        }
        bufferedReader.close();
        outputStreamWriter.close();
        process.destroy();
        if (!new File(context.getFilesDir().getAbsolutePath() + "/boot_new.img").exists())
            throw new IOException(log.toString());
    }

    public static void linkDtbs(Context context) throws IOException {
        File out;

        if (dtb_type == dtb_types.dtb)
            out = new File(context.getFilesDir().getAbsolutePath() + "/dtb");
        else if (dtb_type == dtb_types.kernel_dtb)
            out = new File(context.getFilesDir().getAbsolutePath() + "/kernel_dtb");
        else if (dtb_type == dtb_types.both) {
            out = new File(context.getFilesDir().getAbsolutePath() + "/dtb");
        } else {
            throw new IOException();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(out);
        for (int i = 0; i < dtb_num; i++) {
            File input = new File(context.getFilesDir().getAbsolutePath() + "/" + i + ".dtb");
            FileInputStream fileInputStream = new FileInputStream(input);
            byte[] b = new byte[(int) input.length()];
            if (fileInputStream.read(b) != input.length())
                throw new IOException();
            fileOutputStream.write(b);
            fileInputStream.close();
        }
        fileOutputStream.close();
    }

    public static void dts2bootImage(Context context) throws IOException {
        for (int i = 0; i < dtb_num; i++) {
            dts2dtb(context, i);
        }
        linkDtbs(context);
        dtb2bootImage(context);
    }

    public static int getDtbIndex() {
        int ret = -1;
        try {
            ret = Integer.parseInt(SystemProperties.get("ro.boot.dtb_idx", null));
        } catch (Exception ignored) {

        }
        return ret;
    }
}
