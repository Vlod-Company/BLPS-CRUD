package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

class DeploymentBindingsTest {
    @Test
    void addsFormsOnceWithoutChangingSourceResource() throws Exception {
        var source = new ClassPathResource("processes/" + PurchaseDeploymentBindings.RESOURCE_NAME);
        byte[] original;
        try (var stream = source.getInputStream()) {
            original = stream.readAllBytes();
        }
        var configuration = new SpringProcessEngineConfiguration();
        configuration.setDeploymentResources(new Resource[] {source});
        var plugin = new PurchaseDeploymentBindings();
        plugin.preInit(configuration);
        plugin.preInit(configuration);

        assertThat(configuration.getDeploymentResources()).extracting(Resource::getFilename)
                .containsExactly(PurchaseDeploymentBindings.RESOURCE_NAME, "flight-selection.html", "payment-link.html", "flight-search.html");
        for (Resource resource : configuration.getDeploymentResources()) {
            try (var stream = resource.getInputStream()) {
                assertThat(stream.readAllBytes()).isNotEmpty();
            }
        }
        try (var stream = source.getInputStream()) {
            assertThat(stream.readAllBytes()).isEqualTo(original);
        }
    }
}
