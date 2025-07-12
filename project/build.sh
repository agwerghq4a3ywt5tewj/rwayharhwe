#!/bin/bash

# Fallen God Testament - Build Script
# This script compiles the plugin without requiring Maven in WebContainer

echo "🏗️  Building Fallen God Testament Plugin..."

# Create necessary directories
mkdir -p target/classes
mkdir -p target/lib

echo "📁 Created build directories"

# Download Paper API if not present
if [ ! -f "target/lib/paper-api-1.21.5.jar" ]
then
    echo "📥 Downloading Paper API..."
    curl -o target/lib/paper-api-1.21.5.jar "https://api.papermc.io/v2/projects/paper/versions/1.21.5/builds/114/downloads/paper-1.21.5-114.jar"
fi

# Download SnakeYAML if not present
if [ ! -f "target/lib/snakeyaml-2.2.jar" ]
then
    echo "📥 Downloading SnakeYAML..."
    curl -o target/lib/snakeyaml-2.2.jar "https://repo1.maven.org/maven2/org/yaml/snakeyaml/2.2/snakeyaml-2.2.jar"
fi

echo "📚 Dependencies ready"

# Find all Java source files
JAVA_FILES=$(find src/main/java -name "*.java" | tr '\n' ' ')

echo "☕ Compiling Java sources..."

# Compile Java sources
javac -cp "target/lib/*" -d target/classes $JAVA_FILES

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✅ Java compilation successful"

# Copy resources
echo "📋 Copying resources..."
cp -r src/main/resources/* target/classes/

# Create JAR file
echo "📦 Creating JAR file..."
cd target/classes
jar cf ../testament-1.7.0.jar .
cd ../..

# Extract and include SnakeYAML in the JAR
echo "🔗 Including dependencies..."
cd target
mkdir -p temp
cd temp
jar xf ../lib/snakeyaml-2.2.jar
# Remove signature files to avoid conflicts
rm -rf META-INF/*.SF META-INF/*.DSA META-INF/*.RSA
# Add to our JAR
jar uf ../testament-1.7.0.jar .
cd ..
rm -rf temp
cd ..

echo "🎉 Build complete!"
echo "📄 Plugin JAR: target/testament-1.7.0.jar"
echo "📦 Datapack: datapack/ folder"

# Verify JAR contents
echo "🔍 Verifying JAR contents..."
jar tf target/testament-1.7.0.jar | head -10

echo ""
echo "🚀 Ready to deploy!"
echo "   1. Copy target/testament-1.7.0.jar to server/plugins/"
echo "   2. Copy datapack/ folder to world/datapacks/fallengod_testament/"
echo "   3. Restart server or /reload"
echo "   4. Create altar structures and save as .nbt files"
echo ""
echo "📖 See INSTALLATION.md for detailed setup instructions"