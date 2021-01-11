package xzr.konabess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
        ArrayList<String> header;
        ArrayList<level> levels;
    }
    private static class level{
        ArrayList<String> lines;
    }

    private static ArrayList<String> lines_in_dts;

    private static void init(Context context) throws IOException {
        lines_in_dts=new ArrayList<>();
        bins=new ArrayList<>();
        bin_position=-1;
        BufferedReader bufferedReader=new BufferedReader(new FileReader(new File(KonaBessCore.dts_path)));
        String s;
        while((s=bufferedReader.readLine())!=null){
            lines_in_dts.add(s);
        }
    }

    private static void decode() throws Exception{
        int i=-1;
        String this_line;
        int start=-1;
        int end;
        int bracket=0;
        while (++i < lines_in_dts.size()) {
            this_line = lines_in_dts.get(i).trim();

            if((ChipInfo.which== ChipInfo.type.kona||ChipInfo.which== ChipInfo.type.kona_old)
                    &&this_line.equals("qcom,gpu-pwrlevels {")){
                if(ChipInfo.which== ChipInfo.type.kona)
                    ChipInfo.which= ChipInfo.type.kona_old;
                start=i;
                if(bin_position<0)
                    bin_position=i;
                bracket++;
                continue;
            }
            if ((ChipInfo.which== ChipInfo.type.kona||ChipInfo.which== ChipInfo.type.msmnile)
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
                    && (ChipInfo.which== ChipInfo.type.kona||ChipInfo.which== ChipInfo.type.msmnile)) {
                end = i;
                if (end >= start) {
                    decode_bin(lines_in_dts.subList(start, end + 1));
                    lines_in_dts.subList(start, end + 1).clear();
                } else {throw new Exception();}
                i=start-1;
                start = -1;
                continue;
            }

            if (bracket == 0 && start>=0 && ChipInfo.which== ChipInfo.type.kona_old) {
                end = i;
                if (end >= start) {
                    decode_bin(lines_in_dts.subList(start, end + 1));
                    lines_in_dts.subList(start, end + 1).clear();
                } else {throw new Exception();}
                break;
            }
        }
    }

    private static void decode_bin(List<String> lines) throws Exception{
        bin bin=new bin();
        bin.header=new ArrayList<>();
        bin.levels=new ArrayList<>();
        int i=0;
        int bracket=0;
        int start=0;
        int end;
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

    private static List<String> genBack(){
        ArrayList<String> lines=new ArrayList<>();
        ArrayList<String> new_dts=new ArrayList<>(lines_in_dts);
        if(ChipInfo.which== ChipInfo.type.kona||ChipInfo.which== ChipInfo.type.msmnile) {
            for (int bin_id = 0; bin_id < bins.size(); bin_id++) {
                lines.add("qcom,gpu-pwrlevels-" + bin_id + " {");
                lines.addAll(bins.get(bin_id).header);
                for (int pwr_level_id = 0; pwr_level_id < bins.get(bin_id).levels.size(); pwr_level_id++) {
                    lines.add("qcom,gpu-pwrlevel@" + pwr_level_id + " {");
                    lines.add("reg = <" + pwr_level_id + ">;");
                    lines.addAll(bins.get(bin_id).levels.get(pwr_level_id).lines);
                    lines.add("};");
                }
                lines.add("};");
            }
        } else if(ChipInfo.which== ChipInfo.type.kona_old){
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
        new_dts.addAll(bin_position,lines);
        return new_dts;
    }

    private static void writeOut(Context context,List<String> new_dts) throws IOException{
        File file=new File(KonaBessCore.dts_path);
        file.createNewFile();
        BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(file));
        for(String s:new_dts){
            bufferedWriter.write(s);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private static void generateALevel(Activity activity,int last,int levelid,LinearLayout page) throws Exception{
        ListView listView=new ListView(activity);
        ArrayList<ParamAdapter.item> items=new ArrayList<>();

        items.add(new ParamAdapter.item(){{
            title="返回上一级";
            subtitle="";
        }});

        for(String line:bins.get(last).levels.get(levelid).lines){
            items.add(new ParamAdapter.item(){{
                title=KonaBessStr.convert_level_params(DtsHelper.decode_hex_line(line).name);
                subtitle=DtsHelper.shouldUseHex(line)?DtsHelper.decode_hex_line(line).value:DtsHelper.decode_int_line(line).value+"";
            }});
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
            if(position==0){
                    generateLevels(activity,last,page);
                return;
            }
            EditText editText=new EditText(activity);
            editText.setInputType(DtsHelper.shouldUseHex(DtsHelper.decode_hex_line(bins.get(last).levels.get(levelid).lines.get(position-1)).name)?InputType.TYPE_CLASS_TEXT:InputType.TYPE_CLASS_NUMBER);
            editText.setText(items.get(position).subtitle);
            new AlertDialog.Builder(activity)
                    .setTitle("编辑"+items.get(position).title+"")
                    .setView(editText)
                    .setMessage(KonaBessStr.help(DtsHelper.decode_hex_line(bins.get(last).levels.get(levelid).lines.get(position-1)).name))
                    .setPositiveButton("保存", (dialog, which) -> {
                        try {
                            bins.get(last).levels.get(levelid).lines.set(
                                    position-1,
                                    DtsHelper.encodeIntOrHexLine(DtsHelper.decode_hex_line(bins.get(last).levels.get(levelid).lines.get(position-1)).name,editText.getText().toString()));
                            generateALevel(activity, last, levelid, page);
                            Toast.makeText(activity,"保存成功",Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            DialogUtil.showError(activity,"保存错误");
                        }
                    })
                    .setNegativeButton("取消",null)
                    .create().show();
            } catch (Exception e) {
                DialogUtil.showError(activity,"编辑失败");
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
        if(ChipInfo.which== ChipInfo.type.kona_old){
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
        if(ChipInfo.which== ChipInfo.type.kona_old){
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
        Toast.makeText(context,"不能再增加更多频率了",Toast.LENGTH_SHORT).show();
        return false;
    }

    private static void generateLevels(Activity activity,int id, LinearLayout page) throws Exception{
        ListView listView=new ListView(activity);
        ArrayList<ParamAdapter.item> items=new ArrayList<>();

        items.add(new ParamAdapter.item(){{
            title="上一级";
            subtitle="";
        }});

        items.add(new ParamAdapter.item(){{
            title="新建";
            subtitle="增加新的频率";
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
            title="新建";
            subtitle="增加新的频率";
        }});

        listView.setOnItemClickListener((parent, view, position, id1) -> {
            if(position==bins.get(id).levels.size()+1){
                if(!canAddNewLevel(id,activity))
                    return;
                bins.get(id).levels.add(bins.get(id).levels.size()-2,level_clone(bins.get(id).levels.get(bins.get(id).levels.size()-2)));
                try {
                    generateLevels(activity,id,page);
                    offset_initial_level(id,1);
                } catch (Exception e) {
                    DialogUtil.showError(activity,"增加新频率失败");
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
                } catch (Exception e) {
                    DialogUtil.showError(activity,"增加新频率失败");
                }
                return;
            }
            position-=2;
            try {
                generateALevel(activity,id,position,page);
            } catch (Exception e) {
                DialogUtil.showError(activity,"打开频率失败");
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, idd) -> {
            if(position==bins.get(id).levels.size()+1)
                return true;
            try {
                new AlertDialog.Builder(activity)
                        .setTitle("删除频率")
                        .setMessage("你确定要删除" + getFrequencyFromLevel(bins.get(id).levels.get(position - 2)) / 1000000 + "MHz吗？")
                        .setPositiveButton("确认删除", (dialog, which) -> {
                            bins.get(id).levels.remove(position-2);
                            try {
                                generateLevels(activity,id,page);
                                offset_initial_level(id,-1);
                            } catch (Exception e) {
                                DialogUtil.showError(activity,"删除失败");
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create().show();
            }catch (Exception ignored){}
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
        ListView listView=new ListView(activity);
        ArrayList<ParamAdapter.item> items=new ArrayList<>();

        for(int i=0;i<bins.size();i++){
            ParamAdapter.item item=new ParamAdapter.item();
            item.title=KonaBessStr.convert_bins(i);
            item.subtitle="";
            items.add(item);
        }

        listView.setAdapter(new ParamAdapter(items,activity));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                generateLevels(activity, position, page);
            }
            catch (Exception e){
                DialogUtil.showError(activity,"加载频率表失败");
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
            button.setText("保存GPU频率表修改");
            toolbar.addView(button);
            button.setOnClickListener(v -> {
                try {
                    writeOut(activity,genBack());
                    Toast.makeText(activity,"保存成功",Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    DialogUtil.showError(activity,"保存修改失败");
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
                waiting= DialogUtil.getWaitDialog(activity,"正在获取GPU频率表，请稍后");
                waiting.show();
            });

            try{
                init(activity);
                decode();
                patch_throttle_level();
            }catch (Exception e){
                activity.runOnUiThread(() -> DialogUtil.showError(activity,"获取GPU频率表失败"));
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
                    DialogUtil.showError(activity,"获取GPU频率表失败");
                }
                showedView.addView(page);
            });

        }
    }
}
