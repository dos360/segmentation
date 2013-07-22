package org.apache.lucene.analysis.cn.smart.dict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegGraph {
	
	/**
	 * 用一个ArrayList记录startOffset相同的Token，这个startOffset就是Token的key 再hash
	 */
	private Map<Integer, List<SegToken>> tokenListTable = new HashMap<Integer, List<SegToken>>();
	
	private int maxStart = -1;
	
	/**
	 * 查看startOffset为s的Token是否存在，如果没有则说明s处没有Token或者还没有添加
	 * 
	 * @param s startOffset
	 * @return
	 */
	public boolean isStartExist(int s) {
		return tokenListTable.get(s) != null;
	}
	
	/**
	 * 取出startOffset为s的所有Tokens，如果没有则返回null
	 * 
	 * @param s
	 * @return 所有相同startOffset的Token的序列
	 */
	public List<SegToken> getStartList(int s) {
		return tokenListTable.get(s);
	}
	
	public int getMaxStart() {
		return maxStart;
	}
	
	/**
	 * 为SegGraph中的所有Tokens生成一个统一的index，index从0开始，
	 * 按照startOffset递增的顺序排序，相同startOffset的Tokens按照放置先后顺序排序
	 */
	public List<SegToken> makeIndex() {
		List<SegToken> result = new ArrayList<SegToken>();
		int s = -1, count = 0, size = tokenListTable.size();
		List<SegToken> tokenList;
		short index = 0;
		while (count < size) {
			if (isStartExist(s)) {
				tokenList = tokenListTable.get(s);
			}
		}
	}

}
