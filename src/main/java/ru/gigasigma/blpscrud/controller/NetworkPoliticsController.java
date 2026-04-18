package ru.gigasigma.blpscrud.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gigasigma.blpscrud.controller.dto.NetworkPoliticsDto;
import ru.gigasigma.blpscrud.entity.NetworkPolitics;
import ru.gigasigma.blpscrud.service.NetworkPoliticsService;

import java.util.List;

@RestController
@RequestMapping("/api/network-politics")
@RequiredArgsConstructor
public class NetworkPoliticsController {

    private final NetworkPoliticsService politicsService;

    @GetMapping
    public List<NetworkPoliticsDto> getAll() {
        return politicsService.findAll().stream()
                .map(NetworkPoliticsDto::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public NetworkPoliticsDto getById(@PathVariable Long id) {
        return NetworkPoliticsDto.fromEntity(politicsService.findById(id));
    }

    @PostMapping
    public NetworkPoliticsDto create(@Valid @RequestBody NetworkPoliticsDto dto) {
        NetworkPolitics saved = politicsService.save(dto.toEntity());
        return NetworkPoliticsDto.fromEntity(saved);
    }

    @PutMapping("/{id}")
    public NetworkPoliticsDto update(@PathVariable Long id, @Valid @RequestBody NetworkPoliticsDto dto) {
        NetworkPolitics updated = politicsService.update(id, dto.toEntity());
        return NetworkPoliticsDto.fromEntity(updated);
    }

    @PatchMapping("/{id}")
    public NetworkPoliticsDto patch(@PathVariable Long id, @RequestBody NetworkPoliticsDto dto) {
        NetworkPolitics patched = politicsService.patch(id, dto.toEntity());
        return NetworkPoliticsDto.fromEntity(patched);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        politicsService.delete(id);
    }
}