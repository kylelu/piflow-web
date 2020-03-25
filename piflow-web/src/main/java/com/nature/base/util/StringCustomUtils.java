package com.nature.base.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class StringCustomUtils {

    static Logger logger = LoggerUtil.getLogger();

    private static String EXCLUSIVE_STR = "[PiFlow]";
    //private static String SPECIAL_SYMBOLS_1 = "&";
    //private static String SPECIAL_SYMBOLS_2 = "<";
    //private static String SPECIAL_SYMBOLS_3 = ">";
    //private static String SPECIAL_SYMBOLS_4 = "'";
    //private static String SPECIAL_SYMBOLS_5 = "\"";


    /**
     * Translation of special symbols(< , > , & , " , ')
     *
     * @param sourceStr
     * @return
     */
    public static String replaceSpecialSymbolsXml(String sourceStr) {
        if (StringUtils.isNotBlank(sourceStr)) {
            String translation = sourceStr;
            translation = translation.replace(EXCLUSIVE_STR, EXCLUSIVE_STR + "^_^" + EXCLUSIVE_STR);
            translation = translation.replace("&", EXCLUSIVE_STR + "&amp;" + EXCLUSIVE_STR);
            translation = translation.replace("<", EXCLUSIVE_STR + "&lt;" + EXCLUSIVE_STR);
            translation = translation.replace(">", EXCLUSIVE_STR + "&gt;" + EXCLUSIVE_STR);
            translation = translation.replace("'", EXCLUSIVE_STR + "&apos;" + EXCLUSIVE_STR);
            translation = translation.replace("\"", EXCLUSIVE_STR + "&quot;" + EXCLUSIVE_STR);
            return translation;
        }
        return sourceStr;
    }

    public static String recoverSpecialSymbolsXml(String sourceStr) {
        if (StringUtils.isNotBlank(sourceStr)) {
            String translation = sourceStr;
            translation = translation.replace(EXCLUSIVE_STR + "&amp;" + EXCLUSIVE_STR, "&");
            translation = translation.replace(EXCLUSIVE_STR + "&lt;" + EXCLUSIVE_STR, "<");
            translation = translation.replace(EXCLUSIVE_STR + "&gt;" + EXCLUSIVE_STR, ">");
            translation = translation.replace(EXCLUSIVE_STR + "&apos;" + EXCLUSIVE_STR, "'");
            translation = translation.replace(EXCLUSIVE_STR + "&quot;" + EXCLUSIVE_STR, "\"");
            translation = translation.replace(EXCLUSIVE_STR + "^_^" + EXCLUSIVE_STR, EXCLUSIVE_STR);
            return translation;
        }
        return sourceStr;
    }

    /**
     * Translation of special symbols(< , > , & , " , ')
     *
     * @param sourceStr
     * @return
     */
    public static String replaceSpecialSymbolsPage(String sourceStr) {
        if (StringUtils.isNotBlank(sourceStr)) {
            String translation = sourceStr;
            translation = translation.replace("&", "&amp;");
            translation = translation.replace("<", "&lt;");
            translation = translation.replace(">", "&gt;");
            translation = translation.replace("'", "&apos;");
            translation = translation.replace("\"", "&quot;");
            return translation;
        }
        return sourceStr;
    }

}
