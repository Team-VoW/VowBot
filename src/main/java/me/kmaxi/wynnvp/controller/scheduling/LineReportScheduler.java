package me.kmaxi.wynnvp.controller.scheduling;

import lombok.RequiredArgsConstructor;
import me.kmaxi.wynnvp.services.GuildService;
import me.kmaxi.wynnvp.services.LineReportHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LineReportScheduler {
    private final GuildService guildService;

    private final LineReportHandler lineReportHandler;

    @Scheduled(fixedRate = 10000) // Runs every 10 seconds
    public void sendAllReportsPeriodically() {
        if (guildService.getGuild() == null) return;
        lineReportHandler.sendAllNewReports();
    }
}
