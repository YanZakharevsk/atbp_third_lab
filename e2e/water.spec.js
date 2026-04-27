const { test, expect } = require('@playwright/test');
const WaterPage = require('./pages/WaterPage');

test.describe('Калькулятор нормы воды', () => {
    let waterPage;

    test.beforeEach(async ({ page }) => {
        waterPage = new WaterPage(page);
        await waterPage.navigate();
    });

    // ========== ПОЗИТИВНЫЕ СЦЕНАРИИ ==========

    test('Жаркая погода (Гродно, 35°C) - норма увеличена на 20%', async () => {
        await waterPage.selectCity('grodno');
        await waterPage.setWeight(70);
        await waterPage.setActivity(60);
        await waterPage.clickCalculate();

        const liters = await waterPage.getWaterNormInLiters();
        // Сервер округляет до 1 знака: 3120 мл = 3.1 л
        expect(liters).toBeCloseTo(3.1, 1);

        const tempText = await waterPage.getResultTemp();
        expect(tempText).toContain('35°C');
    });

    test('Экстремальная жара (Брест, 40°C) - норма увеличена на 20%', async () => {
        await waterPage.selectCity('brest');
        await waterPage.setWeight(70);
        await waterPage.setActivity(60);
        await waterPage.clickCalculate();

        const liters = await waterPage.getWaterNormInLiters();
        expect(liters).toBeCloseTo(3.1, 1);
    });

    // ========== ПАРАМЕТРИЗОВАННЫЕ ТЕСТЫ (Data-Driven) ==========

    const weightTestCases = [
        { city: 'grodno', weight: 50, expectedLiters: 2.4 },   // 2400 мл = 2.4 л
        { city: 'grodno', weight: 80, expectedLiters: 3.5 },   // 3480 мл = 3.5 л (округление)
        { city: 'minsk', weight: 90, expectedLiters: 3.2 },    // 3200 мл = 3.2 л
        { city: 'minsk', weight: 50, expectedLiters: 2.0 },    // 2000 мл = 2.0 л
        { city: 'minsk', weight: 100, expectedLiters: 3.5 },   // 3500 мл = 3.5 л
    ];

    for (const testCase of weightTestCases) {
        test(`Вес ${testCase.weight} кг, город ${testCase.city} -> норма ${testCase.expectedLiters} л`, async () => {
            await waterPage.selectCity(testCase.city);
            await waterPage.setWeight(testCase.weight);
            await waterPage.setActivity(60);
            await waterPage.clickCalculate();

            const liters = await waterPage.getWaterNormInLiters();
            expect(liters).toBeCloseTo(testCase.expectedLiters, 1);
        });
    }

    // Разное время активности
    const activityTestCases = [
        { city: 'minsk', weight: 70, activity: 0, expectedLiters: 2.1 },   // 2100 мл = 2.1 л
        { city: 'minsk', weight: 70, activity: 30, expectedLiters: 2.4 },   // 2350 мл = 2.4 л (округление)
        { city: 'minsk', weight: 70, activity: 120, expectedLiters: 3.1 },  // 3100 мл = 3.1 л
        { city: 'grodno', weight: 70, activity: 0, expectedLiters: 2.5 },   // 2520 мл = 2.5 л (округление)
        { city: 'grodno', weight: 70, activity: 120, expectedLiters: 3.7 }, // 3720 мл = 3.7 л (округление)
    ];

    for (const testCase of activityTestCases) {
        test(`Активность ${testCase.activity} мин, город ${testCase.city} -> норма ${testCase.expectedLiters} л`, async () => {
            await waterPage.selectCity(testCase.city);
            await waterPage.setWeight(testCase.weight);
            await waterPage.setActivity(testCase.activity);
            await waterPage.clickCalculate();

            const liters = await waterPage.getWaterNormInLiters();
            expect(liters).toBeCloseTo(testCase.expectedLiters, 1);
        });
    }

    // ========== НЕГАТИВНЫЕ СЦЕНАРИИ ==========

    const invalidWeightCases = [
        { weight: 0, error: 'between 5 and 250' },
        { weight: 1, error: 'between 5 and 250' },
        { weight: 4, error: 'between 5 and 250' },
        { weight: 251, error: 'between 5 and 250' },
        { weight: 300, error: 'between 5 and 250' },
        { weight: 500, error: 'between 5 and 250' },
    ];

    for (const testCase of invalidWeightCases) {
        test(`Недопустимый вес ${testCase.weight} кг - ошибка`, async () => {
            await waterPage.selectCity('grodno');
            await waterPage.setWeight(testCase.weight);
            await waterPage.setActivity(60);
            await waterPage.clickCalculate();

            const error = await waterPage.getErrorText();
            expect(error).toContain(testCase.error);
        });
    }

    test('Отрицательный вес - ошибка на фронтенде', async () => {
        await waterPage.selectCity('grodno');
        await waterPage.setWeight(-50);
        await waterPage.setActivity(60);
        await waterPage.clickCalculate();

        const error = await waterPage.getErrorText();
        expect(error).toContain('Вес не может быть отрицательным');
    });

    test('Город не выбран - ошибка', async () => {
        await waterPage.setWeight(70);
        await waterPage.setActivity(60);
        await waterPage.clickCalculate();

        const error = await waterPage.getErrorText();
        expect(error).toContain('выберите город');
    });

    test('Пустое поле веса - ошибка', async () => {
        await waterPage.selectCity('grodno');
        await waterPage.setWeight('');
        await waterPage.clickCalculate();

        const error = await waterPage.getErrorText();
        expect(error).toContain('введите вес');
    });

    test('Отрицательное время активности - ошибка', async () => {
        await waterPage.selectCity('grodno');
        await waterPage.setWeight(70);
        await waterPage.setActivity(-30);
        await waterPage.clickCalculate();

        const error = await waterPage.getErrorText();
        expect(error).toContain('отрицательным');
    });
});