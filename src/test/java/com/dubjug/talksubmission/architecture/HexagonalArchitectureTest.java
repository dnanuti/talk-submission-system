package com.dubjug.talksubmission.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.dubjug.talksubmission");
    }

    @Test
    void domainDoesNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .as("Domain must not depend on Spring")
                .check(classes);
    }

    @Test
    void domainDoesNotDependOnJPA() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .as("Domain must not depend on JPA")
                .check(classes);
    }

    @Test
    void domainDoesNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .as("Domain must not depend on infrastructure adapters")
                .check(classes);
    }

    @Test
    void applicationDoesNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .as("Application layer must not depend on infrastructure")
                .check(classes);
    }

    @Test
    void applicationDoesNotDependOnSpringFramework() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .as("Application layer must not depend on Spring")
                .check(classes);
    }
}
