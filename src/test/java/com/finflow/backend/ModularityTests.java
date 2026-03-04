package com.finflow.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void verifyModularStructure() {
        ApplicationModules modules = ApplicationModules.of(FinFlowBackendApplication.class);
        modules.verify();
    }
}

