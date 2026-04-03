package ru.gigasigma.blpscrud.security;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
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
            if (Files.exists(path)) {
                return;
            }
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            writeAccounts(path, List.of(new XmlAccount("admin", passwordEncoder.encode("admin"), "ROLE_ADMIN")));
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

    public boolean existsByLogin(String login) {
        return findByLogin(login).isPresent();
    }

    public void createUser(String login, String passwordHash, String role) {
        lock.writeLock().lock();
        try {
            Path path = resolvePath();
            List<XmlAccount> accounts = new ArrayList<>(readAccounts(path));
            if (accounts.stream().anyMatch(account -> account.login().equals(login))) {
                throw new IllegalArgumentException("Login is already taken");
            }
            accounts.add(new XmlAccount(login, passwordHash, role));
            accounts.sort(Comparator.comparing(XmlAccount::login));
            writeAccounts(path, accounts);
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
                            element.getAttribute("login"),
                            element.getAttribute("passwordHash"),
                            element.getAttribute("role")
                    ));
                }
                return accounts;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read XML user store", ex);
        }
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
                accountElement.setAttribute("login", account.login());
                accountElement.setAttribute("passwordHash", account.passwordHash());
                accountElement.setAttribute("role", account.role());
                root.appendChild(accountElement);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            try (FileChannel channel = FileChannel.open(
                    tempFile,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            )) {
                try (OutputStream outputStream = Channels.newOutputStream(channel)) {
                    transformer.transform(new DOMSource(document), new StreamResult(outputStream));
                    outputStream.flush();
                }
                channel.force(true);
            }

            try {
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {}
            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw new IllegalStateException("Failed to write XML user store", ex);
        }
    }
}