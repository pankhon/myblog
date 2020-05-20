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

import com.mtons.mblog.base.lang.EntityStatus;
import com.mtons.mblog.base.lang.MtonsException;
import com.mtons.mblog.base.utils.MD5;
import com.mtons.mblog.modules.data.AccountProfile;
import com.mtons.mblog.modules.data.BadgesCount;
import com.mtons.mblog.modules.data.UserVO;
import com.mtons.mblog.modules.entity.User;
import com.mtons.mblog.modules.repository.RoleRepository;
import com.mtons.mblog.modules.repository.UserRepository;
import com.mtons.mblog.modules.service.MessageService;
import com.mtons.mblog.modules.service.UserService;
import com.mtons.mblog.base.utils.BeanMapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageService messageService;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Page<UserVO> paging(Pageable pageable, String name) {
        Page<User> page = userRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();

            if (StringUtils.isNoneBlank(name)) {
                predicate.getExpressions().add(
                        builder.like(root.get("name"), "%" + name + "%"));
            }

            query.orderBy(builder.desc(root.get("id")));
            return predicate;
        }, pageable);

        List<UserVO> rets = new ArrayList<>();
        page.getContent().forEach(n -> rets.add(BeanMapUtils.copy(n)));
        return new PageImpl<>(rets, pageable, page.getTotalElements());
    }

    @Override
    public Map<Long, UserVO> findMapByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> list = userRepository.findAllById(ids);
        Map<Long, UserVO> ret = new HashMap<>();

        list.forEach(po -> ret.put(po.getId(), BeanMapUtils.copy(po)));
        return ret;
    }

    /**
     * 登录操作
     * @param username
     * @param password
     * @return
     */
    @Override
    @Transactional
    public AccountProfile login(String username, String password) {
        User po = userRepository.findByUsername(username);

        if (null == po) {
            return null;
        }

//		Assert.state(po.getStatus() != Const.STATUS_CLOSED, "您的账户已被封禁");

        Assert.state(StringUtils.equals(po.getPassword(), password), "密码错误");

        po.setLastLogin(Calendar.getInstance().getTime());
        userRepository.save(po);
        AccountProfile u = BeanMapUtils.copyPassport(po);

        BadgesCount badgesCount = new BadgesCount();
        badgesCount.setMessages(messageService.unread4Me(u.getId()));

        u.setBadgesCount(badgesCount);
        return u;
    }

    /**
     * 查找当前账户数据，包含通知未读信息获取
     * @param id
     * @return
     */
    @Override
    @Transactional
    public AccountProfile findProfile(Long id) {
        User po = userRepository.findById(id).orElse(null);

        //assert断言判断user对象是否为空
        Assert.notNull(po, "账户不存在");

//		Assert.state(po.getStatus() != Const.STATUS_CLOSED, "您的账户已被封禁");
        //设置登录时间
        po.setLastLogin(Calendar.getInstance().getTime());

        //将user的数据拷贝到AccountProfile当中使用
        AccountProfile u = BeanMapUtils.copyPassport(po);

        //创建消息包装类
        BadgesCount badgesCount = new BadgesCount();

        //查出未读的消息数
        badgesCount.setMessages(messageService.unread4Me(u.getId()));

        //将未读消息数填入AccountProfile账户中
        u.setBadgesCount(badgesCount);

        return u;
    }

    @Override
    @Transactional
    public UserVO register(UserVO user) {
        Assert.notNull(user, "Parameter user can not be null!");

        Assert.hasLength(user.getUsername(), "用户名不能为空!");
        Assert.hasLength(user.getPassword(), "密码不能为空!");

        User check = userRepository.findByUsername(user.getUsername());

        Assert.isNull(check, "用户名已经存在!");

        if (StringUtils.isNotBlank(user.getEmail())) {
            User emailCheck = userRepository.findByEmail(user.getEmail());
            Assert.isNull(emailCheck, "邮箱已经存在!");
        }

        User po = new User();

        BeanUtils.copyProperties(user, po);

        if (StringUtils.isBlank(po.getName())) {
            po.setName(user.getUsername());
        }

        Date now = Calendar.getInstance().getTime();
        po.setPassword(MD5.md5(user.getPassword()));
        po.setStatus(EntityStatus.ENABLED);
        po.setCreated(now);

        userRepository.save(po);

        return BeanMapUtils.copy(po);
    }

    //更改用户的基本信息，如昵称或个性签名
    @Override
    @Transactional
    public AccountProfile update(UserVO user) {

        //用业务层uservo 对象获取基本信息数据，向持久层userPO对象进行数据封装
        User po = userRepository.findById(user.getId()).get();
        po.setName(user.getName());
        po.setSignature(user.getSignature());
        //将从vo对象获取的数据持久层po对象，将新数据写入到数据库，完成更改用户信息操作
        userRepository.save(po);
        return BeanMapUtils.copyPassport(po);
    }

    @Override
    @Transactional
    public AccountProfile updateEmail(long id, String email) {
        User po = userRepository.findById(id).get();

        if (email.equals(po.getEmail())) {
            throw new MtonsException("邮箱地址没做更改");
        }

        User check = userRepository.findByEmail(email);

        if (check != null && check.getId() != po.getId()) {
            throw new MtonsException("该邮箱地址已经被使用了");
        }
        po.setEmail(email);
        userRepository.save(po);
        return BeanMapUtils.copyPassport(po);
    }

    @Override
    public UserVO get(long userId) {
        Optional<User> optional = userRepository.findById(userId);
        if (optional.isPresent()) {
            return BeanMapUtils.copy(optional.get());
        }
        return null;
    }

    @Override
    public UserVO getByUsername(String username) {
        return BeanMapUtils.copy(userRepository.findByUsername(username));
    }

    @Override
    public UserVO getByEmail(String email) {
        return BeanMapUtils.copy(userRepository.findByEmail(email));
    }

    @Override
    @Transactional
    public AccountProfile updateAvatar(long id, String path) {
        User po = userRepository.findById(id).get();
        po.setAvatar(path);
        userRepository.save(po);
        return BeanMapUtils.copyPassport(po);
    }

    @Override
    @Transactional
    public void updatePassword(long id, String newPassword) {
        User po = userRepository.findById(id).get();

        Assert.hasLength(newPassword, "密码不能为空!");

        po.setPassword(MD5.md5(newPassword));
        userRepository.save(po);
    }

    @Override
    @Transactional
    public void updatePassword(long id, String oldPassword, String newPassword) {
        User po = userRepository.findById(id).get();

        Assert.hasLength(newPassword, "密码不能为空!");

        Assert.isTrue(MD5.md5(oldPassword).equals(po.getPassword()), "当前密码不正确");
        po.setPassword(MD5.md5(newPassword));
        userRepository.save(po);
    }

    @Override
    @Transactional
    public void updateStatus(long id, int status) {
        User po = userRepository.findById(id).get();

        po.setStatus(status);
        userRepository.save(po);
    }

    @Override
    public long count() {
        return userRepository.count();
    }

}
