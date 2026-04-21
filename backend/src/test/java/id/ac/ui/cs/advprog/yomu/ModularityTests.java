package id.ac.ui.cs.advprog.yomu;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTests {

    @Test
    void verifiesModularStructure() {
        ApplicationModules.of(BackendApplication.class).verify();
    }
}
