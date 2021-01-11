package xzr.konabess;


public class KonaBessStr {
    public static String convert_bins(int which) throws Exception{
        if(ChipInfo.which== ChipInfo.type.kona)
            return convert_bins_kona(which);
        else if(ChipInfo.which== ChipInfo.type.kona_old)
            return convert_bins_kona_old(which);
        else if(ChipInfo.which== ChipInfo.type.msmnile)
            return convert_bins_msmnile(which);
        else if(ChipInfo.which== ChipInfo.type.lahaina)
            return convert_bins_lahaina(which);

        throw new Exception();
    }

    public static String convert_bins_kona(int which){
        switch(which){
            case 0:
                return "骁龙865";
            case 1:
                return "骁龙865+";
            case 2:
                return "骁龙865-";
        }
        return "未知频率表"+which;
    }

    public static String convert_bins_kona_old(int which){
        switch(which){
            case 0:
                return "骁龙865（安卓10）";
        }
        return "未知频率表"+which;
    }

    public static String convert_bins_msmnile(int which){
        switch(which){
            case 0:
                return "骁龙855";
            case 1:
                return "骁龙855+";
        }
        return "未知频率表"+which;
    }

    public static String convert_bins_lahaina(int which){
        switch(which){
            case 0:
                return "骁龙888";
        }
        return "未知频率表"+which;
    }

    public static String convert_level_params(String input){
        input=input.replace("qcom,","");
        if(input.equals("gpu-freq"))
            return "频率";
        if(input.equals("level"))
            return "电压";

        return input;
    }

    public class editVolt{
        public static final String title="修改电压";
        public static final String msg="这是指这个频率对应电压，您可以为这个频率指定要使用的电压等级。\n" +
                "电压等级从上往下，电压依次提高。";
    }

    public static String help(String what){
        if(what.equals("qcom,gpu-freq"))
            return ChipInfo.which== ChipInfo.type.lahaina?
                    "这是GPU的工作频率，您可以通过改变该数值来改变频率表中的可用频率。\n" +
                    "此处频率的单位是Hz，也就是说在MHz频率后面加上6个零，您可以在上一级菜单中以MHz查看频率。\n" +
                    "如果您搞不清楚要加几个零，那么建议只修改数字的前几位。"
                    :
                    "这是GPU的工作频率，您可以通过改变该数值来改变频率表中的可用频率。\n" +
                    "但是，切记，请确保该频率能够在电压表中找到对应的电压，否则，开机是不可能的。\n" +
                    "此处频率的单位是Hz，也就是说在MHz频率后面加上6个零，您可以在上一级菜单中以MHz查看频率。\n" +
                    "如果您搞不清楚要加几个零，那么建议只修改数字的前几位。";
        if(what.contains("bus"))
            return "这大概是这个GPU频率所对应的内存总线频率相关配置，数值指定的是频率挡位。\n" +
                    "请不要将数值修改的过大（超过默认最高频率所给的挡位），这可能导致找不到指定挡位，从而无法开机。";
        if(what.contains("acd"))
            return "ACD也许是指“自动校准的动态自适应时钟分配”？似乎是一个和电压调整有关的东西，再问就是不知道。";
        return "";
    }

    public static String generic_help(){
        return ChipInfo.which== ChipInfo.type.lahaina?generic_help_1_table:generic_help_2_table;
    }
    private static String generic_help_2_table="欢迎使用本工具！\n" +
            "通过使用本工具，您可以自由的对骁龙855、865、888系列处理器的GPU进行超频、降压等操作。\n" +
            "以下通过一个示例来说明如何超频：\n" +
            "首先点击“编辑GPU频率表”，软件将会显示设备中存在的频率表，一个设备中可能存在多个频率表。" +
            "我已对频率表按照高通默认的情况进行了注释，但是注释不一定准确，因此请根据各种GPU频率" +
            "调整软件中的频率表来判断您的设备到底在使用哪个频率表。\n" +
            "点击一个频率表，将会显示这个频率表中的所有频率，然后点击新建，此时最高频率会被复制一遍。" +
            "我们点击复制出的新频率，进入参数设置页面，点击上方的“频率”，根据需求修改。\n" +
            "修改完毕后，点击保存。您可以在保存后再次点击“编辑GPU频率表”来刷新查看，确认保存成功。\n" +
            "然后我们点击“编辑GPU电压表”，此时将会显示所有频率表可使用的频率，如果频率表指定的频率不在其中，无法开机是必然的。\n" +
            "我们点击新建，此时最上方的频率会被复制一份，我们进入新建的频率。\n" +
            "点击“频率”，将其修改为之前在编辑频率表时指定的频率。然后，可以为这个频率选择一个合适的电压等级。\n" +
            "再次点击“保存”，您可以在保存后再次点击“编辑GPU电压表”来刷新查看，确认保存成功。\n" +
            "确认一切没问题后，刷入镜像，重启手机即可。\n" +
            "一句话概括，请确保频率表中的频率存在于电压表中。\n";

    private static String generic_help_1_table="欢迎使用本工具！\n" +
            "通过使用本工具，您可以自由的对骁龙855、865、888系列处理器的GPU进行超频、降压等操作。\n" +
            "以下通过一个示例来说明如何超频：\n" +
            "首先点击“编辑GPU频率表”，软件将会显示设备中存在的频率表，一个设备中可能存在多个频率表。" +
            "我已对频率表按照高通默认的情况进行了注释，但是注释不一定准确，因此请根据各种GPU频率" +
            "调整软件中的频率表来判断您的设备到底在使用哪个频率表。\n" +
            "点击一个频率表，将会显示这个频率表中的所有频率，然后点击新建，此时最高频率会被复制一遍。" +
            "我们点击复制出的新频率，进入参数设置页面，点击上方的“频率”，根据需求修改。\n" +
            "修改完毕后，点击保存。您可以在保存后再次点击“编辑GPU频率表”来刷新查看，确认保存成功。\n" +
            "确认一切没问题后，刷入镜像，重启手机即可。\n";
}
