# Contributing to Flixio

Thank you for your interest in contributing to Flixio! Flixio is an open-source media application built with Kotlin Multiplatform and Compose Multiplatform.

The project is currently maintained by [Vivek Kumar](https://github.com/VivxKumar07). Community contributions, bug reports, and suggestions are welcome.

---

## How Can You Contribute?

### 1. Reporting Bugs
If you encounter a bug or unexpected behavior:
- Check existing [GitHub Issues](https://github.com/VivxKumar07/Flixio/issues) to ensure the bug has not already been reported.
- If it has not been reported, open a new **Bug Report** using the issue template.
- Please provide:
  - Your device model and Android version.
  - The app version and build variant (e.g. `0.4.25 fullDebug`).
  - Clear, step-by-step instructions to reproduce the issue.
  - Relevant logs or screenshots (especially for visual glitches or crashes).

### 2. Suggesting Features & Enhancements
Feature suggestions and improvements are always appreciated:
- Open a new **Feature Request** on GitHub Issues.
- Describe the feature, why it is useful, and how you envision it working within Flixio's design language.

### 3. Submitting Pull Requests
We welcome pull requests for bug fixes, performance improvements, localization, and approved features.

To contribute code:
1. **Fork** the repository and create your branch from `cmp-rewrite`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Make your changes**:
   - Keep your code clean, well-structured, and consistent with the existing Kotlin and Compose Multiplatform architecture.
   - Avoid bundling unrelated changes into a single pull request.
3. **Verify the build**:
   - Ensure the app compiles and builds successfully before submitting:
     ```bash
     ./gradlew :composeApp:compileAndroidMain --no-configuration-cache
     ./gradlew :androidApp:assembleFullDebug "-Pnuvio.android.distribution=full" --no-configuration-cache
     ```
4. **Submit your Pull Request**:
   - Open a PR against the `cmp-rewrite` branch.
   - Provide a clear summary of what changed and why.
   - Include before-and-after screenshots or screen recordings for any UI changes.

---

## Code of Conduct

Please maintain a friendly, constructive, and respectful environment when communicating on issues and pull requests.
