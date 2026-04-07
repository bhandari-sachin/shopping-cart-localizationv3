# ---------- Stage 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy pom first (better caching)
COPY pom.xml .
# Cache dependencies
RUN mvn -B dependency:go-offline
# Copy source
COPY src ./src
# Build JAR
RUN mvn clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jdk
WORKDIR /app

# WSLg handles the display protocol automatically (X11 or Wayland).
# We only need the bare minimum native libs for JavaFX to render.
RUN apt-get update && apt-get install -y \
    # --- GTK3 (native dialogs, window decorations) --- \
    libgtk-3-0 \
    # --- OpenGL (JavaFX hardware rendering) --- \
    libgl1 \
    # --- GLib (GTK dependency) ---
    libglib2.0-0 \
    # --- Audio ---
    libasound2t64 \
    libpulse0 \
    # --- Utilities ---
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Download JavaFX SDK
RUN mkdir -p /javafx-sdk \
    && wget -O /tmp/javafx.zip https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_linux-x64_bin-sdk.zip \
    && unzip /tmp/javafx.zip -d /javafx-sdk \
    && mv /javafx-sdk/javafx-sdk-21.0.2/lib /javafx-sdk/lib \
    && rm -rf /javafx-sdk/javafx-sdk-21.0.2 /tmp/javafx.zip

# Copy JAR from build stage
COPY --from=build /app/target/shopping-cart-localization.jar app.jar

# JVM tuning
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Do NOT set DISPLAY or WAYLAND_DISPLAY here.
# WSLg injects these automatically at runtime — hardcoding breaks portability.

# Run JavaFX app
CMD ["sh", "-c", "java $JAVA_OPTS \
    --module-path /javafx-sdk/lib \
    --add-modules javafx.controls,javafx.graphics,javafx.base,javafx.swing,javafx.fxml \
    --add-opens javafx.graphics/com.sun.javafx.util=ALL-UNNAMED \
    --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
    --add-opens javafx.graphics/com.sun.javafx.css=ALL-UNNAMED \
    --add-opens javafx.base/com.sun.javafx.runtime=ALL-UNNAMED \
    --add-opens javafx.base/com.sun.javafx.collections=ALL-UNNAMED \
    --add-opens javafx.fxml/com.sun.javafx.fxml=ALL-UNNAMED \
    -jar app.jar"]