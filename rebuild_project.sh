#!/bin/bash

echo "🔨 Building from scratch with working template..."

# Fanya clean directory
rm -rf app/src/main/res
rm -f app/build.gradle build.gradle settings.gradle

# Unde minimal working Android project structure
mkdir -p app/src/main/res/{values,layout,drawable}

# 1. Unde app/build.gradle rahisi
cat > app/build.gradle << 'GRADLEEOF'
plugins {
    id 'com.android.application'
}

android {
    compileSdk 33
    namespace 'com.ghosttester.kumbukumbu'

    defaultConfig {
        applicationId "com.ghosttester.kumbukumbu"
        minSdk 21
        targetSdk 33
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        debug {
            minifyEnabled false
        }
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.squareup.okhttp3:okhttp:4.9.3'
}
GRADLEEOF

# 2. Unde build.gradle kuu
cat > build.gradle << 'MAINGRADLEEOF'
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.4.2'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
MAINGRADLEEOF

# 3. Unde settings.gradle
cat > settings.gradle << 'SETTINGSEOF'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MixxSMS"
include ':app'
SETTINGSEOF

# 4. Unde resources rahisi
cat > app/src/main/res/values/strings.xml << 'EOF'
<resources>
    <string name="app_name">Mixx SMS</string>
</resources>
