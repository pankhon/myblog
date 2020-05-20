package com.mtons.mblog.modules.service.impl;

import com.mtons.mblog.modules.data.PermissionTree;
import com.mtons.mblog.modules.entity.Permission;
import com.mtons.mblog.modules.repository.PermissionRepository;
import com.mtons.mblog.modules.service.PermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;

/**
 */
@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;

    private Sort sort = Sort.by(
            new Sort.Order(Sort.Direction.DESC, "weight"),
            new Sort.Order(Sort.Direction.ASC, "id")
    );

    @Override
    public Page<Permission> paging(Pageable pageable, String name) {
        Page<Permission> page = permissionRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();

            if (StringUtils.isNoneBlank(name)) {
                predicate.getExpressions().add(
                        builder.like(root.get("name"), "%" + name + "%"));
            }

            query.orderBy(builder.desc(root.get("id")));
            return predicate;
        }, pageable);
        return page;
    }

    /**
     * 获取角色权限列表数的数据
     * @return
     */
    @Override
    public List<PermissionTree> tree() {
        //获取所以的权限表数据
        List<Permission> data = permissionRepository.findAll(sort);
        List<PermissionTree> results = new LinkedList<>();
        Map<Long, PermissionTree> map = new LinkedHashMap<>();

        //遍历所有的权限列表数据，移植至Map集合
        for (Permission po : data) {
            PermissionTree m = new PermissionTree();

            //复制data集合中的权限表数据到PermissionTree的对象m
            BeanUtils.copyProperties(po, m);

            //将取得数据的PermissionTree对象，put添加到map集合中

            map.put(po.getId(), m);
        }

        for (PermissionTree m : map.values()) {
            if (m.getParentId() == 0) {
                results.add(m);
            } else {
                PermissionTree p = map.get(m.getParentId());
                if (p != null) {
                    p.addItem(m);
                }
            }
        }

        return results;
    }

    @Override
    public List<PermissionTree> tree(int parentId) {
        List<Permission> list = permissionRepository.findAllByParentId(parentId, sort);
        List<PermissionTree> results = new ArrayList<>();

        list.forEach(po -> {
            PermissionTree menu = new PermissionTree();
            BeanUtils.copyProperties(po, menu);
            results.add(menu);
        });
        return results;
    }

    @Override
    public List<Permission> list() {
        return permissionRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Override
    public Permission get(long id) {
        return permissionRepository.findById(id).get();
    }

}
