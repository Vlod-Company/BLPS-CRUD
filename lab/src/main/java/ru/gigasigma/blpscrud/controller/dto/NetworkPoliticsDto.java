package ru.gigasigma.blpscrud.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.gigasigma.blpscrud.entity.NetworkPolitics;

import java.util.List;
import java.util.stream.Collectors;

public record NetworkPoliticsDto(
        Long id,

        @NotBlank
        @Size(max = 100)
        String name,

        String description,

        List<String> roles,

        List<AddressDto> addresses
) {

    public static NetworkPoliticsDto fromEntity(NetworkPolitics entity) {
        if (entity == null) {
            return null;
        }

        List<AddressDto> addressDtos = entity.getAddresses() != null
                ? entity.getAddresses().stream()
                .map(AddressDto::fromEntity)
                .collect(Collectors.toList())
                : List.of();

        return new NetworkPoliticsDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getRoles(),
                addressDtos
        );
    }

    public NetworkPolitics toEntity() {
        NetworkPolitics entity = new NetworkPolitics();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setDescription(this.description);

        entity.setRoles(this.roles != null ? List.copyOf(this.roles) : List.of());

        if (this.addresses != null) {
            List<NetworkPolitics.NetworkAddress> addressEntities = this.addresses.stream()
                    .map(addrDto -> addrDto.toEntity(entity))
                    .collect(Collectors.toList());
            entity.setAddresses(addressEntities);
        } else {
            entity.setAddresses(List.of());
        }

        return entity;
    }

    public record AddressDto(
            Long id,

            @NotBlank
            String addr,

            String description
    ) {
        public static AddressDto fromEntity(NetworkPolitics.NetworkAddress entity) {
            if (entity == null) {
                return null;
            }
            return new AddressDto(
                    entity.getId(),
                    entity.getAddr(),
                    entity.getDescription()
            );
        }

        public NetworkPolitics.NetworkAddress toEntity(NetworkPolitics parentPolitics) {
            NetworkPolitics.NetworkAddress entity = new NetworkPolitics.NetworkAddress();
            entity.setId(this.id);
            entity.setAddr(this.addr);
            entity.setDescription(this.description);
            entity.setPolitics(parentPolitics);
            return entity;
        }
    }
}