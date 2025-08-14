package jp.alhinc.calculate_sales;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";
	
	//商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";
	
	//商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	//支店コード正規表現
	private static final String REGULAR_NUMBER_EXPRESSION = "^\\d{3}$";
	
	//商品コード正規表現
	private static final String REGULAR_CHARACTER_EXPRESSION = "^[a-zA-Z0-9]{8}$";
	// エラーメッセージ
	private static final String FILE_NOT_SERTAL_NUMBERS = "売上ファイル名が連番になっていません";
	private static final String FORMAT_IS_INVALID = "のフォーマットが不正です";
	private static final String BRANCH_CODE_IS_INVALID = "の支店コードが不正です";
	private static final String NUMBER_OF_INVALID = "合計金額が10桁を超えました";
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "定義ファイルのフォーマットが不正です";
	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		//★「例外処理」コマンドライン引数を受け取っていなかったらエラー
		if(args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		//商品コードと商品名
		Map<String, String>commodityCodes = new HashMap<>();
		//商品コードと売上金額
		Map<String,Long>  commoditySales= new HashMap<>();
		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, REGULAR_NUMBER_EXPRESSION, "支店", FILE_INVALID_FORMAT, "支店" ,  FILE_NOT_EXIST)) {
			return;
		}
		
		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_COMMODITY_LST, commodityCodes, commoditySales, REGULAR_CHARACTER_EXPRESSION, "商品", FILE_INVALID_FORMAT, "商品" , FILE_NOT_EXIST)) {
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
		for(int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();
			//売上ファイルを選定してリストに格納する
			if(files[i].isFile() && fileName.matches("^\\d{8}\\.rcd$")){
				rcdFiles.add(files[i]);
			}
		}
		Collections.sort(rcdFiles);
		//★「例外処理」もしも追加したものが連番ではなかったらエラーを吐く
		for(int i = 0; i < rcdFiles.size() - 1; i++){
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			if(latter - former != 1) {
				System.out.println(FILE_NOT_SERTAL_NUMBERS);
				return;
			}
		}

		//売上ファイルのみが入ったリストから一つずつファイルを取り出し読み取る
		for (int i = 0; i < rcdFiles.size(); i++) {
			FileReader fr  = null;
	    	BufferedReader br = null;
	    	ArrayList<String> fileElements = new ArrayList<>();
	    	String branchCode = null;
	    	String commodityCode= null;
	    	String money = null;
	    	Long fileSale = 0L;
			try {
		    	//ファイルの読み込み準備
		    	fr = new FileReader(rcdFiles.get(i));
		    	br = new BufferedReader(fr);

		    	//一行ずつ読み取り、それをFileElementsリストに入れていく
		    	String line;
		    	while((line = br.readLine()) != null){
					//Listに追加する。
					fileElements.add(line);
				}
		    	//★「例外処理」3行でない場合エラーを吐く
				if(fileElements.size() != 3) {
					System.out.println(rcdFiles.get(i).getName() + FORMAT_IS_INVALID);
					return;
				}
				//リストから支店名変数branchCodeと商品コード変数productCodeと売り上げ変数moneyに入れていく
				branchCode = fileElements.get(0);	
				//★「例外処理」支店コードが支店名ファイルに入っていなければエラーを吐く
		    	if(!branchNames.containsKey(branchCode)) {
		    		System.out.println(rcdFiles.get(i).getName() + BRANCH_CODE_IS_INVALID);
		    		return;
		    	}
		    	//2行目は商品コード
		    	commodityCode = fileElements.get(1);
		    	//3行目は売り上げ
				money = fileElements.get(2);
		    	
				//★「例外処理」money（売り上げ）が数字であるか。
				if (!money.matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//moneyはLong型にしてもともとのもの入ってるものと加算
				fileSale = Long.parseLong(money);
		    	Long saleAmount = branchSales.get(branchCode) + fileSale;
		    	//branchSalesの売り上げが11ケタ以上ならエラーを吐く
		    	if(saleAmount >= 10000000000L) {
		    		System.out.println(NUMBER_OF_INVALID);
		    		return;
		    	}
		    	//branchSalesのMapに支店コードと売り上げの変数を入れる
		    	branchSales.put(branchCode, saleAmount);
		    	
		    	
		    	//commoditySalesに商品コードと売り上げを入れる
		    	Long commodityAmount = commoditySales.get(commodityCode) + fileSale;
		    	//branchSalesの売り上げが11ケタ以上ならエラーを吐く
		    	if(commodityAmount >= 10000000000L) {
		    		System.out.println(NUMBER_OF_INVALID);
		    		return;
		    	}
		    	commoditySales.put(commodityCode, commodityAmount);
		    	
			}catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return;
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
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityCodes, commoditySales)) {
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
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales, String regularExpression, String product, String formatError, String subProduct, String fileError) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			if(!file.exists()) {
				System.out.println(subProduct + fileError);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");
				String branchFirst = items[0];
				String branchSecond = items[1];

				//★「例外処理」支店定義ファイルの中身のフォーマット確認
				if((items.length != 2) || (!branchFirst.matches(regularExpression))) {
					System.out.println(product + formatError);
					return false;
				}
				branchNames.put(branchFirst, branchSecond );
				branchSales.put(branchFirst, 0L);
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
				//商品の場合、key=商品コード、name=商品名、sales=売り上げ
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


