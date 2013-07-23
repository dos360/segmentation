package org.apache.lucene.analysis.cn.smart.dict;

import org.apache.lucene.analysis.cn.smart.Utility;

public class SegTokenFilter {
	
	public SegToken filter(SegToken token) {
		switch (token.wordType) {
		case FULLWIDTH_NUMBER:
		case FULLWIDTH_STRING:
			for (int i = 0; i < token.charArray.length; i++) {
				if (token.charArray[i] >= 0xFF10)
					token.charArray[i] -= 0xFEE0;
				
				if (token.charArray[i] >= 0x0041 && token.charArray[i] <= 0x005A)
					token.charArray[i] += 0x0020;
			}
			break;
		case STRING:
			for (int i = 0; i < token.charArray.length; i++) {
				if (token.charArray[i] >= 0x0041 && token.charArray[i] <= 0x005A)
					token.charArray[i] += 0x0020;
			}
			break;
		case DELIMITER:
			token.charArray = Utility.COMMON_DELIMITER;// 统一转成 ,
			break;
		default:
			break;
		}
		return token;
	}

}
