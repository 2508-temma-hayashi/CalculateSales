package jp.alhinc.calculate_sales;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}
		
		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//引数でもらったパスを変数に入れた
		String pass = args[0];
		//引数でもらったパスの中のファイルを配列にまとめた
		File[] files = new File(pass).listFiles();
		//選定された売上ファイルのみのリストのオブジェクトを作った
		List<File> rcdFiles = new ArrayList<>();
		//filesの中から売上ファイルを選定する（ファイル名の表記で）
		for(int i = 0 ; i < files.length ; i++) {
			String fileName = files[i].getName();
			//売上ファイルを選定してリストに格納する
			if(fileName.matches("^\\d{8}\\.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}
		//売り上げのファイルのみが入ったリストから一つずつファイルを取り出し読み取る
		for (int i = 0; i < rcdFiles.size(); i++) {
			FileReader fr  = null;
	    	BufferedReader br = null;
	    	ArrayList<String> FileElements = new ArrayList<>();
	    	String branchCode = null;
	    	String money = null;
	    	Long trueMoney = null;
			try {
		    	//ファイルの読み込み準備
		    	fr = new FileReader(rcdFiles.get(i));
		    	br = new BufferedReader(fr);
		    	
		    	//一行ずつ読み取り、それをFileElements配列に入れていく
				while(true){
					String element = br.readLine();
					if( element == null) {
		    			break;
		    		}
					FileElements.add(element);
				}
				//配列から支店名変数branchCodeと売り上げ変数moneyに入れていく（偶数番目か奇数番目かで）
				for(i = 0 ; i < FileElements.size() ; i++) {
					if((i + 2) % 2 == 0) {
					branchCode = FileElements.get(i);
					}else {money = FileElements.get(i);
						trueMoney = Long.parseLong(money);//moneyはLong型にしておく
					}
				}
		    	//branchSalesのMapに変数を入れる
		    	branchSales.put(branchCode, trueMoney);
		    }catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
		    }finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}
		
		
		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");
				String branchNumber = items[0];
				String branchName = items[1];
				branchNames.put(branchNumber, branchName );
				branchSales.put(branchNumber, 0L);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		
		//書き込みファイルを作成
		File outFile = new File(path,fileName);
		//ファイルへの書き込むオブジェクトを宣言
		BufferedWriter bw = null;
		
		try {
			//ファイルへ書き込むオブジェクトを作成
			bw = new BufferedWriter(new FileWriter(outFile));
			//名前と売り上げを支店コードの個数だけ繰り返しそれぞれ取得する。
			for(String key :branchNames.keySet()) {
				String name = branchNames.get(key);
				Long sales = branchSales.get(key);
				
				//それをファイルの中に書く
				bw.write(key + "," + name + "," + sales);
				//改行して次の準備
				bw.newLine();
			}
		}catch(IOException e){
			System.out.println(UNKNOWN_ERROR);
			return false;
		}finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;

	}

}
