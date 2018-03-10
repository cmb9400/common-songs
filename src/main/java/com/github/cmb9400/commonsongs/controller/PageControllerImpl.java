package com.github.cmb9400.commonsongs.controller;

import com.github.cmb9400.commonsongs.service.SpotifyDataService;
import com.github.cmb9400.commonsongs.service.SpotifyHelperService;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PageControllerImpl implements PageController {


    @Autowired
    SpotifyHelperService spotifyHelperService;

    @Autowired
    SpotifyDataService spotifyDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PageControllerImpl.class);


    @Override
    public String index(Model model, HttpSession session) {
        if (session.getAttribute("api") == null) {
            String authURL = spotifyHelperService.getAuthorizationURL();
            model.addAttribute("loginURL", authURL);

            return "login";
        }
        else {
            SpotifyApi api = (SpotifyApi) session.getAttribute("api");
            String userId = spotifyHelperService.getUserId(api);

            model.addAttribute("user", spotifyDataService.getUser(userId));
            return "index";
        }
    }


    @Override
    public String callback(String code, Model model, HttpSession session) {
        try {
            SpotifyApi api = spotifyHelperService.login(code);
            spotifyDataService.createUser(api);
            session.setAttribute("api", api);

            return "redirect:/";
        }
        catch (SpotifyWebApiException | IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @Override
    public String getGroup(String groupId, Model model, HttpSession session) {
        return "group";
    }

    @Override
    public ResponseEntity updateSavedTracks(HttpSession session) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", false);

        if (session.getAttribute("api") != null) {
            LOGGER.info("Getting saved tracks...");
            SpotifyApi api = (SpotifyApi) session.getAttribute("api");
            response.put("success", spotifyDataService.collectTracks(api));
            LOGGER.info("Saved tracks collected.");
        }

        // return a 200 ok with body of {"success": <true|false>}
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public String createGroup(String name, HttpSession session) {
        if (name == null || name.trim().equals("")
                || session.getAttribute("api") == null) {
            return "index";
        }
        else {
            try {
                LOGGER.info("Creating group with name " + name + "...");
                SpotifyApi api = (SpotifyApi) session.getAttribute("api");
                String groupId = spotifyDataService.createGroup(api, name);
                spotifyDataService.registerUserWithGroup(api, groupId);

                return "redirect:/group?groupId=" + groupId;
            }
            catch(IOException | SpotifyWebApiException e) {
                return "index";
            }
        }
    }

}
