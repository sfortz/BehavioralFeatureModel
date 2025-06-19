<style>
r { color: Red }
o { color: Orange }
g { color: Green }
</style>

# Behavioral Feature Models Toolkit

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://maven.apache.org)
<!--- [![License](https://img.shields.io/badge/license-MIT-blue.svg)](#license) -->

This project provides a toolkit for modeling, transforming, and analyzing **Behavioral Feature Models (BFMs)** and related behavioral models. 
BFMs are a novel formalism designed to unify structural and behavioral variability in software product lines (SPLs) within a single compositional model. 
In a BFM, each feature of a traditional feature model is enriched with an associated behavioral specification, and constraints can be expressed between behaviors of different features—allowing the behavior of each product in the SPL to emerge from the composition of its selected features.
Building on our research introducing BFMs, this project includes a suite of translation algorithms, supporting data structures, and converters to enable inter-model transformations across a range of formats, including BFMs, Bundle Event Structures (BESs), Featured Event Structures (FESs), Featured Transition Systems (FTSs), Transition Systems (TSs), and Feature Models (FMs).
It also supports integration with established tools such as <o>UVL and ViBeS</o>, facilitating interoperability within the SPL and behavioral modeling ecosystems.

## 🧩 Features

- Support for core data structures for representing behavioral and structural variability models:
  - **Behavioral Event Structures** (BES)
  - **Behavioral Feature Models** (BFM)
  - **Featured Event Structures** (FES)
  - **Feature Models** (FM) in XML and UVL formats
  - **Featured Transition Systems** (FTS)
  - **Transition Systems** (TS)
- Bidirectional Translation between various formalisms
- IO utilities for loading/saving models in XML (all supported formalisms) and DOT formats (TSs and FTSs only)
- Example models and test cases under `src/main/resources/` and `src/test/resources/`
- Benchmark datasets, Integration <r>and unit tests</r> for validation
- Tool Compatibility: UVL-based feature models, ViBeS TS/FTS support.

## 📁 Project Structure

```bash
.
├── pom.xml                    # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/              # Core Java source code
│   │   │   └── uk/kcl/info/bfm/
│   │   │       ├── *.java     # Model classes and logic
│   │   │       ├── exceptions/
│   │   │       ├── io/xml/    # XML input/output handlers
│   │   │       ├── utils/     # Labeling + translation utilities (contains our algorithm implementations)
│   │   │       └── Main.java  # Optional entry point
│   │   └── resources/         # Sample input files (.bfm, .bes, etc.)
│   └── test/                  
│       ├── java/              
│       │   └── uk/kcl/info/bfm/
│       │       ├── integration/ # Integration test cases
│       │       └── unit/        # Unit test cases
│       └── resources/testcases/
└── target/                    # Compiled output



```


## 🚀 Getting Started

### Prerequisites

- Java 23+
- Maven 4.0+

### Build the project

```bash
mvn clean install
```

### Run (if applicable)

You can run the main class:

```bash
mvn exec:java -Dexec.mainClass="uk.kcl.info.bfm.Main"
java -cp target/your-artifact-name.jar uk.kcl.info.bfm.Main
```

⚠️ The Main class is currently minimal or a placeholder. You can extend it to support command-line transformations or load specific models from XML.
Replace your-artifact-name.jar with the name generated in your POM.

### 🧪 Testing
This project uses JUnit for testing.
To execute all unit and integration tests run:

```bash
mvn test
```

Test cases and input models are located in:
- `src/test/resources/testcases/`
- `src/test/java/uk/kcl/info/bfm/{integration, unit}/`

Each test verifies bidirectional transformations between modeling formats using the provided examples.

### 📦 Input Formats
The resources/ directory includes example files:

#### 📂 Model Examples
Various folders under src/main/resources/ and src/test/resources/ contain example models in different formats. For example:
- *.bfm: Behavioral Feature Models
- *.fes: Featured Event Structures
- *.fts: Featured Transition Systems
- *.ts: Transition Systems
- *.xml and *.uvl: Feature Models
You can modify or extend these examples to fit your use case.

### 📄 License
This project is licensed under the <r>[...]</r> License. See the LICENSE file for details.

### 📚 Acknowledgements

This project is based on the research paper introducing the Behavioral Feature Model formalism.
<r>[...]</r>

### 🤝 Contributing
Contributions are welcome! If you'd like to improve the codebase or add new features/translations:
- Fork the repository.
- Create a new branch.
- Submit a pull request with a clear explanation.

### 📬 Contact
For any inquiries or academic collaboration, please contact [Sophie Fortz](mailto:sophie.fortz@kcl.ac.uk).
