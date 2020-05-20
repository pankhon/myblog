/*
+--------------------------------------------------------------------------
|   Mblog [#RELEASE_VERSION#]
|   ========================================
|   Copyright (c) 2014, 2015 mtons. All Rights Reserved
|   http://www.mtons.com
|
+---------------------------------------------------------------------------
*/
package com.mtons.mblog.modules.service.impl;

import com.mtons.mblog.base.lang.Consts;
import com.mtons.mblog.modules.entity.Post;
import com.mtons.mblog.modules.repository.ChannelRepository;
import com.mtons.mblog.modules.service.ChannelService;
import com.mtons.mblog.modules.entity.Channel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**

 *
 */
@Service
@Transactional(readOnly = true)
public class ChannelServiceImpl implements ChannelService {
	@Autowired
	private ChannelRepository channelRepository;

	@Override
	public List<Channel> findAll(int status) {
		//根据weight加权数降序排序，创建排序的字段
		Sort sort = Sort.by(Sort.Direction.DESC, "weight", "id");
		List<Channel> list;

		//判断状态，Consts.IGNORE=-1
		// 若为-1，则进入else语句，查询出所有状态的栏目信息
		//若为0> Consts.IGNORE，则第一个判断条件成立，查询出所有非隐藏状态的栏目
		if (status > Consts.IGNORE) {
			list = channelRepository.findAllByStatus(status, sort);
		} else {
			//根据sort排序的字段，进行全部栏目包含隐藏栏目的查询
			list = channelRepository.findAll(sort);
		}
		return list;
	}

	@Override
	public Map<Integer, Channel> findMapByIds(Collection<Integer> ids) {
		List<Channel> list = channelRepository.findAllById(ids);
		if (null == list) {
			return Collections.emptyMap();
		}
		return list.stream().collect(Collectors.toMap(Channel::getId, n -> n));
	}

	@Override
	public Channel getById(int id) {
		return channelRepository.findById(id).get();
	}

    /**
     * 新增或更新栏目
     * @param channel
     */
	@Override
	@Transactional
	public void update(Channel channel) {

	    //1、根据id查找栏目数据，若为新增栏目，那么在数据库中若查不到数据
        //2、返回值中对象channel为null
	    Optional<Channel> optional = channelRepository.findById(channel.getId());

	    //判断查询的数据Optional中Channel对象是否为null
        //若channel为null，则返回po=new Channel()，给po新new一个对象
	    Channel po = optional.orElse(new Channel());

	    //利用工具将参数Channel的数据全体复制到新对象po当中
		BeanUtils.copyProperties(channel, po);
		channelRepository.save(po);
	}

    /**
     * 更新栏目的置顶
     * @param id
     * @param weighted
     */
	@Override
	@Transactional
	public void updateWeight(int id, int weighted) {
		Channel po = channelRepository.findById(id).get();

        //设置初始max值为0
		int max = Consts.ZERO;

		//判断weighted数值是否等于1
		if (Consts.FEATURED_ACTIVE == weighted) {

            //获取栏目加权数的最大值进行+1处理
			max = channelRepository.maxWeight() + 1;
		}
		po.setWeight(max);
		channelRepository.save(po);
	}

	@Override
	@Transactional
	public void delete(int id) {
		channelRepository.deleteById(id);
	}

	@Override
	public long count() {
		return channelRepository.count();
	}

}
