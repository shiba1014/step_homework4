import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WordChain {
	
	private static BufferedReader bufferedReader;
	// pages.txtの配列
	static HashMap<Integer, String> wordMap = new HashMap<Integer, String>();
	// links.txtの配列
	static HashMap<Integer, ArrayList<Integer>> linkMap = new HashMap<Integer, ArrayList<Integer>>();
	// すでに使用された語句のインデックスたち
	static ArrayList<Integer> usedWordIndex = new ArrayList<Integer>();
	// 現在のしりとりに使われている単語の情報たち
	static ArrayList<HashMap<String, Integer>> currentWordChain = new ArrayList<HashMap<String, Integer>>();
	// 最も長いしりとり
	static ArrayList<Integer> longestWordChain = new ArrayList<Integer>();
	// しりとりの一番初めの語句のインデックス
	static Integer firstWordIndex = 0;
	
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
			System.out.println("finish reading pages");
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public static void readLinks() {
		try {
			System.out.println("start reading links");
			File file = new File("src/wikipedia_links/links_test.txt");
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
	
	public static void initArray() {
		usedWordIndex.add(0);
		HashMap<String, Integer> wordInfo = new HashMap<String, Integer>();
		wordInfo.put("wordIndex", 0);
		wordInfo.put("restartIndex", 0);
		currentWordChain.add(wordInfo);
	}
	
	public static void searchWordChain(Integer index, Integer from) {
		
		String word = wordMap.get(index);
		Character lastChar = getLastChar(word);
		ArrayList<Integer> link = linkMap.get(index);
		Integer nextWordIndex = -1;
		Integer restartIndex = 0;
		
		// 続く語句を探索
		for (int i = from; i < link.size(); i++) {
			// すでに使われている語句を除く
			if (usedWordIndex.contains(i)) {
				continue;
			}
			String secondWord = wordMap.get(link.get(i));
			Character firstChar = getLastChar(secondWord);
			if (firstChar == lastChar) {
				System.out.println("yeah");
				// 次に探索する単語のインデックスと、戻ってきた際に探索を再開するインデックスを取得
				nextWordIndex = link.get(i);
				restartIndex = i;
				break;
			}
		}
		if (nextWordIndex != -1) {
			// 続く語句が見つかった場合、次の探索に移る
			usedWordIndex.add(index);
			HashMap<String, Integer> newWordInfo = new HashMap<String, Integer>();
			newWordInfo.put("wordIndex", index);
			newWordInfo.put("restartIndex", restartIndex);
			// しりとりの長さが最大だった場合、記録を更新
			if (currentWordChain.size() > longestWordChain.size()) {
				longestWordChain.clear();
				for (Integer i : usedWordIndex) {
					longestWordChain.add(i);
				}
			}
			searchWordChain(nextWordIndex, 0);
		} else {
			// 続く語句が見つからなかった場合
			usedWordIndex.remove(usedWordIndex.size() - 1);
			currentWordChain.remove(currentWordChain.size() - 1);
			if (currentWordChain.size() == 0) {
				// 初めの位置まで戻ってきた場合、次のインデックスを探索する
				firstWordIndex++;
				searchWordChain(firstWordIndex,0);
			} else {
				// 前回のところから探索を再開する
				HashMap<String, Integer> lastWordInfo = currentWordChain.get(currentWordChain.size() - 1);
				searchWordChain(lastWordInfo.get("wordIndex"), lastWordInfo.get("restartIndex"));
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
	
	public static void main(String args[]){
		readPages();
		readLinks();
		initArray();
		System.out.println("start word chain");		
		searchWordChain(firstWordIndex, 0);
		System.out.println("finish word chain");
		System.out.println(longestWordChain);
	}
}
