/*
+--------------------------------------------------------------------------
|   Mblog [#RELEASE_VERSION#]
|   ========================================
|   Copyright (c) 2014, 2015 mtons. All Rights Reserved
|   http://www.mtons.com
|
+---------------------------------------------------------------------------
*/
package com.mtons.mblog.web.controller.admin;

import com.mtons.mblog.base.lang.Result;
import com.mtons.mblog.base.lang.Consts;
import com.mtons.mblog.config.ContextStartup;
import com.mtons.mblog.modules.entity.Channel;
import com.mtons.mblog.modules.service.ChannelService;
import com.mtons.mblog.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Controller("adminChannelController")
@RequestMapping("/admin/channel")
public class ChannelController extends BaseController {
	@Autowired
	private ChannelService channelService;
	@Autowired
	private ContextStartup contextStartup;
	
	@RequestMapping("/list")
//	@RequiresPermissions("channel:list")
	public String list(ModelMap model) {
		//查找出所有状态的栏目信息
		model.put("list", channelService.findAll(Consts.IGNORE));
		return "/admin/channel/list";
	}

    /**
     * 处理点击添加栏目时返回的视图界面操作
     * @param id
     * @param model
     * @return
     */
	@RequestMapping("/view")
	public String view(Integer id, ModelMap model) {
		if (id != null) {
			Channel view = channelService.getById(id);
			model.put("view", view);
		}
		return "/admin/channel/view";
	}

    /**
     * 新增栏目和修改栏目
     * @param view  由于channel对象的成员变量名字，与前台传来的参数数据一样，
     *              则可以自动封装到channel对象当中，进行赋值
     * @return
     */
	@RequestMapping("/update") 
//	@RequiresPermissions("channel:update")
	public String update(Channel view) {
		if (view != null) {
			channelService.update(view);

			//重置栏目的信息
			// 通过setAttribute将查到的所有状态为0，即非隐藏栏目，设置到channels，方便前台数据获取
			contextStartup.resetChannels();
		}
		//重定向访问url解析路径，进入对应的channel控制器
		return "redirect:/admin/channel/list";
	}

    /**
     * 更新栏目的置顶操作
     * @param id
     * @param request
     * @return
     */
	@RequestMapping("/weight")
	@ResponseBody
	public Result weight(@RequestParam Integer id, HttpServletRequest request) {
		int weight = ServletRequestUtils.getIntParameter(request, "weight", Consts.FEATURED_ACTIVE);
		channelService.updateWeight(id, weight);
		contextStartup.resetChannels();
		return Result.success();
	}
	
	@RequestMapping("/delete")
	@ResponseBody
//	@RequiresPermissions("channel:delete")
	public Result delete(Integer id) {
		Result data = Result.failure("操作失败");
		if (id != null) {
			try {
				channelService.delete(id);
				data = Result.success();

				contextStartup.resetChannels();
			} catch (Exception e) {
				data = Result.failure(e.getMessage());
			}
		}
		return data;
	}
	
}
