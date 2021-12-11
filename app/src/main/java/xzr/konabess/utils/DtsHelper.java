package xzr.konabess.utils;

public class DtsHelper {
    public static boolean shouldUseHex(String line) {
        if (line.contains("qcom,acd-level"))
            return true;
        return false;
    }

    public static class intLine {
        public String name;
        public long value;
    }

    public static class hexLine {
        public String name;
        public String value;
    }

    public static intLine decode_int_line_hz(String line) throws Exception {
        intLine intLine = new intLine();
        line = line.trim();
        int i;
        for (i = 0; i < line.length(); i++) {
            if (line.startsWith("=", i)) {
                break;
            }
        }
        if (i == line.length())
            throw new Exception();
        intLine.name = line.substring(0, i);
        intLine.name = intLine.name.trim();

        String value = line.substring(i + 1);
        value = value.replace("<0x0 ", "")
                .replace(">", "")
                .replace(";", "");

        if (value.contains("0x")) {
            value = value.replace("0x", "").trim();
            intLine.value = Long.parseLong(value, 16);
        } else {
            value = value.trim();
            intLine.value = Long.parseLong(value);
        }

        return intLine;
    }

    //To handle dtc bug
    public static int decode_stringed_int(String input) throws Exception {
        input = input.replace("\"", "")
                .replace(";", "")
                .replace("\\a", "\7")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\v", "\11")
                .replace("\\\\", "\\")
                .replace("\\'", "'")
                .replace("\\\"", "\"")
                .trim();
        char[] chars = input.toCharArray();
        if (chars.length != 3)
            throw new Exception();
        int ret = 0;
        for (int i = 1; i <= chars.length; i++) {
            ret += (int) chars[chars.length - i] * Math.pow(256, i);
        }
        return ret;
    }

    public static intLine decode_int_line(String line) throws Exception {
        intLine intLine = new intLine();
        line = line.trim();
        int i;
        for (i = 0; i < line.length(); i++) {
            if (line.startsWith("=", i)) {
                break;
            }
        }
        if (i == line.length())
            throw new Exception();
        intLine.name = line.substring(0, i);
        intLine.name = intLine.name.trim();

        String value = line.substring(i + 1);
        if (value.contains("\"")) {
            intLine.value = decode_stringed_int(value);
            return intLine;
        }

        value = value.replace("<", "")
                .replace(">", "")
                .replace(";", "");

        if (value.contains("0x")) {
            value = value.replace("0x", "").trim();
            intLine.value = Long.parseLong(value, 16);
        } else {
            value = value.trim();
            intLine.value = Long.parseLong(value);
        }

        return intLine;
    }

    public static hexLine decode_hex_line(String line) throws Exception {
        hexLine hexLine = new hexLine();
        line = line.trim();
        int i;
        for (i = 0; i < line.length(); i++) {
            if (line.startsWith("=", i)) {
                break;
            }
        }
        if (i == line.length())
            throw new Exception();
        hexLine.name = line.substring(0, i);
        hexLine.name = hexLine.name.trim();

        String value = line.substring(i + 1);
        value = value.replace("<", "")
                .replace(">", "")
                .replace(";", "").trim();

        hexLine.value = value;

        return hexLine;
    }

    public static String encodeIntOrHexLine(String name, String value) {
        return name + " = <" + value + ">;";
    }
}
