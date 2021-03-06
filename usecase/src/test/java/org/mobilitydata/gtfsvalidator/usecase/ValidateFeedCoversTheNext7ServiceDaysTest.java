/*
 *  Copyright (c) 2020. MobilityData IO.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.mobilitydata.gtfsvalidator.usecase;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gtfsvalidator.domain.entity.gtfs.FeedInfo;
import org.mobilitydata.gtfsvalidator.domain.entity.notice.base.Notice;
import org.mobilitydata.gtfsvalidator.domain.entity.notice.error.FeedInfoExpiresInLessThan7DaysNotice;
import org.mobilitydata.gtfsvalidator.usecase.port.GtfsDataRepository;
import org.mobilitydata.gtfsvalidator.usecase.port.ValidationResultRepository;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ValidateFeedCoversTheNext7ServiceDaysTest {

    // suppressed warning regarding ignored result of method since it is not necessary here
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void feedInfoExpiringInLessThan7DaysShouldGenerateNotice() {
        final FeedInfo mockFeedInfo = mock(FeedInfo.class);
        final LocalDate mockDate = mock(LocalDate.class);
        when(mockFeedInfo.getFeedEndDate()).thenReturn(mockDate);
        when(mockFeedInfo.getFeedPublisherUrl()).thenReturn("feed publisher url");
        when(mockFeedInfo.getFeedLang()).thenReturn("feed lang");
        when(mockDate.isBefore(any())).thenReturn(true);
        when(mockDate.toString()).thenReturn("feed end date");
        final GtfsDataRepository mockGtfsDataRepo = mock(GtfsDataRepository.class);
        final Map<String, FeedInfo> mockFeedInfoCollection = new HashMap<>();
        mockFeedInfoCollection.put("feed publisher name", mockFeedInfo);

        when(mockGtfsDataRepo.getFeedInfoAll()).thenReturn(mockFeedInfoCollection);

        final ValidationResultRepository mockResultRepo = mock(ValidationResultRepository.class);
        final Logger mockLogger = mock(Logger.class);
        final LocalDate currentDate = LocalDate.now();

        final ValidateFeedCoversTheNext7ServiceDays underTest =
                new ValidateFeedCoversTheNext7ServiceDays(mockGtfsDataRepo, mockResultRepo, mockLogger);

        underTest.execute();

        verify(mockLogger, times(1))
                .info("Validating rule 'E038 - Dataset should be valid for at least the next 7 days'");

        verify(mockGtfsDataRepo, times(1)).getFeedInfoAll();

        //noinspection ResultOfMethodCallIgnored
        verify(mockFeedInfo, times(1)).getFeedEndDate();
        verify(mockFeedInfo, times(1)).getFeedPublisherUrl();
        verify(mockFeedInfo, times(1)).getFeedLang();

        verify(mockDate, times(1)).isBefore(currentDate.plusDays(7));
        final ArgumentCaptor<FeedInfoExpiresInLessThan7DaysNotice> captor =
                ArgumentCaptor.forClass(FeedInfoExpiresInLessThan7DaysNotice.class);

        verify(mockResultRepo, times(1)).addNotice(captor.capture());

        final List<FeedInfoExpiresInLessThan7DaysNotice> noticeList = captor.getAllValues();

        assertEquals("feed_info.txt", noticeList.get(0).getFilename());
        assertEquals(currentDate.toString(), noticeList.get(0).getNoticeSpecific(
                Notice.KEY_CURRENT_DATE));
        assertEquals("feed end date", noticeList.get(0).getNoticeSpecific(Notice.KEY_FEED_INFO_END_DATE));
        assertEquals("feed_end_date", noticeList.get(0).getNoticeSpecific(Notice.KEY_FIELD_NAME));
        assertEquals("feed_publisher_name", noticeList.get(0).getNoticeSpecific(
                Notice.KEY_COMPOSITE_KEY_FIRST_PART));
        assertEquals("feed_publisher_url", noticeList.get(0).getNoticeSpecific(
                Notice.KEY_COMPOSITE_KEY_SECOND_PART));
        assertEquals("feed_lang", noticeList.get(0).getNoticeSpecific(Notice.KEY_COMPOSITE_KEY_THIRD_PART));
        assertEquals("feed publisher name", noticeList.get(0).getNoticeSpecific(
                Notice.KEY_COMPOSITE_KEY_FIRST_VALUE));
        assertEquals("feed publisher url", noticeList.get(0).getNoticeSpecific(
                Notice.KEY_COMPOSITE_KEY_SECOND_VALUE));
        assertEquals("feed lang", noticeList.get(0).getNoticeSpecific(Notice.KEY_COMPOSITE_KEY_THIRD_VALUE));

        verifyNoMoreInteractions(mockDate, mockFeedInfo, mockResultRepo, mockGtfsDataRepo, mockLogger);
    }

    // suppressed warning regarding ignored result of method since it is not necessary here
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void feedInfoWithExpirationDateInMoreThan60DaysShouldNotGenerateNotice() {
        final FeedInfo mockFeedInfo = mock(FeedInfo.class);
        final LocalDate mockDate = mock(LocalDate.class);
        when(mockFeedInfo.getFeedEndDate()).thenReturn(mockDate);
        when(mockDate.isBefore(any())).thenReturn(false);
        when(mockDate.toString()).thenReturn("feed end date");
        final GtfsDataRepository mockGtfsDataRepo = mock(GtfsDataRepository.class);
        final Map<String, FeedInfo> mockFeedInfoCollection = new HashMap<>();
        mockFeedInfoCollection.put("feed publisher name", mockFeedInfo);

        when(mockGtfsDataRepo.getFeedInfoAll()).thenReturn(mockFeedInfoCollection);
        final ValidationResultRepository mockResultRepo = mock(ValidationResultRepository.class);
        final Logger mockLogger = mock(Logger.class);
        final LocalDate currentDate = LocalDate.now();

        final ValidateFeedCoversTheNext7ServiceDays underTest =
                new ValidateFeedCoversTheNext7ServiceDays(mockGtfsDataRepo, mockResultRepo, mockLogger);

        underTest.execute();

        verify(mockLogger, times(1))
                .info("Validating rule 'E038 - Dataset should be valid for at least the next 7 days'");

        verify(mockGtfsDataRepo, times(1)).getFeedInfoAll();

        verify(mockFeedInfo, times(1)).getFeedEndDate();
        verify(mockDate, times(1)).isBefore(currentDate.plusDays(7));

        verifyNoInteractions(mockResultRepo);
        verifyNoMoreInteractions(mockDate, mockFeedInfo, mockGtfsDataRepo, mockLogger);
    }

    // suppressed warning regarding ignored result of method since it is not necessary here
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void feedInfoWithoutFeedEndDateShouldNotGenerateNotice() {
        final FeedInfo mockFeedInfo = mock(FeedInfo.class);
        when(mockFeedInfo.getFeedEndDate()).thenReturn(null);
        final GtfsDataRepository mockGtfsDataRepo = mock(GtfsDataRepository.class);
        final Map<String, FeedInfo> mockFeedInfoCollection = new HashMap<>();
        mockFeedInfoCollection.put("feed publisher name", mockFeedInfo);
        when(mockGtfsDataRepo.getFeedInfoAll()).thenReturn(mockFeedInfoCollection);
        final ValidationResultRepository mockResultRepo = mock(ValidationResultRepository.class);
        final Logger mockLogger = mock(Logger.class);

        final ValidateFeedCoversTheNext7ServiceDays underTest =
                new ValidateFeedCoversTheNext7ServiceDays(mockGtfsDataRepo, mockResultRepo, mockLogger);

        underTest.execute();

        verify(mockLogger, times(1))
                .info("Validating rule 'E038 - Dataset should be valid for at least the next 7 days'");

        verify(mockGtfsDataRepo, times(1)).getFeedInfoAll();

        verify(mockFeedInfo, times(1)).getFeedEndDate();

        verifyNoInteractions(mockResultRepo);
        verifyNoMoreInteractions(mockFeedInfo, mockGtfsDataRepo, mockLogger);
    }
}
