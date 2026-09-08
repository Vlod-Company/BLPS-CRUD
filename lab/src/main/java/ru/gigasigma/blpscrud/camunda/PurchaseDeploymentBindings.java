package ru.gigasigma.blpscrud.camunda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/** Supplies application bindings for the model without writing to the BPMN file. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class PurchaseDeploymentBindings extends AbstractProcessEnginePlugin {
    public static final String RESOURCE_NAME = "aviasales-camunda7.bpmn";
    public static final String REDIRECT_MESSAGE = "RedirectReceived";

    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
        if (!(configuration instanceof SpringProcessEngineConfiguration spring)) {
            return;
        }
        Resource[] resources = spring.getDeploymentResources();
        if (resources == null) {
            return;
        }
        Resource[] bound = resources.clone();
        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            if (RESOURCE_NAME.equals(resource.getFilename())) {
                try (var input = resource.getInputStream()) {
                    bound[i] = new ByteArrayResource(bind(input.readAllBytes())) {
                        @Override
                        public String getFilename() {
                            return RESOURCE_NAME;
                        }
                    };
                } catch (IOException exception) {
                    throw new IllegalStateException("Cannot read purchase BPMN", exception);
                }
            }
        }
        var deploymentResources = new ArrayList<Resource>(Arrays.asList(bound));
        if (Arrays.stream(resources).anyMatch(resource -> RESOURCE_NAME.equals(resource.getFilename()))) {
            for (String name : List.of("flight-selection.html", "payment-link.html", "flight-search.html")) {
                if (deploymentResources.stream().noneMatch(resource -> name.equals(resource.getFilename()))) {
                    deploymentResources.add(new ClassPathResource("forms/" + name));
                }
            }
        }
        spring.setDeploymentResources(deploymentResources.toArray(Resource[]::new));
    }

    public static byte[] bind(byte[] source) {
        BpmnModelInstance model = Bpmn.readModelFromStream(new ByteArrayInputStream(source));
        MessageEventDefinition redirectEvent = model.getModelElementById("MessageEventDefinition_030ywqz");
        if (redirectEvent.getMessage() == null) {
            Message message = model.newInstance(Message.class);
            message.setId("Message_RedirectReceived");
            message.setName(REDIRECT_MESSAGE);
            model.getDefinitions().addChildElement(message);
            redirectEvent.setMessage(message);
        }
        ServiceTask externalOrder = model.getModelElementById("Activity_00mcqel");
        externalOrder.setCamundaDelegateExpression("${externalPurchaseDelegate}");
        UserTask search = model.getModelElementById("Task_User_Search");
        search.setCamundaFormKey("embedded:deployment:flight-search.html");

        UserTask selection = model.getModelElementById("Task_User_Flight_Select");
        selection.setCamundaFormKey("embedded:deployment:flight-selection.html");
        var selectionExtensions = selection.getExtensionElements();
        if (selectionExtensions == null) {
            selectionExtensions = model.newInstance(ExtensionElements.class);
            selection.setExtensionElements(selectionExtensions);
        }
        for (var form : selectionExtensions.getElementsQuery()
                .filterByType(CamundaFormData.class).list()) {
            selectionExtensions.removeChildElement(form);
        }
        boolean hasValidation = selectionExtensions.getElementsQuery().filterByType(CamundaTaskListener.class)
                .list().stream().anyMatch(listener -> "complete".equals(listener.getCamundaEvent())
                        && "${validatePurchaseFormListener}".equals(listener.getCamundaDelegateExpression()));
        if (!hasValidation) {
            CamundaTaskListener listener = model.newInstance(CamundaTaskListener.class);
            listener.setCamundaEvent("complete");
            listener.setCamundaDelegateExpression("${validatePurchaseFormListener}");
            selectionExtensions.addChildElement(listener);
        }

        UserTask redirectTask = model.getModelElementById("Activity_0o8m1af");
        redirectTask.setCamundaFormKey("embedded:deployment:payment-link.html");
        UserTask paymentTask = model.getModelElementById("Task_User_Payment_Redirect");
        paymentTask.setCamundaFormKey("embedded:deployment:payment-link.html");
        var extensions = redirectTask.getExtensionElements();
        if (extensions == null) {
            extensions = model.newInstance(ExtensionElements.class);
            redirectTask.setExtensionElements(extensions);
        }
        boolean hasOwnerListener = extensions.getElementsQuery().filterByType(CamundaTaskListener.class)
                .list().stream().anyMatch(listener -> "create".equals(listener.getCamundaEvent())
                        && "${assignProcessOwnerTaskListener}".equals(listener.getCamundaDelegateExpression()));
        if (!hasOwnerListener) {
            CamundaTaskListener listener = model.newInstance(CamundaTaskListener.class);
            listener.setCamundaEvent("create");
            listener.setCamundaDelegateExpression("${assignProcessOwnerTaskListener}");
            extensions.addChildElement(listener);
        }
        var output = new ByteArrayOutputStream();
        Bpmn.writeModelToStream(output, model);
        return output.toByteArray();
    }
}
