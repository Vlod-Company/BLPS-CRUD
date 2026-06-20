package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
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

    public static final String ADMIN_GROUP = "ROLE_ADMIN";
    public static final String USER_GROUP = "ROLE_USER";

    private static final String CAMUNDA_ADMIN_GROUP = "camunda-admin";
    private static final String WORKFLOW_GROUP_TYPE = "WORKFLOW";

    private final IdentityService identityService;
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
        if (ADMIN_GROUP.equals(role)) {
            ensureMembership(account.login(), CAMUNDA_ADMIN_GROUP);
        }
        log.info("Synchronized Camunda identity. user={}, group={}", account.login(), role);
    }

    private void ensureGroups() {
        ensureGroup(USER_GROUP, "Application users");
        ensureGroup(ADMIN_GROUP, "Application administrators");
        ensureGroup(CAMUNDA_ADMIN_GROUP, "Camunda administrators");
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

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return USER_GROUP;
        }
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
