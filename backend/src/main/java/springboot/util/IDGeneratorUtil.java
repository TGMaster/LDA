/*
 * Copyright (c) HOGO, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of HOGO,
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with HOGO.
 */
package com.spring.boot.util;

import java.io.Serializable;
import java.util.Properties;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

/**
 * 
 * @author TGMaster
 */
public class IDGeneratorUtil extends UUIDGenerator {

    private String entityName;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry dialect) {
        entityName = params.getProperty(ENTITY_NAME);
        super.configure(type, params, dialect);
    }

    public Serializable generate(SessionImplementor session, Object object) {
        Serializable id = session.getEntityPersister(entityName, object).getIdentifier(object, session);

        if (id == null) {
            return CommonUtil.generateUUID();
        } else {
            return id;
        }
    }

}
