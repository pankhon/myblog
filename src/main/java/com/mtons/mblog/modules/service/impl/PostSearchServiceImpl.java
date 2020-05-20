package com.mtons.mblog.modules.service.impl;

import com.mtons.mblog.modules.aspect.PostStatusFilter;
import com.mtons.mblog.modules.data.PostVO;
import com.mtons.mblog.modules.data.UserVO;
import com.mtons.mblog.modules.entity.Post;
import com.mtons.mblog.modules.service.PostSearchService;
import com.mtons.mblog.modules.service.UserService;
import com.mtons.mblog.base.utils.BeanMapUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**

 */
@Slf4j
@Service
@Transactional
public class PostSearchServiceImpl implements PostSearchService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserService userService;

    @Override
    @PostStatusFilter
    public Page<PostVO> search(Pageable pageable, String term) throws Exception {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder builder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Post.class).get();

        //创建Lucene查询,进行search。近似查询
        Query luceneQuery = builder
                .keyword()
                .fuzzy()
                //通过editdistanceupto()，可以定义术语之间的偏离程度。它可以设置为0、1和2，默认值为2。
                .withEditDistanceUpTo(1)
                //通过withPrefixLength()，定义前缀的长度，这个长度是由模糊性所忽略的:
                .withPrefixLength(1)
                .onFields("title", "summary", "tags")    //指定要检索的属性
                .matching(term)     //matching里面为要检索的内容
                .createQuery();

        //包装Lucene 查询至 Hibernate查询
        FullTextQuery query = fullTextEntityManager.createFullTextQuery(luceneQuery, Post.class);
        query.setFirstResult((int) pageable.getOffset());

        query.setMaxResults(pageable.getPageSize());

        //构造中文分词器
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();

        //格式化Html标签器
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span style='color:red;'>", "</span>");

        //传入评分
        QueryScorer scorer = new QueryScorer(luceneQuery);

        //高亮分析器
        Highlighter highlighter = new Highlighter(formatter, scorer);

        List<Post> list = query.getResultList();
        List<PostVO> rets = list.stream().map(po -> {
            PostVO post = BeanMapUtils.copy(po);

            try {
                // 处理高亮
                String title = highlighter.getBestFragment(analyzer, "title", post.getTitle());
                String summary = highlighter.getBestFragment(analyzer, "summary", post.getSummary());

                //将带有<span>标签元素的标红高亮标题，重新写入到post对象中
                if (StringUtils.isNotEmpty(title)) {
                    post.setTitle(title);
                }
                //将带有<span>标签元素的标红高亮摘要，重新写入到post对象中
                if (StringUtils.isNotEmpty(summary)) {
                    post.setSummary(summary);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return post;
        }).collect(Collectors.toList());
        buildUsers(rets);
        return new PageImpl<>(rets, pageable, query.getResultSize());
    }

    @Override
    public void resetIndexes() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        fullTextEntityManager.createIndexer(Post.class).start();
    }

    private void buildUsers(List<PostVO> list) {
        if (null == list) {
            return;
        }
        HashSet<Long> uids = new HashSet<>();
        list.forEach(n -> uids.add(n.getAuthorId()));
        Map<Long, UserVO> userMap = userService.findMapByIds(uids);
        list.forEach(p -> p.setAuthor(userMap.get(p.getAuthorId())));
    }
}
