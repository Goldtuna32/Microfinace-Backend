package com.sme.service;

import com.google.api.client.auth.oauth2.Credential;
 
import com.google.api.client.json.JsonFactory;


import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
 
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;


import com.google.api.client.json.JsonFactory;

import com.google.api.client.json.jackson2.JacksonFactory;


import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;
import com.google.api.client.util.DateTime;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;

 
import java.util.*;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "HolidayManager";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
 
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Credential getCredentials() throws Exception {
        InputStream in = getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(Paths. get(TOKENS_DIRECTORY_PATH).toFile()))
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public List<Map<String, String>> fetchMyanmarHolidays(int year) throws Exception {
        Credential credential = getCredentials();

        // Build Google Calendar API client
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        String calendarId = "en.mm#holiday@group.v.calendar.google.com"; // Myanmar Holidays Calendar ID
        // Fetch events (holidays)
        Events events = service.events().list(calendarId)
                .setTimeMin(new com.google.api.client.util.DateTime(year + "-01-01T00:00:00Z"))
                .setTimeMax(new com.google.api.client.util.DateTime(year + "-12-31T23:59:59Z"))
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();

        List<Map<String, String>> holidays = new ArrayList<>();

        for (Event event : events.getItems()) {
            String date = event.getStart().getDate().toString();
            String summary = event.getSummary();

            Map<String, String> holiday = new HashMap<>();
            holiday.put("date", date);
            holiday.put("name", summary);
            holidays.add(holiday);
        }
        return holidays;
    }
}
