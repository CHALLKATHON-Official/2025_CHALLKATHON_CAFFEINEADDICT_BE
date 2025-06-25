# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

If the user's prompt starts with “EP:”, then the user wants to enhance the prompt. Read the PROMPT_ENHANCER.md file and
follow the guidelines to enhance the user's prompt. Show the user the enhancement and get their permission to run it
before taking action on the enhanced prompt. The enhanced prompts will follow the language of the original prompt (e.g.,
Korean prompt input will output Korean prompt enhancements, English prompt input will output English prompt
enhancements, etc.)

## Project Overview

Momento is a Spring Boot 3.5.3 + Kotlin family memory sharing platform with AI-powered question generation. It supports
Kakao OAuth2 authentication and includes an intelligent daily question system for family engagement.

## Key Commands

### Development

```bash
# Start application (local development)
./gradlew bootRun

# Build application
./gradlew build

# Build without tests
./gradlew bootJar

# Clean build
./gradlew clean build
```

### Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage  
./gradlew test jacocoTestReport
```

### Code Quality

```bash
# Check Kotlin code style (if ktlint is configured)
./gradlew ktlintCheck

# Apply Kotlin code formatting (if ktlint is configured)
./gradlew ktlintFormat
```

## Architecture Overview

### Layer Structure

- **Controller Layer**: REST API endpoints (`@RestController`)
- **Service Layer**: Business logic (`@Service`, `@Transactional`)
- **Repository Layer**: Data access (`@Repository`, JPA + QueryDSL)
- **Entity Layer**: Domain models (`@Entity`, Kotlin data classes)

### Key Domain Modules

- **auth**: Authentication & authorization (JWT, OAuth2, Spring Security)
- **domain/user**: User management and family relationships
- **domain/question**: AI-powered question generation system
- **domain/family**: Family grouping and management
- **global**: Cross-cutting concerns (config, exceptions, common base classes)

### AI Question System

The project includes a sophisticated AI question generation system:

- **OpenAI Assistant API integration** for contextual family questions
- **Redis-based 3-tier caching** (cache → pre-generated pool → AI generation)
- **Daily scheduler** for automatic question assignment (9 AM KST)
- **Family context analysis** for personalized questions

## Configuration Profiles

- **local**: Development with local MySQL
- **dev**: Development environment with dev database
- **prod**: Production environment

Environment variables required:

- `SPRING_PROFILES_ACTIVE`: Profile selection
- `JWT_SECRET`: JWT token signing key
- `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`: OAuth2 credentials
- `OPENAI_API_KEY`: For AI question generation
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`: S3 file uploads

## Technology Stack

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.3, Spring Security, Spring Data JPA
- **Database**: MySQL with QueryDSL for complex queries
- **Authentication**: JWT + OAuth2 (Kakao)
- **AI Integration**: OpenAI Assistant API
- **Caching**: Redis for AI question caching
- **File Storage**: AWS S3
- **Documentation**: SpringDoc OpenAPI 3

## Code Conventions

### Kotlin Style

- Use constructor injection for dependencies
- Prefer `val` over `var` for immutability
- Use data classes for DTOs
- Apply `@Transactional` at service layer
- Use companion objects for factory methods

### Naming Patterns

- Controllers: `{Domain}Controller` (e.g., `UserController`)
- Services: `{Domain}Service` (e.g., `FamilyQuestionService`)
- Repositories: `{Entity}Repository` (e.g., `UserRepository`)
- DTOs: `{Action}{Entity}Request/Response` (e.g., `CreateUserRequest`)

### Package Structure

```
com.challkathon.momento/
├── auth/                    # Authentication module
├── domain/                  # Domain-driven modules
│   ├── user/               # User management
│   ├── question/           # AI question system
│   └── family/             # Family management
└── global/                 # Cross-cutting concerns
    ├── config/             # Configuration classes
    ├── exception/          # Global exception handling
    └── common/             # Shared base classes
```

## Development Notes

### Database

- Uses JPA with Hibernate
- QueryDSL for complex queries (generated classes in `build/generated/`)
- Connection pooling via HikariCP

### Security

- JWT tokens for stateless authentication
- OAuth2 integration with Kakao
- CORS configured for frontend integration
- Spring Security configuration in `SecurityConfig.kt`

### API Documentation

- Swagger UI available at `/swagger-ui.html`
- OpenAPI docs at `/v3/api-docs`

### Redis Integration

- Used for AI question caching and session management
- Required for AI question system to function efficiently

### AI Question System

- Automatically generates personalized family questions
- 3-tier caching strategy to minimize OpenAI API costs
- Daily scheduler assigns questions at 9 AM KST
- Categories: MEMORY, DAILY, FUTURE, GRATITUDE