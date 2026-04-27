@water_calculation
Feature: Расчет дневной нормы воды
  Как пользователь системы
  Я хочу получать рекомендации по потреблению воды
  Чтобы поддерживать водный баланс с учетом погоды

  Background:
    Given API доступен по адресу "/api/status"

  @positive_scenarios
  Scenario Outline: Расчет нормы воды для разных городов и веса
    Given я запрашиваю температуру для города "<city>"
    When я отправляю POST запрос на "/api/water/norm" с параметрами:
      | weight          | <weight>         |
      | activityMinutes | 60               |
      | city            | <city>           |
    Then API возвращает статус-код <status_code>
    And рассчитанная норма воды равна <expected_norm>
    And сообщение содержит "<message>"
    And температура в ответе соответствует полученной ранее

    Examples:
      | city      | weight | status_code | expected_norm | message                  |
      | grodno    | 70     | 200         | 3120.0        | Increased due to hot     |
      | minsk     | 70     | 200         | 2600.0        | Normal calculation       |
      | gomel     | 70     | 200         | 2600.0        | Normal calculation       |
      | brest     | 70     | 200         | 3120.0        | Increased due to hot     |

  @negative_scenarios
  Scenario Outline: Проверка валидации веса
    Given я запрашиваю температуру для города "grodno"
    When я отправляю POST запрос на "/api/water/norm" с параметрами:
      | weight          | <weight>         |
      | activityMinutes | 60               |
      | city            | grodno           |
    Then API возвращает статус-код 400
    And рассчитанная норма воды равна 0.0
    And сообщение содержит "<error_message>"

    Examples:
      | weight | error_message          |
      | 2      | between 5 and 250      |
      | 300    | between 5 and 250      |
      | 4.9    | between 5 and 250      |
      | 250.1  | between 5 and 250      |

  @negative_scenarios
  Scenario Outline: Проверка валидации времени активности
    Given я запрашиваю температуру для города "grodno"
    When я отправляю POST запрос на "/api/water/norm" с параметрами:
      | weight          | 70                |
      | activityMinutes | <activity_minutes> |
      | city            | grodno            |
    Then API возвращает статус-код 400
    And рассчитанная норма воды равна 0.0
    And сообщение содержит "cannot be negative"

    Examples:
      | activity_minutes |
      | -1               |
      | -10              |
      | -60              |

  @integration
  Scenario: Интеграция с сервисом погоды
    When я запрашиваю температуру для города "grodno"
    Then температура должна быть в диапазоне от -50 до 50

    When я отправляю POST запрос на "/api/water/norm" с параметрами:
      | weight          | 70    |
      | activityMinutes | 60    |
      | city            | grodno |
    Then API возвращает статус-код 200
    And температура в ответе соответствует полученной ранее
    And статус ответа "success"

  @hot_weather
  Scenario: Проверка увеличения нормы при жаркой погоде
    Given я запрашиваю температуру для города "brest"
    Then температура должна быть в диапазоне от -50 до 50

    When я отправляю POST запрос на "/api/water/norm" с параметрами:
      | weight          | 70    |
      | activityMinutes | 60    |
      | city            | brest |
    Then API возвращает статус-код 200
    And норма увеличена на 20% по сравнению с базовой
    And сообщение содержит "Increased due to hot"