package me.kmaxi.wynnvp.controller.scheduling;

import me.kmaxi.wynnvp.services.GuildService;
import me.kmaxi.wynnvp.services.LineReportHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LineReportScheduler {

    @Autowired
    private GuildService guildService;

    @Autowired
    private LineReportHandler lineReportHandler;

    @Scheduled(fixedRate = 10000) // Runs every 10 seconds
    public void sendAllReportsPeriodically() {
        System.out.println("Checking if guild is null...");
        if (guildService.getGuild() == null) return;
        System.out.println("Sending all reports...");
        lineReportHandler.sendAllNewReports();
    }
}
