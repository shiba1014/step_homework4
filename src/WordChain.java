import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.spi.ServiceRegistry;

public class WordChain {
	
	private static BufferedReader bufferedReader;
	// pages.txtの配列
	static HashMap<Integer, String> wordMap = new HashMap<Integer, String>();
	// links.txtの配列
	static HashMap<Integer, ArrayList<Integer>> linkMap = new HashMap<Integer, ArrayList<Integer>>();
	// すでに使用された語句のインデックスたち
	static ArrayList<Integer> usedWordIndex = new ArrayList<Integer>();
	// 現在のしりとりに使われている単語の情報たち
	static ArrayList<HashMap<String, Integer>> finishSearchWords = new ArrayList<HashMap<String, Integer>>();
	// 最も長いしりとり
	static ArrayList<Integer> longestWordChain = new ArrayList<Integer>();
	// しりとりの一番初めの語句のインデックス
	static Integer firstWordIndex = 0;
	
	static Integer  wordIndex = 0;
	static Integer restartIndex = 0;
	
	public static final String REGLEX = "^[a-zA-Z0-9 -/:-@\\[-\\`\\{-\\~]+$";
//	public static final String REGLEX = "^[^ -~｡-ﾟ]+$";
	
	public static void readPages(){
		try {
			System.out.println("start reading pages");
			File file = new File("src/wikipedia_links/pages.txt");
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			while(bufferedReader.ready()){
				String[] line = bufferedReader.readLine().split("\t");
				wordMap.put(Integer.parseInt(line[0]), line[1]);
			}
			System.out.printf("wordMapCount == %d\n", wordMap.size());
			System.out.println("finish reading pages");
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public static void readLinks() {
		try {
			System.out.println("start reading links");
			File file = new File("src/wikipedia_links/links.txt");
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			Integer currentIndex = 0;
			String[] line = bufferedReader.readLine().split("\t");
			while(bufferedReader.ready()){
				ArrayList<Integer> links = new ArrayList<Integer>();
				while(Integer.parseInt(line[0]) == currentIndex){
					links.add(Integer.parseInt(line[1]));
					String string = bufferedReader.readLine();
					if(string != null){
						line = string.split("\t");
					} else break;
				}
				if(links != null){
					linkMap.put(currentIndex, links);
				}
				currentIndex = Integer.parseInt(line[0]);
			}
			System.out.println("finish reading links");
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
public static void searchWordChain() {
	
		usedWordIndex.add(0);
		
		while(true){
			String word = wordMap.get(wordIndex);
			if (word.matches(REGLEX)) {
				undo();
				continue;
			}
			Character lastChar = getLastChar(word);
			ArrayList<Integer> link = linkMap.get(wordIndex);
			if (link == null) {
				undo();
				continue;
			}
			Integer nextWordIndex = -1;
			// 続く語句を探索 ========================================================
			for (int i = restartIndex; i < link.size(); i++) {
				// すでに使われている語句を除く
				if (usedWordIndex.contains(link.get(i))) {
					continue;
				}
				
				String nextWord = wordMap.get(link.get(i));
				if (nextWord.matches(REGLEX)) {
					continue;
				}
				Character firstChar = getFirstChar(nextWord);
				if (firstChar == lastChar) {
//					System.out.println("find word chain");
					// 次に探索する単語のインデックスと、戻ってきた際に探索を再開するインデックスを取得
					nextWordIndex = link.get(i);
					restartIndex = i+1;
					
					if(usedWordIndex.size() > 1){
						String result = "";
						for (Integer index : usedWordIndex) {
							result += wordMap.get(index);
							result += " -> ";
						}
						System.out.println(result);
					}
					break;
				}
			}
			// =====================================================================
			
			if (nextWordIndex != -1) {
				usedWordIndex.add(nextWordIndex);
				// 続く語句が見つかった場合、配列に1. 今回の語句、2. 今回探索した位置 を記憶し、次の探索に移る
				HashMap<String, Integer> newWordInfo = new HashMap<String, Integer>();
				newWordInfo.put("wordIndex", wordIndex);
				newWordInfo.put("restartIndex", restartIndex);
				finishSearchWords.add(newWordInfo);
				// しりとりの長さが最大だった場合、記録を更新
				if (usedWordIndex.size() > longestWordChain.size()) {
					longestWordChain.clear();
					for (Integer i : usedWordIndex) {
						longestWordChain.add(i);
					}
				}
				wordIndex = nextWordIndex;
				restartIndex = 0;
			} else {
				if (usedWordIndex.size() == 0) {
					return;
				}
				undo();
			}
		}
	}
	
	public static char getLastChar(String word){
		char lastChar = word.charAt(word.length() - 1);
		return lastChar;
	}
	
	public static char getFirstChar(String word){
		char firstChar = word.charAt(0);
		return firstChar;
	}
	
	public static void undo() {
		// 続く語句が見つからなかった場合
		usedWordIndex.remove(usedWordIndex.size() - 1);
		if (usedWordIndex.size() == 0) {
		// 初めの位置まで戻ってきた場合、次のインデックスを探索する
			firstWordIndex++;
//			System.out.printf("firstWordIndex == %d\n", firstWordIndex);
			if (firstWordIndex == wordMap.size()){
				return;
			}
				usedWordIndex.add(firstWordIndex);
				finishSearchWords.clear();
				wordIndex = firstWordIndex;
				restartIndex = 0;
			} else {
				// 前回のところから探索を再開する
				HashMap<String, Integer> lastWordInfo = finishSearchWords.get(finishSearchWords.size() - 1);
				wordIndex = lastWordInfo.get("wordIndex");
				restartIndex = lastWordInfo.get("restartIndex");
				finishSearchWords.remove(finishSearchWords.size() - 1);
			}
	}
	
	public static void prepareTest(){
		wordMap.put(0, "egg");
		wordMap.put(1, "apple");
		wordMap.put(2, "lemon");
		wordMap.put(3, "tomato");
		
		ArrayList<Integer> test = new ArrayList<>();
		test.add(1);
		test.add(2);
		test.add(3);

		ArrayList<Integer> test2 = new ArrayList<>();
		test2.add(0);
		test2.add(2);
		test2.add(3);
		
		ArrayList<Integer> test3 = new ArrayList<>();
		test3.add(0);
		test3.add(1);
		test3.add(3);
		
		ArrayList<Integer> test4 = new ArrayList<>();
		test4.add(0);
		test4.add(1);
		test4.add(2);
		
		linkMap.put(0, test);
		linkMap.put(1, test2);
		linkMap.put(2, test3);
		linkMap.put(3, test4);
	}
	
	public static void main(String args[]){
		
		readPages();
		readLinks();
		System.out.println("start word chain");		
		searchWordChain();
		System.out.println("finish word chain");
		
		// TEST CODE
//		prepareTest();
//		searchWordChain();
		
		System.out.println(longestWordChain);
		String result = "";
		for (Integer index : longestWordChain) {
			result += wordMap.get(index);
			result += " -> ";
		}
		System.out.println(result);
	}
}
