package ru.gigasigma.blpscrud.security;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
@RequiredArgsConstructor
public class XmlUserStore {

    private static final String ROOT_TAG = "accounts";
    private static final String ACCOUNT_TAG = "account";

    private final PasswordEncoder passwordEncoder;

    @Value("${security.users-file}")
    private String usersFilePath;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @PostConstruct
    public void initialize() {
        lock.writeLock().lock();
        try {
            Path path = resolvePath();
            if (!Files.exists(path)) {
                Path parent = path.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                writeAccounts(path, List.of(new XmlAccount(1L, "admin", passwordEncoder.encode("admin"), "ROLE_ADMIN", "Administrator")));
                return;
            }

            List<XmlAccount> existingAccounts = readAccounts(path);
            List<XmlAccount> normalizedAccounts = normalizeAccounts(existingAccounts);
            if (!normalizedAccounts.equals(existingAccounts)) {
                writeAccounts(path, normalizedAccounts);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize XML user store", ex);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<XmlAccount> findByLogin(String login) {
        lock.readLock().lock();
        try {
            return readAccounts(resolvePath()).stream()
                    .filter(account -> account.login().equals(login))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<XmlAccount> findById(Long id) {
        lock.readLock().lock();
        try {
            return readAccounts(resolvePath()).stream()
                    .filter(account -> account.id().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean existsByLogin(String login) {
        return findByLogin(login).isPresent();
    }

    public XmlAccount createUser(String login, String passwordHash, String role, String fullName) {
        lock.writeLock().lock();
        try {
            Path path = resolvePath();
            List<XmlAccount> accounts = normalizeAccounts(new ArrayList<>(readAccounts(path)));
            if (accounts.stream().anyMatch(account -> account.login().equals(login))) {
                throw new IllegalArgumentException("Login is already taken");
            }
            long nextId = accounts.stream()
                    .map(XmlAccount::id)
                    .max(Long::compareTo)
                    .orElse(0L) + 1;
            XmlAccount account = new XmlAccount(nextId, login, passwordHash, role, fullName);
            accounts.add(account);
            accounts.sort(Comparator.comparing(XmlAccount::id));
            writeAccounts(path, accounts);
            return account;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to persist user credentials", ex);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Path resolvePath() {
        return Path.of(usersFilePath).toAbsolutePath().normalize();
    }

    private List<XmlAccount> readAccounts(Path path) {
        try {
            if (!Files.exists(path)) {
                return List.of();
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            try (InputStream inputStream = Files.newInputStream(path)) {
                Document document = builder.parse(inputStream);
                NodeList nodes = document.getDocumentElement().getElementsByTagName(ACCOUNT_TAG);
                List<XmlAccount> accounts = new ArrayList<>(nodes.getLength());
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);
                    accounts.add(new XmlAccount(
                            parseId(element.getAttribute("id")),
                            element.getAttribute("login"),
                            element.getAttribute("passwordHash"),
                            element.getAttribute("role"),
                            parseFullName(element.getAttribute("fullName"), element.getAttribute("login"))
                    ));
                }
                return accounts;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read XML user store", ex);
        }
    }

    private List<XmlAccount> normalizeAccounts(List<XmlAccount> accounts) {
        List<XmlAccount> normalized = new ArrayList<>(accounts.size());
        long nextId = 1L;
        for (XmlAccount account : accounts.stream().sorted(Comparator.comparing(XmlAccount::login)).toList()) {
            Long accountId = account.id() == null ? nextId : account.id();
            nextId = Math.max(nextId, accountId + 1);
            String fullName = account.fullName() == null || account.fullName().isBlank() ? account.login() : account.fullName();
            normalized.add(new XmlAccount(accountId, account.login(), account.passwordHash(), account.role(), fullName));
        }
        return normalized;
    }

    private Long parseId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }
        return Long.parseLong(rawId);
    }

    private String parseFullName(String fullName, String login) {
        return fullName == null || fullName.isBlank() ? login : fullName;
    }

    private void writeAccounts(Path path, List<XmlAccount> accounts) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tempDir = parent == null ? Path.of(".") : parent;
        Path tempFile = Files.createTempFile(tempDir, "security-users-", ".xml.tmp");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement(ROOT_TAG);
            document.appendChild(root);

            for (XmlAccount account : accounts) {
                Element accountElement = document.createElement(ACCOUNT_TAG);
                accountElement.setAttribute("id", String.valueOf(account.id()));
                accountElement.setAttribute("login", account.login());
                accountElement.setAttribute("passwordHash", account.passwordHash());
                accountElement.setAttribute("role", account.role());
                accountElement.setAttribute("fullName", account.fullName());
                root.appendChild(accountElement);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            try (OutputStream outputStream = Files.newOutputStream(
                    tempFile,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            )) {
                transformer.transform(new DOMSource(document), new StreamResult(outputStream));
                outputStream.flush();
            }

            try {
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw new IllegalStateException("Failed to write XML user store", ex);
        }
    }
}
