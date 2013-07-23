package org.apache.lucene.analysis.cn.smart.dict;

public class PathNode implements Comparable<PathNode> {
	
	public double weight;
	
	public int preNode;

	public int compareTo(PathNode p) {
		if (weight < p.weight)
			return -1;
		else if (weight > p.weight)
			return 1;
		return 0;
	}

}
