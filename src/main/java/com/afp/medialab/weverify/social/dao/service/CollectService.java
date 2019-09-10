package com.afp.medialab.weverify.social.dao.service;

import com.afp.medialab.weverify.social.dao.entity.CollectTable;
import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class CollectService {

    @Autowired
    CollectInterface collectInterface;

    public void persist(@RequestBody final CollectTable collectTable)
    {
        collectInterface.save(collectTable);
    }
}
