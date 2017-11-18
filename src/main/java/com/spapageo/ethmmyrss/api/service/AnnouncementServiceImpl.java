package com.spapageo.ethmmyrss.api.service;

import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.api.domain.Announcement;
import com.spapageo.ethmmyrss.client.AnnouncementFetcher;
import com.spapageo.ethmmyrss.jdbi.AnnouncementDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class AnnouncementServiceImpl implements AnnouncementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementServiceImpl.class);

    private final AnnouncementDAO announcementDAO;
    private final AnnouncementFetcher announcementFetcher;
    private final ThmmyRssConfiguration configuration;

    public AnnouncementServiceImpl(AnnouncementDAO announcementDAO, AnnouncementFetcher announcementFetcher,
                                   ThmmyRssConfiguration configuration) {
        this.announcementDAO = announcementDAO;
        this.announcementFetcher = announcementFetcher;
        this.configuration = configuration;
    }

    @Override
    public void updateAnnouncementsForAllLessons() {
        LOGGER.info("Starting to update the annoucements using username: {}", configuration.getFetcher().getUsername());
        Optional<String> jsession = announcementFetcher
                .login(configuration.getFetcher().getUsername(), configuration.getFetcher().getPassword());
        if (jsession.isPresent()) {
            for (int lessonId : configuration.getLessonIds().keySet()) {
                updateAnnouncementForLessonId(jsession.get(), lessonId);
            }
        }
        LOGGER.info("Done");
    }

    private void updateAnnouncementForLessonId(String jsession, int lessonId) {
        try {
            if (announcementFetcher.lessonLogin(lessonId, jsession)) {
                List<Announcement> announcements = announcementFetcher
                        .lessonAnnouncements(configuration.getFetcher().getEthmmyUrl(), lessonId, jsession);
                if (announcements.size() > 10) {
                    announcements = announcements.subList(0, 10);
                }
                announcementDAO.insertAnnouncements(announcements);
            } else {
                LOGGER.error("Error while logging in for lessons {}", lessonId);
            }
        } catch (Exception e) {
            LOGGER.error("Error while processing lessond " + lessonId, e);
        }
    }

    @Override
    public List<Announcement> getLessonAnnouncements(int lessonId) {
        return announcementDAO.getLast10AnnouncementsForLessonId(lessonId);
    }
}
