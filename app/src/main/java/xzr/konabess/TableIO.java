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
import java.util.Arrays;
import java.util.Date;

import xzr.konabess.adapters.ParamAdapter;
import xzr.konabess.utils.DialogUtil;
import xzr.konabess.utils.GzipUtils;

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
        public static final String FREQ="freq";
        public static final String VOLT="volt";
    }

    private static AlertDialog waiting_import;

    private static boolean decodeAndWriteData(JSONObject jsonObject) throws Exception{
        if(!ChipInfo.checkChipGeneral(ChipInfo.type.valueOf(jsonObject.getString(json_keys.CHIP))))
            return true;
        ArrayList<String> freq=new ArrayList<>(Arrays.asList(jsonObject.getString(json_keys.FREQ).split("\n")));
        GpuTableEditor.writeOut(GpuTableEditor.genBack(freq));
        if(ChipInfo.which!= ChipInfo.type.lahaina && ChipInfo.which!= ChipInfo.type.lahaina_singleBin && ChipInfo.which!= ChipInfo.type.shima){
            ArrayList<String> volt=new ArrayList<>(Arrays.asList(jsonObject.getString(json_keys.VOLT).split("\n")));
            //Init again because the dts file has been updated
            GpuVoltEditor.init();
            GpuVoltEditor.decode();
            GpuVoltEditor.writeOut(GpuVoltEditor.genBack(volt));
        }

        return false;
    }

    private static String getFreqData(){
        StringBuilder data= new StringBuilder();
        for(String line:GpuTableEditor.genTable())
            data.append(line).append("\n");
        return data.toString();
    }

    private static String getVoltData(){
        StringBuilder data= new StringBuilder();
        for(String line:GpuVoltEditor.genTable())
            data.append(line).append("\n");
        return data.toString();
    }

    private static String getConfig(String desc) throws IOException {
        JSONObject jsonObject = new JSONObject();
        try {
            /*jsonObject.put(json_keys.MODEL, getCurrent("model"));
            jsonObject.put(json_keys.BRAND, getCurrent("brand"));
            jsonObject.put(json_keys.ID, getCurrent("id"));
            jsonObject.put(json_keys.VERSION, getCurrent("version"));
            jsonObject.put(json_keys.FINGERPRINT, getCurrent("fingerprint"));
            jsonObject.put(json_keys.MANUFACTURER, getCurrent("manufacturer"));
            jsonObject.put(json_keys.DEVICE, getCurrent("device"));
            jsonObject.put(json_keys.NAME, getCurrent("name"));
            jsonObject.put(json_keys.BOARD, getCurrent("board"));*/
            jsonObject.put(json_keys.CHIP, ChipInfo.which);
            jsonObject.put(json_keys.DESCRIPTION, desc);
            jsonObject.put(json_keys.FREQ,getFreqData());
            if(ChipInfo.which!= ChipInfo.type.lahaina && ChipInfo.which!= ChipInfo.type.lahaina_singleBin
                    &&ChipInfo.which!= ChipInfo.type.shima)
                jsonObject.put(json_keys.VOLT,getVoltData());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return GzipUtils.compress(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void import_edittext(Activity activity){
        EditText editText=new EditText(activity);
        editText.setHint(activity.getResources().getString(R.string.paste_here));

        new AlertDialog.Builder(activity)
                .setTitle(R.string.import_data)
                .setView(editText)
                .setPositiveButton(R.string.confirm, (dialog, which) -> new showDecodeDialog(activity,editText.getText().toString()).start())
                .setNegativeButton(R.string.cancel,null)
                .create().show();
    }

    private static abstract class ConfirmExportCallback{
        public abstract void onConfirm(String desc);
    }

    private static void showExportDialog(Activity activity,ConfirmExportCallback confirmExportCallback){
        EditText editText=new EditText(activity);
        editText.setHint(R.string.input_introduction_here);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.export_data)
                .setMessage(R.string.export_data_msg)
                .setView(editText)
                .setPositiveButton(R.string.confirm, (dialog, which) -> confirmExportCallback.onConfirm(editText.getText().toString()))
                .setNegativeButton(R.string.cancel,null)
                .create().show();
    }

    private static void export_cpy(Activity activity,String desc){
        // TODO: clipboard
        try {
            DialogUtil.showDetailedInfo(activity, R.string.export_done, R.string.export_done_msg, "konabess://" + getConfig(desc));
        }catch (Exception e){
            DialogUtil.showError(activity,R.string.error_occur);
        }
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
                    Toast.makeText(activity,activity.getResources().getString(R.string.success_export_to)+" "+out.getAbsolutePath(),Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(activity,R.string.failed_export,Toast.LENGTH_SHORT).show();
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
                    String decoded_data = GzipUtils.uncompress(data);
                    jsonObject = new JSONObject(decoded_data);
                    activity.runOnUiThread(() -> {
                        waiting_import.dismiss();
                        try {
                            new AlertDialog.Builder(activity)
                                    .setTitle(R.string.going_import)
                                    .setMessage(jsonObject.getString(json_keys.DESCRIPTION)+"\n"+activity.getResources().getString(R.string.compatible_chip) + ChipInfo.name2chipdesc(jsonObject.getString(json_keys.CHIP),activity))
                                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
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
                                                    Toast.makeText(activity,R.string.success_import,Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(activity,R.string.failed_incompatible,Toast.LENGTH_LONG).show();
                                            });
                                        }).start();
                                    })
                                    .setNegativeButton(R.string.cancel, null)
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
                    Toast.makeText(activity,R.string.failed_decoding,Toast.LENGTH_LONG).show();
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
                activity.runOnUiThread(() -> Toast.makeText(activity,R.string.unable_get_target_file,Toast.LENGTH_SHORT).show());
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
            title=activity.getResources().getString(R.string.import_from_file);
            subtitle=activity.getResources().getString(R.string.import_from_file_msg);
        }});

        items.add(new ParamAdapter.item(){{
            title=activity.getResources().getString(R.string.export_to_file);
            subtitle=activity.getResources().getString(R.string.export_to_file_msg);
        }});

        items.add(new ParamAdapter.item(){{
            title=activity.getResources().getString(R.string.import_from_clipboard);
            subtitle=activity.getResources().getString(R.string.import_from_clipboard_msg);
        }});

        items.add(new ParamAdapter.item(){{
            title=activity.getResources().getString(R.string.export_to_clipboard);
            subtitle=activity.getResources().getString(R.string.export_to_clipboard_msg);
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
                waiting_import=DialogUtil.getWaitDialog(activity,R.string.wait_importing);
                waiting=DialogUtil.getWaitDialog(activity,R.string.prepare_import_export);
                waiting.show();
            });

            try{
                GpuTableEditor.init();
                GpuTableEditor.decode();
                if(ChipInfo.which!= ChipInfo.type.lahaina && ChipInfo.which!= ChipInfo.type.lahaina_singleBin
                        &&ChipInfo.which!= ChipInfo.type.shima) {
                    GpuVoltEditor.init();
                    GpuVoltEditor.decode();
                }
            }catch (Exception e){
                activity.runOnUiThread(() -> DialogUtil.showError(activity,R.string.failed_getting_freq_voltage));
            }

            activity.runOnUiThread(() -> {
                waiting.dismiss();
                showedView.removeAllViews();
                page=new LinearLayout(activity);
                page.setOrientation(LinearLayout.VERTICAL);
                try {
                    generateView(activity,page);
                } catch (Exception e){
                    DialogUtil.showError(activity,R.string.error_occur);
                }
                showedView.addView(page);
            });

        }
    }
}
