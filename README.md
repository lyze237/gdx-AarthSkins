# gdx-AarthSkins

A libgdx implemented of aartificial's way on loading textures 

[![Build, Test, Publish](https://github.com/lyze237/gdx-AarthSkins/workflows/Test/badge.svg?branch=main)](https://github.com/lyze237/gdx-AarthSkins/actions?query=workflow%3A%22Test%22)
[![License](https://img.shields.io/github/license/lyze237/gdx-AarthSkins)](https://github.com/lyze237/gdx-AarthSkins/blob/main/LICENSE)
[![Jitpack](https://jitpack.io/v/lyze237/gdx-AarthSkins.svg)](https://jitpack.io/#lyze237/gdx-AarthSkins)
[![Donate](https://img.shields.io/badge/Donate-%3C3-red)](https://coffee.lyze.dev)

# How does this work?

[![Youtube Explanation](https://img.youtube.com/vi/HsOKwUwL1bE/0.jpg)](https://www.youtube.com/watch?v=HsOKwUwL1bE)

# Example

![](images/example.png)

| Source file | Map files | Result |

# Installation

1. Open or create `gradle.properties` in the root folder of your project, add the following line:

```properties
gdxAarthSkinsVersion=VERSION
```

Check [Jitpack](https://jitpack.io/#lyze237/gdx-AarthSkins/) for the latest version and replace `VERSION` with that.

2. Add the jitpack repo to your build file.

```groovy
allprojects {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

3. Add that to your core modules dependencies inside your root `build.gradle`

```groovy
project(":core") {
    // ...

    dependencies {
        // ...
        implementation "com.github.lyze237:gdx-AarthSkins:$gdxAarthSkinsVersion"
    }
}
```

## Html/Gwt project

1. Gradle dependency:

```groovy
implementation "com.github.lyze237:gdx-AarthSkins:$gdxAarthSkinsVersion:sources"
```

2. In your application's `.gwt.xml` file add (Normally `GdxDefinition.gwt.xml`):

```xml
<inherits name="dev.lyze.gdxAarthSkin"/>
```

## How to test

By default, if you run `./gradlew test` gradle runs headless tests. If you want to test `lwjgl` tests (so with an actual
gui), then you need to run them with `./gradlew test -Plwjgl=true`

Set environment variable `SLEEPY` to a millisecond number to sleep between each LWJGL test. (For example: SLEEPY=3000 would wait 3 seconds after every test.)
