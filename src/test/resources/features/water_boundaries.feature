@boundary_testing
Feature: Граничные значения для расчета нормы воды
  Как система
  Я должна корректно обрабатывать граничные значения
  Чтобы обеспечивать надежность расчетов

  Background:
    Given API доступен по адресу "/api/status"

  @boundary
  Scenario Outline: Проверка граничных значений веса
    Given я запрашиваю температуру для города "moscow"
    When я отправляю POST запрос на "/api/water/norm" с параметрами:
      | weight          | <weight>         |
      | activityMinutes | 60               |
      | city            | moscow           |
    Then API возвращает статус-код <status_code>
    And поле "waterNorm" равно <expected_norm>

    Examples:
      | weight | status_code | expected_norm |
      | 5      | 200         | 2150.0        |
      | 5.1    | 200         | 653.0         |
      | 249.9  | 200         | 7997.0        |
      | 250    | 200         | 8000.0        |
      | 4.9    | 400         | 0.0           |
      | 250.1  | 400         | 0.0           |

  @boundary
  Scenario Outline: Проверка граничных значений температуры
    Given в городе "<city>" температура <temp>°C
    When я отправляю POST запрос на "/api/water/norm" с параметрами:
      | weight          | 70               |
      | activityMinutes | 60               |
      | city            | <city>           |
    Then API возвращает статус-код 200
    And поле "waterNorm" равно <expected_norm>

    Examples:
      | city        | temp | expected_norm |
      | cold-city   | 29.9 | 2600.0        |
      | border-city | 30.0 | 2600.0        |
      | hot-city    | 30.1 | 3120.0        |
      | hot-city-35 | 35.0 | 3120.0        |