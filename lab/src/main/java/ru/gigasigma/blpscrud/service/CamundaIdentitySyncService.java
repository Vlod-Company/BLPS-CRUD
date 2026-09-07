package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.security.XmlAccount;
import ru.gigasigma.blpscrud.security.XmlUserStore;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamundaIdentitySyncService {

    public static final String ADMIN_GROUP = "roleAdmin";
    public static final String USER_GROUP = "roleUser";

    private static final String WORKFLOW_GROUP_TYPE = "WORKFLOW";
    private static final String APP_ADMIN_ROLE = "ROLE_ADMIN";
    private static final String APP_USER_ROLE = "ROLE_USER";
    private static final String TASKLIST_APP = "tasklist";
    private static final String COCKPIT_APP = "cockpit";

    private final IdentityService identityService;
    private final AuthorizationService authorizationService;
    private final XmlUserStore xmlUserStore;

    @Value("${camunda.identity.default-user-password:password}")
    private String defaultUserPassword;

    @EventListener(ApplicationReadyEvent.class)
    public void syncExistingUsers() {
        ensureGroups();
        xmlUserStore.findAll().forEach(account -> syncAccount(account, defaultUserPassword, false));
    }

    public void syncRegisteredUser(XmlAccount account, String rawPassword) {
        ensureGroups();
        syncAccount(account, rawPassword, true);
    }

    public void syncAuthenticatedUser(XmlAccount account, String rawPassword) {
        ensureGroups();
        syncAccount(account, rawPassword, true);
    }

    private void syncAccount(XmlAccount account, String password, boolean updatePassword) {
        String role = normalizeRole(account.role());
        ensureUser(account.login(), password, account.fullName(), updatePassword);
        ensureMembership(account.login(), role);
        log.info("Synchronized Camunda identity. user={}, group={}", account.login(), role);
    }

    private void ensureGroups() {
        ensureGroup(USER_GROUP, APP_USER_ROLE);
        ensureGroup(ADMIN_GROUP, APP_ADMIN_ROLE);
        ensureTasklistAccess(USER_GROUP);
        ensureTasklistAccess(ADMIN_GROUP);
        ensureCockpitAccess(ADMIN_GROUP);
        removeGroupAuthorization(USER_GROUP, Resources.TASK, Authorization.ANY);
        ensureTaskAccess(ADMIN_GROUP);
    }

    private void ensureGroup(String groupId, String name) {
        Group existing = identityService.createGroupQuery().groupId(groupId).singleResult();
        if (existing != null) {
            return;
        }

        Group group = identityService.newGroup(groupId);
        group.setName(name);
        group.setType(WORKFLOW_GROUP_TYPE);
        identityService.saveGroup(group);
    }

    private void ensureUser(String login, String password, String fullName, boolean updatePassword) {
        User existing = identityService.createUserQuery().userId(login).singleResult();
        if (existing != null) {
            if (updatePassword) {
                existing.setPassword(password);
                identityService.saveUser(existing);
            }
            return;
        }

        User user = identityService.newUser(login);
        user.setPassword(password);
        user.setFirstName(fullName == null || fullName.isBlank() ? login : fullName);
        identityService.saveUser(user);
    }

    private void ensureMembership(String userId, String groupId) {
        long existingMemberships = identityService.createUserQuery()
                .userId(userId)
                .memberOfGroup(groupId)
                .count();
        if (existingMemberships > 0) {
            return;
        }

        identityService.createMembership(userId, groupId);
    }

    private void ensureTasklistAccess(String groupId) {
        ensureGroupAuthorization(groupId, Resources.APPLICATION, TASKLIST_APP, Permissions.ACCESS);
        ensureGroupAuthorization(groupId, Resources.FILTER, Authorization.ANY, Permissions.READ);
        ensureGroupAuthorization(groupId, Resources.PROCESS_DEFINITION, "Process_Main",
                Permissions.READ, Permissions.CREATE_INSTANCE);
        ensureGroupAuthorization(groupId, Resources.PROCESS_INSTANCE, Authorization.ANY, Permissions.CREATE);
    }

    private void ensureCockpitAccess(String groupId) {
        ensureGroupAuthorization(groupId, Resources.APPLICATION, COCKPIT_APP, Permissions.ACCESS);
    }

    private void ensureTaskAccess(String groupId) {
        ensureGroupAuthorization(groupId, Resources.TASK, Authorization.ANY, Permissions.READ, Permissions.TASK_WORK);
    }

    private void removeGroupAuthorization(String groupId, Resources resource, String resourceId) {
        authorizationService.createAuthorizationQuery()
                .groupIdIn(groupId)
                .resourceType(resource)
                .resourceId(resourceId)
                .list()
                .forEach(authorization -> {
                    authorizationService.deleteAuthorization(authorization.getId());
                    log.info("Removed broad Camunda authorization. group={}, resource={}, resourceId={}",
                            groupId, resource, resourceId);
                });
    }

    private void ensureGroupAuthorization(
            String groupId,
            Resources resource,
            String resourceId,
            Permissions... permissions
    ) {
        Authorization existing = authorizationService.createAuthorizationQuery()
                .groupIdIn(groupId)
                .resourceType(resource)
                .resourceId(resourceId)
                .singleResult();
        if (existing != null) {
            boolean changed = false;
            for (Permissions permission : permissions) {
                if (!existing.isPermissionGranted(permission)) {
                    existing.addPermission(permission);
                    changed = true;
                }
            }
            if (changed) {
                authorizationService.saveAuthorization(existing);
            }
            return;
        }

        Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        authorization.setGroupId(groupId);
        authorization.setResource(resource);
        authorization.setResourceId(resourceId);
        for (Permissions permission : permissions) {
            authorization.addPermission(permission);
        }
        authorizationService.saveAuthorization(authorization);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return USER_GROUP;
        }
        String appRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return switch (appRole) {
            case APP_ADMIN_ROLE -> ADMIN_GROUP;
            case APP_USER_ROLE -> USER_GROUP;
            default -> appRole.replaceAll("[^A-Za-z0-9]", "");
        };
    }
}
