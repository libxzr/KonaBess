package xzr.konabess;


public class ChipInfo {
    public static enum type{
        kona,
        kona_singleBin,
        msmnile,
        msmnile_singleBin,
        lahaina_singleBin,
        unknown
    }

    public static String name2chipdesc(String name){
        type t=type.valueOf(name);
        switch (t){
            case kona:
                return "骁龙865系列";
            case kona_singleBin:
                return "骁龙865（安卓10）";
            case msmnile:
                return "骁龙855系列";
            case msmnile_singleBin:
                return "骁龙855（安卓9）";
            case lahaina_singleBin:
                return "骁龙888";
        }
        return "未知";
    }

    public static type which;

    public static class rpmh_levels{
        public static int[] levels(){
            if(ChipInfo.which==type.kona||ChipInfo.which==type.kona_singleBin)
                return rpmh_levels_kona.levels;
            else if(ChipInfo.which==type.msmnile||ChipInfo.which==type.msmnile_singleBin)
                return rpmh_levels_msmnile.levels;
            else if(ChipInfo.which==type.lahaina_singleBin)
                return rpmh_levels_lahaina_singleBin.levels;

            return new int[]{};
        }
        public static String[] level_str(){
            if(ChipInfo.which==type.kona||ChipInfo.which==type.kona_singleBin)
                return rpmh_levels_kona.level_str;
            else if(ChipInfo.which==type.msmnile||ChipInfo.which==type.msmnile_singleBin)
                return rpmh_levels_msmnile.level_str;
            else if(ChipInfo.which==type.lahaina_singleBin)
                return rpmh_levels_lahaina_singleBin.level_str;

            return new String[]{};
        }
    }

    private static class rpmh_levels_kona{
        public static final int[] levels={48,64,80,96,128,144,192,224,256,320,336,384,416};
        public static final String[] level_str={
                "MIN_SVS",
                "LOW_SVS",
                "LOW_SVS_L1",
                "LOW_SVS_L2",
                "SVS",
                "SVS_L0",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L1",
                "NOM_L2",
                "TURBO",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_msmnile{
        private static final int RPMH_REGULATOR_LEVEL_OFFSET=1;
        public static final int[] levels={
                48+RPMH_REGULATOR_LEVEL_OFFSET,
                64+RPMH_REGULATOR_LEVEL_OFFSET,
                128+RPMH_REGULATOR_LEVEL_OFFSET,
                192+RPMH_REGULATOR_LEVEL_OFFSET,
                224+RPMH_REGULATOR_LEVEL_OFFSET,
                256+RPMH_REGULATOR_LEVEL_OFFSET,
                320+RPMH_REGULATOR_LEVEL_OFFSET,
                336+RPMH_REGULATOR_LEVEL_OFFSET,
                352+RPMH_REGULATOR_LEVEL_OFFSET,
                384+RPMH_REGULATOR_LEVEL_OFFSET,
                416+RPMH_REGULATOR_LEVEL_OFFSET
        };
        public static final String[] level_str={
                "MIN_SVS",
                "LOW_SVS",
                "SVS",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L1",
                "NOM_L2",
                "NOM_L3",
                "TURBO",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_lahaina_singleBin{
        public static final int[] levels={48,64,80,96,128,144,192,224,256,320,336,384,416};
        public static final String[] level_str={
                "MIN_SVS",
                "LOW_SVS",
                "LOW_SVS_L1",
                "LOW_SVS_L2",
                "SVS",
                "SVS_L0",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L1",
                "NOM_L2",
                "TURBO",
                "TURBO_L1"
        };
    }

}
