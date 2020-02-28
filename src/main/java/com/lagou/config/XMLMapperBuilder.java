package com.lagou.config;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;
import com.lagou.pojo.enums.SqlCommandType;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

public class XMLMapperBuilder {

    private Configuration configuration;

    public XMLMapperBuilder(Configuration configuration) {
        this.configuration =configuration;
    }

    public void parse(InputStream inputStream) throws DocumentException {

        Document document = new SAXReader().read(inputStream);
        Element rootElement = document.getRootElement();

        String namespace = rootElement.attributeValue("namespace");

        //处理选择语句
        parseNode(rootElement.selectNodes("//select"), namespace, SqlCommandType.SELECT);
        //处理插入语句
        parseNode(rootElement.selectNodes("//insert"), namespace, SqlCommandType.INSERT);
        //处理更新语句
        parseNode(rootElement.selectNodes("//update"), namespace, SqlCommandType.UPDATE);
        //处理删除语句
        parseNode(rootElement.selectNodes("//delete"), namespace, SqlCommandType.DELETE);

    }

    /**
     * 处理节点数据
     * @param list
     * @param namespace
     */
    private void parseNode(List<Element> list, String namespace, SqlCommandType sqlCommandType) {
        for (Element element : list) {
            String id = element.attributeValue("id");
            String resultType = element.attributeValue("resultType");
            String paramterType = element.attributeValue("paramterType");
            String sqlText = element.getTextTrim();
            MappedStatement mappedStatement = new MappedStatement();
            mappedStatement.setId(id);
            mappedStatement.setResultType(resultType);
            mappedStatement.setParamterType(paramterType);
            mappedStatement.setSql(sqlText);
            mappedStatement.setSqlCommandType(sqlCommandType);
            String key = namespace+"."+id;
            configuration.getMappedStatementMap().put(key,mappedStatement);
        }
    }


}
