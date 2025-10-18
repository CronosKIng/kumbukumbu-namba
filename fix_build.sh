#!/bin/bash
# fix_android_build.sh - Kurekebisha matatizo ya Gradle na Android SDK

echo "🔧 Kurekebisha Matatizo ya Android Build..."

# 1. Hakikisha gradlew ina ruhusa
echo "1. Kutoa ruhusa za gradlew..."
chmod +x gradlew

# 2. Rekebisa local.properties kwa SDK sahihi
echo "2. Kusasisha local.properties..."
if [ -n "$ANDROID_HOME" ]; then
    echo "sdk.dir=$ANDROI_HOME" > local.properties
    echo "✅ SDK path: $ANDROID_HOME"
elif [ -d "$HOME/Android/Sdk" ]; then
    echo "sdk.dir=$HOME/Android/Sdk" > local.properties
    echo "✅ SDK path: $HOME/Android/Sdk"
else
    echo "sdk.dir=/usr/local/android-sdk" > local.properties
    echo "⚠️  SDK path set to default"
fi

# 3. Sasisha settings.gradle
echo "3. Kusasisha settings.gradle..."
cat > settings.gradle << 'EOF'
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
rootProject.name = "KumbukumbuNamba"
include ':app'
EOF

# 4. Sasisha build.gradle ya project
echo "4. Kusasisha project build.gradle..."
cat > build.gradle << 'EOF'
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
EOF

# 5. Sasisha app build.gradle
echo "5. Kusasisha app build.gradle..."
cat > app/build.gradle << 'EOF'
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

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }

    buildTypes {
        debug {
            minifyEnabled false
            debuggable true
        }
        release {
            minifyEnabled false
        }
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
EOF

# 6. Sasisha Gradle wrapper
echo "6. Kusasisha Gradle wrapper..."
cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-all.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

# 7. Safisha na kujenga tena
echo "7. Kusafisha na kujenga upya..."
./gradlew clean
./gradlew --stop

echo "8. Kujenga project..."
./gradlew assembleDebug --stacktrace --info

if [ $? -eq 0 ]; then
    echo "✅ Urekebishaji umekamilika! Build imefanikiwa!"
else
    echo "❌ Build imeshindwa. Angalia makosa hapo juu."
fi
