package me.kmaxi.wynnvp.services.data;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import me.kmaxi.wynnvp.APIKeys;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class LineReportServiceTest {

    @Test
    void canBeCreatedBySpring() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(APIKeys.class, APIKeys::new);
            context.register(LineReportService.class);
            context.refresh();

            assertNotNull(context.getBean(LineReportService.class));
        }
    }
}
