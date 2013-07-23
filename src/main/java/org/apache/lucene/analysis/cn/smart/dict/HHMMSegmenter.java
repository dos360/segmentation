package org.apache.lucene.analysis.cn.smart.dict;

import java.util.List;

import org.apache.lucene.analysis.cn.smart.CharType;
import org.apache.lucene.analysis.cn.smart.WordType;

import org.apache.lucene.analysis.cn.smart.Utility;

public class HHMMSegmenter {
	
	private static WordDictionary wordDict = WordDictionary.getInstance();
	
	/**
	 * 寻找sentence中所有可能的Token，最后再添加两个特殊Token，"始##始",
	 * "末##末"，"始##始"Token的起始位置是-1,"末##末"Token的起始位置是句子的长度
	 * 
	 * @param sentence
	 *            输入的句子，不包含"始##始","末##末"等
	 * @return 所有可能的Token
	 * @see MultiTokenMap
	 */
	private SegGraph createSegGraph(String sentence) {
		int i = 0, j;
		int length = sentence.length();
		int foundIndex;
		CharType[] charTypeArray = getCharTypes(sentence);
		StringBuffer sb = new StringBuffer();
		SegToken token;
		int frequency = 0; // word 出现的次数
		boolean hasFullWidth;
		WordType wordType;
		
		
		SegGraph segGraph = new SegGraph();
		while (i < length) {
			hasFullWidth = false;
			switch (charTypeArray[i]) {
			case SPACE_LIKE:
				i++;
				break;// 跳过
			case HANZI:
				j = i + 1;
				sb.delete(0, sb.length());
				// 不管单个汉字能不能构成词， 都将单个汉字存到segGraph中去，
				sb.append(sentence.charAt(i));
				char[] charArray = new char[] { sentence.charAt(i) };
				// 获取词频
				frequency = wordDict.getFrequency(charArray);
				token = new SegToken(charArray, i, j, WordType.CHINESE_WORD, frequency);
				segGraph.addToken(token);
				
				foundIndex = wordDict.getPrefixMatch(charArray);
				while (j <= length && foundIndex != -1) {
					
				}
			}
		}
	}

	/**
	 * 为sentence中的每个字符确定唯一的字符类型
	 * 
	 * @see Utility.charType(char)
	 * @param sentence
	 *            输入的完成句子
	 * @return 返回的字符类型数组，如果输入为null，返回也是null
	 */
	private CharType[] getCharTypes(String sentence) {
		int length = sentence.length();
		CharType[] charTypeArray = new CharType[length];
		// 生成对应单个汉字的字符类型数组
		for (int i = 0; i < length; i++)
			charTypeArray[i] = Utility.getCharType(sentence.charAt(i));
		return charTypeArray;
	}
	
	/** 最短路径分一手词先 */
	public List<SegToken> process(String sentence) {
		SegGraph segGraph = createSegGraph(sentence);
		BiSegGraph biSegGraph = new BiSegGraph(segGraph);
		List<SegToken> shortPath = biSegGraph.getShortPath();
		return shortPath;
	}
	
	

}
