package xzr.konabess;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Environment;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import xzr.konabess.adapters.ParamAdapter;
import xzr.konabess.utils.DialogUtil;

import static xzr.konabess.KonaBessCore.getCurrent;

public class TableIO {
    private static class json_keys{
        public static final String MODEL="model";
        public static final String BRAND="brand";
        public static final String ID="id";
        public static final String VERSION="version";
        public static final String FINGERPRINT="fingerprint";
        public static final String MANUFACTURER="manufacturer";
        public static final String DEVICE="device";
        public static final String NAME="name";
        public static final String BOARD="board";
        public static final String CHIP="chip";
        public static final String DESCRIPTION="desc";
        public static final String DATA="data";
    }

    private static AlertDialog waiting_import;

    private static boolean decodeAndWriteData(JSONObject jsonObject) throws Exception{
        String decoded_data = jsonObject.getString(json_keys.DATA);

        String[] lines=decoded_data.split("\n");
        if(!ChipInfo.which.toString().equals(jsonObject.getString(json_keys.CHIP)))
            return true;
        boolean freq_started=false;
        boolean volt_started=false;
        ArrayList<String> freq=new ArrayList<>();
        ArrayList<String> volt=new ArrayList<>();
        for(String line:lines){
            if(line.equals("#Freq start")){
                freq_started=true;
                continue;
            }
            if(line.equals("#Freq end")){
                freq_started=false;
                continue;
            }
            if(line.equals("#Volt start")){
                volt_started=true;
                continue;
            }
            if(line.equals("#Volt end")){
                volt_started=false;
                continue;
            }
            if(freq_started) {
                freq.add(line);
                continue;
            }
            if(volt_started){
                volt.add(line);
            }
        }

        GpuTableEditor.writeOut(GpuTableEditor.genBack(freq));
        if(ChipInfo.which!= ChipInfo.type.lahaina_singleBin){
            //Init again because the dts file has been updated
            GpuVoltEditor.init();
            GpuVoltEditor.decode();
            GpuVoltEditor.writeOut(GpuVoltEditor.genBack(volt));
        }

        return false;
    }

    private static String getAndEncodeData(){
        StringBuilder data= new StringBuilder();
        data.append("#Freq start\n");
        for(String line:GpuTableEditor.genTable()){
            data.append(line).append("\n");
        }
        data.append("#Freq end\n");
        if(ChipInfo.which!= ChipInfo.type.lahaina_singleBin){
            data.append("#Volt start\n");
            for(String line:GpuVoltEditor.genTable()){
                data.append(line).append("\n");
            }
            data.append("#Volt end\n");
        }
        return data.toString();
    }

    private static String getConfig(String desc) {
        JSONObject jsonObject = new JSONObject();
        try {
            // TODO: Import confirmation
            jsonObject.put(json_keys.MODEL, getCurrent("model"));
            jsonObject.put(json_keys.BRAND, getCurrent("brand"));
            jsonObject.put(json_keys.ID, getCurrent("id"));
            jsonObject.put(json_keys.VERSION, getCurrent("version"));
            jsonObject.put(json_keys.FINGERPRINT, getCurrent("fingerprint"));
            jsonObject.put(json_keys.MANUFACTURER, getCurrent("manufacturer"));
            jsonObject.put(json_keys.DEVICE, getCurrent("device"));
            jsonObject.put(json_keys.NAME, getCurrent("name"));
            jsonObject.put(json_keys.BOARD, getCurrent("board"));
            jsonObject.put(json_keys.CHIP, ChipInfo.which);
            jsonObject.put(json_keys.DESCRIPTION, desc);
            jsonObject.put(json_keys.DATA, getAndEncodeData());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void import_edittext(Activity activity){
        EditText editText=new EditText(activity);
        editText.setHint("在这里粘贴频率和电压信息");

        new AlertDialog.Builder(activity)
                .setTitle("导入")
                .setView(editText)
                .setPositiveButton("确认", (dialog, which) -> new showDecodeDialog(activity,editText.getText().toString()).start())
                .setNegativeButton("取消",null)
                .create().show();
    }

    private static abstract class ConfirmExportCallback{
        public abstract void onConfirm(String desc);
    }

    private static void showExportDialog(Activity activity,ConfirmExportCallback confirmExportCallback){
        EditText editText=new EditText(activity);
        editText.setHint("在这里输入简介");

        new AlertDialog.Builder(activity)
                .setTitle("导出数据")
                .setMessage("您可以自定义数据的简介，在导入数据时，这些文字将被呈现")
                .setView(editText)
                .setPositiveButton("确认", (dialog, which) -> confirmExportCallback.onConfirm(editText.getText().toString()))
                .setNegativeButton("取消",null)
                .create().show();
    }

    private static void export_cpy(Activity activity,String desc){
        // TODO: clipboard
        DialogUtil.showDetailedInfo(activity,"导出完毕","以下是导出的频率和电压内容", "konabess://"+getConfig(desc));
    }

    private static class exportToFile extends Thread{
        Activity activity;
        boolean error;
        String desc;
        public exportToFile(Activity activity,String desc){
            this.activity=activity;
            this.desc=desc;
        }
        public void run(){
            error=false;
            File out=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/konabess-"+new SimpleDateFormat("MMddHHmmss").format(new Date())+".txt");
            try {
                BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(out));
                bufferedWriter.write("konabess://"+getConfig(desc));
                bufferedWriter.close();
            } catch (IOException e) {
                error=true;
            }
            activity.runOnUiThread(() -> {
                if(!error)
                    Toast.makeText(activity,"成功导出到"+out.getAbsolutePath(),Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(activity,"导出失败",Toast.LENGTH_SHORT).show();
            });
        }
    }

    private static class showDecodeDialog extends Thread{
        Activity activity;
        String data;
        boolean error;
        JSONObject jsonObject;
        public showDecodeDialog(Activity activity,String data){
            this.activity=activity;
            this.data=data;
        }
        public void run(){
            error= !data.startsWith("konabess://");
            if(!error) {
                try {
                    data = data.replace("konabess://", "");
                    String decoded_data = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
                    jsonObject = new JSONObject(decoded_data);
                    activity.runOnUiThread(() -> {
                        waiting_import.dismiss();
                        try {
                            new AlertDialog.Builder(activity)
                                    .setTitle("将要导入数据")
                                    .setMessage("芯片：" + jsonObject.getString(json_keys.CHIP))
                                    .setPositiveButton("确认", (dialog, which) -> {
                                        waiting_import.show();
                                        new Thread(() -> {
                                            try {
                                                error=decodeAndWriteData(jsonObject);
                                            }catch (Exception e){
                                                error=true;
                                            }
                                            activity.runOnUiThread(() -> {
                                                waiting_import.dismiss();
                                                if(!error)
                                                    Toast.makeText(activity,"导入成功",Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(activity,"导入失败，数据与当前设备不兼容",Toast.LENGTH_LONG).show();
                                            });
                                        }).start();
                                    })
                                    .setNegativeButton("取消", null)
                                    .create().show();
                        }catch (Exception e){
                            error=true;
                        }
                    });
                } catch (Exception e) {
                    error=true;
                }
            }
            if(error)
                activity.runOnUiThread(() -> {
                    waiting_import.dismiss();
                    Toast.makeText(activity,"导入失败，解析数据时发生了错误",Toast.LENGTH_LONG).show();
                });
        }
    }

    private static class importFromFile extends MainActivity.fileWorker{
        Activity activity;
        boolean error;
        public importFromFile(Activity activity){
            this.activity=activity;
        }
        public void run(){
            if(uri==null)
                return;
            error=false;
            activity.runOnUiThread(() -> {
                waiting_import.show();
            });
            try {
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(activity.getContentResolver().openInputStream(uri)));
                new showDecodeDialog(activity,bufferedReader.readLine()).start();
                bufferedReader.close();
            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity,"获取目标文件失败",Toast.LENGTH_SHORT).show());
            }
        }
    }

    private static void generateView(Activity activity, LinearLayout page) {
        ((MainActivity)activity).onBackPressedListener=new MainActivity.onBackPressedListener(){
            @Override
            public void onBackPressed() {
                ((MainActivity)activity).showMainView();
            }
        };

        ListView listView=new ListView(activity);
        ArrayList<ParamAdapter.item> items=new ArrayList<>();

        items.add(new ParamAdapter.item(){{
            title="从文件导入";
            subtitle="从文件导入外部频率与电压参数";
        }});

        items.add(new ParamAdapter.item(){{
            title="导出到文件";
            subtitle="导出当前频率和电压参数到文件";
        }});

        items.add(new ParamAdapter.item(){{
            title="从剪贴板导入";
            subtitle="从剪贴板导入外部频率与电压参数";
        }});

        items.add(new ParamAdapter.item(){{
            title="导出到剪贴板";
            subtitle="导出当前频率和电压参数到剪贴板";
        }});

        listView.setOnItemClickListener((parent, view, position, id) -> {

                if(position==0){
                MainActivity.runWithFilePath(activity,new importFromFile(activity));
            }
            else if(position==1){
                showExportDialog(activity, new ConfirmExportCallback() {
                    @Override
                    public void onConfirm(String desc) {
                        MainActivity.runWithStoragePermission(activity,new exportToFile(activity,desc));
                    }
                });
            }else if(position==2){
                import_edittext(activity);
            }
            else if(position==3){
                showExportDialog(activity, new ConfirmExportCallback() {
                    @Override
                    public void onConfirm(String desc) {
                        export_cpy(activity,desc);
                    }
                });
            }
        });

        listView.setAdapter(new ParamAdapter(items,activity));

        page.removeAllViews();
        page.addView(listView);
    }

    static class TableIOLogic extends Thread{
        Activity activity;
        AlertDialog waiting;
        LinearLayout showedView;
        LinearLayout page;
        public TableIOLogic(Activity activity, LinearLayout showedView){
            this.activity=activity;
            this.showedView=showedView;
        }
        public void run(){
            activity.runOnUiThread(() -> {
                waiting_import=DialogUtil.getWaitDialog(activity,"正在导入数据，请稍后");
                waiting=DialogUtil.getWaitDialog(activity,"正在准备进行备份还原");
                waiting.show();
            });

            try{
                GpuTableEditor.init();
                GpuTableEditor.decode();
                if(ChipInfo.which!= ChipInfo.type.lahaina_singleBin) {
                    GpuVoltEditor.init();
                    GpuVoltEditor.decode();
                }
            }catch (Exception e){
                activity.runOnUiThread(() -> DialogUtil.showError(activity,"加载频率电压数据失败"));
            }

            activity.runOnUiThread(() -> {
                waiting.dismiss();
                showedView.removeAllViews();
                page=new LinearLayout(activity);
                page.setOrientation(LinearLayout.VERTICAL);
                try {
                    generateView(activity,page);
                } catch (Exception e){
                    DialogUtil.showError(activity,"准备备份还原失败");
                }
                showedView.addView(page);
            });

        }
    }
}
