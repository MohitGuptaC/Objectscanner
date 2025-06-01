# Object Scanner

An Android application that scans objects and recommends recipes based on detected ingredients.

## Dataset
This project uses the vegetable image classification dataset from:
> Ahmed, M. I., Mamun, S. M., & Asif, A. U. Z. (2021). DCNN-based vegetable image classification using transfer learning: A comparative study. In 2021 5th International Conference on Computer, Communication and Signal Processing (ICCCSP) (pp. 235-243). IEEE.

## Features

- Object detection using camera
- Recipe recommendations based on detected ingredients
- Integration with Zepto for missing ingredients
- Web view for recipe details and shopping links

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Run the app on an Android device or emulator

## Requirements

- Android Studio Arctic Fox or newer
- Minimum SDK version: Check app/build.gradle.kts for the specific version
- Gradle version: Check gradle/wrapper/gradle-wrapper.properties

## Project Structure

- `app/src/main/java/com/example/objectscanner/` - Main source code
- `app/src/main/res/` - Resources (layouts, strings, etc.)
- `app/src/main/assets/` - Contains dataset.json with recipe information

## Building a Signed APK (Release Build)

To build a signed APK for release:

### For CI/CD (GitHub Actions, etc.):
- The workflow uses environment variables for signing (no changes needed).
- The following block in `app/build.gradle.kts` should be **uncommented**:

   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file(System.getenv("ORG_GRADLE_PROJECT_storeFile"))
           storePassword = System.getenv("ORG_GRADLE_PROJECT_storePassword")
           keyAlias = System.getenv("ORG_GRADLE_PROJECT_keyAlias")
           keyPassword = System.getenv("ORG_GRADLE_PROJECT_keyPassword")
       }
   }
   ```

### For local Android Studio builds:
- **Comment out the entire `signingConfigs` block in `app/build.gradle.kts`.**
- Use Android Studio's **Build > Generate Signed Bundle / APK...** wizard, which will let you pick your keystore and enter credentials interactively. No signing config is needed in the Gradle file for local builds.

3. **Open the project in Android Studio.**
4. **Select the `release` build variant** in the Build Variants panel.
5. Go to **Build > Generate Signed Bundle / APK...** and follow the prompts.
6. The signed APKs will be in `app/build/outputs/apk/release/`.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the BSD 3-Clause License - see the LICENSE file for details.
