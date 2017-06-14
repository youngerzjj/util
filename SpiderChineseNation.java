package com.vcg.legacy.region;

import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从统计局网站爬最新的行政区划代码数据
 *
 * Created by lizhenhe on 17/6/13.
 */
public class Spider {
    public static final String url = "http://www.stats.gov.cn/tjsj/tjbz/xzqhdm/201703/t20170310_1471429.html";

    public static void main(String[] args) throws IOException, ParserException {
        URL realUrl = new URL(url);
        // 打开和URL之间的连接
        URLConnection connection = realUrl.openConnection();
        // 设置通用的请求属性
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        // 建立实际的连接
        connection.connect();

        String result = "";
        BufferedReader in = null;

        // 定义 BufferedReader输入流来读取URL的响应
        in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }

        Map<String, String> map = new HashMap<String, String>();


        Parser parser = Parser.createParser(result, "UTF-8");
//        // 通过过滤器过滤出<A>标签
//        NodeList nodeList = parser
//                .extractAllNodesThatMatch(new NodeFilter() {
//                    //实现该方法,用以过滤标签
//                    public boolean accept(Node node) {
//                        if (node instanceof LinkTag)//<A>标记
//                            return true;
//                        return false;
//                    }
//                });
        NodeList nodeList = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "MsoNormal"));
        int pid = 0;
        int lastLevel = -1;


        List<String> sqls = new ArrayList<>();
        // 打印
        for (int i = 0; i < nodeList.size(); i++) {
            TagNode node = (TagNode) nodeList.elementAt(i);
            NodeList children = node.getChildren();
            Node firstChild = node.getFirstChild().getFirstChild();
            int level = 0;
            if(firstChild == null){
                level = 0;
            }else if(1 == firstChild.getText().length()){
                level = 1;
            }else if (2 == firstChild.getText().length()){
                level = 2;
            }
            if(level == 0 ){
                pid = 0;
            }


            System.out.println("leve: " + level + ",pid :" +pid);
            String code = "";
            String name = "";
            //如果firstChild 内容为null 说明是一级， 内容为2个空格为二级，为3个空格为三级
            for (int j = 0 ; j < children.size() ; j++){
                Node node1 = children.elementAt(j);
                TagNode t = (TagNode) node1;
                if(node1.getFirstChild() != null){
                    TagNode tagNode = (TagNode)node1;
                    String text = tagNode.getFirstChild().getText();
                    //编码
                    if("EN-US".equals((tagNode.getAttribute("lang")))) {
                        System.out.println("编码：" + text);
                        code = text + "";
                    }else if(StringUtils.isNoneBlank(text)){//名称
                        System.out.println("名称 " + text.trim()) ;
                        name = StringUtils.remove(text,"　");
                    }

                }

            }

            String sql = "( '"+ code +"', '"+ name +"', '"+ pid +"', '"+ level+"')";
            sqls.add(sql);

            TagNode firstChild1 = (TagNode) node.getChildren().extractAllNodesThatMatch(new HasAttributeFilter("lang")).elementAt(0);
//            System.out.println(firstChild1);
            if("EN-US".equals((firstChild1.getAttribute("lang")))) {
                System.out.println("-------EN-US");
                if(level != lastLevel) {
                    pid = Integer.parseInt(firstChild1.getFirstChild().getText());
                    System.out.println("pid: " + pid);
                }
            }
            lastLevel = level;

//            break;
        }
        System.out.println(StringUtils.join(sqls, ","));
    }
} 
