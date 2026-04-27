package com.atbp.lab3.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClientException;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
public class WaterSteps {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private ResponseEntity<Map> response;
    private String currentCity;
    private Double currentTemperature;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Given("API доступен по адресу {string}")
    public void apiIsAvailable(String path) {
        String url = getBaseUrl() + path;

        try {
            response = restTemplate.getForEntity(url, Map.class);

            assertThat("Статус-код должен быть 200",
                    response.getStatusCode(), equalTo(HttpStatus.OK));

            Map<String, Object> body = response.getBody();
            assertThat("Статус должен быть online",
                    body.get("status"), equalTo("online"));

            System.out.println("API доступен на " + url);
            System.out.println("Ответ: " + body);
        } catch (RestClientException e) {
            throw new RuntimeException("API недоступен: " + url, e);
        }
    }

    @Given("я запрашиваю температуру для города {string}")
    public void getTemperatureForCity(String city) {
        currentCity = city;
        String url = getBaseUrl() + "/api/weather/" + city;

        response = restTemplate.getForEntity(url, Map.class);

        assertThat("Статус-код должен быть 200",
                response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> body = response.getBody();
        currentTemperature = (Double) body.get("temperature");

        System.out.println("Температура в городе " + city + ": " + currentTemperature + "°C");
        System.out.println("Полный ответ: " + body);
    }

    @When("я отправляю POST запрос на {string} с параметрами:")
    public void sendPostRequest(String path, Map<String, String> params) {
        String url = getBaseUrl() + path;

        Map<String, Object> requestBody = Map.of(
                "weight", Double.parseDouble(params.get("weight")),
                "activityMinutes", Integer.parseInt(params.get("activityMinutes")),
                "city", params.get("city")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        System.out.println("POST запрос на " + url);
        System.out.println("Тело: " + requestBody);

        response = restTemplate.postForEntity(url, request, Map.class);

        System.out.println("Статус: " + response.getStatusCodeValue());
        System.out.println("Ответ: " + response.getBody());
    }

    @Then("API возвращает статус-код {int}")
    public void verifyStatusCode(int expectedCode) {
        int actualCode = response.getStatusCodeValue();
        assertThat("Статус-код не соответствует",
                actualCode, equalTo(expectedCode));
        System.out.println("Статус-код: " + actualCode);
    }

    @Then("рассчитанная норма воды равна {double}")
    public void verifyWaterNorm(double expectedNorm) {
        Map<String, Object> body = response.getBody();

        if (response.getStatusCode().is2xxSuccessful()) {
            double actualNorm = (Double) body.get("waterNorm");
            assertThat("Норма воды рассчитана неверно",
                    actualNorm, equalTo(expectedNorm));
            System.out.println("Норма воды: " + actualNorm + " мл (ожидалось: " + expectedNorm + " мл)");
        } else {
            double actualNorm = (Double) body.get("waterNorm");
            assertThat("При ошибке норма воды должна быть 0", actualNorm, equalTo(0.0));
        }
    }

    @Then("сообщение содержит {string}")
    public void verifyMessage(String expectedMessagePart) {
        Map<String, Object> body = response.getBody();
        String actualMessage = (String) body.get("message");

        assertThat("Сообщение не содержит ожидаемый текст",
                actualMessage, containsString(expectedMessagePart));
        System.out.println("Сообщение содержит: \"" + expectedMessagePart + "\"");
    }

    @Then("температура в ответе соответствует полученной ранее")
    public void verifyTemperatureMatches() {
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = response.getBody();
            double responseTemp = (Double) body.get("temperature");

            assertThat("Температура не совпадает",
                    responseTemp, equalTo(currentTemperature));
            System.out.println("Температура совпадает: " + responseTemp + "°C");
        }
    }

    @Then("температура должна быть в диапазоне от {int} до {int}")
    public void verifyTemperatureRange(int min, int max) {
        assertThat("Температура вне диапазона",
                currentTemperature,
                both(greaterThanOrEqualTo((double)min))
                        .and(lessThanOrEqualTo((double)max)));
        System.out.println("Температура " + currentTemperature + "°C в диапазоне [" + min + ", " + max + "]");
    }

    @Then("норма увеличена на 20% по сравнению с базовой")
    public void verifyIncreasedNorm() {
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = response.getBody();
            double weight = 70;
            double activityMinutes = 60;

            double baseNorm = 30 * weight + (activityMinutes / 60.0) * 500;
            double actualNorm = (Double) body.get("waterNorm");
            double expectedIncreasedNorm = Math.round(baseNorm * 1.2 * 10) / 10.0;

            assertThat("Норма должна быть увеличена на 20%",
                    actualNorm, equalTo(expectedIncreasedNorm));

            System.out.println("Базовая норма: " + baseNorm + " мл");
            System.out.println("Увеличенная норма: " + actualNorm + " мл");
        }
    }

    @Then("статус ответа {string}")
    public void verifyStatus(String expectedStatus) {
        Map<String, Object> body = response.getBody();
        String actualStatus = (String) body.get("status");

        assertThat("Статус ответа не соответствует",
                actualStatus, equalTo(expectedStatus));
        System.out.println("Статус ответа: " + actualStatus);
    }
}