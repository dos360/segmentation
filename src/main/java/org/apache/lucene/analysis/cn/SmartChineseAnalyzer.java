/**
 * 
 */
package org.apache.lucene.analysis.cn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SentenceTokenizer;
import org.apache.lucene.analysis.cn.smart.WordSegmenter;
import org.apache.lucene.analysis.cn.smart.WordTokenizer;

/**
 * 
 * SmartChineseAnalyzer 是一个智能中文分词模块， 能够利用概率对汉语句子进行最优切分，
 * 并内嵌英文tokenizer，能有效处理中英文混合的文本内容。
 * 
 * 它的原理基于自然语言处理领域的隐马尔科夫模型(HMM)， 利用大量语料库的训练来统计汉语词汇的词频和跳转概率，
 * 从而根据这些统计结果对整个汉语句子计算最似然(likelihood)的切分。
 * 
 * 因为智能分词需要词典来保存词汇的统计值，SmartChineseAnalyzer的运行需要指定词典位置，如何指定词典位置请参考
 * org.apache.lucene.analysis.cn.smart.AnalyzerProfile
 * 
 * SmartChineseAnalyzer的算法和语料库词典来自于ictclas1.0项目(http://www.ictclas.org)，
 * 其中词典已获取www.ictclas.org的apache license v2(APLv2)的授权。在遵循APLv2的条件下，欢迎用户使用。
 * 在此感谢www.ictclas.org以及ictclas分词软件的工作人员的无私奉献！
 * 
 * @see org.apache.lucene.analysis.cn.smart.AnalyzerProfile
 * 
 */
public class SmartChineseAnalyzer extends Analyzer {

	private Set<String> stopWords = null;

	private WordSegmenter wordSegment;

	public SmartChineseAnalyzer() {
		this(false);
	}

	/**
	 * SmartChineseAnalyzer内部带有默认停止词库，主要是标点符号。如果不希望结果中出现标点符号，
	 * 可以将useDefaultStopWords设为true， useDefaultStopWords为false时不使用任何停止词
	 * 
	 * @param useDefaultStopWords
	 */
	public SmartChineseAnalyzer(boolean useDefaultStopWords) {
		if (useDefaultStopWords) {
			stopWords = loadStopWords(this.getClass().getResourceAsStream(
					"stopwords.txt"));
		}
		wordSegment = new WordSegmenter();
	}

	/**
	 * 从停用词文件中加载停用词， 停用词文件是普通UTF-8编码的文本文件， 每一行是一个停用词，注释利用“//”， 停用词中包括中文标点符号，
	 * 中文空格， 以及使用率太高而对索引意义不大的词。
	 * 
	 * @param input
	 *            停用词文件
	 * @return 停用词组成的HashSet
	 */
	public Set<String> loadStopWords(InputStream resourceAsStream) {
		String line;
		Set<String> stopWords = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream, "UTF-8"));
			while ((line = br.readLine()) != null) {
				if (line.indexOf("//") != -1) {
					line = line.substring(0, line.indexOf("//"));
				}
				line = line.trim();
				if (line.length() != 0)
					stopWords.add(line.toLowerCase());
			}
			br.close();
		} catch (IOException e) {
		      System.err.println("WARNING: cannot open stop words list!");
	    }
		return stopWords;
	}

	/**
	 * 使用自定义的而不使用内置的停止词库，停止词可以使用SmartChineseAnalyzer.loadStopWords(InputStream)
	 * 加载
	 * 
	 * @param stopWords
	 * @see SmartChineseAnalyzer.loadStopWords(InputStream)
	 */
	public SmartChineseAnalyzer(Set<String> stopWords) {
		this.stopWords = stopWords;
		wordSegment = new WordSegmenter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String,
	 * java.io.Reader)
	 */
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = new SentenceTokenizer(reader);
		result = new WordTokenizer(result, wordSegment);
//		 result = new LowerCaseFilter(result);
		// 不再需要LowerCaseFilter，因为SegTokenFilter已经将所有英文字符转换成小写
		// stem太严格了, This is not bug, this feature:)
		result = new PorterStemFilter(result);
		if (stopWords != null) {
			result = new StopFilter(result, stopWords, false);
		}
		return result;
	}
	
	public static void main(String[] args) {
		Reader sentence = new StringReader(
				"我从小就不由自主地认为自己长大以后一定得成为一个象我父亲一样的画家, 可能是父母潜移默化的影响。其实我根本不知道作为画家意味着什么，我是否喜欢，最重要的是否适合我，我是否有这个才华。其实人到中年的我还是不确定我最喜欢什么，最想做的是什么？我相信很多人和我一样有同样的烦恼。毕竟不是每个人都能成为作文里的宇航员，科学家和大教授。知道自己适合做什么，喜欢做什么，能做好什么其实是个非常困难的问题。"
						+ "幸运的是，我想我的孩子不会为这个太过烦恼。通过老大，我慢慢发现美国高中的一个重要功能就是帮助学生分析他们的专长和兴趣，从而帮助他们选择大学的专业和未来的职业。我觉得帮助一个未成形的孩子找到她未来成长的方向是个非常重要的过程。"
						+ "美国高中都有专门的职业顾问，通过接触不同的课程，和各种心理，个性，兴趣很多方面的问答来帮助每个学生找到最感兴趣的专业。这样的教育一般是要到高年级才开始， 可老大因为今年上计算机的课程就是研究一个职业走向的软件项目，所以她提前做了这些考试和面试。看来以后这样的教育会慢慢由电脑来测试了。老大带回家了一些试卷，我挑出一些给大家看看。这门课她花了2个多月才做完，这里只是很小的一部分。"
						+ "在测试里有这样的一些问题："
						+ "你是个喜欢动手的人吗？ 你喜欢修东西吗？你喜欢体育运动吗？你喜欢在室外工作吗？你是个喜欢思考的人吗？你喜欢数学和科学课吗？你喜欢一个人工作吗？你对自己的智力自信吗？你的创造能力很强吗？你喜欢艺术，音乐和戏剧吗？  你喜欢自由自在的工作环境吗？你喜欢尝试新的东西吗？ 你喜欢帮助别人吗？你喜欢教别人吗？你喜欢和机器和工具打交道吗？你喜欢当领导吗？你喜欢组织活动吗？你什么和数字打交道吗？");
		
	}

}
