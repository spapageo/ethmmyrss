package com.spapageo.ethmmyrss.api.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spapageo.ethmmyrss.FetcherConfiguration;
import com.spapageo.ethmmyrss.ThmmyRssConfiguration;
import com.spapageo.ethmmyrss.api.domain.Announcement;
import com.spapageo.ethmmyrss.client.AnnouncementFetcher;
import com.spapageo.ethmmyrss.jdbi.AnnouncementDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AnnouncementServiceImplTest {

    @Mock
    private AnnouncementFetcher announcementFetcher;

    @Mock
    private AnnouncementDAO announcementDAO;

    @Mock
    private ThmmyRssConfiguration thmmyRssConfiguration;

    @Mock
    private FetcherConfiguration fetcherConfiguration;

    @InjectMocks
    private AnnouncementServiceImpl announcementService;


    @Test
    public void updateAnnouncementsForAllLessons() throws Exception {

        when(thmmyRssConfiguration.getFetcher()).thenReturn(fetcherConfiguration);
        when(fetcherConfiguration.getUsername()).thenReturn("username");
        when(fetcherConfiguration.getPassword()).thenReturn("password");
        when(thmmyRssConfiguration.getLessonIds()).thenReturn(ImmutableMap.of(5, "lesson 1",
                6, "lesson 2"));

        Announcement announcement = mock(Announcement.class);
        when(announcementFetcher.login(any(), any())).thenReturn(Optional.of("token"));
        when(announcementFetcher.lessonLogin(anyInt(), anyString())).thenReturn(true);
        when(announcementFetcher.lessonAnnouncements(eq(null), anyInt(), anyString()))
                .thenReturn(Collections.singletonList(announcement));

        announcementService.updateAnnouncementsForAllLessons();

        verify(announcementFetcher).login("username", "password");
        verify(announcementFetcher).lessonLogin(5, "token");
        verify(announcementFetcher).lessonLogin(6, "token");
        verify(announcementFetcher).lessonAnnouncements(null, 5, "token");
        verify(announcementFetcher).lessonAnnouncements(null, 6, "token");
        verify(announcementDAO, times(2)).insertAnnouncements(Collections.singletonList(announcement));
    }

    @Test
    public void getLessonAnnouncements() throws Exception {
        Announcement announcement = mock(Announcement.class);
        List<Announcement> announcements = ImmutableList.of(announcement);
        when(announcementDAO.getLast10AnnouncementsForLessonId(anyInt()))
                .thenReturn(announcements);

        assertEquals(announcements, announcementService.getLessonAnnouncements(1));
    }

}