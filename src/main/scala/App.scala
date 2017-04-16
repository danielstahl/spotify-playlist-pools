import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

import com.wrapper.spotify.Api
import com.wrapper.spotify.methods.PlaylistRequest
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest
import com.wrapper.spotify.models.{ClientCredentials, Playlist, SimpleArtist}

import collection.JavaConversions._
import scala.util.Random
import scalaj.http.Http

/**
  * Main entry point
  */
object App {
  val SECRET_FILE = "secret.txt"

  def main(args: Array[String]): Unit = {

    val secret = scala.io.Source.fromFile(SECRET_FILE)
      .getLines()
      .next()
      .split(":")

    val api: Api = Api.builder()
      .clientId(secret.head)
      .clientSecret(secret(1))
      .build()

    val request: ClientCredentialsGrantRequest = api.clientCredentialsGrant().build()
    val clientCredentials: ClientCredentials = request.get()

    api.setAccessToken(clientCredentials.getAccessToken)

    val playlistUris =
      Seq("spotify:user:danielstahl:playlist:5umGwIaFKb0sgffuZTCybz")

    val trackPools = playlistUris.map(playlistUri => shuffledPlaylistPool(playlistUri, api))

    val random = new Random

    val indices = 1 to 50 map(i => random.nextInt(playlistUris.size))

    val tracks = indices.
      map(trackPools(_)
        .nextTrack())
      .filter(_.isDefined)
      .map(_.get)
      .distinct

    println(tracks.mkString(" "))

  }

  def shuffledPlaylistPool(playlistUri: String, api: Api): PlaylistPool = {
    val (user, id) = playlistUserAndId(playlistUri)
    val playlist = fetchPlaylist(user, id, api)
    val playlistTracks = getPlaylistTracks(playlist)
    val playlistTrackArtists = getPlaylistTrackArtists(playlist)
    val playlistArtists = getPlaylistArtists(playlist)
    val shuffledTracks = shuffleTracks(playlistTracks)
    PlaylistPool(playlist, playlistTracks, shuffledTracks, playlistTrackArtists, playlistArtists)
  }

  def playlistUserAndId(playlistUri: String): (String, String) = {
    val uriParts = playlistUri.split(":")
    (uriParts(2), uriParts(4))
  }

  def fetchPlaylist(user: String, id: String, api: Api): Playlist =
    api.getPlaylist(user, id).build().get()


  def getPlaylistTracks(playlist: Playlist): List[String] =
    playlist.getTracks.getItems.map(_.getTrack.getUri).toList

  def getPlaylistTrackArtists(playlist: Playlist): Map[String, List[String]] =
    playlist.getTracks.getItems.map(
      track =>
        (track.getTrack.getUri, track.getTrack.getArtists.map(
          artist =>
            artist.getUri).toList)).toMap


  def getPlaylistArtists(playlist: Playlist): Map[String, SimpleArtist] =
    playlist.getTracks.getItems.flatMap(
      track =>
        track.getTrack.getArtists.map(
          artist =>
            (artist.getUri, artist)).toList).toMap

  def shuffleTracks(trackUris: List[String]): List[String] =
    util.Random.shuffle(trackUris)
}

case class PlaylistPool(playlist: Playlist, tracks: List[String], shuffledTracks: List[String], trackArtists: Map[String, List[String]], artists: Map[String, SimpleArtist]) {
  var trackIterator = shuffledTracks.iterator

  var artistFrequency: Map[String, Int] = Map()

  def nextTrack(): Option[String] = {
    trackIterator = trackIterator.dropWhile(
      track =>
        trackArtists(track).exists(artist => artistFrequency.getOrElse(artist, 0) >= 2))

    if(trackIterator.hasNext) {
      val track = trackIterator.next()

      trackArtists(track).foreach(artist => {
        val artistFreq = artistFrequency.get(artist).map(_ + 1).getOrElse(1)
        artistFrequency = artistFrequency + (artist -> artistFreq)
      })
      Option(track)
    } else Option.empty
  }


}
