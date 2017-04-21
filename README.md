# Spotify playlist pools

The idea with this program is that you have pools of tracks in the form of Spotify playlists. This program will make a track list based on the supplied playlists. 
The list of tracks will be randomly chosen. 
  
```shell
java -jar spotify-playlist-pools-assembly-1.0-SNAPSHOT.jar -a <artist-frequency> <playlists>
``` 

To authorize against the Spotify webapi the program also uses client credentials to authorize against the Spotify webapi. There are tree ways to authorize 
against Spotify, described [here](https://developer.spotify.com/web-api/authorization-guide/). With the Client credintials flow you login through the clientId and 
clientSecret found in the [developer site](https://developer.spotify.com/) for your application. To run the program you need to add the 
clientId and clientSectret separated by colon (`<clientId>:<clientSecret>`) in a file called `secret.txt`.




 
