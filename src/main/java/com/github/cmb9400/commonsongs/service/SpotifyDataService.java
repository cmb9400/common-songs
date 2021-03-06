package com.github.cmb9400.commonsongs.service;

import com.github.cmb9400.commonsongs.domain.Group;
import com.github.cmb9400.commonsongs.domain.MockRepository;
import com.github.cmb9400.commonsongs.domain.User;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.SavedTrack;
import com.wrapper.spotify.model_objects.specification.Track;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class SpotifyDataService {

    @Autowired
    private MockRepository database;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyDataService.class);


    /**
     * for a given user, collect all of their saved tracks to the database
     */
    public int collectTracks(SpotifyApi api) {
        try {
            // Collect all of a user's saved tracks using spotify's paginated request format
            // TODO maybe keep the track item itself instead of just URI? check their .equals() method
            // TODO handle the same track with different spotify URIs, ex 3xZ4wgiv2fIiIWrKYPLlng and 0OgGn1ofaj55l2PcihQQGV
            Set<Track> savedTracks = new HashSet<>();
            Paging<SavedTrack> savedTrackPage = api.getUsersSavedTracks().build().execute();
            for (int i = 0; i <= savedTrackPage.getTotal(); i += 50) {
                savedTrackPage = api.getUsersSavedTracks().limit(50).offset(i).build().execute(); // 50 is spotify's max

                savedTracks.addAll(
                        Arrays.stream(savedTrackPage.getItems())
                                .map(SavedTrack::getTrack)
                                .collect(Collectors.toList())
                );
            }

            // save the set of saved tracks
            String userId = api.getCurrentUsersProfile().build().execute().getId();
            database.setUserSavedTracks(userId, savedTracks);

            return savedTracks.size();
        }
        catch (SpotifyWebApiException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return -1;
        }
    }


    /**
     * create a new group and add the user to it
     * @return the new group's id
     */
    @Transactional
    public String createGroup(SpotifyApi api, String name) {
        String groupId = RandomStringUtils.randomAlphanumeric(8);

        // make sure it doesn't already exist.
        // this is susceptible to issues at large scale as the database fills with IDs
        while(groupIdExists(groupId)) {
            groupId = RandomStringUtils.randomAlphanumeric(8);
        }

        Group group = new Group(groupId, name, new HashSet<>());
        database.createGroup(group);

        return groupId;
    }


    /**
     * determine if a group id exists in the database
     */
    public boolean groupIdExists(String groupId) {
        return database.getGroup(groupId) != null;
    }

    /**
     * add a user to the database
     */
    public void createUser(SpotifyApi api) throws IOException, SpotifyWebApiException {
        String userId = api.getCurrentUsersProfile().build().execute().getId();

        if(database.getUser(userId) == null) {
            User user = new User(userId, new HashSet<Track>(), new HashSet<Group>());
            database.createUser(user);
        }
    }

    /**
     * get a user from the database
     */
    public User getUser(String userId) {
        return database.getUser(userId);
    }


    /**
     * get a group from the database
     */
    public Group getGroup(String groupId) {
        return database.getGroup(groupId);
    }


    /**
     * register a user with a group
     */
    public void registerUserWithGroup(SpotifyApi api, String groupId) throws IOException, SpotifyWebApiException {
        String userId = api.getCurrentUsersProfile().build().execute().getId();

        database.registerUserWithGroup(userId, groupId);
    }

}
