/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.komunumo;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private final JavaClasses classesWithoutTests = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("org.komunumo");
    private final JavaClasses onlyTests = new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
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
                .check(classesWithoutTests);
    }


    @Test
    void dtos_should_be_records_or_enums() {
        ArchCondition<JavaClass> beRecordOrEnum = new ArchCondition<>("be a record or enum") {
            @Override
            public void check(@NotNull final JavaClass clazz, @NotNull final ConditionEvents events) {
                final var isRecord = clazz.isRecord();
                final var isEnum = clazz.isEnum();

                if (!isRecord && !isEnum) {
                    final var message = clazz.getSimpleName() + " is neither a record nor an enum";
                    events.add(SimpleConditionEvent.violated(clazz, message));
                }
            }
        };

        classes()
                .that()
                .resideInAPackage("..dto..")
                .should(beRecordOrEnum)
                .because("DTOs should be implemented as Java records or enums to ensure immutability and clarity")
                .check(classesWithoutTests);
    }

    @Test
    void forbiddenDateTimeTypes_should_only_be_used_in_ZonedDateTimeConverter() {
        final var forbiddenTypes = Set.of(
                Calendar.class.getName(),
                Date.class.getName(),
                LocalDate.class.getName(),
                LocalDateTime.class.getName(),
                LocalTime.class.getName()
        );
        final var forbiddenTypeList = forbiddenTypes.stream()
                .map(fqn -> fqn.substring(fqn.lastIndexOf('.') + 1))
                .sorted()
                .collect(Collectors.joining(", "));

        final var notDependOnForbiddenDateTimeTypes = new ArchCondition<JavaClass>(
                "not depend on " + forbiddenTypeList) {
            @Override
            public void check(@NotNull JavaClass clazz, @NotNull ConditionEvents events) {
                if (clazz.getFullName().equals("org.komunumo.data.converter.ZonedDateTimeConverter")) {
                    return; // exception for ZonedDateTimeConverter
                }

                clazz.getDirectDependenciesFromSelf().forEach(dependency -> {
                    var targetName = dependency.getTargetClass().getFullName();
                    if (forbiddenTypes.contains(targetName)) {
                        events.add(SimpleConditionEvent.violated(
                                dependency,
                                "Class " + clazz.getName() + " depends on forbidden type: " + targetName
                        ));
                    }
                });
            }
        };

        classes()
                .should(notDependOnForbiddenDateTimeTypes)
                .because("Komunumo needs the timezone information!")
                .check(classesWithoutTests);
    }

    @Test
    void junit4_assertions_should_not_be_used() {
        ArchRule rule = noClasses()
                .should()
                .accessClassesThat()
                .resideInAnyPackage("org.junit")
                .andShould()
                .accessClassesThat()
                .haveSimpleName("Assert")
                .because("only AssertJ should be used for assertions");
        rule.check(onlyTests);
    }

    @Test
    void junit5_assertions_should_not_be_used() {
        ArchRule rule = noClasses()
                .should()
                .accessClassesThat()
                .resideInAnyPackage("org.junit.jupiter.api")
                .andShould()
                .accessClassesThat()
                .haveSimpleName("Assertions")
                .because("only AssertJ should be used for assertions");
        rule.check(onlyTests);
    }

    @Test
    void hamcrest_should_not_be_used() {
        ArchRule rule = noClasses()
                .should()
                .accessClassesThat()
                .resideInAnyPackage("org.hamcrest..")
                .because("Hamcrest matchers should not be used");
        rule.check(onlyTests);
    }

}
