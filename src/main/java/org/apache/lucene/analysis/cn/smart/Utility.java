package org.apache.lucene.analysis.cn.smart;

public class Utility {

	public static final char[] COMMON_DELIMITER = new char[] {','};
	
	/**
	 * 需要跳过的符号，例如制表符，回车，换行等等。
	 */
	public static final String SPACES = " 　\t\r\n";
	
	public static final int MAX_FREQUENCE = 2079997 + 80000;

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

	/**
	 * 比较两个整数数组的大小, 分别从数组的一定位置开始逐个比较, 当依次相等且都到达末尾时, 返回相等, 否则未到达末尾的大于到达末尾的;
	 * 当未到达末尾时有一位不相等, 该位置数值大的数组大于小的
	 * 
	 * @param larray
	 * @param lstartIndex
	 *            larray的起始位置
	 * @param rarray
	 * @param rstartIndex
	 *            rarray的起始位置
	 * @return 0表示相等，1表示larray > rarray, -1表示larray < rarray
	 */
	public static int compareArray(char[] larray, int lstartIndex,
			char[] rarray, int rstartIndex) {
		if (larray == null) {
			if (rarray == null || rstartIndex > rarray.length)
				return 0;
			else 
				return -1;
		} else {
			if (rarray == null) {
				if (lstartIndex >= larray.length)
					return 0;
				else
					return 1;
			}
		}
		
		int li = lstartIndex , ri = rstartIndex;
		while (li < larray.length && ri < rarray.length && larray[li] == rarray[ri]) {
			li ++;
			ri ++;
		}
		if (li == larray.length) {
			if (ri == rarray.length)
				return 0;
			else 
				return -1;
		} else {
			if (ri == rarray.length) {
				// larray没有结束，但是rarray已经结束，因此larray > rarray
				return 1;
			} else {
				if (larray[li] > rarray[ri])
					return 1;
				else
					return -1;
			}
		}
	}

}
