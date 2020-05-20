package com.mtons.mblog.modules.service;

import com.mtons.mblog.modules.entity.Links;

import java.util.List;

/**

 */
public interface LinksService {
    List<Links> findAll();
    void update(Links links);
    void delete(long id);
}
