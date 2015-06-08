package com.agcocorp.harvestergs.routing

import cucumber.api.junit.Cucumber
import cucumber.api.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber.class)
@CucumberOptions(
    format=["pretty", "html:build/reports/cucumber"]
)
public class RunCukesTest {
//leave me empty!
}

