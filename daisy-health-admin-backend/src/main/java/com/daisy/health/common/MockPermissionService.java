package com.daisy.health.common;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mock")
public class MockPermissionService extends PermissionService {
    public MockPermissionService() {
        super(null, null);
    }

    @Override
    public boolean canAccess(AuthenticatedUser user, String method, String uri) {
        return true;
    }
}
