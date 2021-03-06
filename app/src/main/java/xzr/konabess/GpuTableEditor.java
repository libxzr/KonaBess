package xzr.konabess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xzr.konabess.adapters.ParamAdapter;
import xzr.konabess.utils.DialogUtil;
import xzr.konabess.utils.DtsHelper;

public class GpuTableEditor {
    private static int bin_position;
    private static ArrayList<bin> bins;
    private static class bin{
        int id;
        ArrayList<String> header;
        ArrayList<level> levels;
    }
    private static class level{
        ArrayList<String> lines;
    }

    private static ArrayList<String> lines_in_dts;

    public static void init() throws IOException {
        lines_in_dts=new ArrayList<>();
        bins=new ArrayList<>();
        bin_position=-1;
        BufferedReader bufferedReader=new BufferedReader(new FileReader(new File(KonaBessCore.dts_path)));
        String s;
        while((s=bufferedReader.readLine())!=null){
            lines_in_dts.add(s);
        }
    }

    public static void decode() throws Exception{
        int i=-1;
        String this_line;
        int start=-1;
        int end;
        int bracket=0;
        while (++i < lines_in_dts.size()) {
            this_line = lines_in_dts.get(i).trim();

            if((ChipInfo.which== ChipInfo.type.kona_singleBin
                    ||ChipInfo.which== ChipInfo.type.msmnile_singleBin
                    ||ChipInfo.which== ChipInfo.type.lahaina_singleBin)
                    &&this_line.equals("qcom,gpu-pwrlevels {")){
                start=i;
                if(bin_position<0)
                    bin_position=i;
                bracket++;
                continue;
            }
            if ((ChipInfo.which== ChipInfo.type.kona
                    ||ChipInfo.which== ChipInfo.type.msmnile
                    ||ChipInfo.which== ChipInfo.type.lahaina
                    ||ChipInfo.which== ChipInfo.type.lito_v1||ChipInfo.which== ChipInfo.type.lito_v2)
                    &&this_line.contains("qcom,gpu-pwrlevels-")) {
                start = i;
                if(bin_position<0)
                    bin_position=i;
                if (bracket != 0)
                    throw new Exception();
                bracket++;
                continue;
            }

            if (this_line.contains("{")&&start>=0)
                bracket++;
            if (this_line.contains("}")&&start>=0)
                bracket--;

            if (bracket == 0 && start>=0
                    && (ChipInfo.which== ChipInfo.type.kona
                    ||ChipInfo.which== ChipInfo.type.msmnile
                    ||ChipInfo.which== ChipInfo.type.lahaina
                    ||ChipInfo.which== ChipInfo.type.lito_v1||ChipInfo.which== ChipInfo.type.lito_v2)) {
                end = i;
                if (end >= start) {
                    decode_bin(lines_in_dts.subList(start, end + 1));
                    lines_in_dts.subList(start, end + 1).clear();
                } else {throw new Exception();}
                i=start-1;
                start = -1;
                continue;
            }

            if (bracket == 0 && start>=0 && (ChipInfo.which== ChipInfo.type.kona_singleBin
                    ||ChipInfo.which== ChipInfo.type.msmnile_singleBin
                    ||ChipInfo.which== ChipInfo.type.lahaina_singleBin)) {
                end = i;
                if (end >= start) {
                    decode_bin(lines_in_dts.subList(start, end + 1));
                    lines_in_dts.subList(start, end + 1).clear();
                } else {throw new Exception();}
                break;
            }
        }
    }

    private static int getBinID(String line, int prev_id){
        line=line.trim();
        line=line.replace(" {","")
                .replace("-","");
        try{
            for(int i=line.length()-1;i>=0;i--){
                prev_id=Integer.parseInt(line.substring(i));
            }
        }catch (Exception ignored){}
        return prev_id;
    }

    private static void decode_bin(List<String> lines) throws Exception{
        bin bin=new bin();
        bin.header=new ArrayList<>();
        bin.levels=new ArrayList<>();
        bin.id=bins.size();
        int i=0;
        int bracket=0;
        int start=0;
        int end;
        bin.id=getBinID(lines.get(0),bin.id);
        while(++i<lines.size()&&bracket>=0){
            String line=lines.get(i);

            line=line.trim();
            if(line.equals(""))
                continue;

            if(line.contains("{")){
                if(bracket!=0)
                    throw new Exception();
                start=i;
                bracket++;
                continue;
            }

            if(line.contains("}")){
                if(--bracket<0)
                    continue;
                end=i;
                if(end>=start)
                    bin.levels.add(decode_level(lines.subList(start,end+1)));
                continue;
            }

            if(bracket == 0){
                bin.header.add(line);
            }
        }
        bins.add(bin);
    }

    private static level decode_level(List<String> lines){
        level level=new level();
        level.lines=new ArrayList<>();

        for(String line:lines){
            line=line.trim();
            if(line.contains("{")||line.contains("}"))
                continue;
            if(line.contains("reg"))
                continue;
            level.lines.add(line);
        }

        return level;
    }

    public static List<String> genTable(){
        ArrayList<String> lines=new ArrayList<>();
        if(ChipInfo.which== ChipInfo.type.kona
                ||ChipInfo.which== ChipInfo.type.msmnile
                ||ChipInfo.which== ChipInfo.type.lahaina
                ||ChipInfo.which== ChipInfo.type.lito_v1||ChipInfo.which== ChipInfo.type.lito_v2) {
            for (int bin_id = 0; bin_id < bins.size(); bin_id++) {
                lines.add("qcom,gpu-pwrlevels-" + bins.get(bin_id).id + " {");
                lines.addAll(bins.get(bin_id).header);
                for (int pwr_level_id = 0; pwr_level_id < bins.get(bin_id).levels.size(); pwr_level_id++) {
                    lines.add("qcom,gpu-pwrlevel@" + pwr_level_id + " {");
                    lines.add("reg = <" + pwr_level_id + ">;");
                    lines.addAll(bins.get(bin_id).levels.get(pwr_level_id).lines);
                    lines.add("};");
                }
                lines.add("};");
            }
        } else if(ChipInfo.which== ChipInfo.type.kona_singleBin
                ||ChipInfo.which== ChipInfo.type.msmnile_singleBin
                ||ChipInfo.which== ChipInfo.type.lahaina_singleBin){
            lines.add("qcom,gpu-pwrlevels {");
            lines.addAll(bins.get(0).header);
            for (int pwr_level_id = 0; pwr_level_id < bins.get(0).levels.size(); pwr_level_id++) {
                lines.add("qcom,gpu-pwrlevel@" + pwr_level_id + " {");
                lines.add("reg = <" + pwr_level_id + ">;");
                lines.addAll(bins.get(0).levels.get(pwr_level_id).lines);
                lines.add("};");
            }
            lines.add("};");
        }
        return lines;
    }

    public static List<String> genBack(List<String> table){
        ArrayList<String> new_dts=new ArrayList<>(lines_in_dts);
        new_dts.addAll(bin_position,table);
        return new_dts;
    }

    public static void writeOut(List<String> new_dts) throws IOException{
        File file=new File(KonaBessCore.dts_path);
        file.createNewFile();
        BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(file));
        for(String s:new_dts){
            bufferedWriter.write(s);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private static String generateSubtitle(String line) throws Exception{
        if(DtsHelper.decode_hex_line(line).name.equals("qcom,level")){
            return GpuVoltEditor.levelint2str(DtsHelper.decode_int_line(line).value);
        }
        return DtsHelper.shouldUseHex(line)?DtsHelper.decode_hex_line(line).value:DtsHelper.decode_int_line(line).value+"";
    }

    private static void generateALevel(Activity activity,int last,int levelid,LinearLayout page) throws Exception{
        ((MainActivity)activity).onBackPressedListener=new MainActivity.onBackPressedListener(){
            @Override
            public void onBackPressed() {
                try {
                    generateLevels(activity, last, page);
                }catch (Exception ignored){}
            }
        };

        ListView listView=new ListView(activity);
        ArrayList<ParamAdapter.item> items=new ArrayList<>();

        items.add(new ParamAdapter.item(){{
            title=activity.getResources().getString(R.string.back);
            subtitle="";
        }});

        for(String line:bins.get(last).levels.get(levelid).lines){
            items.add(new ParamAdapter.item(){{
                title=KonaBessStr.convert_level_params(DtsHelper.decode_hex_line(line).name,activity);
                subtitle=generateSubtitle(line);
            }});
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
            if(position==0){
                    generateLevels(activity,last,page);
                return;
            }
            String raw_name=DtsHelper.decode_hex_line(bins.get(last).levels.get(levelid).lines.get(position-1)).name;
            String raw_value=DtsHelper.shouldUseHex(bins.get(last).levels.get(levelid).lines.get(position-1))
                    ?DtsHelper.decode_hex_line(bins.get(last).levels.get(levelid).lines.get(position-1)).value
                    :DtsHelper.decode_int_line(bins.get(last).levels.get(levelid).lines.get(position-1)).value+"";

            if(raw_name.equals("qcom,level")){
                try {
                    Spinner spinner = new Spinner(activity);
                    spinner.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, ChipInfo.rpmh_levels.level_str()));
                    spinner.setSelection(GpuVoltEditor.levelint2int(Integer.parseInt(raw_value)));

                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.edit)
                            .setView(spinner)
                            .setMessage(R.string.editvolt_msg)
                            .setPositiveButton(R.string.save, (dialog, which) -> {
                                try {
                                    bins.get(last).levels.get(levelid).lines.set(
                                            position - 1,
                                            DtsHelper.encodeIntOrHexLine(raw_name, ChipInfo.rpmh_levels.levels()[spinner.getSelectedItemPosition()]+""));
                                    generateALevel(activity, last, levelid, page);
                                    Toast.makeText(activity, R.string.save_success, Toast.LENGTH_SHORT).show();
                                }catch (Exception exception){
                                    DialogUtil.showError(activity,R.string.save_failed);
                                    exception.printStackTrace();
                                }
                            })
                            .setNegativeButton(R.string.cancel,null)
                            .create().show();

                }catch (Exception e){
                    DialogUtil.showError(activity,R.string.error_occur);
                }
            }
            else {
                EditText editText = new EditText(activity);
                editText.setInputType(DtsHelper.shouldUseHex(raw_name) ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER);
                editText.setText(raw_value);
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getResources().getString(R.string.edit) + " \""+items.get(position).title+"\"")
                        .setView(editText)
                        .setMessage(KonaBessStr.help(raw_name,activity))
                        .setPositiveButton(R.string.save, (dialog, which) -> {
                            try {
                                bins.get(last).levels.get(levelid).lines.set(
                                        position - 1,
                                        DtsHelper.encodeIntOrHexLine(raw_name, editText.getText().toString()));
                                generateALevel(activity, last, levelid, page);
                                Toast.makeText(activity, R.string.save_success, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                DialogUtil.showError(activity, R.string.save_failed);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create().show();
            }
            } catch (Exception e) {
                DialogUtil.showError(activity,R.string.error_occur);
            }
        });

        listView.setAdapter(new ParamAdapter(items,activity));

        page.removeAllViews();
        page.addView(listView);
    }

    private static level level_clone(level from){
        level next=new level();
        next.lines=new ArrayList<>(from.lines);
        return next;
    }

    private static void offset_initial_level_old(int offset) throws Exception{
        boolean started=false;
        int bracket=0;
        for(int i=0;i<lines_in_dts.size();i++){
            String line=lines_in_dts.get(i);

            if(line.contains("qcom,kgsl-3d0")&&line.contains("{")) {
                started = true;
                bracket++;
                continue;
            }

            if(line.contains("{")){
                bracket++;
                continue;
            }

            if(line.contains("}")){
                bracket--;
                if(bracket==0)
                    break;
                continue;
            }

            if(!started)
                continue;

            if(line.contains("qcom,initial-pwrlevel")){
                lines_in_dts.set(i,
                        DtsHelper.encodeIntOrHexLine(DtsHelper.decode_int_line(line).name,
                                DtsHelper.decode_int_line(line).value+offset+""));
            }

        }
    }

    private static void offset_initial_level(int bin_id,int offset) throws Exception{
        if(ChipInfo.which== ChipInfo.type.kona_singleBin
                ||ChipInfo.which== ChipInfo.type.msmnile_singleBin
                ||ChipInfo.which== ChipInfo.type.lahaina_singleBin){
            offset_initial_level_old(offset);
            return;
        }
        for(int i=0;i<bins.get(bin_id).header.size();i++){
            String line=bins.get(bin_id).header.get(i);
            if(line.contains("qcom,initial-pwrlevel")){
                bins.get(bin_id).header.set(i,
                        DtsHelper.encodeIntOrHexLine(
                                DtsHelper.decode_int_line(line).name,
                                DtsHelper.decode_int_line(line).value+offset+""));
                break;
            }
        }
    }

    private static void offset_ca_target_level(int bin_id,int offset) throws Exception{
        for(int i=0;i<bins.get(bin_id).header.size();i++){
            String line=bins.get(bin_id).header.get(i);
            if(line.contains("qcom,ca-target-pwrlevel")){
                bins.get(bin_id).header.set(i,
                        DtsHelper.encodeIntOrHexLine(
                                DtsHelper.decode_int_line(line).name,
                                DtsHelper.decode_int_line(line).value+offset+""));
                break;
            }
        }
    }

    private static void patch_throttle_level_old() throws Exception{
        boolean started=false;
        int bracket=0;
        for(int i=0;i<lines_in_dts.size();i++){
            String line=lines_in_dts.get(i);

            if(line.contains("qcom,kgsl-3d0")&&line.contains("{")) {
                started = true;
                bracket++;
                continue;
            }

            if(line.contains("{")){
                bracket++;
                continue;
            }

            if(line.contains("}")){
                bracket--;
                if(bracket==0)
                    break;
                continue;
            }

            if(!started)
                continue;

            if(line.contains("qcom,throttle-pwrlevel")){
                lines_in_dts.set(i,
                        DtsHelper.encodeIntOrHexLine(DtsHelper.decode_int_line(line).name,
                                "0"));
            }

        }
    }

    private static void patch_throttle_level() throws Exception{
        if(ChipInfo.which== ChipInfo.type.kona_singleBin
                ||ChipInfo.which== ChipInfo.type.msmnile_singleBin
                ||ChipInfo.which== ChipInfo.type.lahaina_singleBin){
            patch_throttle_level_old();
            return;
        }
        for(int bin_id=0;bin_id<bins.size();bin_id++) {
            for (int i = 0; i < bins.get(bin_id).header.size(); i++) {
                String line = bins.get(bin_id).header.get(i);
                if (line.contains("qcom,throttle-pwrlevel")) {
                    bins.get(bin_id).header.set(i,
                            DtsHelper.encodeIntOrHexLine(
                                    DtsHelper.decode_int_line(line).name, "0"));
                    break;
                }
            }
        }
    }

    public static boolean canAddNewLevel(int binID, Context context){
        if(bins.get(binID).levels.size()<10)
            return true;
        Toast.makeText(context,R.string.unable_add_more,Toast.LENGTH_SHORT).show();
        return false;
    }

    public static int min_level_chip_offset() throws Exception{
        if(ChipInfo.which== ChipInfo.type.lahaina || ChipInfo.which== ChipInfo.type.lahaina_singleBin)
            return 1;
        if(ChipInfo.which== ChipInfo.type.kona||ChipInfo.which== ChipInfo.type.kona_singleBin
                ||ChipInfo.which== ChipInfo.type.msmnile||ChipInfo.which== ChipInfo.type.msmnile_singleBin
                ||ChipInfo.which== ChipInfo.type.lito_v1||ChipInfo.which== ChipInfo.type.lito_v2)
            return 2;
        throw new Exception();
    }

    private static void generateLevels(Activity activity,int id, LinearLayout page) throws Exception{
        ((MainActivity)activity).onBackPressedListener=new MainActivity.onBackPressedListener(){
            @Override
            public void onBackPressed() {
                try {
                    generateBins(activity, page);
                }catch (Exception ignored){}
            }
        };

        ListView listView=new ListView(activity);
        ArrayList<ParamAdapter.item> items=new ArrayList<>();

        items.add(new ParamAdapter.item(){{
            title=activity.getResources().getString(R.string.back);
            subtitle="";
        }});

        items.add(new ParamAdapter.item(){{
            title=activity.getResources().getString(R.string.new_item);
            subtitle=activity.getResources().getString(R.string.new_desc);
        }});

        for(level level:bins.get(id).levels){
            long freq=getFrequencyFromLevel(level);
            if(freq==0)
                continue;;
            ParamAdapter.item item=new ParamAdapter.item();
            item.title=freq/1000000+"MHz";
            item.subtitle="";
            items.add(item);
        }

        items.add(new ParamAdapter.item(){{
            title=activity.getResources().getString(R.string.new_item);
            subtitle=activity.getResources().getString(R.string.new_desc);
        }});

        listView.setOnItemClickListener((parent, view, position, id1) -> {
            if(position==items.size()-1){
                if(!canAddNewLevel(id,activity))
                    return;
                try {
                bins.get(id).levels.add(bins.get(id).levels.size()-min_level_chip_offset(),
                        level_clone(bins.get(id).levels.get(bins.get(id).levels.size()-min_level_chip_offset())));
                    generateLevels(activity,id,page);
                    offset_initial_level(id,1);
                    if(ChipInfo.which== ChipInfo.type.lito_v1||ChipInfo.which== ChipInfo.type.lito_v2)
                        offset_ca_target_level(id,1);
                } catch (Exception e) {
                    DialogUtil.showError(activity,R.string.error_occur);
                }
                return;
            }
            if(position==0){
                try {
                    generateBins(activity, page);
                }catch (Exception ignored){}
                return;
            }
            if(position==1){
                if(!canAddNewLevel(id,activity))
                    return;
                bins.get(id).levels.add(0,level_clone(bins.get(id).levels.get(0)));
                try {
                    generateLevels(activity,id,page);
                    offset_initial_level(id,1);
                    if(ChipInfo.which== ChipInfo.type.lito_v1||ChipInfo.which== ChipInfo.type.lito_v2)
                        offset_ca_target_level(id,1);
                } catch (Exception e) {
                    DialogUtil.showError(activity,R.string.error_occur);
                }
                return;
            }
            position-=2;
            try {
                generateALevel(activity,id,position,page);
            } catch (Exception e) {
                DialogUtil.showError(activity,R.string.error_occur);
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, idd) -> {
            if(position==items.size()-1)
                return true;
            if(bins.get(id).levels.size()==1)
                return true;
            try {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.remove)
                        .setMessage(String.format(activity.getResources().getString(R.string.remove_msg),
                                getFrequencyFromLevel(bins.get(id).levels.get(position - 2)) / 1000000))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            bins.get(id).levels.remove(position-2);
                            try {
                                generateLevels(activity,id,page);
                                offset_initial_level(id,-1);
                                if(ChipInfo.which== ChipInfo.type.lito_v1||ChipInfo.which== ChipInfo.type.lito_v2)
                                    offset_ca_target_level(id,-1);
                            } catch (Exception e) {
                                DialogUtil.showError(activity,R.string.error_occur);
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .create().show();
            }catch (Exception ignored){ignored.printStackTrace();}
            return true;
        });

        listView.setAdapter(new ParamAdapter(items,activity));

        page.removeAllViews();
        page.addView(listView);
    }

    private static long getFrequencyFromLevel(level level) throws Exception{
        for(String line:level.lines){
            if(line.contains("qcom,gpu-freq")){
                return DtsHelper.decode_int_line(line).value;
            }
        }
        throw new Exception();
    }

    private static void generateBins(Activity activity,LinearLayout page) throws Exception{
        ((MainActivity)activity).onBackPressedListener=new MainActivity.onBackPressedListener(){
            @Override
            public void onBackPressed() {
                ((MainActivity)activity).showMainView();
            }
        };

        ListView listView=new ListView(activity);
        ArrayList<ParamAdapter.item> items=new ArrayList<>();

        for(int i=0;i<bins.size();i++){
            ParamAdapter.item item=new ParamAdapter.item();
            item.title=KonaBessStr.convert_bins(bins.get(i).id,activity);
            item.subtitle="";
            items.add(item);
        }

        listView.setAdapter(new ParamAdapter(items,activity));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                generateLevels(activity, position, page);
            }
            catch (Exception e){
                DialogUtil.showError(activity,R.string.error_occur);
            }
        });

        page.removeAllViews();
        page.addView(listView);
    }

    private static View generateToolBar(Activity activity){
        LinearLayout toolbar=new LinearLayout(activity);
        HorizontalScrollView horizontalScrollView=new HorizontalScrollView(activity);
        horizontalScrollView.addView(toolbar);

        {
            Button button=new Button(activity);
            button.setText(R.string.save_freq_table);
            toolbar.addView(button);
            button.setOnClickListener(v -> {
                try {
                    writeOut(genBack(genTable()));
                    Toast.makeText(activity,R.string.save_success,Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    DialogUtil.showError(activity,R.string.save_failed);
                }
            });
        }
        return horizontalScrollView;
    }

    static class gpuTableLogic extends Thread{
        Activity activity;
        AlertDialog waiting;
        LinearLayout showedView;
        LinearLayout page;
        public gpuTableLogic(Activity activity, LinearLayout showedView){
            this.activity=activity;
            this.showedView=showedView;
        }
        public void run(){
            activity.runOnUiThread(() -> {
                waiting= DialogUtil.getWaitDialog(activity,R.string.getting_freq_table);
                waiting.show();
            });

            try{
                init();
                decode();
                patch_throttle_level();
            }catch (Exception e){
                activity.runOnUiThread(() -> DialogUtil.showError(activity,R.string.getting_freq_table_failed));
            }

            activity.runOnUiThread(() -> {
                waiting.dismiss();
                showedView.removeAllViews();
                showedView.addView(generateToolBar(activity));
                page=new LinearLayout(activity);
                page.setOrientation(LinearLayout.VERTICAL);
                try {
                    generateBins(activity, page);
                } catch (Exception e){
                    DialogUtil.showError(activity,R.string.getting_freq_table_failed);
                }
                showedView.addView(page);
            });

        }
    }
}
