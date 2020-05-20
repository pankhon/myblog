/**
 *
 */
package com.mtons.mblog.modules.template.directive;

import com.mtons.mblog.modules.data.PostVO;
import com.mtons.mblog.modules.service.PostService;
import com.mtons.mblog.modules.template.DirectiveHandler;
import com.mtons.mblog.modules.template.TemplateDirective;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * 根据作者取文章列表
 *
 *
 */
@Component
public class UserContentsDirective extends TemplateDirective {
    @Autowired
	private PostService postService;

	@Override
	public String getName() {
		return "user_contents";
	}

    @Override
    public void execute(DirectiveHandler handler) throws Exception {
        //userId为自定义标签的user.id值
	    long userId = handler.getInteger("userId", 0);

	    //包装分页对象
        Pageable pageable = wrapPageable(handler);

        //根据作者id查取文章内容
        Page<PostVO> result = postService.pagingByAuthorId(pageable, userId);
        handler.put(RESULTS, result).render();
    }

}
