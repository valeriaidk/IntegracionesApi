package com.extech.IntegracionesApis.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

@Slf4j
@Component
public class BrowserLauncher {

    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() {
        log.info("🚀 BrowserLauncher: ApplicationReadyEvent recibido");
        
        String url = "http://localhost:8081/swagger-ui/index.html";
        
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                log.info("🖥️ Desktop soportado: {}", Desktop.getDesktop());
                log.info("🌐 Abriendo navegador en: {}", url);
                Desktop.getDesktop().browse(new URI(url));
                log.info("✅ Swagger UI abierto automáticamente en el navegador");
            } else {
                log.warn("⚠️ Desktop no soportado, intentando método alternativo para Windows");
                openBrowserWindows(url);
            }
        } catch (Exception e) {
            log.error("❌ Error al abrir navegador automáticamente", e);
            log.info("📋 Swagger UI disponible manualmente en: {}", url);
        }
    }
    
    private void openBrowserWindows(String url) {
        try {
            log.info("🌐 Abriendo navegador con cmd.exe en: {}", url);
            ProcessBuilder pb = new ProcessBuilder(
                "cmd", "/c", "start " + url
            );
            pb.start();
            log.info("✅ Swagger UI abierto automáticamente en el navegador (método Windows)");
        } catch (Exception e) {
            log.error("❌ Error al abrir navegador con cmd.exe", e);
            log.info("📋 Swagger UI disponible manualmente en: {}", url);
        }
    }
}
