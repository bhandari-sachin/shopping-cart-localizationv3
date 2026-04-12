package com.example.shoppingcart;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MainApp Tests")
class MainAppTest {

    private static boolean javafxStarted;

    @BeforeAll
    static void startJavaFx() throws Exception {
        if (javafxStarted) {
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX platform did not start in time");
        javafxStarted = true;
    }

    @Test
    @DisplayName("start should load the FXML and configure the Stage")
    void startShouldLoadFxmlAndConfigureStage() throws Exception {
        Stage stage = runOnFxThread(Stage::new);

        runOnFxThread(() -> {
            MainApp app = new MainApp();
            app.start(stage);
            return null;
        });

        assertNotNull(stage.getScene());
        assertEquals("sachinbh/shopping Cart", stage.getTitle());
        assertEquals(600.0, stage.getMinWidth());
        assertEquals(400.0, stage.getMinHeight());

        runOnFxThread(() -> {
            stage.hide();
            return null;
        });
    }

    private static <T> T runOnFxThread(Callable<T> callable) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return callable.call();
        }

        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(callable.call());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "FX task did not finish in time");
        if (error.get() != null) {
            if (error.get() instanceof Exception exception) {
                throw exception;
            }
            throw new RuntimeException(error.get());
        }
        return result.get();
    }
}

