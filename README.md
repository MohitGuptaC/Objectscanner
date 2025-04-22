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

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the GNU General Public License v3.0 (GPL-3.0) - see the LICENSE file for details. This means that any derivative works must also be distributed under the same license terms. 