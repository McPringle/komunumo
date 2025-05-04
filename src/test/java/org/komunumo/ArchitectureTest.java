package org.komunumo;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.komunumo", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule jooq_classes_should_only_be_accessed_by_service_layer =
        noClasses()
            .that()
            .resideOutsideOfPackages(
                    "org.komunumo.data.service..",
                    "org.komunumo.data.db.."
            )
            .should()
            .accessClassesThat()
            .resideInAnyPackage("org.komunumo.data.db..")
            .because("only service and jOOQ-generated classes should access the jOOQ model directly");

    @ArchTest
    static final ArchRule dtos_must_be_records =
            classes()
                    .that()
                    .resideInAPackage("..dto..")
                    .should()
                    .beAssignableTo(Record.class)
                    .because("DTOs should be implemented as Java records to ensure immutability and clarity");
}
