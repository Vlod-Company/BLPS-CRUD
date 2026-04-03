package ru.gigasigma.blpscrud.security;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.springframework.security.crypto.password.PasswordEncoder;

public class XmlUserLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private XmlUserStore xmlUserStore;
    private PasswordEncoder passwordEncoder;
    private UserPrincipal userPrincipal;
    private RolePrincipal rolePrincipal;
    private boolean authenticated;

    @Override
    public void initialize(
            Subject subject,
            CallbackHandler callbackHandler,
            Map<String, ?> sharedState,
            Map<String, ?> options
    ) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.xmlUserStore = (XmlUserStore) options.get(XmlUserStore.class.getName());
        this.passwordEncoder = (PasswordEncoder) options.get(PasswordEncoder.class.getName());
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("CallbackHandler is required");
        }
        if (xmlUserStore == null || passwordEncoder == null) {
            throw new LoginException("LoginModule dependencies are not configured");
        }

        NameCallback nameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        } catch (IOException | UnsupportedCallbackException ex) {
            throw new LoginException("Failed to read credentials: " + ex.getMessage());
        }

        String login = nameCallback.getName();
        char[] password = passwordCallback.getPassword();
        passwordCallback.clearPassword();

        XmlAccount account = xmlUserStore.findByLogin(login)
                .orElseThrow(() -> new FailedLoginException("Invalid username or password"));

        String rawPassword = password == null ? "" : new String(password);
        if (!passwordEncoder.matches(rawPassword, account.passwordHash())) {
            throw new FailedLoginException("Invalid username or password");
        }

        this.userPrincipal = new UserPrincipal(account.login());
        this.rolePrincipal = new RolePrincipal(account.role());
        this.authenticated = true;
        return true;
    }

    @Override
    public boolean commit() {
        if (!authenticated) {
            return false;
        }
        subject.getPrincipals().add(userPrincipal);
        subject.getPrincipals().add(rolePrincipal);
        return true;
    }

    @Override
    public boolean abort() {
        clearState();
        return true;
    }

    @Override
    public boolean logout() {
        if (Objects.nonNull(userPrincipal)) {
            subject.getPrincipals().remove(userPrincipal);
        }
        if (Objects.nonNull(rolePrincipal)) {
            subject.getPrincipals().remove(rolePrincipal);
        }
        clearState();
        return true;
    }

    private void clearState() {
        this.authenticated = false;
        this.userPrincipal = null;
        this.rolePrincipal = null;
    }
}