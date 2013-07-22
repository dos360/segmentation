/**
 * 
 */
package org.apache.lucene.analysis.cn.smart.dict;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.lucene.analysis.cn.smart.AnalyzerProfile;

/**
 * @author lsheng
 * 
 */
public class BigramDictionary extends AbstractDictionary {

	private BigramDictionary() {
	}

	public static final char WORD_SEGMENT_CHAR = '@';

	private static BigramDictionary singleInstance;

	public static final int PRIME_BIGRAM_LENGTH = 402137;

	/**
	 * bigramTable 来存储词与词之间的跳转频率， bigramHashTable 和 frequencyTable
	 * 就是用来存储这些频率的数据结构。 为了提高查询速度和节省内存， 采用 hash 值来代替关联词作为查询依据， 关联词就是
	 * (formWord+'@'+toWord) ， 利用 FNV1 hash 算法来计算关联词的hash值 ，并保存在 bigramHashTable
	 * 中，利用 hash 值来代替关联词有可能会产生很小概率的冲突， 但是 long 类型
	 * (64bit)的hash值有效地将此概率降到极低。bigramHashTable[i]与frequencyTable[i]一一对应
	 */
	private long[] bigramHashTable;

	private int[] frequencyTable;

	private int max = 0;

	private int repeat = 0;

	public synchronized static BigramDictionary getInstance() {
		if (singleInstance == null) {
			singleInstance = new BigramDictionary();
			try {
				singleInstance.load();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				String dictRoot = AnalyzerProfile.ANALYSIS_DATA_DIR;
				singleInstance.load(dictRoot);
			}
		}
		return singleInstance;
	}

	private void load() throws ClassNotFoundException, IOException {
		InputStream input = this.getClass().getResourceAsStream(
				"bigramdict.mem");
		loadFromInputStream(input);
	}

	private void loadFromInputStream(InputStream serialObjectInputStream)
			throws IOException, ClassNotFoundException {
		ObjectInputStream input = new ObjectInputStream(serialObjectInputStream);
		bigramHashTable = (long[]) input.readObject();
		frequencyTable = (int[]) input.readObject();
		input.close();
	}

	private void load(String dictRoot) {
		String bigramDictPath = dictRoot + "/bigramdict.dct";
		
		File serialObj = new File(dictRoot + "/bigramdict.mem");
		
		if (serialObj.exists() && loadFromObj(serialObj)) {
			
		} else {
			try {
				bigramHashTable = new long[PRIME_BIGRAM_LENGTH];
				frequencyTable = new int[PRIME_BIGRAM_LENGTH];
				for (int i = 0; i < PRIME_BIGRAM_LENGTH; i++) {
					// 实际上将0作为初始值有一点问题，因为某个字符串可能hash值为0，但是概率非常小，因此影响不大
					bigramHashTable[i] = 0; // -1
					frequencyTable[i] = 0; // -1
				}
				loadFromFile(bigramDictPath);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
			saveToObj(serialObj);
		}
	}

	/**
	 * 将词库文件加载到WordDictionary的相关数据结构中，只是加载，没有进行合并和修改操作
	 * 
	 * @param dctFilePath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private boolean loadFromObj(File serialObj) {
		return false;
	}
	
	private int getAvaliableIndex(long hashId, char carray[]) {
		return -1;
	}
	
	public int getFrequency(char[] carray) {
		int index = getBigramItemIndex(carray);
		if (index != -1)
			return frequencyTable[index];
		return 0;
	}

}
