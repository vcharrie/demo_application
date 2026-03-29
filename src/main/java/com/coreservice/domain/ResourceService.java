package com.coreservice.domain;

import java.util.List;

public interface ResourceService {

    Resource create(Resource resource);

    List<Resource> findAll();

    Resource findById(String id);

    void delete(String id);
}
