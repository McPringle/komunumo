package org.komunumo;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private final JavaClasses imported = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("org.komunumo");

    @Test
    void jooq_classes_should_only_be_accessed_by_service_layer() {
        noClasses()
                .that()
                .resideOutsideOfPackages("org.komunumo.data.service..", "org.komunumo.data.db..")
                .should()
                .accessClassesThat()
                .resideInAnyPackage("org.komunumo.data.db..")
                .because("only the service layer and jOOQ code should access jOOQ types directly")
                .check(imported);
    }

    @Test
    void dtos_should_be_records() {
        classes()
                .that()
                .resideInAPackage("..dto..")
                .should()
                .beAssignableTo(Record.class)
                .because("DTOs should be implemented as Java records to ensure immutability and clarity")
                .check(imported);
    }
}
