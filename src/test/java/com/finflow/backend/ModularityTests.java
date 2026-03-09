package com.finflow.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

    @Test
    void verifyModularStructure() {
        ApplicationModules modules = ApplicationModules.of(FinFlowBackendApplication.class);
        modules.verify();
    }
    @Test
    void writeDocumentationSnippets() {
        var modules = ApplicationModules.of(FinFlowBackendApplication.class);

        new Documenter(modules)
                .writeModulesAsPlantUml() // Sơ đồ tổng quan toàn bộ hệ thống
                .writeIndividualModulesAsPlantUml() // Sơ đồ chi tiết cho từng Module
                .writeModuleCanvases(); // Sinh ra file Markdown mô tả các Event, Bean...
    }
}

