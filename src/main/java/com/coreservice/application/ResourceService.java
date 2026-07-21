package com.coreservice.application;

import java.util.List;

import com.coreservice.domain.Resource;

public interface ResourceService {

    Resource create(Resource resource);

    List<Resource> findAll();

    Resource findById(String id);

    void delete(String id);
}
