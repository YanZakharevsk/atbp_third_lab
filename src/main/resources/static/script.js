const API_BASE_URL = 'http://localhost:8081';

document.addEventListener('DOMContentLoaded', () => {
    const citySelect = document.getElementById('city');
    const weightInput = document.getElementById('weight');
    const activityInput = document.getElementById('activity');
    const calculateBtn = document.getElementById('calculateBtn');
    const resultDiv = document.getElementById('result');
    const errorDiv = document.getElementById('error');
    const resultText = document.querySelector('.result-text');
    const resultTemp = document.querySelector('.result-temp');

    calculateBtn.addEventListener('click', async () => {
        resultDiv.classList.add('hidden');
        errorDiv.classList.add('hidden');

        // Валидация
        const city = citySelect.value;
        const weight = weightInput.value;
        const activity = activityInput.value;

        if (!city) {
            showError('Пожалуйста, выберите город');
            return;
        }

        if (!weight || weight.trim() === '') {
            showError('Пожалуйста, введите вес');
            return;
        }

        const weightNum = parseFloat(weight);
        if (isNaN(weightNum)) {
            showError('Вес должен быть числом');
            return;
        }

        if (weightNum < 0) {
            showError('Вес не может быть отрицательным');
            return;
        }

        let activityNum = 0;
        if (activity && activity.trim() !== '') {
            activityNum = parseInt(activity);
            if (isNaN(activityNum)) {
                showError('Время активности должно быть числом');
                return;
            }
            if (activityNum < 0) {
                showError('Время активности не может быть отрицательным');
                return;
            }
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/water/norm`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    weight: weightNum,
                    activityMinutes: activityNum,
                    city: city
                })
            });

            const data = await response.json();

            if (response.ok && data.status === 'success') {
                const liters = data.waterNorm / 1000;
                resultText.innerHTML = `Рекомендуемая норма: <strong>${liters.toFixed(1)} литров</strong> (${data.waterNorm} мл)`;
                resultTemp.innerHTML = `🌡️ Температура в городе: ${data.temperature}°C<br>📝 ${data.message}`;
                resultDiv.classList.remove('hidden');
            } else {
                showError(data.message || 'Ошибка при расчете');
            }
        } catch (err) {
            showError('Ошибка соединения с сервером. Убедитесь, что сервер запущен.');
        }
    });

    function showError(message) {
        errorDiv.textContent = message;
        errorDiv.classList.remove('hidden');
    }
});