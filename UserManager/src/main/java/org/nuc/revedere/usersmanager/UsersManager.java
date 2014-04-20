package org.nuc.revedere.usersmanager;

import org.nuc.revedere.service.core.SupervisedService;

public class UsersManager extends SupervisedService{
    private final static String USERSMANAGER_SERVICE_NAME = "UsersManager";
    
    public UsersManager() throws Exception {
        super(USERSMANAGER_SERVICE_NAME);
    }

}
