# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

If the user's prompt starts with “EP:”, then the user wants to enhance the prompt. Read the PROMPT_ENHANCER.md file and
follow the guidelines to enhance the user's prompt. Show the user the enhancement and get their permission to run it
before taking action on the enhanced prompt.
The enhanced prompts will follow the language of the original prompt (e.g., Korean prompt input will output Korean
prompt enhancements, English prompt input will output English prompt enhancements, etc.)

## Project Overview

This is a Spring Boot application written in Kotlin for the 2025 CHALLKATHON_CAFFEINEADDICT backend. The project uses:

- **Framework**: Spring Boot 3.5.3 with Kotlin 1.9.25
- **Database**: MySQL with Spring Data JPA
- **Security**: Spring Security with OAuth2 client support
- **Build System**: Gradle with Kotlin DSL
- **Java Version**: 17

## Key Architecture

- **Main Application**: `src/main/kotlin/com/challkathon/caffeine/CaffeineApplication.kt`
- **Package Structure**: `com.challkathon.caffeine`
- **Resources**: `src/main/resources/` (application.yml is currently empty)
- **Database**: MySQL connector configured
- **Security**: OAuth2 client and Spring Security enabled
- **Validation**: Bean validation support included

## Common Commands

### Build and Run

```bash
./gradlew build          # Build the project
./gradlew bootRun        # Run the application
./gradlew clean          # Clean build artifacts
```

### Testing

```bash
./gradlew test           # Run all tests
./gradlew test --tests "ClassName"  # Run specific test class
```

### Development

```bash
./gradlew bootRun        # Run with DevTools (hot reload enabled)
```

## Dependencies

The project includes:

- Spring Boot Starter Web (REST API)
- Spring Boot Starter Data JPA (Database access)
- Spring Boot Starter Security (Authentication/Authorization)
- Spring Boot Starter OAuth2 Client (OAuth2 integration)
- Spring Boot Starter Validation (Bean validation)
- MySQL Connector
- Jackson Kotlin Module (JSON serialization)
- Spring Boot DevTools (Development utilities)

## Database Configuration

- MySQL database expected
- JPA entities should use `@Entity`, `@MappedSuperclass`, or `@Embeddable` annotations
- AllOpen plugin configured for JPA annotations

## Development Notes

- Kotlin compiler configured with JSR-305 strict mode
- JUnit 5 used for testing
- Application configuration in `application.yml` (currently empty)