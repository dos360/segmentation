package org.apache.lucene.analysis.cn.smart.dict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.cn.smart.Utility;

public class BiSegGraph {

	private Map<Integer, List<SegTokenPair>> tokenPairListTable = new HashMap<Integer, List<SegTokenPair>>();

	private List<SegToken> segTokenList;

	private static BigramDictionary bigramDict = BigramDictionary.getInstance();

	public BiSegGraph(SegGraph segGraph) {
		segTokenList = segGraph.makeIndex();
		generateBitSegGraph(segGraph);
	}

	/**
	 * 生成两两词之间的二叉图表，将结果保存在一个MultiTokenPairMap中
	 * 
	 * @param segGraph
	 *            所有的Token列表
	 * @param smooth
	 *            平滑系数
	 * @param biDict
	 *            二叉词典
	 * @return
	 * 
	 * @see MultiTokenPairMap
	 */
	private void generateBitSegGraph(SegGraph segGraph) {
		double smooth = 0.1;
		int wordPairFreq = 0;
		int maxStart = segGraph.getMaxStart();
		double oneWordFreq, weight, tinyDouble = 1.0 / Utility.MAX_FREQUENCE;

		int next;
		char[] idBuffer;
		// 为segGraph中的每一个元素赋以一个坐标
		segTokenList = segGraph.makeIndex();
		// 因为startToken（"始##始"）的起始位置是-1因此key为-1时可以取出startToken
		int key = -1;
		List<SegToken> nextTokens = null;
		while (key < maxStart) {
			if (segGraph.isStartExist(key)) {
				List<SegToken> tokenList = segGraph.getStartList(key);

				// 为某一个key对应的所有token都计算一次
				for (SegToken t : tokenList) {
					oneWordFreq = t.weight;
					next = t.endOffset;
					nextTokens = null;
					// 找到下一个对应的Token，例如“阳光海岸”，当前Token是“阳光”， 下一个Token可以是“海”或者“海岸”
					// 如果找不到下一个Token，则说明到了末尾，重新循环。
					while (next <= maxStart) {
						// 因为endToken的起始位置是sentenceLen，因此等于sentenceLen是可以找到endToken
						if (segGraph.isStartExist(next)) {
							nextTokens = segGraph.getStartList(next);
							break;
						}
						next++;
					}
					if (nextTokens == null)
						break;
					for (SegToken st : nextTokens) {
						idBuffer = new char[t.charArray.length
								+ st.charArray.length + 1];
						System.arraycopy(t.charArray, 0, idBuffer, 0,
								t.charArray.length);
						idBuffer[t.charArray.length] = BigramDictionary.WORD_SEGMENT_CHAR;
						System.arraycopy(st.charArray, 0, idBuffer,
								t.charArray.length + 1, st.charArray.length);
					}
				}
			}
		}
	}

}
