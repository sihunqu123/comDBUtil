package util.commonUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import util.commonUtil.model.Logger;

/**
 * 分页工具类
 * 用法: PageUtil pageUtil = new PageUtil(request, sql, url); 然后pageUtil.getXX各种数据就行了
 * @explain 其中的url是翻页要提交到的页面.如 aaa.jsp 或 bbb.action?xxx=23
 * @bonus 可以用pageUtil.setPageSize(10); 来设置每页显示的页数.
 * @bonus 可以用pageUtil.getgetPageFootHtml()来获取下面的页码.
 * @bonus 使用<u style="color:blue; cursor:pointer;" pageUtil_orderByColumn="a.detail_id">要排序的列</u>可以实现排序功能
 * @caution 建议使用<form method="post"></form>来包住分页的元素,否则分页后页面的参数将丢失.(name可以没有, 但method="post"必须有)
 * @author TianChen
 */
public class PagingUtil {
	private Integer currentPage = 0;	//当前页数
	private Integer pageSize = 5;		//每页显示条数
	private Integer totalPage = 0;		//总页数
	private Integer totalResult = 0;	//总记录数
	private int orderPlace = 0;					//sql语句中 " order "的位置
	private String sql;			//查询sql
	private List list;			//存放查询结果的list,里面放的是Map, Map的key是sql中的结果列别名(大小写都保留了的).value是值
	private String pageFooter = ("<div class='cpcltpage'><ul id='pageFooter'>");
	private String pageNumParameterName = "page";			//页数参数的名字	--当前页
	//private String pageSizeParameterName = "pageUtil_pageSize";			//页数参数的名字		--每页显示条数
	private String pageSizeParameterName = "rows";			//页数参数的名字		--每页显示条数
	private String orderByColumnParameterName = "sort";			//页数参数的名字	--由哪列排序
	private String orderWayParameterName = "order";			//页数参数的名字		--排序方式(desc还是asc)
	
//	private String orderByColumnParameterName = "pageUtil_orderByColumn";			//页数参数的名字	--由哪列排序
//	private String orderWayParameterName = "pageUtil_orderWay";			//页数参数的名字		--排序方式(desc还是asc)
	private Map paramMap = null;
	private String orderByColumn = "";		//由哪列排序
	private String orderWay = "";			//排序方式(0:desc还是1:asc)
	private boolean hasQuery = false;		//是否已经执行了查询 
	private String[] pageSizeArr = {"5","10","20","50","100"};
	private boolean showChangePageSize = false;
	private String url = "";
	private Connection conn = null;
	
//	protected static Logger logger = Logger.getLogger(PagingUtil.class.getCanonicalName());
	protected static Logger logger = ComLogUtil.getLogger(PagingUtil.class.getCanonicalName());
	
	
	/**
	 * @return the conn
	 */
	public Connection getConn() {
		return conn;
	}

	
	/**
	 * @param conn the conn to set
	 */
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	private int getOrderPlace() {
		return orderPlace;
	}

	private void setOrderPlace(int orderPlace) {
		this.orderPlace = orderPlace;
	}

	private String[] getPageSizeArr() {
		return pageSizeArr;
	}

	private void setPageSizeArr(String[] pageSizeArr) {
		this.pageSizeArr = pageSizeArr;
	}

	private String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isShowChangePageSize() {
		return showChangePageSize;
	}

	public void setShowChangePageSize(boolean showChangePageSize) {
		this.showChangePageSize = showChangePageSize;
	}

	public String getPageSizeParameterName() {
		return pageSizeParameterName;
	}

	public void setPageSizeParameterName(String pageSizeParameterName) {
		this.pageSizeParameterName = pageSizeParameterName;
	}

	public String getOrderByColumnParameterName() {
		return orderByColumnParameterName;
	}

	public void setOrderByColumnParameterName(String orderByColumnParameterName) {
		this.orderByColumnParameterName = orderByColumnParameterName;
	}

	public String getOrderWayParameterName() {
		return orderWayParameterName;
	}

	public void setOrderWayParameterName(String orderWayParameterName) {
		this.orderWayParameterName = orderWayParameterName;
	}

	private boolean isHasQuery() {
		return hasQuery;
	}

	private void setHasQuery(boolean hasQuery) {
		this.hasQuery = hasQuery;
	}

	public String getOrderByColumn() {
		return orderByColumn;
	}

	public void setOrderByColumn(String orderByColumn) {
		this.orderByColumn = orderByColumn;
	}

	public String getOrderWay() {
		return orderWay;
	}

	public void setOrderWay(String orderWay) {
		this.orderWay = orderWay;
	}

	public String getPageNumParameterName() {
		return pageNumParameterName;
	}

	public void setPageNumParameterName(String pageNumParameterName) {
		this.pageNumParameterName = pageNumParameterName;
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getTotalPage() throws Exception {
		if(!hasQuery) {		//如过还没有执行查询, 则先查询(查询了过后才会有totalPage等数据)
			querySql();
		}
		return totalPage;
	}

	private void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	public Integer getTotalResult() throws Exception {
		if(!hasQuery) {		//如过还没有执行查询, 则先查询(查询了过后才会有totalPage等数据)
			querySql();
		}
		return totalResult;
	}

	private void setTotalResult(Integer totalResult) {
		this.totalResult = totalResult;
	}

	private String getSql() {
		return sql;
	}

	private void setSql(String sql) {
		this.sql = sql;
	}

	public List getList() throws Exception {
		if(!hasQuery) {		//如过还没有执行查询, 则先查询(查询了过后才会有totalPage等数据)
			querySql();
		}
		return list;
	}

	private void setList(List list) {
		this.list = list;
	}

	private String getPageFooter() {
		return pageFooter;
	}

	private void setPageFooter(String pageFooter) {
		this.pageFooter = pageFooter;
	}

	
	/**
	 * 通过构造器设定request和sql.(推举使用)
	 * @param request
	 * @param sql
	 * @param url
	 * @throws Exception
	 */
	public PagingUtil(Object requestOrMap, String sql, String url, Connection conn) throws Exception {
		super();
		if(ComStrUtil.isBlank(sql)) {
			throw new Exception("Error:  sql is null!!!!");
		}
		logger.info("[PagingUtil] sql: " + sql);
		if(requestOrMap != null) {
			if("java.util.HashMap".equals(requestOrMap.getClass().getCanonicalName())) {
				this.paramMap = (Map)requestOrMap;
			}
		}
		while(sql.matches(".+;+\\s*$")){		//去掉末尾万恶的分号
			sql = sql.replaceAll(";+\\s*$", "");
		}
		this.sql = sql;
		this.url = url;
		this.conn  = conn;
		//querySql();
	}
	
	/**
	 * 取得总条数等信息.
	 * @throws Exception
	 */
	private void querySql() throws Exception {
		if(hasQuery) {
			return;
		} else {
			String tempStr = "";
			
			if(paramMap != null) {//从Map里面读取初始参数
				if(!ComStrUtil.isBlank(CommonUtil.getStrValueFrmKVObj(paramMap, pageSizeParameterName))) {
					this.pageSize = Integer.parseInt(CommonUtil.getStrValueFrmKVObj(paramMap, pageSizeParameterName));
				}
				if(!ComStrUtil.isBlank(CommonUtil.getStrValueFrmKVObj(paramMap, pageNumParameterName))) {
					this.currentPage = Integer.parseInt(CommonUtil.getStrValueFrmKVObj(paramMap, pageNumParameterName));
				}
				if(!ComStrUtil.isBlank(CommonUtil.getStrValueFrmKVObj(paramMap, "currentPage"))) {			//这个currentPage没多大的用,只是为了兼容以前的. 
					this.currentPage = Integer.parseInt(CommonUtil.getStrValueFrmKVObj(paramMap, "currentPage"));
				}
				tempStr = CommonUtil.getStrValueFrmKVObj(paramMap, orderByColumnParameterName);
				if(!ComStrUtil.isBlank(tempStr)) {
					CommonUtil.IsHavingKeyWord(tempStr);
					this.orderByColumn = tempStr;
				}
				if(!ComStrUtil.isBlank(CommonUtil.getStrValueFrmKVObj(paramMap, orderWayParameterName))) {
					this.orderWay = CommonUtil.getStrValueFrmKVObj(paramMap, orderWayParameterName);
				}
			}
			this.currentPage = currentPage < 1 ? 1:currentPage;	//若设置的当前页数小于1 则默认为1
			this.pageSize = pageSize < 1 ? 1 : pageSize;
		}
		//ClassPathXmlApplicationContext ctx = null; 
		Map map = null;
		List list4Count = null;
		//long currentTimeMillis = System.currentTimeMillis();
		//ctx = new ClassPathXmlApplicationContext("applicationContext.xml");		//加载spring
		//ApplicationContext ctx = ApplicationContextUtil.getContext();
		//long currentTimeMillis2 = System.currentTimeMillis();
		//System.out.println("2-1:  " + (currentTimeMillis2-currentTimeMillis));
		//dao = (DaoManager)ctx.getBean("daoManager");	//得到注入好的dao
		//long currentTimeMillis3 = System.currentTimeMillis();
		//System.out.println("3-2:  " + (currentTimeMillis3 - currentTimeMillis2));
		/** */
		try {
			//得到总记录条数
			//System.out.println("[PagingUtil] getCountSql: " +getCountSql());
			list4Count = getMapList(getCountSql());
			
			if(list4Count.size() == 1) {		//如果只有一个count, 说明没有group by
				totalResult = Integer.parseInt(((Map)list4Count.get(0)).get("totalCount") + "");
			} else if(list4Count.size() > 1) {
				totalResult = list4Count.size();
			} else {
				totalResult = 0;
			}
			//totalResult = Integer.parseInt(((Map)(dao.getPageList(getCountSql(), null).get(0))).get("totalCount") + "");
			
			//计算总页数
			totalPage = (int) Math.ceil(totalResult*1.0 / pageSize);
			currentPage = currentPage > totalPage ? totalPage : currentPage; //若'当前页数'大于'总页数', 则把'当前页数'变小到'总页数'
			if(totalPage == 0) {	//若没有记录,则当前页数为0,且不执行下面的查询了
				list = new ArrayList();
				return;
			}
			map = new HashMap();
			map.put("beginIdenx", (currentPage - 1) * pageSize);
			map.put("endIndex", pageSize);
			sql = getSqlWithOrderBy();			//取得包含order by的语句
			//logger.warn("[PagingUtil]  getSqlWithLimit: " + getSqlWithLimit(sql, map));
			list = getMapList(getSqlWithLimit(sql, map));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			map = null;
			list4Count = null;
			hasQuery = true;
			//System.out.println("lll:  " + (System.currentTimeMillis() - currentTimeMillis3));
			//System.out.println("PageUtil [currentPage=" + currentPage + ", pageSize=" + pageSize + ", totalPage=" + totalPage + ", totalResult=" + totalResult + ", sql=" + sql + "]");
		}
		
	}
	
	/**
	 * @param sql
	 * @param map
	 */
	private String getSqlWithLimit(String sql, Map map) {
		if(map != null) {
			if(map.containsKey("beginIdenx") && map.containsKey("endIndex")){
				//mysql
				sql = sql + " limit " + map.get("beginIdenx") + "," + map.get("endIndex");	
			}
		}
		return sql;
	}


	/**
	 * 生成带有order by排序的sql语句.
	 * @return
	 */
	private String getSqlWithOrderBy() {
		if(ComStrUtil.isBlank(orderByColumn)) {		//如果没有设置排序的列, 直接返回原sql
			return sql;
		}
		if(orderPlace == 0 || orderPlace == sql.length()) {		//sql句子本身没有oder by句子
			return sql + " order by " + orderByColumn + convertOrderByWay(orderWay);
		} else {
			return sql.substring(0, orderPlace) + " order by " + orderByColumn + convertOrderByWay(orderWay);
		}
	}

	/**
	 * 取得查询总条数的语句.
	 * @return
	 * @throws Exception 
	 */
	private String getCountSql() throws Exception {
		String lowerSql = sql.toLowerCase(); //小写了的sql
		String tempSql = lowerSql;			 //临时sql
		int i = 0;
		int fromPlace = 0;					//sql语句中 " from "的位置
		int leftNum = 0;					//指定语句中,  "("的数量
		int rightNum = 0;					//指定语句中,  ")"的数量
		boolean hasOrder = false;
//		tempSql = ComRegexUtil.replaceAllByRegex(tempSql, "\\\\\\\\", "11");	//去掉里面的\\双斜杠, 为接下来的去掉转义做好准备
//		tempSql = ComRegexUtil.replaceAllByRegex(tempSql, "\\\\'", "11");	//去掉里面的\'双斜杠
//		tempSql = ComRegexUtil.replaceAllByRegex(tempSql, "\\\\(", "11");	//去掉里面的\(双斜杠
//		tempSql = ComRegexUtil.replaceAllByRegex(tempSql, "\\\\)", "11");	//去掉里面的\)双斜杠
		if(CommonUtil.countSubstringInString(tempSql, "'") % 2 != 0) {	//若"("与")"的数量不想等,则sql不正确
			throw new Exception("Sql语句错误!");
		}
		fromPlace = lowerSql.indexOf("'");
		while (tempSql.indexOf("'", fromPlace + 1) > -1) {			//把其中"'"和"'"之间的替换成"*"马赛克掉, 以免干扰判断最外层" from "的正确位置
			//System.out.println(i + ":         " + tempSql);
			//System.out.println(i + ":         " + fromPlace + "  :   " + lowerSql.indexOf("'", fromPlace + 1));
			tempSql = CommonUtil.replaceRegion4String(tempSql, CommonUtil.getDuplicateChar("*", tempSql.indexOf("'", fromPlace + 1) + 1 - fromPlace), fromPlace, tempSql.indexOf("'", fromPlace + 1) + 1);
//			System.out.println(i + "  hahahah tempSql.length() " + tempSql.length() + " tempSql:" + tempSql);
			fromPlace = tempSql.indexOf("'", fromPlace + 1);
			//System.out.println("************ fromPlace " + fromPlace);
			i++;
			if(i > 2000) {
				throw new Exception("错误!!");
			}
		}
		fromPlace = 0;				//从0开始找" from "
		do {						//根据"最外层' from '的左面的"("和")"的数量一定相等的原则, 找出最外层" from ".
			fromPlace = tempSql.indexOf(" from ", fromPlace + 1);
			leftNum = CommonUtil.countSubstringInString(tempSql.substring(0, fromPlace), "(");
			rightNum = CommonUtil.countSubstringInString(tempSql.substring(0, fromPlace), ")");
//			System.out.println("rightNum " + rightNum + ";   leftNum " + leftNum);
			if(leftNum == rightNum) {	//若相等了, 则说明这个是最外层" from "的位置
				break;
			}
		} while (fromPlace > -1);
		
		orderPlace = 0;				//从0开始找" order "
		orderPlace = tempSql.indexOf(" order ", orderPlace + 1);	//先招一个" order "
		while (orderPlace > -1) {						//根据"最外层' order '的左面的"("和")"的数量一定相等的原则, 找出最外层" order ".
			leftNum = CommonUtil.countSubstringInString(tempSql.substring(0, orderPlace), "(");
			rightNum = CommonUtil.countSubstringInString(tempSql.substring(0, orderPlace), ")");
//			System.out.println("rightNum " + rightNum + ";   leftNum " + leftNum);
			if(leftNum == rightNum) {	//若相等了, 则说明这个是最外层" from "的位置
				hasOrder = true;
				break;
			}
			orderPlace = tempSql.indexOf(" order ", orderPlace + 1);
		}
		if(!hasOrder) {					//若没有最外层 " order ",则默认" order "位置为最后. 
			orderPlace = sql.length();	
		} else {						//若最外层排了序的, 则给本类的成员变量orderByColumn和orderWay赋值
			if(ComStrUtil.isBlank(orderByColumn)) {		//如果本身没有设置 orderBy的列
				String tempOrderBySql = tempSql.substring(orderPlace);
				//去掉 " order by " 和 " asc|desc" 从而得到排序的列
				orderByColumn = tempOrderBySql.replaceFirst("order\\s+by", "").replaceFirst(" asc|desc", "");
				//去掉 " order by " 和 orderByColumn 从而得到排序列方式
				orderWay = tempOrderBySql.replaceFirst("order\\s+by", "").replaceFirst(orderByColumn, "");
				//System.out.println("********* orderByColumn:" +orderByColumn + "  ---orderWay: " + orderWay);
			}
			orderByColumn = orderByColumn.replace(";", "");
			orderWay = orderWay.replace(";", "");
		}
		
//		System.out.println("orderPlace " + orderPlace + "  sql.length(): " + sql.length() + "  tempSql.length(): " + tempSql.length());
//		System.out.println(sql.charAt(orderPlace));
		//System.out.println("hehehheeheh ： select count(*) totalCount " +  sql.substring(fromPlace, orderPlace));
		return " select count(*) totalCount " +  sql.substring(fromPlace, orderPlace);
	}
	
	/**
	 * 得到下面分页的html
	 * @return String
	 * @throws Exception 
	 */
	public String getPageFootHtml() throws Exception {
		if(!hasQuery) {		//如过还没有执行查询, 则先查询(查询了过后才会有totalPage等数据)
			querySql();
		}
		if(showChangePageSize) {		//如设置了要现实 "每页多少页" 才显示
			pageFooter += "<li id='pageUtil_pageSize_li'><span>每页显示</span><select name='pageUtil_pageSize_sel'>";		//循环显示出"每页多少页"的option
			for(int k = 0; k < pageSizeArr.length; k++) {
				if(pageSizeArr[k].equals(pageSize + "")) {
					pageFooter += "<option value='" + pageSizeArr[k] + "' selected>" + pageSizeArr[k] + "</option>";
				} else {
					pageFooter += "<option value='" + pageSizeArr[k] + "'>" + pageSizeArr[k] + "</option>";
				}
			}
			pageFooter += "</select></li>";
		}
		
		pageFooter += "<li class='pageUtil_page' inf='1'>首页</li>";
		if(currentPage != 1) {												//如果是第一页,就没有上一页
			pageFooter += "<li class='pageUtil_paging' inf='" + (currentPage - 1) + "'>上一页</li>";
		}
		
		int i = 1;			//初始是从第1页开始
		int j = totalPage;	//初始是到第totalPage页结束
		if (totalPage > 9) {		//若totalPage>9,才会考虑不完全显示页数
			if(currentPage > 5) {		//若当前页大于5
				pageFooter += "<li disabled='disabled' class='pageUtil_more'><span>......</span></li>";	//前面加"...."
				if(currentPage + 4 > totalPage) {	//若currentPage + 4 > totalPage,直接显示最后9页即可
					j = totalPage;
					i = j - 8;
				} else {
					j = currentPage + 4;		//若currentPage + 4 <= totalPage,直接显示currentd的前后4页即可
					i = j - 8;
				}
			} else {
				j = i + 8;	//current < 5时, 直接显示1-9页即可
			}
		}
		for(; i <= j; i++) {									
			if(currentPage == i) {	//如果是当前页
				pageFooter += "<li disabled='disabled' class='pageUtil_nownum'><span>" + i + "</span></li>";
			} else {
				pageFooter += "<li inf='" + i + "' class='pageUtil_num'><a href='#' onclick='return false;'>" + i + "</a></li>";
			}
		}
		if(totalPage > 9 && currentPage + 4 < totalPage) {
			pageFooter += "<li disabled='disabled' class='pageUtil_more'><span>......</span></li>";	//后面加"...."
		}
		if(currentPage != totalPage) {										//如果是最后一页,就没有下一页
			pageFooter += "<li class='pageUtil_paging' inf='" + (currentPage + 1) + "'>下一页</li>";
		}
		pageFooter += "<li inf='" + totalPage + "' class='pageUtil_page'>末页</li>";
		pageFooter += "<li disabled='disabled' class='' >共&nbsp;<span style='color:#f00'>" + totalPage + "</span>&nbsp;页</li>";
		pageFooter += "<input type='hidden' name='" + pageSizeParameterName + "' value='" + pageSize + "'/>";		//加入pageSize
		pageFooter += "<li disabled='disabled' class='pageUtil_resCount_li'>(共&nbsp;<span style='color:#f00'>" + totalResult + "</span>&nbsp;条)</li></ul></div>";
		
		if(ComStrUtil.isBlank(url)) {					//若未设置url,则默认为当前jsp
			//url = CommonUtil.getJspFileName(request);
		}
		//添加javascript脚本
		pageFooter += "<script type='text/javascript'>$(function() {$('#pageFooter').children('li:not(:has(span))').each(function() {$(this).click(function() {if($(this).parents('form').length==0){$(this).wrap('<form method=\"post\"></form>');}var form = $(this).parents('form:eq(0)');$(form).attr('action',addParam2Url('" + url + "',new Array(new Array('" + pageNumParameterName + "'), new Array($(this).attr('inf')))));";
		//pageFooter += "alert($(form).attr('action'));";
		pageFooter += "$(form).submit();});});";
		pageFooter += "$('u').each(function() { "
					+ 		"if(typeof($(this).attr('pageUtil_orderByColumn')) == 'undefined') {"
					+ 			"return;"
					+ 		"} else {"
					//+		"alert($.trim($(this).attr('" + orderByColumnParameterName + "')));"
					//+ 		"alert('" + orderByColumn.trim() + "');"
					+		"	if($.trim($(this).attr('" + orderByColumnParameterName + "')) == '" + orderByColumn.trim() + "'){$(this).attr('" + orderWayParameterName + "','" + changeOrderByWay(orderWay) + "');$(this).after('<span style=\"font:20px;color:red;\">" + getOrderByArrow() + "</span>')}"
					//alert($(this).attr("pageUtil_orderByColumn"));
					+ 			"$(this).click(function() { "
					//+			"alert($(this).attr('" + orderByColumnParameterName + "'));"
					//+			"alert($(this).attr('" + orderWayParameterName + "'));"
					+				"if($(this).parents('form').length==0){$(this).wrap('<form method=\"post\"></form>');}var form = $(this).parents('form:eq(0)');$(form).attr('action',addParam2Url('" + url + "',new Array(new Array('" + orderByColumnParameterName + "','" + orderWayParameterName + "','" + pageNumParameterName + "'), new Array($(this).attr('" + orderByColumnParameterName + "'),$(this).attr('" + orderWayParameterName + "'),'1'))));$(form).submit(); "
					+			"});"	
					+		"} "
					+	"});";
		//System.out.println("cccccccccccccccc:" + changeOrderByWay(orderWay));
		pageFooter += "$(\"u\").attr('" + orderWayParameterName + "','" + changeOrderByWay(orderWay) + "');";
		
		pageFooter += "$(\"select[name='pageUtil_pageSize_sel']\").change(function(){"
				   //+ 	"alert(0);"
				   +    "if($(this).parents('form').length==0){$(this).wrap('<form method=\"post\"></form>');}var form = $(this).parents('form:eq(0)');$(form).attr('action',addParam2Url('" + url + "',new Array(new Array('" + pageSizeParameterName + "'), new Array($(this).val()))));$(form).submit(); "
				   +  "});";
		pageFooter += "})</script>";
		return pageFooter;
	}
	
	/**
	 * 转换 oderByWay. 把 有或没有空格的"asc" 或 "1"或 "" 或null  转成 " asc ";有或没有空格的"desc" 或 "0" 转成 " desc "
	 * @return
	 */
	private String convertOrderByWay(String way) {
		if(ComStrUtil.isBlank(way) || way.trim().equalsIgnoreCase("undefined") || way.trim().equalsIgnoreCase("asc") || way.trim().equals("1")) {
			return " asc ";
		} else {
			return " desc ";
		}
	}
	
	private String getOrderByArrow() {
		if(ComStrUtil.isBlank(orderWay) || orderWay.trim().equalsIgnoreCase("undefined") || orderWay.trim().equalsIgnoreCase("asc") || orderWay.trim().equals("1")) {
			return "↑";
		} else {
			return "↓";
		}
	}
	
	/**
	 * 转换 oderByWay. 转换出于 convertOrderByWay() 方法相反的结果
	 * @return
	 */
	private String changeOrderByWay(String way) {
		if(ComStrUtil.isBlank(way) || way.trim().equalsIgnoreCase("undefined") || way.trim().equalsIgnoreCase("asc") || way.trim().equals("1")) {
			return " desc ";
		} else {
			return " asc ";
		}
	}
	
	/**
	 * 根据sql查询并返回一个装着HashMap<String, String>的list
	 * @param sql
	 * @return
	 * @throws Exception 
	 */
	public List<HashMap<String, String>> getMapList(String sql) throws Exception {
		List<HashMap<String, String>> selectResultListHashMaps = new ArrayList<HashMap<String, String>>();
		Connection conn = this.conn;
		Statement stmt = null;
		ResultSet rs = null;
		//logger.info("[getMapList]  sql" +  sql);
		int columnType = 0;
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
					} else {//解决时间 2015-01-01 00:00:00.0 最后面多的.0问题
						columnType = rsmd.getColumnType(i + 1);
						//logger.error("tmpString:" + tmpString + " columnType:" + columnType);
						if(columnType == 93) {
							tmpString = tmpString.substring(0, 19);
						}
					}
					itemHashMap.put(itemString, tmpString);
				}
				selectResultListHashMaps.add(itemHashMap);
			}
		} catch (SQLException e) {
			logger.error("error sql:" + sql);
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				/**
				if (conn != null) {
					conn.close();
					conn = null;
				}*/
				//System.out.println(conn);
				rs = null;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return selectResultListHashMaps;
	}
	
	public static void main(String[] args) {
//		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
//		DaoManager dao = (DaoManager)ctx.getBean("dao");
//		System.out.println(dao);
//		DaoManager dao2 = (DaoManager)ctx.getBean("dao");
//		System.out.println(dao2);
		//System.out.println();;
	}
	
}
