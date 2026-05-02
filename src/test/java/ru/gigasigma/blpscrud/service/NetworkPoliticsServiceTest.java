package ru.gigasigma.blpscrud.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gigasigma.blpscrud.entity.NetworkPolitics;
import ru.gigasigma.blpscrud.repository.NetworkPoliticsRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkPoliticsServiceTest {

    @Mock
    private NetworkPoliticsRepository repository;

    @InjectMocks
    private NetworkPoliticsService service;

    @Test
    void saveAssignsParentPolicyToAddresses() {
        NetworkPolitics.NetworkAddress address = new NetworkPolitics.NetworkAddress();
        address.setId(7L);
        address.setAddr("0.0.0.0/0");

        NetworkPolitics politics = new NetworkPolitics();
        politics.setId(99L);
        politics.setName("test");
        politics.setRoles(List.of("ROLE_USER"));
        politics.setAddresses(List.of(address));

        when(repository.save(politics)).thenReturn(politics);

        service.save(politics);

        ArgumentCaptor<NetworkPolitics> captor = ArgumentCaptor.forClass(NetworkPolitics.class);
        verify(repository).save(captor.capture());

        NetworkPolitics saved = captor.getValue();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getAddresses()).hasSize(1);
        assertThat(saved.getAddresses().get(0).getPolitics()).isSameAs(saved);
        assertThat(saved.getAddresses().get(0).getId()).isNull();
    }
}
