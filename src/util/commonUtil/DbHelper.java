
package util.commonUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import util.commonUtil.*;

/**
 * 
 */
@SuppressWarnings("all")
public class DbHelper {
	
	private final static String grepMark = "DbHelper";
	private Connection conn;
	private Statement st = null;
	private Integer batchNum = 0;
	
	
	
	public Integer getBatchNum() {
		return batchNum;
	}

	public void setBatchNum(Integer batchNum) {
		this.batchNum = batchNum;
	}

	/**
	 * Not allowed to use the connectionless constructor
	 */
	private DbHelper() {
	}
	
	public DbHelper(Connection conn) {
		this.conn = conn;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}
	
	/**
	 * 添加批量执行语句
	 * @return 已有的还未执行的batch数目
	 */
	public Integer addBatch(String sql) throws Exception {
		if(this.st == null) this.st = conn.createStatement();
		this.st.addBatch(sql);
		return batchNum++;
	}
	
	/**
	 * 批量执行语句
	 * @return 每个语句影响的行数
	 */
	public int[] excuteBatch() throws Exception {
		batchNum = 0;
		return this.st.executeBatch();
	}
	
	/**
	 * 根据sql查询并返回一个装着HashMap<String, String>的list
	 * @param sql
	 * @return
	 * @throws SQLException 
	 */
	public List<HashMap<String, String>> getMapList(String sql) throws SQLException {
		List<HashMap<String, String>> selectResultListHashMaps = new ArrayList<HashMap<String, String>>();
		Connection conn = this.conn;
		Statement stmt = null;
		ResultSet rs = null;
		//logger.info("[getMapList]  sql" +  sql);
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int lengInt = rsmd.getColumnCount();
			while (rs.next()) {
				String itemString = "";
				HashMap<String, String> itemHashMap = new HashMap<String, String>();
				for (int i = 0; i < lengInt; i++) {
					itemString = rsmd.getColumnLabel(i + 1);
					String tmpString = rs.getString(itemString);
					if (ComStrUtil.isBlank(tmpString)) {
						tmpString = "";
					}
					itemHashMap.put(itemString, tmpString);
				}
				selectResultListHashMaps.add(itemHashMap);
			}
		} catch (SQLException e) {
			ComLogUtil.error(grepMark + " excute failed sql:" + sql);
			throw e;
		} finally {
			closeResource(stmt, rs);
			/**
			if (conn != null) {
				conn.close();
				conn = null;
			}*/
		}
		return selectResultListHashMaps;
	}
	
	
	
	/**
	 * 执行数据库修改操作(包括增删改).
	 * @return 增删改 所影响的条数
	 */
	public Integer executeSql(String sql) throws Exception {
		int updateLine = 0;	//影响的条数(增删改都会返回相应的影响的条数)
		Statement st = null;
		try {
			st = conn.createStatement();
			//log.debug(sql);
			updateLine = st.executeUpdate(sql);
			st.close();
			st = null;
		} catch (Exception ex) {
			ComLogUtil.error(grepMark + " 执行错误的 sql:" + sql);
			throw ex;
		} finally {
			closeResource(st, null);
		}
		return updateLine;
	}
	
	/**
	 * 执行数据库修改操作(包括增删改).
	 * @return 增删改 所影响的条数
	 */
	public Integer executeSql(StringBuilder sql) throws Exception {
		return executeSql(sql.toString());
	}
	
	
	/**
	 * 根据sql和参数查询并返回一个装着HashMap<String, String>的list
	 * @param sql
	 * @param paramList
	 * @return
	 * @throws SQLException 
	 */
	public List<HashMap<String, String>> getMapList(String sql, List<Object> paramList) throws SQLException {
		return excutePstmtQry(sql, paramList);
	}
	
	/**
	 * 执行数据库修改操作(包括增删改).
	 * @return 增删改 所影响的条数
	 */
	public Integer executeSql(String sql, List<Object> paramList) throws Exception {
		return excutePstmt(sql, paramList);
	}
	
	/**
	 * 执行数据库修改操作(包括增删改).
	 * @return 
	 */
	public Integer executeSql4Insert(String sql, List<Object> paramList) throws Exception {
		return excutePstmt(sql, paramList);
	}
	
	/**
	 * 执行数据库修改操作(包括增删改).
	 * @return 增删改 所影响的条数
	 */
	public int[] executeSqlBatch(String sql, List<List<Object>> paramList) throws Exception {
		return excutePstmtBatch(sql, paramList);
	}
	
	/**
	 * 根据sql语句批量执行增删改操作
	 * conn取得方法:
	 * java.sql.Connection conn = baseManager.getSession().connection();
	 * java.sql.Connection conn = HibernateSession.connection();
	 * @param conn
	 * @param sql
	 * @param paramList 参数list
	 * @throws Exception
	 */
	private List<HashMap<String, String>> excutePstmtQry(String sql, List<Object> paramList) throws SQLException {
		PreparedStatement pstmt = null;
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		Object param = null;
		ResultSet rs = null;				//结果集
		ResultSetMetaData metaData = null;	
		Integer columnNum = null;			//总字段数
		String[] columnNames = null;		//存放字段名的String[]
		Integer k = 0;
		try {
			pstmt = this.conn.prepareStatement(sql);
			for (int j = 0; j < paramList.size(); j++) {
				param = paramList.get(j);
				if (param instanceof java.util.Date) {
					pstmt.setTimestamp(j + 1, new java.sql.Timestamp(((java.util.Date) param).getTime()));
				} else {
					pstmt.setObject(j + 1, param);
				}
			}
			rs = pstmt.executeQuery();
			metaData = rs.getMetaData();
			columnNum = metaData.getColumnCount();
			columnNames = new String[columnNum];
			//把每个字段名放在String[]中
			for(k = 0; k < columnNum; k++) {
				columnNames[k] = metaData.getColumnLabel(k + 1);
			}
			//遍历每条记录
			while(rs.next()) {
				HashMap<String, String> resMap = new HashMap<String, String>();
				for(k = 0; k < columnNum; k++) {
					//把字段名作为map的key, 结果的值作为map的value(若指为null这是"". 否则就把结果toString放进map的value)
					resMap.put(columnNames[k], rs.getObject(k + 1)==null?"":rs.getObject(k + 1).toString());
				}
				list.add(resMap);
			}
			return list;
		} finally {
			param = null;
			list = null;
			metaData = null;
			columnNames = null;
			closeResource(pstmt, rs);
		}
	}
	
	/**
	 * 根据sql语句执行增删改操作
	 * conn取得方法:
	 * java.sql.Connection conn = baseManager.getSession().connection();
	 * java.sql.Connection conn = HibernateSession.connection();
	 * @param conn
	 * @param sql
	 * @param paramList
	 * @throws Exception
	 */
	private Integer excutePstmt(String sql,List<Object> paramList) throws Exception{
		int updateLine = 0;	//影响的条数(增删改都会返回相应的影响的条数)
		PreparedStatement pstmt = null;
		Object param = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			for (int j = 0; j < paramList.size(); j++) {
				param = paramList.get(j);
				if (param instanceof java.util.Date) {
					pstmt.setTimestamp(j + 1, new java.sql.Timestamp(((java.util.Date) param).getTime()));
				} else {
					pstmt.setObject(j + 1, param);
				}
			}
			//int i = Statement.RETURN_GENERATED_KEYS;
			//pstmt.get
			updateLine = pstmt.executeUpdate();
		} finally {
			param = null;
			paramList = null;
			closeResource(pstmt, null);
		}
		return updateLine;
	}
	
	/**
	 * 根据sql语句批量执行增删改操作
	 * conn取得方法:
	 * java.sql.Connection conn = baseManager.getSession().connection();
	 * java.sql.Connection conn = HibernateSession.connection();
	 * @param conn
	 * @param sql
	 * @param paramList
	 * @throws Exception
	 */
	private int[] excutePstmtBatch(String sql,List<List<Object>> paramList) throws Exception{
		int[] updateLine = null;	//影响的条数(增删改都会返回相应的影响的条数)
		PreparedStatement pstmt = null;
		List<Object> list = null;
		Object param = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			for (int i = 0; i < paramList.size(); i++) {
				list = paramList.get(i);
				for (int j = 0; j < list.size(); j++) {
					param = list.get(j);
					if (param instanceof java.util.Date) {
						pstmt.setTimestamp(j + 1, new java.sql.Timestamp(((java.util.Date) param).getTime()));
					} else {
						pstmt.setObject(j + 1, param);
					}
				}
				pstmt.addBatch();
			}
			updateLine = pstmt.executeBatch();
		} finally {
			param = null;
			list = null;
			closeResource(pstmt, null);
		}
		return updateLine;
	}
	
	/**
	 * 关闭相应的资源
	 * @param stmt
	 * @param rs
	 */
	private static void closeResource(Statement stmt, ResultSet rs) {
		if (rs != null) try {if(!rs.isClosed()) rs.close(); rs = null;} catch (SQLException e){e.printStackTrace();}
		if (stmt != null) try {if(!stmt.isClosed()) stmt.close(); stmt = null;}catch (SQLException e) {e.printStackTrace();}
	}
	
	/**
	 * 开事务
	 * @throws SQLException
	 */
	public void beginTransaction() throws SQLException {
		this.conn.setAutoCommit(false);
	}
	
	/**
	 * 提交事务
	 * @throws SQLException
	 */
	public void commitTransaction() throws SQLException {
		try {
			this.conn.commit();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * 回滚
	 * @throws SQLException
	 */
	public void rollbackTransaction() throws SQLException {
		try {
			this.conn.rollback();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * 关闭连接(自己调用)
	 */
	public void closeConnection() {
		closeConnection(conn);
	}
	
	/**
	 * 关闭连接(共大家调用)
	 */
	public static void closeConnection(Connection conn) {
		try {
			if(conn!=null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException ex) {
			ComLogUtil.error("Close DBConnection: " + ex.getMessage(), ex);
			ex.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException ex) {
					ComLogUtil.error("finally Close DBConnection: " + ex.getMessage(), ex);
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 取得count数
	 * @param sql count的sql如: SELECT COUNT(0) FROM user where user_id = 'ddd'
	 * @return count 数
	 * @throws Exception
	 */
	public Integer getCount(String sql) throws Exception {
			int result = 0;
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql);
				rs.next();
				result = rs.getInt(1);	//取得count结果
			} catch (Exception ex) {
				ComLogUtil.error(grepMark + " 执行错误的count sql:" + sql);
				throw ex;
			} finally {
				closeResource(stmt, rs);
			}
			return result;
	}
	
	/**
	 * 取得count数
	 * @param sql count的sql如: SELECT COUNT(0) FROM user where user_id = 'ddd'
	 * @return count 数
	 * @throws Exception
	 */
	public Integer getCount(StringBuilder sql) throws Exception {
		return getCount(sql.toString()); 
	}
	
	/**
	 * 取得count数
	 * @param sql count的sql如: SELECT COUNT(0) FROM user where user_id = 'ddd'
	 * @return count 数
	 * @throws Exception
	 */
	public Integer getCount(StringBuffer sql) throws Exception {
		return getCount(sql.toString()); 
	}
	
	
	public static void main(String[] args) {
		System.err.println("Adding new column in table example!");
        Connection conn=null;
        PreparedStatement ps=null;
        String sql="insert into test(date_time_, phone, book_id) values(?, ?, ?)";
        ResultSet rs=null;
         
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = java.sql.DriverManager.getConnection("jdbc:mysql://ouxiu8881.mysql.rds.aliyuncs.com:3306/weipaike?useUnicode=true&zeroDateTimeBehavior=convertToNull&characterEncoding=utf8&autoReconnect=true","mydb","mydb123456");
             
            ps = conn.prepareStatement( sql, PreparedStatement.RETURN_GENERATED_KEYS );
            ps.setString(1, "jdbc1");
            ps.executeUpdate();
            rs=ps.getGeneratedKeys();
             
            if ( !rs.next() ) {
               System.err.println("wrong ...");
            }
            final int CUSTOMER_ID_COLUMN_INDEX=1;
            System.err.println(rs.getInt( CUSTOMER_ID_COLUMN_INDEX )); //输出刚插入数据返回的Id号
             
        } catch (ClassNotFoundException e) {
             
            e.printStackTrace();
        } catch (SQLException e) {
             
            e.printStackTrace();
        }
        finally{
             
                try {
                    if(rs!=null) rs.close();
                    if(ps!=null) ps.close();
                    if(conn!=null) conn.close();
                } catch (SQLException e) {
                     
                    e.printStackTrace();
                }
             
        }
	}
}
