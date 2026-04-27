class WaterPage {
    constructor(page) {
        this.page = page;

        // Селекторы
        this.citySelect = '#city';
        this.weightInput = '#weight';
        this.activityInput = '#activity';
        this.calculateBtn = '#calculateBtn';
        this.resultDiv = '#result';
        this.resultText = '.result-text';
        this.resultTemp = '.result-temp';
        this.errorDiv = '#error';
    }

    async navigate() {
        await this.page.goto('/');
    }

    async selectCity(city) {
        await this.page.selectOption(this.citySelect, city);
    }

    async setWeight(weight) {
        await this.page.fill(this.weightInput, String(weight));
    }

    async setActivity(minutes) {
        await this.page.fill(this.activityInput, String(minutes));
    }

    async clickCalculate() {
        await this.page.click(this.calculateBtn);
    }

    async getResultText() {
        await this.page.waitForSelector(this.resultDiv, { state: 'visible' });
        return this.page.textContent(this.resultText);
    }

    async getResultTemp() {
        await this.page.waitForSelector(this.resultDiv, { state: 'visible' });
        return this.page.textContent(this.resultTemp);
    }

    async getErrorText() {
        await this.page.waitForSelector(this.errorDiv, { state: 'visible' });
        return this.page.textContent(this.errorDiv);
    }

    async isResultVisible() {
        return this.page.locator(this.resultDiv).isVisible();
    }

    async isErrorVisible() {
        return this.page.locator(this.errorDiv).isVisible();
    }

    async getWaterNormInLiters() {
        const text = await this.getResultText();
        const match = text.match(/(\d+\.?\d*)\s+литров/);
        return match ? parseFloat(match[1]) : null;
    }
}

module.exports = WaterPage;