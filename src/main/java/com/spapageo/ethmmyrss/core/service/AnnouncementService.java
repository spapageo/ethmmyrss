package com.spapageo.ethmmyrss.core.service;

import com.spapageo.ethmmyrss.api.domain.Announcement;

import java.util.List;

public interface AnnouncementService {

    void updateAnnouncementsForAllLessons();

    List<Announcement> getLessonAnnouncements(int lessonId);
}
