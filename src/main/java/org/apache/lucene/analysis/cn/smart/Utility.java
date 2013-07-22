package org.apache.lucene.analysis.cn.smart;

public class Utility {

	/**
	 * 需要跳过的符号，例如制表符，回车，换行等等。
	 */
	public static final String SPACES = " 　\t\r\n";

	/** 得到一个字符的字符类型 */
	public static CharType getCharType(char ch) {
		// 汉字分词要在英文分词之后进行 最多的是汉字
		if (ch >= 0x4E00 && ch <= 0x9FA5)
			return CharType.HANZI;
		if ((ch >= 0x0041 && ch <= 0x005A) || (ch >= 0x0061 && ch <= 0x007A))
			return CharType.LETTER;
		if (ch >= 0x0030 && ch <= 0x0039)
		      return CharType.DIGIT;
		if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '　')
			return CharType.SPACE_LIKE;
		// 最前面的其它的都是标点符号了
		if ((ch >= 0x0021 && ch <= 0x00BB) || (ch >= 0x2010 && ch <= 0x2642)
				|| (ch >= 0x3001 && ch <= 0x301E))
			return CharType.DELIMITER;
		
		// 全角字符区域
		if ((ch >= 0xFF21 && ch <= 0xFF3A) || (ch >= 0xFF41 && ch <= 0xFF5A))
			return CharType.FULLWIDTH_LETTER;
		if (ch >= 0xFF10 && ch <= 0xFF19)
			return CharType.FULLWIDTH_DIGIT;
		if (ch >= 0xFE30 && ch <= 0xFF63)
			return CharType.DELIMITER;
		return CharType.OTHER;
	}

}
