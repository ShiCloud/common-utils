package com.github.shicloud.utils;


public class CamelNameUtils {
	
	/**
	 * convert camel name to underscore name
	 * @return
	 */
	public static String camel2underscore(String camelName){
		//先把第一个字母大写
		camelName = capitalize(camelName);
		
		String regex = "([A-Z][a-z]+)";
		String replacement = "$1_";

		String underscoreName = camelName.replaceAll(regex, replacement);
		//output: Pur_Order_Id_ 接下来把最后一个_去掉，然后全部改小写
		
		underscoreName = underscoreName.toLowerCase().substring(0, underscoreName.length()-1);
		
		return underscoreName;
	}
	
	/**
	 * convert underscore name to camel name
	 * @param underscoreName
	 * @return
	 */
	public static String underscore2camel(String underscoreName){
		String[] sections = underscoreName.split("_");
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<sections.length;i++){
			String s = sections[i];
			if(i==0){
				sb.append(s);
			}else{
				sb.append(capitalize(s));
			}
		}
		return sb.toString();
	}
	
	/**
	 * capitalize the first character
	 * @param str
	 * @return
	 */
	public static String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
            .append(Character.toTitleCase(str.charAt(0)))
            .append(str.substring(1))
            .toString();
    }
}
