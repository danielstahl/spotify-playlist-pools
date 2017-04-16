
import com.wrapper.spotify.Api
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest
import com.wrapper.spotify.models.{ClientCredentials, Playlist, SimpleArtist}

import collection.JavaConversions._
import scala.util.Random

import org.rogach.scallop._

/**
  * Main entry point
  */
object App {
  val SECRET_FILE = "secret.txt"

  def main(args: Array[String]): Unit = {
    val conf = new Conf(args)

    println(s"Building pool playlist with ${conf.playlists()} with ${conf.artistFrequency()} allowed artists")
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

    val playlistUris = conf.playlists()

    val trackPools = playlistUris.map(playlistUri => shuffledPlaylistPool(playlistUri, api, conf.artistFrequency()))

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

  def shuffledPlaylistPool(playlistUri: String, api: Api, allowedArtistFrequency: Int): PlaylistPool = {
    val (user, id) = playlistUserAndId(playlistUri)
    val playlist = fetchPlaylist(user, id, api)
    val playlistTracks = getPlaylistTracks(playlist)
    val playlistTrackArtists = getPlaylistTrackArtists(playlist)
    val playlistArtists = getPlaylistArtists(playlist)
    val shuffledTracks = shuffleTracks(playlistTracks)
    PlaylistPool(playlist, playlistTracks, shuffledTracks, playlistTrackArtists, playlistArtists, allowedArtistFrequency)
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

case class PlaylistPool(playlist: Playlist, tracks: List[String], shuffledTracks: List[String], trackArtists: Map[String, List[String]], artists: Map[String, SimpleArtist], allowedArtistFrequency: Int) {
  private var trackIterator = shuffledTracks.iterator

  var artistFrequency: Map[String, Int] = Map()

  def nextTrack(): Option[String] = {
    trackIterator = trackIterator.dropWhile(
      track =>
        trackArtists(track).exists(artist => artistFrequency.getOrElse(artist, 0) >= allowedArtistFrequency))

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

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val artistFrequency = opt[Int]( default = Some(5))

  val playlists = trailArg[List[String]]()
  verify()
}
