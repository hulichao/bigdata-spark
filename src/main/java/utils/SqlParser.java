package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.apache.spark.util.CollectionsUtils;

/**
 * 从hive sql解析得到表
 */
public class SqlParser {
    public static void main(String[] args) throws ParseException, FileNotFoundException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("data/sql")));
        String sql = bufferedReader.lines().collect(Collectors.toList()).get(0);
        ParseDriver pd = new ParseDriver();
        ASTNode ast = pd.parse(sql);

        String strTree = ast.toStringTree();

        Set<String> result = new HashSet<>();
        Set<String> res = getTableList(result, strTree);

        System.out.println(StringUtils.join(res, ","));
        System.out.println();
    }

    /**
     * 递归截取字符串获取表名
     * @param strTree
     * @return
     */
    public static Set<String> getTableList(Set<String> set, String strTree){
        int i1 = strTree.indexOf("TOK_TABNAME");
        String substring1 = "";
        String substring2 = "";
        if(i1>0){
            substring1 = strTree.substring(i1+12);
            int i2 = substring1.indexOf(")");
            substring2 = substring1.substring(0,i2);

            String db_table = substring2.split("\\s+")[0] + "." + substring2.split("\\s+")[1];
            set.add(db_table);
            getTableList(set, substring1);
        }
        return set;
    }
}
