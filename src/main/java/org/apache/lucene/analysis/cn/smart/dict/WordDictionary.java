package org.apache.lucene.analysis.cn.smart.dict;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WordDictionary extends AbstractDictionary {

	/** 私有化构造子 为了单例 */
	private WordDictionary() {
	}

	private static WordDictionary singleInstance;

	/**
	 * 一个较大的素数，保证hash能够遍历查找所有的位置
	 */
	public static final int PRIME_INDEX_LENGTH = 12071;

	/**
	 * wordIndexTable保证将Unicode中的所有汉字编码hash到PRIME_INDEX_LENGTH长度的数组中，
	 * 当然会有冲突，但实际上本程序只处理GB2312字符部分，6768个字符加上一些ASCII字符，
	 * 因此对这些字符是有效的，为了保证比较的准确性，保留原来的字符在charIndexTable中以确定查找的准确性
	 */
	private short[] wordIndexTable;

	private char[] charIndexTable;

	/**
	 * 存储所有词库的真正数据结构，为了避免占用空间太多，用了两个单独的多维数组来存储词组和频率。
	 * 每个词放在一个char[]中，每个char对应一个汉字或其他字符，每个频率放在一个int中，
	 * 这两个数组的前两个下表是一一对应的。因此可以利用wordItem_charArrayTable[i][j]来查词，
	 * 用wordItem_frequencyTable[i][j]来查询对应的频率
	 */
	private char[][][] wordItem_charArrayTable;

	private int[][] wordItem_frequencyTable;

	public synchronized static WordDictionary getInstance() {
		if (singleInstance == null)
			singleInstance = new WordDictionary();
		try {
			// 加载词典
			singleInstance.load();
		} catch (IOException e) {
			String wordDictRoot = AnalyzerProfile.ANALYSIS_DATA_DIR;
			singleInstance.load(wordDictRoot);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return singleInstance;
	}

	/**
	 * 从外部文件夹dctFileRoot加载词典库文件，首先测试是否有coredict.mem文件， 如果有则直接作为序列化对象加载，
	 * 如果没有则加载词典库源文件coredict.dct
	 * 
	 * @param dctFileName
	 *            词典库文件的路径
	 */
	public void load(String dctFileRoot) {
		String dctFilePath = dctFileRoot + "/coredict.dct";
		File serialObj = new File(dctFileRoot + "coredict.mem");

		if (serialObj.exists() && loadFromObj(serialObj)) {

		} else {
			try {
				wordIndexTable = new short[PRIME_INDEX_LENGTH];
				charIndexTable = new char[PRIME_INDEX_LENGTH];
				for (int i = 0; i < PRIME_INDEX_LENGTH; i++) {
					// init
					charIndexTable[i] = 0;
					wordIndexTable[i] = -1;
				}
				wordItem_charArrayTable = new char[GB2312_CHAR_NUM][];
				wordItem_frequencyTable = new int[GB2312_CHAR_NUM][];
				loadMainDataFromFile(dctFilePath);
				expandDelimiterData();
				mergeSameWords();
				sortEachItems();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}

			saveToObj(serialObj);
		}
	}

	/**
	 * 从jar内部加载词典库文件，要求保证WordDictionary类当前路径中有coredict.mem文件，以将其作为序列化对象加载
	 * 
	 * @param dctFileName
	 *            词典库文件的路径
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void load() {
		InputStream input = this.getClass().getResourceAsStream("coredict.mem");
		loadFromObjectInputStream(input);
	}

	/** 从序列化对象加载 */
	private boolean loadFromObj(File serialObj) {
		try {
			loadFromObjectInputStream(new FileInputStream(serialObj));
			return true;
		}
		return false;
	}

	/**
	 * 从文件流加载
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void loadFromObjectInputStream(InputStream serialObjInputStream)
			throws IOException, ClassNotFoundException {
		ObjectInputStream input = new ObjectInputStream(serialObjInputStream);
		wordIndexTable = (short[]) input.readObject();
		charIndexTable = (char[]) input.readObject();
		wordItem_charArrayTable = (char[][][]) input.readObject();
		wordItem_frequencyTable = (int[][]) input.readObject();
		input.close();
	}

	/** 存储文件到序列化对象 */
	private void saveToObj(File serialObj) {
		try {
			ObjectOutputStream output = new ObjectOutputStream(
					new FileOutputStream(serialObj));
			output.writeObject(wordIndexTable);
			output.writeObject(charIndexTable);
			output.writeObject(wordItem_charArrayTable);
			output.writeObject(wordItem_frequencyTable);
			output.close();
		} catch (Exception e) {

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
	private int loadMainDataFromFile(String dctFilePath)
			throws FileNotFoundException, IOException {
		int i, cnt, length, total = 0;
		// 文件中只统计了6763个汉字加5个空汉字符3756~3760，其中第3756个用来存储符号信息。
		int[] buffer = new int[3];
		byte[] intBuffer = new byte[4];
		String tmpword;
		RandomAccessFile dctFile = new RandomAccessFile(dctFilePath, "r");

		// 字典中第一个汉字出现的位置是0 最后一个是6767
		for (i = GB2312_FIRST_CHAR; i < GB2312_FIRST_CHAR + CHAR_NUM_IN_FILE; i++) {
			// if (i == 5231)
			// System.out.println(i);

			dctFile.read(intBuffer); // 原词库文件在c下开发，所以写入的文件为little
			// endian编码 而java为big endian ，需要转换
			cnt = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
			if (cnt <= 0) {
				wordItem_charArrayTable[i] = null;
				wordItem_frequencyTable[i] = null;
				continue;
			}
			wordItem_charArrayTable[i] = new char[cnt][];
			wordItem_frequencyTable[i] = new int[cnt];
			total += cnt;
			int j = 0;
			while (j < cnt) {
				dctFile.read(intBuffer);
				buffer[0] = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN).getInt();// frequency
				dctFile.read(intBuffer);
				buffer[1] = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN).getInt();// length;
				dctFile.read(intBuffer);
				buffer[2] = ByteBuffer.wrap(intBuffer).order(ByteOrder.LITTLE_ENDIAN).getInt();// handle;
				
				wordItem_frequencyTable[i][j] = buffer[0];
				
				length = buffer[1];
				if (length > 0) {
					// 按这个长度读入字节到lchbuffer
					byte[] lchBuffer = new byte[length];
					dctFile.read(lchBuffer);
					tmpword = new String(lchBuffer, "GB2312");
					wordItem_charArrayTable[i][j] = tmpword.toCharArray();
				} else {
					wordItem_frequencyTable[i][j] = null;
				}
				// System.out.println(indexTable[i].wordItems[j]);
				j ++;
			}
			
			String str = getCCByGB2312Id(i);
			setTableIndex(str.charAt(0), i);
		}
		dctFile.close();
		return total;
	}
	
	/**
	 * 原词库将所有标点符号的信息合并到一个列表里(从1开始的3755处)。这里将其展开，分别放到各个符号对应的列表中
	 */
	private void expandDelimiterData() {
		int i;
		int cnt;
		// 标点符号在从1开始的3755处, 将原始的标点符号对应的字典分配到对应的标点符号当中
		int delimiterIndex = 3755 + GB2312_FIRST_CHAR;
		i = 0;
		while (i < wordItem_charArrayTable[delimiterIndex].length) {
			char c = wordItem_charArrayTable[delimiterIndex][i][0];
			int j = getGB2312Id(c); // 该标点符号所在位置的index值
			if (wordItem_charArrayTable[j] == null) {
				int k = i;
				// 从i开始计数后面以j开头的符号的worditem的个数
				while (k < wordItem_charArrayTable[delimiterIndex].length
						&& wordItem_charArrayTable[delimiterIndex][k][0] == c) {
					k ++;
				}
				// 此时的k-i为j的标点符号对应的wordItem的个数
				cnt = k -1;
				if (cnt != 0) {
					wordItem_charArrayTable[j] = new char[cnt][];
					wordItem_frequencyTable[j] = new int[cnt];
				}
				
				// 为每一个wordItem赋值
				for (k = 0; k < cnt; k++, i++) {
					wordItem_frequencyTable[j][k] = wordItem_frequencyTable[delimiterIndex][i];
					wordItem_charArrayTable[j][k] = new char[wordItem_charArrayTable[delimiterIndex][i].length - 1];
					System.arraycopy(
							wordItem_charArrayTable[delimiterIndex][i], 1,
							wordItem_charArrayTable[j][k], 0,
							wordItem_charArrayTable[j][k].length);
				}
				setTableIndex(c, j);
			}
		}
		// 将原来符号对应的数组删除
		wordItem_charArrayTable[delimiterIndex] = null;
		wordItem_frequencyTable[delimiterIndex] = null;
	}

	/**
	 * 本程序不做词性标注，因此将相同词不同词性的频率合并到同一个词下，以减小存储空间，加快搜索速度
	 */
	private void mergeSameWords() {
		for (int i = 0; i < GB2312_FIRST_CHAR + CHAR_NUM_IN_FILE; i++) {
			if (wordItem_charArrayTable[i] == null)
				continue;
			int len = 1;
			
			for (int j = 1; j < wordItem_charArrayTable[i].length; j++) {
				if (Utility.compareArray(wordItem_charArrayTable[i][j], 0,
			            wordItem_charArrayTable[i][j - 1], 0) != 0)
			          len++;
				// TODO
			}
		}
	}
	
	private void sortEachItems() {
		
	}

}
