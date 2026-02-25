# Budget Planner

Десктопное приложение для планирования бюджета с графическим интерфейсом на Swing и использованием Spring Boot.

## Описание проекта

Budget Planner - это приложение для управления личными финансами, которое позволяет:
- Добавлять доходы и расходы с указанием даты и комментария
- Отслеживать баланс на любую дату
- Редактировать и удалять записи о финансовых операциях
- Сохранять данные в локальной базе данных H2

## Технологический стек

- **Java 21** - основной язык разработки
- **Spring Boot 3.5.7** - фреймворк для DI и управления приложением
- **Spring Data JPA** - для работы с базой данных
- **H2 Database** - встраиваемая база данных
- **Swing** - GUI фреймворк для десктопного интерфейса
- **MigLayout** - менеджер компоновки для Swing
- **FlatLaf** - современный look and feel для Swing
- **JXDatePicker** - компонент для выбора дат
- **Lombok** - для сокращения boilerplate кода
- **JUnit 5** - для тестирования

## Архитектура

Приложение построено по принципам Clean Architecture с разделением на слои:

- **Domain Layer**: `Planner`, `Story`, `BudgetChange` - основные бизнес-сущности
- **Use Cases**: `AddStoryUseCase`, `GetPlannerUseCase`, `CalculateBalanceForDateUseCase` и др. - бизнес-логика
- **Infrastructure**: `PlannerRepository` - работа с данными
- **UI**: `BudgetSwingUI` - пользовательский интерфейс

## Структура проекта

```
src/main/java/ru/sportmaster/mpadapter/
├── Bootstrap.java              # Главный класс приложения
├── BudgetSwingUI.java          # GUI интерфейс
├── Planner.java                # Основная сущность планировщика
├── Story.java                  # Сущность финансовой записи
├── BudgetChange.java           # Сущность изменения бюджета
├── PlannerRepository.java      # Репозиторий для работы с БД
├── *UseCase.java               # Классы бизнес-логики
└── *Id.java, *Name.java        # Value objects
```

## Запуск приложения

### Требования
- Java 21 или выше
- Gradle 7.0 или выше

### Сборка и запуск
```bash
# Клонирование репозитория
git clone <repository-url>
cd budget_planner

# Сборка проекта
./gradlew build

# Запуск приложения
./gradlew bootRun
```

Или запустить напрямую через IDE класс `Bootstrap.java`.

## Использование

1. **Добавление записи**: Введите сумму (положительную для дохода, отрицательную для расхода), выберите дату и добавьте комментарий. Нажмите "Добавить".

2. **Просмотр баланса**: Выберите дату в календаре баланса, чтобы увидеть суммарный баланс на эту дату.

3. **Редактирование**: Выделите запись в таблице и измените её параметры в полях ввода.

4. **Удаление**: Выделите запись и нажмите кнопку "Удалить".

## База данных

Приложение использует встраиваемую базу данных H2, которая автоматически создается при первом запуске. Файл БД сохраняется в корне проекта как `budget_planner.mv.db`.

## Тестирование

```bash
# Запуск тестов
./gradlew test
```

## Особенности реализации

- Приложение запускается без веб-сервера (`WebApplicationType.NONE`)
- Используется режим `headless(false)` для поддержки GUI
- Все операции с данными выполняются через Use Cases для чистоты архитектуры
- Поддержка валидации данных через Bean Validation
- Автоматическое сохранение данных при изменениях через JPA

## Лицензия

Проект распространяется под лицензией MIT.

---

# Budget Planner (English)

A desktop budget planning application with Swing GUI and Spring Boot backend.

## Project Overview

Budget Planner is a personal finance management application that allows you to:
- Add income and expenses with date and comments
- Track balance for any specific date
- Edit and delete financial transaction records
- Save data to local H2 database

## Technology Stack

- **Java 21** - main development language
- **Spring Boot 3.5.7** - framework for DI and application management
- **Spring Data JPA** - for database operations
- **H2 Database** - embedded database
- **Swing** - GUI framework for desktop interface
- **MigLayout** - layout manager for Swing
- **FlatLaf** - modern look and feel for Swing
- **JXDatePicker** - date picker component
- **Lombok** - for reducing boilerplate code
- **JUnit 5** - for testing

## Architecture

The application follows Clean Architecture principles with layer separation:

- **Domain Layer**: `Planner`, `Story`, `BudgetChange` - core business entities
- **Use Cases**: `AddStoryUseCase`, `GetPlannerUseCase`, `CalculateBalanceForDateUseCase` etc. - business logic
- **Infrastructure**: `PlannerRepository` - data operations
- **UI**: `BudgetSwingUI` - user interface

## Project Structure

```
src/main/java/ru/sportmaster/mpadapter/
├── Bootstrap.java              # Main application class
├── BudgetSwingUI.java          # GUI interface
├── Planner.java                # Main planner entity
├── Story.java                  # Financial record entity
├── BudgetChange.java           # Budget change entity
├── PlannerRepository.java      # Database repository
├── *UseCase.java               # Business logic classes
└── *Id.java, *Name.java        # Value objects
```

## Running the Application

### Requirements
- Java 21 or higher
- Gradle 7.0 or higher

### Build and Run
```bash
# Clone repository
git clone <repository-url>
cd budget_planner

# Build project
./gradlew build

# Run application
./gradlew bootRun
```

Or run directly from IDE using the `Bootstrap.java` class.

## Usage

1. **Add Record**: Enter amount (positive for income, negative for expense), select date and add comment. Click "Add".

2. **View Balance**: Select date in balance calendar to see total balance for that date.

3. **Edit**: Select record in table and modify its parameters in input fields.

4. **Delete**: Select record and click "Delete" button.

## Database

The application uses embedded H2 database that is automatically created on first run. Database file is saved in project root as `budget_planner.mv.db`.

## Testing

```bash
# Run tests
./gradlew test
```

## Implementation Features

- Application runs without web server (`WebApplicationType.NONE`)
- Uses `headless(false)` mode for GUI support
- All data operations performed through Use Cases for clean architecture
- Bean Validation support for data validation
- Automatic data saving through JPA on changes

## License

Project is distributed under MIT license.
