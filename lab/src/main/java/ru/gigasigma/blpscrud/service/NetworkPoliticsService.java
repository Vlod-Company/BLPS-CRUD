package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.gigasigma.blpscrud.entity.NetworkPolitics;
import ru.gigasigma.blpscrud.repository.NetworkPoliticsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NetworkPoliticsService {

    private final NetworkPoliticsRepository repository;

    public List<NetworkPolitics> findAll() {
        return repository.findAll();
    }

    public NetworkPolitics findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("NetworkPolitics not found with id: " + id));
    }

    @Transactional
    public NetworkPolitics save(NetworkPolitics politics) {
        politics.setId(null);

        if (politics.getAddresses() != null) {
            politics.getAddresses().forEach(addr -> {
                addr.setPolitics(politics);
                addr.setId(null);
            });
        }

        return repository.save(politics);
    }

    @Transactional
    public NetworkPolitics update(Long id, NetworkPolitics politicsDetails) {
        NetworkPolitics existing = findById(id);

        existing.setName(politicsDetails.getName());
        existing.setDescription(politicsDetails.getDescription());

        existing.getRoles().clear();
        if (politicsDetails.getRoles() != null) {
            existing.getRoles().addAll(politicsDetails.getRoles());
        }

        existing.getAddresses().clear();
        if (politicsDetails.getAddresses() != null) {
            for (NetworkPolitics.NetworkAddress addr : politicsDetails.getAddresses()) {
                addr.setPolitics(existing);
                addr.setId(null);
                existing.getAddresses().add(addr);
            }
        }

        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        NetworkPolitics existing = findById(id);

        if ("default".equalsIgnoreCase(existing.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete the 'default' policy");
        }

        repository.delete(existing);
    }

    @Transactional
    public NetworkPolitics patch(Long id, NetworkPolitics politicsDetails) {
        NetworkPolitics existing = findById(id);

        if (politicsDetails.getName() != null) {
            existing.setName(politicsDetails.getName());
        }
        if (politicsDetails.getDescription() != null) {
            existing.setDescription(politicsDetails.getDescription());
        }
        if (politicsDetails.getRoles() != null) {
            existing.getRoles().clear();
            existing.getRoles().addAll(politicsDetails.getRoles());
        }

        if (politicsDetails.getAddresses() != null) {
            existing.getAddresses().clear();
            for (NetworkPolitics.NetworkAddress addr : politicsDetails.getAddresses()) {
                addr.setPolitics(existing);
                addr.setId(null);
                existing.getAddresses().add(addr);
            }
        }

        return repository.save(existing);
    }
}
