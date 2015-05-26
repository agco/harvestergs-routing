package com.agcocorp.harvester.routing

import cucumber.api.junit.Cucumber
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith

@RunWith(Cucumber.class)
@CucumberOptions(
        format=["pretty", "html:build/reports/cucumber"],
        tags=["~@manual", "~@review"],
        features=["src/test/cucumber"],
        glue=["src/test/cucumber/steps", "src/test/cucumber/support"]
)
public class RunCukesTest {
//leave me empty!
}

