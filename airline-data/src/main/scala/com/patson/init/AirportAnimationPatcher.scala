package com.patson.init

import com.patson.data.{AirportAnimationSource, AirportSource}
import com.patson.model.animation._

object AirportAnimationPatcher extends App {
  import AirportAnimationType._
  val animations = Array(
    ("JFK", AIRPORT, "https://player.vimeo.com/video/506825925"),
    ("JFK", CITY, "https://player.vimeo.com/video/506868914"),
    ("JFK", CITY, "https://player.vimeo.com/video/508765597"),
    ("JFK", SCENERY, "https://player.vimeo.com/video/507867073"),
    ("LAX", AIRPORT, "https://player.vimeo.com/video/506879495"),
    ("LAX", CITY, "https://player.vimeo.com/video/506879854"),
    ("LAX", SCENERY, "https://player.vimeo.com/video/506879191"),
    ("DUS", AIRPORT, "https://player.vimeo.com/video/506896956"),
    ("YVR", AIRPORT, "https://player.vimeo.com/video/506899463"),
    ("YVR", CITY, "https://player.vimeo.com/video/506899152"),
    ("HND", AIRPORT, "https://player.vimeo.com/video/506972343"),
    ("HND", CITY, "https://player.vimeo.com/video/506972043"),
    ("NRT", AIRPORT, "https://player.vimeo.com/video/506970681"),
    ("NRT", CITY, "https://player.vimeo.com/video/506970067"),
    ("AMS", AIRPORT, "https://player.vimeo.com/video/506969661"),
    ("AMS", SCENERY, "https://player.vimeo.com/video/506969176"),
    ("CDG", AIRPORT, "https://player.vimeo.com/video/506968296"),
    ("CDG", CITY, "https://player.vimeo.com/video/506967713"),
    ("CDG", CITY, "https://player.vimeo.com/video/506967192"),
    ("LHR", AIRPORT, "https://player.vimeo.com/video/506971743"),
    ("LHR", CITY, "https://player.vimeo.com/video/506971156"),
    ("HKG", AIRPORT, "https://player.vimeo.com/video/506966797"),
    ("HKG", CITY, "https://player.vimeo.com/video/506972580"),
    ("ITM", AIRPORT, "https://player.vimeo.com/video/507438279"),
    ("ITM", SCENERY, "https://player.vimeo.com/video/507437447"),
    ("FRA", AIRPORT, "https://player.vimeo.com/video/507433682"),
    ("FRA", CITY, "https://player.vimeo.com/video/507434551"),
    ("YYZ", AIRPORT, "https://player.vimeo.com/video/507436551"),
    ("YYZ", CITY, "https://player.vimeo.com/video/507435617"),
    ("BRU", AIRPORT, "https://player.vimeo.com/video/507869726"),
    ("BRU", CITY, "https://player.vimeo.com/video/507868675"),
    ("EWR", AIRPORT, "https://player.vimeo.com/video/507867951"),
    ("ZRH", CITY, "https://player.vimeo.com/video/507870691"),
    ("ZRH", AIRPORT, "https://player.vimeo.com/video/507866484"),
    ("MEX", CITY, "https://player.vimeo.com/video/508309527"),
    ("MEX", AIRPORT, "https://player.vimeo.com/video/508300132"),
    ("MAN", CITY, "https://player.vimeo.com/video/508300628"),
    ("MAN", AIRPORT, "https://player.vimeo.com/video/508301762"),
    ("KIX", CITY, "https://player.vimeo.com/video/508303833"),
    ("KIX", AIRPORT, "https://player.vimeo.com/video/508304975"),
    ("CGN", CITY, "https://player.vimeo.com/video/508305347"),
    ("CGN", AIRPORT, "https://player.vimeo.com/video/508306547"),
    ("TLV", AIRPORT, "https://player.vimeo.com/video/508307466"),
    ("SYD", CITY, "https://player.vimeo.com/video/508307743"),
    ("NGO", CITY, "https://player.vimeo.com/video/508308675"),
    ("LGW", AIRPORT, "https://player.vimeo.com/video/508302845"),
    ("ORY", AIRPORT, "https://player.vimeo.com/video/508308343"),
    ("BOI", CITY, "https://player.vimeo.com/video/506881395"),
  )


  def patchAirportAnimations() = {
    val animationList = animations.map {
      case (iata, animationType, url) => AirportAnimation(AirportSource.loadAirportByIata(iata).get, animationType, url)
    }.toList
    AirportAnimationSource.updateAirportAnimations(animationList)
  }

  patchAirportAnimations()
}