package com.atbp.lab3;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "src/test/resources/features",
		glue = "com.atbp.lab3.steps",
		plugin = {
				"pretty",
				"html:target/cucumber-reports/cucumber.html",
				"json:target/cucumber-reports/cucumber.json",
				"junit:target/cucumber-reports/cucumber.xml"
		},
		monochrome = true,
		tags = "@water_calculation"
)
public class RunCucumberTest {
	// Этот класс запускает Cucumber тесты
}
