# common-songs
Generate a playlist from songs that spotify users have in common

This project makes use of the [Spotify Web API Java](https://github.com/thelinmichael/spotify-web-api-java) library
and [Spring MVC](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html)


## Setup
Create a Spotify application at <https://developer.spotify.com/my-applications> to get ID and Secret values 

Clone this repo

## Running

### Running through IntelliJ:
The application can be run in IntelliJ through the `Application.java` file

Edit the Spring Boot configuration and add the following VM options from the setup step:

    -DspotifyClientId=<id value>
    -DspotifyClientSecret=<secret value>

### Running via command line:
Using values from the setup step, run

    mvnw clean -f pom.xml spring-boot:run -Drun.jvmArguments="-DspotifyClientId=<id value> -DspotifyClientSecret=<secret value>"
    
To enable debugging, add the following and attach a remote debugger on port 5005
    
    -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
