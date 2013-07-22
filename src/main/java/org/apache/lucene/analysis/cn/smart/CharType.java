package org.apache.lucene.analysis.cn.smart;

public enum CharType {

	/** (全角半角)标点符号 */
	DELIMITER, 
	
	/** (半角) 字母 */
	LETTER, 
	
	/** (半角) 数字*/
	DIGIT, 
	
	/** 汉字 */
	HANZI, 
	
	/** 空格 */
	SPACE_LIKE, 
	
	/** (全角) 字母 */
	FULLWIDTH_LETTER, 
	
	/** (全角) 数字*/
	FULLWIDTH_DIGIT, 
	
	/** 未知 */
	OTHER;
}
