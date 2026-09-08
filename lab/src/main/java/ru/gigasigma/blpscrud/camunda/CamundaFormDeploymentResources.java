package ru.gigasigma.blpscrud.camunda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class CamundaFormDeploymentResources extends AbstractProcessEnginePlugin {
    private static final List<String> FORM_NAMES = List.of(
            "flight-search.html",
            "flight-selection.html",
            "payment-link.html"
    );

    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
        if (!(configuration instanceof SpringProcessEngineConfiguration spring)) {
            return;
        }
        Resource[] resources = spring.getDeploymentResources();
        if (resources == null || Arrays.stream(resources).noneMatch(this::isBpmnResource)) {
            return;
        }

        var deploymentResources = new ArrayList<>(Arrays.asList(resources));
        for (String formName : FORM_NAMES) {
            if (deploymentResources.stream().noneMatch(resource -> formName.equals(resource.getFilename()))) {
                deploymentResources.add(namedForm(formName));
            }
        }
        spring.setDeploymentResources(deploymentResources.toArray(Resource[]::new));
    }

    private boolean isBpmnResource(Resource resource) {
        return resource.getFilename() != null && resource.getFilename().endsWith(".bpmn");
    }

    private Resource namedForm(String formName) {
        try (var input = new ClassPathResource("forms/" + formName).getInputStream()) {
            byte[] content = input.readAllBytes();
            return new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return formName;
                }

                @Override
                public String getDescription() {
                    return formName;
                }
            };
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read Camunda form " + formName, exception);
        }
    }
}
