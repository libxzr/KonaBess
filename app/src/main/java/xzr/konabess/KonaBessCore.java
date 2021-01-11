package xzr.konabess;

import android.content.Context;
import android.util.Log;

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
    public static void cleanEnv(Context context) throws IOException{
        Process process=new ProcessBuilder("sh").start();
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter(process.getOutputStream());
        outputStreamWriter.write("rm -rf "+context.getFilesDir().getAbsolutePath()+"/*\nexit\n");
        outputStreamWriter.flush();
        while(bufferedReader.readLine()!=null){}
        bufferedReader.close();
        process.destroy();
    }
    private static String[] fileList={"dtc","magiskboot"};
    public static void setupEnv(Context context) throws IOException {
        for(String s:fileList){
            AssetsUtil.exportFiles(context,s,context.getFilesDir().getAbsolutePath()+"/"+s);
            File file=new File(context.getFilesDir().getAbsolutePath()+"/"+s);
            file.setExecutable(true);
            if(!file.canExecute())
                throw new IOException();
        }
    }

    public static void reboot() throws IOException{
        Process process=new ProcessBuilder("su").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("svc power reboot\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        while(bufferedReader.readLine()!=null){}
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
    }

    private static String getCurrentSlot() throws IOException{
        Process process=new ProcessBuilder("sh").start();
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter(process.getOutputStream());
        outputStreamWriter.write("getprop ro.boot.slot_suffix\nexit\n");
        outputStreamWriter.flush();
        String ret=bufferedReader.readLine();
        bufferedReader.close();
        outputStreamWriter.close();
        process.destroy();
        return ret==null?"":ret;
    }

    public static void getBootImage(Context context) throws IOException{
        Process process=new ProcessBuilder("su").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("dd if=/dev/block/bootdevice/by-name/boot"+getCurrentSlot()+" of="+context.getFilesDir().getAbsolutePath()+"/boot.img\n");
        outputStreamWriter.write("chmod 644 "+context.getFilesDir().getAbsolutePath()+"/boot.img\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        while(bufferedReader.readLine()!=null){}
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();

        if(!new File(context.getFilesDir().getAbsolutePath()+"/boot.img").exists()
                ||!new File(context.getFilesDir().getAbsolutePath()+"/boot.img").canRead())
            throw new IOException();
    }

    public static void writeBootImage(Context context) throws IOException{
        Process process=new ProcessBuilder("su").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("dd if="+context.getFilesDir().getAbsolutePath()+"/boot_new.img of=/dev/block/bootdevice/by-name/boot"+getCurrentSlot()+"\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        while(bufferedReader.readLine()!=null){}
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
    }

    public static void backupBootImage(Context context) throws IOException{
        Process process=new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("cp -f "+context.getFilesDir().getAbsolutePath()+"/boot.img /sdcard\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        while(bufferedReader.readLine()!=null){}
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
    }

    public static String dts_path;

    public static boolean checkDevice(Context context) throws IOException{
        for(int i=0;i<dtb_num;i++) {
            boolean okay=false;
            if (checkKona(context,i)) {
                ChipInfo.which = ChipInfo.type.kona;
                okay=true;
            } else if (checkMsmnile(context,i)) {
                ChipInfo.which = ChipInfo.type.msmnile;
                okay=true;
            }
            if(okay) {
                dts_path=context.getFilesDir().getAbsolutePath()+"/"+i+".dts";
                return true;
            }
        }
        return false;
    }

    public static boolean checkKona(Context context,int index) throws IOException{
        Process process=new ProcessBuilder("sh").start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("cat "+context.getFilesDir().getAbsolutePath()+"/"+index+".dts | grep model | grep 'kona v2.1'\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        boolean is_kona=false;
        String s=bufferedReader.readLine();
        if(s!=null)
            is_kona=true;
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
        return is_kona;
    }

    public static boolean checkMsmnile(Context context,int index) throws IOException{
        Process process=new ProcessBuilder("sh").start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter((process.getOutputStream()));
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputStreamWriter.write("cat "+context.getFilesDir().getAbsolutePath()+"/"+index+".dts | grep model | grep 'SM8150 v2'\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        boolean is=false;
        String s=bufferedReader.readLine();
        if(s!=null)
            is=true;
        outputStreamWriter.close();
        bufferedReader.close();
        process.destroy();
        return is;
    }

    private static void unpackBootImage(Context context) throws IOException{
        Process process=new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter(process.getOutputStream());
        BufferedReader bufferedReader=new BufferedReader((new InputStreamReader(process.getInputStream())));
        outputStreamWriter.write("cd "+context.getFilesDir().getAbsolutePath()+"\n");
        outputStreamWriter.write("./magiskboot unpack boot.img\n");
        outputStreamWriter.write("mv kernel_dtb dtb\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        StringBuilder log= new StringBuilder();
        String s;
        while((s=bufferedReader.readLine())!=null){
            log.append(s).append("\n");
        }
        bufferedReader.close();
        outputStreamWriter.close();
        process.destroy();
        if(!new File(context.getFilesDir().getAbsolutePath()+"/dtb").exists())
            throw new IOException(log.toString());
    }

    private static void dtb2dts(Context context,int index)throws IOException{
        Process process=new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter(process.getOutputStream());
        BufferedReader bufferedReader=new BufferedReader((new InputStreamReader(process.getInputStream())));
        outputStreamWriter.write("cd "+context.getFilesDir().getAbsolutePath()+"\n");
        outputStreamWriter.write("./dtc -I dtb -O dts "+index+".dtb -o "+index+".dts\n");
        outputStreamWriter.write("rm -f "+index+".dtb\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        StringBuilder log= new StringBuilder();
        String s;
        while((s=bufferedReader.readLine())!=null){
            log.append(s).append("\n");
        }
        bufferedReader.close();
        outputStreamWriter.close();
        process.destroy();
        if(!new File(context.getFilesDir().getAbsolutePath()+"/"+index+".dts").exists())
            throw new IOException(log.toString());
    }

    private static int dtb_num;

    public static void bootImage2dts(Context context) throws IOException{
        unpackBootImage(context);
        dtb_num=dtb_split(context);
        for(int i=0;i<dtb_num;i++){
            dtb2dts(context,i);
        }
    }

    public static int dtb_split(Context context) throws IOException{
        File dtb=new File(context.getFilesDir().getAbsolutePath()+"/dtb");
        byte[] dtb_bytes=new byte[(int) dtb.length()];
        FileInputStream fileInputStream=new FileInputStream(dtb);
        if(fileInputStream.read(dtb_bytes)!=dtb.length())
            throw new IOException();
        fileInputStream.close();

        int i=0;
        ArrayList<Integer> cut=new ArrayList<>();
        while(i+4<dtb.length()){
            if(dtb_bytes[i]==(byte)0xD0&&dtb_bytes[i+1]==(byte)0x0D
                    &&dtb_bytes[i+2]==(byte)0xFE&&dtb_bytes[i+3]==(byte)0xED){
                cut.add(i);
            }
            i++;
        }

        for(i=0;i<cut.size();i++){
            File out=new File(context.getFilesDir().getAbsolutePath()+"/"+i+".dtb");
            FileOutputStream fileOutputStream=new FileOutputStream(out);
            int end= (int) dtb.length();
            try{
                end=cut.get(i+1);
            }catch (Exception ignored){}

            fileOutputStream.write(dtb_bytes,cut.get(i),end-cut.get(i));
            fileInputStream.close();
        }

        if(!dtb.delete())
            throw new IOException();

        return cut.size();
    }

    private static void dts2dtb(Context context,int index) throws IOException{
        Process process=new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter(process.getOutputStream());
        BufferedReader bufferedReader=new BufferedReader((new InputStreamReader(process.getInputStream())));
        outputStreamWriter.write("cd "+context.getFilesDir().getAbsolutePath()+"\n");
        outputStreamWriter.write("./dtc -I dts -O dtb "+index+".dts -o "+index+".dtb\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        StringBuilder log= new StringBuilder();
        String s;
        while((s=bufferedReader.readLine())!=null){
            log.append(s).append("\n");
        }
        bufferedReader.close();
        outputStreamWriter.close();
        process.destroy();
        if(!new File(context.getFilesDir().getAbsolutePath()+"/"+index+".dtb").exists())
            throw new IOException(log.toString());
    }


    private static void dtb2bootImage(Context context) throws IOException{
        Process process=new ProcessBuilder("sh").redirectErrorStream(true).start();
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter(process.getOutputStream());
        BufferedReader bufferedReader=new BufferedReader((new InputStreamReader(process.getInputStream())));
        outputStreamWriter.write("cd "+context.getFilesDir().getAbsolutePath()+"\n");
        outputStreamWriter.write("cp dtb kernel_dtb\n");
        outputStreamWriter.write("./magiskboot repack boot.img boot_new.img\n");
        outputStreamWriter.write("exit\n");
        outputStreamWriter.flush();
        StringBuilder log= new StringBuilder();
        String s;
        while((s=bufferedReader.readLine())!=null){
            log.append(s).append("\n");
        }
        bufferedReader.close();
        outputStreamWriter.close();
        process.destroy();
        if(!new File(context.getFilesDir().getAbsolutePath()+"/boot.img").exists())
            throw new IOException(log.toString());
    }

    public static void linkDtbs(Context context) throws IOException{
        File out=new File(context.getFilesDir().getAbsolutePath()+"/dtb");
        FileOutputStream fileOutputStream=new FileOutputStream(out);
        for(int i=0;i<dtb_num;i++){
            File input=new File(context.getFilesDir().getAbsolutePath()+"/"+i+".dtb");
            FileInputStream fileInputStream=new FileInputStream(input);
            byte[] b=new byte[(int)input.length()];
            if(fileInputStream.read(b)!=input.length())
                throw new IOException();
            fileOutputStream.write(b);
            fileInputStream.close();
        }
        fileOutputStream.close();
    }

    public static void dts2bootImage(Context context) throws IOException{
        for(int i=0;i<dtb_num;i++) {
            dts2dtb(context,i);
        }
        linkDtbs(context);
        dtb2bootImage(context);
    }
}
