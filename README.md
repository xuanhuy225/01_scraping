# Web Scraping Java Project

A Maven-based Java project for web scraping using JSoup.

## Project Structure

```
01_scraping/
├── pom.xml                 # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/scraping/
│   │   │       └── WebScraper.java    # Main scraping class
│   │   └── resources/              # Application resources
│   └── test/
│       ├── java/
│       │   └── com/example/scraping/
│       │       └── WebScraperTest.java # Unit tests
│       └── resources/              # Test resources
└── README.md
```

## Features

- Web page title extraction
- Link extraction from web pages
- Text content extraction using CSS selectors
- Comprehensive unit tests
- Logging support with SLF4J

## Dependencies

- **JSoup 1.16.1** - HTML parsing and web scraping
- **JUnit Jupiter 5.9.3** - Unit testing framework
- **SLF4J 2.0.7** - Logging framework

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Running the Application

1. Compile the project:
   ```bash
   mvn compile
   ```

2. Run the main class:
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.scraping.WebScraper"
   ```

### Running Tests

```bash
mvn test
```

### Building the Project

```bash
mvn clean package
```

## Usage Examples

```java
WebScraper scraper = new WebScraper();

// Get page title
String title = scraper.getPageTitle("https://example.com");

// Get all links
Elements links = scraper.getLinks("https://example.com");

// Get text by CSS selector
String headingText = scraper.getTextBySelector("https://example.com", "h1");
```

## Development

This project follows standard Maven conventions and uses JSoup for web scraping operations. The code includes error handling and logging for robust web scraping functionality.
