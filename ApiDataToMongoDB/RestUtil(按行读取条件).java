package testMongoDB;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/*
 * 读取接口数据 并存入MongoDB
 * 使用阿里fastJson
 * 接口API中需要输入条件，条件放到txt文件里，
 * 依次按行读入 
 * 其他与固定条件相同
 */

public class RestUtil {

	public String load(String url, String query) throws Exception {
		URL restURL = new URL(url);
		/*
		 * 此处的urlConnection对象实际上是根据URL的请求协议(此处是http)生成的URLConnection类
		 * 的子类HttpURLConnection
		 */
		HttpURLConnection conn = (HttpURLConnection) restURL.openConnection();
		// 请求方式
		conn.setRequestMethod("POST");
		// 设置是否从httpUrlConnection读入，默认情况下是true; httpUrlConnection.setDoInput(true);
		conn.setDoOutput(true);
		// allowUserInteraction 如果为 true，则在允许用户交互（例如弹出一个验证对话框）的上下文中对此 URL 进行检查。
		conn.setAllowUserInteraction(false);

		PrintStream ps = new PrintStream(conn.getOutputStream());
		ps.print(query);

		ps.close();

		BufferedReader bReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		String line, resultStr = "";

		while (null != (line = bReader.readLine())) {
			resultStr += line;
		}
		//System.out.println(resultStr);
		bReader.close();

		return resultStr;

	}
	
	//向MongoDB中插入Json数据
	public void saveJson(String json) {
		Mongo mongo = new Mongo("localhost", 27017);
	    DB db = mongo.getDB("test");
		DBCollection collection = (DBCollection) db.getCollection("user");    

		DBObject bson = (DBObject)JSON.parse(json);
		collection.insert(bson); 
	}

	public static void main(String[] args) {

		try {

			
			String dictPath =  "C:\\Users\\Administrator\\Desktop\\111.txt";
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dictPath), "UTF-8"));
			String s;
			
			int pageno=0;  //通过文件进行读取
			//按行读取
			while ((s = br.readLine()) != null) {
				pageno = Integer.parseInt(s);
				getAndSave(pageno);
			}
			br.close();

		} catch (Exception e) {
			System.out.print(e.getMessage());
		}

	}
	
	public static void getAndSave(int pageno) throws Exception{
		
		RestUtil restUtil = new RestUtil();
		String resultString = restUtil.load(
				"http://218.244.151.132:3000/bpData/getBpDataEx?schemadata=MDT_TGPS_PILE&ispage=1&pagesize=10&pageno="+ pageno
				+ "&sort=road%20&customer=09eb9edad501e8ed917b9dbb0fb4ed66",
				"condition=road%3D%27S2%27");
	
		//String resultString = "{'page':1, 'rows':[{'id':'11','name':'dd','platcode':'eee' }, {'id':'21','name':'edd','platcode':'eff' }]}" ;
		
		 JSONObject object = JSON.parseObject(resultString);
		 
		 Object jsonArray = object.get("rows");
	     //System.out.println("jsonArray :　" + jsonArray);
	     List<PlatformModel> list = JSON.parseArray(jsonArray+"",PlatformModel.class); 
	     for (PlatformModel user : list) {
	    	 	MongoMain.openDB();
	    	 	MongoMain.saveData(user.getMlat(), user.getRoad());
	            //System.out.println("id:" + user.getMlat() + ", name:" + user.getName());
	        }
	}
	
	
}