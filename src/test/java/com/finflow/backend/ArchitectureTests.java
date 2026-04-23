package com.finflow.backend;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.finflow.backend")
class ArchitectureTests {

    private static final String BASE = "com.finflow.backend";

    @ArchTest
    static final ArchRule domain_entities_must_not_import_spring_core =
            noClasses().that().resideInAPackage(BASE + "..domain.entity..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.stereotype..",
                            "org.springframework.web..",
                            "org.springframework.transaction..",
                            "org.springframework.security.."
                    )
                    .because("Domain entities must not depend on Spring web/DI/tx/security infrastructure");

    @ArchTest
    static final ArchRule controllers_must_not_depend_on_entities =
            noClasses().that().resideInAPackage(BASE + "..presentation.controller..")
                    .should().dependOnClassesThat()
                    .resideInAPackage(BASE + "..domain.entity..")
                    .because("Controllers must only use DTOs, never Entity classes directly");

    @ArchTest
    static final ArchRule usecases_must_not_depend_on_other_usecases =
            noClasses().that().haveSimpleNameEndingWith("UseCase")
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("UseCase")
                    .because("Micro-UseCases rule: UseCases must not call other UseCases directly. Use Spring Events instead!");

    @ArchTest
    static final ArchRule controllers_must_not_depend_on_usecase_classes =
            noClasses().that().resideInAPackage(BASE + "..presentation.controller..")
                    .should().dependOnClassesThat()
                    .resideInAPackage(BASE + "..application.usecase..")
                    .because("Controllers depend on port.in interfaces, not concrete UseCase classes");

    @ArchTest
    static final ArchRule finance_submodules_must_not_use_other_submodule_presentation =
            noClasses().that().resideInAnyPackage(
                            BASE + ".finance.budget.application..",
                            BASE + ".finance.budget.domain..",
                            BASE + ".finance.budget.infrastructure..",
                            BASE + ".finance.transaction.application..",
                            BASE + ".finance.transaction.domain..",
                            BASE + ".finance.transaction.infrastructure..",
                            BASE + ".finance.wealth.application..",
                            BASE + ".finance.wealth.domain..",
                            BASE + ".finance.wealth.infrastructure..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            BASE + ".finance.budget.presentation..",
                            BASE + ".finance.transaction.presentation..",
                            BASE + ".finance.wealth.presentation..")
                    .because("Finance submodules should integrate via application contracts, not presentation types");

    @ArchTest
    static final ArchRule investment_market_data_must_not_depend_on_portfolio_infrastructure =
            noClasses().that().resideInAPackage(BASE + ".investment.market_data..")
                    .should().dependOnClassesThat()
                    .resideInAPackage(BASE + ".investment.portfolio.infrastructure..")
                    .because("Market-data context must not rely on portfolio infrastructure adapters");
}

