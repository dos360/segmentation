package org.apache.lucene.analysis.cn.smart.dict;

import org.apache.lucene.analysis.cn.smart.WordType;

public class SegToken {
	
	public char[] charArray;
	
	public int startOffset;
	
	public int endOffset;
	
	public WordType wordType;
	
	public int weight;
	
	public int index;
	
	public SegToken(String word, int start, int end, WordType wordType, int weight) {
		this.charArray = word.toCharArray();
		this.startOffset = start;
		this.endOffset = end;
		this.wordType = wordType;
		this.weight = weight;
	}

	public SegToken(char[] charArray, int startOffset, int endOffset,
			WordType wordType, int weight) {
		this.charArray = charArray;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.wordType = wordType;
		this.weight = weight;
	}
	
}
