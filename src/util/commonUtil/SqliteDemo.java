package util.commonUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;

public class SqliteDemo {

	
	public static void main(String[] args) {
		Connection conn = null;
		DbHelper dbHelper = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:C:\\sqlite\\db\\testdb.db");
			dbHelper = new DbHelper(conn);
			List<HashMap<String,String>> list = dbHelper.getMapList("select * from playlist");
			ComLogUtil.printCollection(list, "result");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(dbHelper != null) {
				dbHelper.closeConnection();
				dbHelper = null;
			}
		}
	}
	
}
