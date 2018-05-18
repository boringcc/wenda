package com.cc.wenda.service;


import com.cc.wenda.model.Question;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.stereotype.Service;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    private static final String SOLR_URL = "http://127.0.0.1:8983/solr/wenda"; //solr的后台路径
    private HttpSolrClient client = new HttpSolrClient.Builder(SOLR_URL).build();
    private static final String QUESTION_TITLE_FIELD = "question_title";      //搜索范围
    private static final String QUESTION_CONTENT_FIELD = "question_content";

    /**
     * 功能：搜寻问题
     * @param keyword       关键字
     * @param offset        偏移值：分页
     * @param count         搜索条数
     * @param hlPre         添加的前缀 变颜色或者加粗
     * @param hlPos         添加的后缀
     * @return
     * @throws Exception
     */
    public List<Question> searchQuestion(String keyword, int offset, int count,
                                         String hlPre, String hlPos) throws Exception {
        List<Question> questionList = new ArrayList<>();
        SolrQuery query = new SolrQuery(keyword);
        //设置搜索的属性
        query.setRows(count);
        query.setStart(offset);
        query.setHighlight(true);
        query.setHighlightSimplePre(hlPre);
        query.setHighlightSimplePost(hlPos);
        query.set("hl.fl", QUESTION_TITLE_FIELD + "," + QUESTION_CONTENT_FIELD); //设置加亮的字段
        QueryResponse response = client.query(query);
        //解析文本数据
        for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet()) {
            Question q = new Question();
            q.setId(Integer.parseInt(entry.getKey()));
            if (entry.getValue().containsKey(QUESTION_CONTENT_FIELD)) {
                List<String> contentList = entry.getValue().get(QUESTION_CONTENT_FIELD);
                if (contentList.size() > 0) {
                    q.setContent(contentList.get(0));
                }
            }
            if (entry.getValue().containsKey(QUESTION_TITLE_FIELD)) {
                List<String> titleList = entry.getValue().get(QUESTION_TITLE_FIELD);
                if (titleList.size() > 0) {
                    q.setTitle(titleList.get(0));
                }
            }
            questionList.add(q);
        }
        return questionList;
    }

    /**
     * 增加索引
     * @param qid
     * @param title
     * @param content
     * @return
     * @throws Exception
     */
    public boolean indexQuestion(int qid, String title, String content) throws Exception {
        SolrInputDocument doc =  new SolrInputDocument();
        doc.setField("id", qid);
        doc.setField(QUESTION_TITLE_FIELD, title);
        doc.setField(QUESTION_CONTENT_FIELD, content);
        UpdateResponse response = client.add(doc, 1000);
        return response != null && response.getStatus() == 0;
    }



}
