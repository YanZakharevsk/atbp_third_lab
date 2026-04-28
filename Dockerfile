FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

# Устанавливаем Node.js 18
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean

# Копируем файлы проекта
COPY . .

# Устанавливаем зависимости
RUN mvn dependency:go-offline -B
RUN npm ci

# Устанавливаем Playwright браузеры
RUN npx playwright install chromium firefox --with-deps

# Собираем JAR
RUN mvn clean package -DskipTests

# Запускаем сервер и тесты
CMD ["sh", "-c", "java -jar target/*.jar & sleep 15 && mvn test && npm run test:ui"]