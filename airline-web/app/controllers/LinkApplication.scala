package controllers

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.math.BigDecimal.double2bigDecimal
import scala.math.BigDecimal.int2bigDecimal
import com.patson.Util
import com.patson.data.AirlineSource
import com.patson.data.AirplaneSource
import com.patson.data.AirportSource
import com.patson.data.LinkSource
import com.patson.model._
import com.patson.model.airplane.Airplane
import com.patson.model.airplane.Model
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.libs.json._
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc._
import com.patson.data.airplane.ModelSource
import play.api.mvc.Security.AuthenticatedRequest
import controllers.AuthenticationObject.AuthenticatedAirline
import com.patson.data.RouteHistorySource
import com.patson.data.LinkHistorySource
import com.patson.DemandGenerator

class LinkApplication extends Controller {
  object TestLinkReads extends Reads[Link] {
     def reads(json: JsValue): JsResult[Link] = {
      val fromAirportId = json.\("fromAirportId").as[Int]
      val toAirportId = json.\("toAirportId").as[Int]
      val airlineId = json.\("airlineId").as[Int]
      val capacity = json.\("capacity").as[Int]
      val price = json.\("price").as[Int]
      val fromAirport = AirportSource.loadAirportById(fromAirportId).get
      val toAirport = AirportSource.loadAirportById(toAirportId).get
      val airline = AirlineSource.loadAirlineById(airlineId).get
      val distance = Util.calculateDistance(fromAirport.latitude, fromAirport.longitude, toAirport.latitude, toAirport.longitude)
      val rawQuality = json.\("quality").as[Int]
      
      val link = Link(fromAirport, toAirport, airline, LinkClassValues.getInstance(price), distance.toInt, LinkClassValues.getInstance(capacity), rawQuality, distance.toInt * 60 / 800, 1)
      (json \ "id").asOpt[Int].foreach { link.id = _ } 
      JsSuccess(link)
    }
  }
  
  
  
  
  implicit object LinkConsumptionFormat extends Writes[LinkConsumptionDetails] {
    def writes(linkConsumption: LinkConsumptionDetails): JsValue = {
//      val fromAirport = AirportSource.loadAirportById(linkConsumption.fromAirportId)
//      val toAirport = AirportSource.loadAirportById(linkConsumption.toAirportId)
//      val airline = AirlineSource.loadAirlineById(linkConsumption.airlineId)
          JsObject(List(
      "linkId" -> JsNumber(linkConsumption.linkId),
//      "fromAirportCode" -> JsString(fromAirport.map(_.iata).getOrElse("XXX")),
//      "fromAirportName" -> JsString(fromAirport.map(_.name).getOrElse("<unknown>")),
//      "toAirportCode" -> JsString(toAirport.map(_.iata).getOrElse("XXX")),
//      "toAirportName" -> JsString(toAirport.map(_.name).getOrElse("<unknown>")),
//      "airlineName" -> JsString(airline.map(_.name).getOrElse("<unknown>")),
      "fromAirportId" -> JsNumber(linkConsumption.fromAirportId),
      "toAirportId" -> JsNumber(linkConsumption.toAirportId),
      "airlineId" -> JsNumber(linkConsumption.airlineId),
      "price" -> Json.toJson(linkConsumption.price),
      "distance" -> JsNumber(linkConsumption.distance),
      "profit" -> JsNumber(linkConsumption.profit),
      "revenue" -> JsNumber(linkConsumption.revenue),
      "fuelCost" -> JsNumber(linkConsumption.fuelCost),
      "crewCost" -> JsNumber(linkConsumption.crewCost),
      "airportFees" -> JsNumber(linkConsumption.airportFees),
      "maintenanceCost" -> JsNumber(linkConsumption.maintenanceCost),
      "depreciation" -> JsNumber(linkConsumption.depreciation),
      "inflightCost" -> JsNumber(linkConsumption.inflightCost),
      "capacity" -> Json.toJson(linkConsumption.capacity),
      "soldSeats" -> Json.toJson(linkConsumption.soldSeats),
      "cycle" -> JsNumber(linkConsumption.cycle)))
      
    }
  }
  
  implicit object LinkHistoryWrites extends Writes[LinkHistory] {
    def writes(linkHistory: LinkHistory): JsValue = {
          JsObject(List(
      "watchedLinkId" -> JsNumber(linkHistory.watchedLinkId),
      "relatedLinks" -> Json.toJson(linkHistory.relatedLinks),
      "invertedRelatedLinks" -> Json.toJson(linkHistory.invertedRelatedLinks)))
    }
  }
  
  implicit object RelatedLinkWrites extends Writes[RelatedLink] {
    def writes(relatedLink : RelatedLink): JsValue = {
          JsObject(List(
      "linkId" -> JsNumber(relatedLink.relatedLinkId),
      "fromAirportId" -> JsNumber(relatedLink.fromAirport.id),
      "fromAirportCode" -> JsString(relatedLink.fromAirport.iata),
      "fromAirportName" -> JsString(relatedLink.fromAirport.name),
      "toAirportId" -> JsNumber(relatedLink.toAirport.id),
      "toAirportCode" -> JsString(relatedLink.toAirport.iata),
      "toAirportName" -> JsString(relatedLink.toAirport.name),
      "fromAirportCity" -> JsString(relatedLink.fromAirport.city),
      "toAirportCity" -> JsString(relatedLink.toAirport.city),
      "fromLatitude" -> JsNumber(relatedLink.fromAirport.latitude),
      "fromLongitude" -> JsNumber(relatedLink.fromAirport.longitude),
      "toLatitude" -> JsNumber(relatedLink.toAirport.latitude),
      "toLongitude" -> JsNumber(relatedLink.toAirport.longitude),
      "airlineId" -> JsNumber(relatedLink.airline.id),
      "airlineName" -> JsString(relatedLink.airline.name),
      "passenger" -> JsNumber(relatedLink.passengers)))
    }
  }
  
  implicit object ModelPlanLinkInfoWrites extends Writes[ModelPlanLinkInfo] {
    def writes(modelPlanLinkInfo : ModelPlanLinkInfo): JsValue = {
      val jsObject = JsObject(List(
      "modelId" -> JsNumber(modelPlanLinkInfo.model.id), 
      "modelName" -> JsString(modelPlanLinkInfo.model.name),
      "capacity" -> JsNumber(modelPlanLinkInfo.model.capacity),
      "duration" -> JsNumber(modelPlanLinkInfo.duration), 
      "maxFrequency" -> JsNumber(modelPlanLinkInfo.maxFrequency),
      "isAssigned" -> JsBoolean(modelPlanLinkInfo.isAssigned)))
      
      var airplaneArray = JsArray()
      modelPlanLinkInfo.airplanes.foreach {
        case(airplane, isAssigned) => 
          airplaneArray = airplaneArray.append(JsObject(List("airplaneId" -> JsNumber(airplane.id), "isAssigned" -> JsBoolean(isAssigned))))
      }
      jsObject + ("airplanes" -> airplaneArray)
    }
    
  }
  
  implicit object LinkWithProfitWrites extends Writes[(Link, Int, Int)] {
    def writes(linkWithProfit: (Link, Int, Int)): JsValue = { 
      val link = linkWithProfit._1
      val profit = linkWithProfit._2
      val revenue = linkWithProfit._3
      Json.toJson(link).asInstanceOf[JsObject] + ("profit" -> JsNumber(profit)) + ("revenue" -> JsNumber(revenue))
    }
  }
  
  implicit object RouteWrites extends Writes[Route] {
    def writes(route : Route): JsValue = { 
      Json.toJson(route.links)
    }
  }
  implicit object linkWithDirectionWrites extends Writes[LinkConsideration] {
    def writes(linkWithDirection : LinkConsideration): JsValue = {
      JsObject(List(
        "linkId" -> JsNumber(linkWithDirection.link.id),
        "fromAirportId" -> JsNumber(linkWithDirection.from.id),
        "toAirportId" -> JsNumber(linkWithDirection.to.id),
        "fromAirportCode" -> JsString(linkWithDirection.from.iata),
        "toAirportCode" -> JsString(linkWithDirection.to.iata),
        "fromAirportName" -> JsString(linkWithDirection.from.name),
        "toAirportName" -> JsString(linkWithDirection.to.name),
        "airlineId" -> JsNumber(linkWithDirection.link.airline.id),
        "airlineName" -> JsString(linkWithDirection.link.airline.name),
        "fromLatitude" -> JsNumber(linkWithDirection.from.latitude),
        "fromLongitude" -> JsNumber(linkWithDirection.from.longitude),
        "toLatitude" -> JsNumber(linkWithDirection.to.latitude),
        "toLongitude" -> JsNumber(linkWithDirection.to.longitude)))
    }
  }
  
  
  case class PlanLinkData(fromAirportId: Int, toAirportId: Int)
  val planLinkForm = Form(
    mapping(
      "fromAirportId" -> number,
      "toAirportId" -> number
    )(PlanLinkData.apply)(PlanLinkData.unapply)
  )
  
  def addTestLink() = Action { request =>
    if (request.body.isInstanceOf[AnyContentAsJson]) {
      val newLink = request.body.asInstanceOf[AnyContentAsJson].json.as[Link](TestLinkReads)
      println("PUT (test)" + newLink)
      
      LinkSource.saveLink(newLink) match {
        case Some(link) =>
          Created(Json.toJson(link))      
        case None => UnprocessableEntity("Cannot insert link")
      }
    } else {
      BadRequest("Cannot insert link")
    }
  }
 
  def addLinkBlock(request : AuthenticatedRequest[AnyContent, Airline]) : Result = {
    if (request.body.isInstanceOf[AnyContentAsJson]) {
      val incomingLink = request.body.asInstanceOf[AnyContentAsJson].json.as[Link]
      val airlineId = incomingLink.airline.id
      
      if (airlineId != request.user.id) {
        println("airline " + request.user.id + " trying to add link for airline " + airlineId + " ! Error")
        return Forbidden
      }
      
      if (incomingLink.getAssignedAirplanes.isEmpty) {
        return BadRequest("Cannot insert link - no airplane assigned")
      }
      
      var existingLink : Option[Link] = LinkSource.loadLinkByAirportsAndAirline(incomingLink.from.id, incomingLink.to.id, airlineId, LinkSource.ID_LOAD)
      
      if (existingLink.isDefined) {
        incomingLink.id = existingLink.get.id
      }
      
      //validate frequency by duration
      val maxFrequency = incomingLink.getAssignedModel().fold(0)(assignedModel => Computation.calculateMaxFrequency(assignedModel, incomingLink.distance))
      if (maxFrequency * incomingLink.getAssignedAirplanes().size < incomingLink.frequency) { //TODO log error!
        println("max frequency exceeded, max " + maxFrequency * incomingLink.getAssignedAirplanes().size + " found " +  incomingLink.frequency)
        return BadRequest("Cannot insert link - frequency exceeded limit")  
      }

      //validate slots      
      val existingFrequency = existingLink.fold(0)(_.frequency)
      val frequencyChange = incomingLink.frequency - existingFrequency
      if ((incomingLink.from.getAirlineSlotAssignment(airlineId) + frequencyChange) > incomingLink.from.getMaxSlotAssignment(airlineId)) {
        println("max slot exceeded, tried to add " + frequencyChange + " but from airport slot at " + incomingLink.from.getAirlineSlotAssignment(airlineId) + "/" + incomingLink.from.getMaxSlotAssignment(airlineId))
        return BadRequest("Cannot insert link - frequency exceeded limit - from airport does not have enough slots")
      }
      if ((incomingLink.to.getAirlineSlotAssignment(airlineId) + frequencyChange) > incomingLink.to.getMaxSlotAssignment(airlineId)) {
        println("max slot exceeded, tried to add " + frequencyChange + " but to airport slot at " + incomingLink.to.getAirlineSlotAssignment(airlineId) + "/" + incomingLink.to.getMaxSlotAssignment(airlineId))
        return BadRequest("Cannot insert link - frequency exceeded limit - to airport does not have enough slots")
      }
      
      val airplanesForThisLink = incomingLink.getAssignedAirplanes
      //validate all airplanes are same model
      val airplaneModels = airplanesForThisLink.foldLeft(Set[Model]())(_ + _.model) //should be just one element
      if (airplaneModels.size != 1) {
        return BadRequest("Cannot insert link - not all airplanes are same model")
      }
      
      //validate the model has the range
      val model = airplaneModels.toList(0)
      if (model.range < incomingLink.distance) {
        return BadRequest("Cannot insert link - model cannot reach that distance")
      }
      
      //validate the model is allowed in the from and to airport
      if (model.range < incomingLink.distance) {
        return BadRequest("Cannot insert link - model cannot reach that distance")
      }
      
      //validate the model is allowed for airport sizes
      if (!incomingLink.from.allowsModel(model) || !incomingLink.to.allowsModel(model)) {
        return BadRequest("Cannot insert link - airport size does not allow that!")
      }
      
      //check if the assigned planes are either previously unassigned or assigned to this link
      val occupiedAirplanes = airplanesForThisLink.flatMap { airplaneForThisLink => 
        val assignedLink = AirplaneSource.loadAirplanesWithAssignedLinkByAirplaneId(airplaneForThisLink.id, AirplaneSource.LINK_SIMPLE_LOAD).get._2
        if (assignedLink.isDefined && assignedLink.get.id != incomingLink.id) {
            List(airplaneForThisLink)
        } else {
            List.empty
        }
      }
        
      if (!occupiedAirplanes.isEmpty) {
        return BadRequest("Cannot insert link - some airplanes already occupied " + occupiedAirplanes)
      }
      
      //valid configuration is valid
      if ((incomingLink.capacity(ECONOMY) * ECONOMY.spaceMultiplier + 
           incomingLink.capacity(BUSINESS) * BUSINESS.spaceMultiplier + 
           incomingLink.capacity(FIRST) * FIRST.spaceMultiplier) > incomingLink.frequency * model.capacity) {
        return BadRequest("Requested capacity exceed the allowed limit, invalid configuration!")
      }
      
      //valid from airport is a base
      if (incomingLink.from.getAirlineBase(airlineId).isEmpty) {
        return BadRequest("Cannot fly from this airport, this is not a base!")
      }
      if (incomingLink.from.id == incomingLink.to.id) {
        return BadRequest("Same from and to airport!")
      }
      
      println("PUT " + incomingLink)
            
      if (existingLink.isEmpty) {
        LinkSource.saveLink(incomingLink) match {
          case Some(link) => Created(Json.toJson(link))      
          case None => UnprocessableEntity("Cannot insert link")
        }
      } else {
        LinkSource.updateLink(incomingLink) match {
          case 1 => Accepted(Json.toJson(incomingLink))      
          case _ => UnprocessableEntity("Cannot update link")
        }
      }
    } else {
      BadRequest("Cannot put link")
    }
  }
  
  def addLink(airlineId : Int) = AuthenticatedAirline(airlineId) { request => addLinkBlock(request) }
  
  def getLink(airlineId : Int, linkId : Int) = AuthenticatedAirline(airlineId) { request =>
    LinkSource.loadLinkById(linkId) match {
      case Some(link) =>
        if (link.airline.id == airlineId) {
          Ok(Json.toJson(link))
        } else {
          Forbidden
        }
      case None =>
        NotFound
    }
    
    
  }
  
  def getAllLinks() = Action {
     val links = LinkSource.loadAllLinks()
    Ok(Json.toJson(links))
  }
  
  def getLinks(airlineId : Int, getProfit : Boolean, toAirportId : Int) = Action {
     
    val links = 
      if (toAirportId == -1) {
        LinkSource.loadLinksByAirlineId(airlineId)
      } else {
        LinkSource.loadLinksByCriteria(List(("airline", airlineId), ("to_airport", toAirportId)))
      }
    if (!getProfit) {
      Ok(Json.toJson(links)).withHeaders(
        ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
      )
    } else {
      val consumptions = LinkSource.loadLinkConsumptionsByAirline(airlineId).foldLeft(Map[Int, LinkConsumptionDetails]()) { (foldMap, linkConsumptionDetails) =>
        foldMap + (linkConsumptionDetails.linkId -> linkConsumptionDetails)
      }
      val linksWithProfit = links.map { link =>  
        (link, consumptions.get(link.id).fold(0)(_.profit), consumptions.get(link.id).fold(0)(_.revenue))  
      }
      Ok(Json.toJson(linksWithProfit)).withHeaders(
        ACCESS_CONTROL_ALLOW_ORIGIN -> "*"
      )
    }
     
  }
  
  def deleteAllLinks() = Action {
    val count = LinkSource.deleteAllLinks()
    Ok(Json.obj("count" -> count))
  }
  
  def deleteLink(airlineId : Int, linkId: Int) = AuthenticatedAirline(airlineId) {
    //verify the airline indeed has that link
    LinkSource.loadLinkById(linkId) match {
      case Some(link) =>
        if (link.airline.id != airlineId) {
        Forbidden
      } else {
        val count = LinkSource.deleteLink(linkId)  
        Ok(Json.obj("count" -> count))    
      }
      case None =>
        NotFound
    }
  }
  
  def getLinkConsumption(airlineId : Int, linkId : Int, cycleCount : Int) = Action {
    LinkSource.loadLinkById(linkId) match {
      case Some(link) =>
        if (link.airline.id == airlineId) {
          val linkConsumptions = LinkSource.loadLinkConsumptionsByLinkId(linkId, cycleCount) 
          if (linkConsumptions.isEmpty) {
            Ok(Json.obj())  
          } else {
            Ok(Json.toJson(linkConsumptions.take(cycleCount)))
          }     
        } else {
          Forbidden
        }
      case None => NotFound
    }
     
  }
  
  def getAllLinkConsumptions() = Action {
     val linkConsumptions = LinkSource.loadLinkConsumptions()
     Ok(Json.toJson(linkConsumptions))
  }


  
  def planLink(airlineId : Int) = Action { implicit request =>
    val PlanLinkData(fromAirportId, toAirportId) = planLinkForm.bindFromRequest.get
    AirportSource.loadAirportById(fromAirportId, true) match {
      case Some(fromAirport) =>
        AirportSource.loadAirportById(toAirportId, true) match {
          case Some(toAirport) =>
            var existingLink : Option[Link] = LinkSource.loadLinkByAirportsAndAirline(fromAirportId, toAirportId, airlineId)
            
            val distance = Util.calculateDistance(fromAirport.latitude, fromAirport.longitude, toAirport.latitude, toAirport.longitude).toInt
            val (maxFrequencyFromAirport, maxFrequencyToAirport) = getMaxFrequencyByAirports(fromAirport, toAirport, Airline.fromId(airlineId), existingLink)
            
            val airplanesWithAssignedLinks : List[(Airplane, Option[Link])] = AirplaneSource.loadAirplanesWithAssignedLinkByOwner(airlineId)
            val freeAirplanes = airplanesWithAssignedLinks.filter {
              case (_ , Some(_)) => false
              case (airplane, None) => 
                airplane.model.range >= distance
            }.map(_._1)
            
            val assignedToThisLinkAirplanes = existingLink match {
              case Some(link) => airplanesWithAssignedLinks.filter {
                case (airplane, Some(assignedLink)) if (link.id == assignedLink.id) => true
                case _ => false
              }.map(_._1)
              case _ => List.empty 
            }               
               
            //group airplanes by model, also add boolean to indicated whether the airplane is assigned to this link
            val availableAirplanesByModel = Map[Model, ListBuffer[(Airplane, Boolean)]]()
            var assignedModel : Option[Model] = existingLink match {
              case Some(link) => link.getAssignedModel()
              case None => None
            }
            
            freeAirplanes.foreach { freeAirplane => 
              availableAirplanesByModel.getOrElseUpdate(freeAirplane.model, ListBuffer[(Airplane, Boolean)]()).append((freeAirplane, false)) 
            }
            assignedToThisLinkAirplanes.foreach { assignedAirplane => 
              availableAirplanesByModel.getOrElseUpdate(assignedAirplane.model, ListBuffer[(Airplane, Boolean)]()).append((assignedAirplane, true))
            }
            val planLinkInfoByModel = ListBuffer[ModelPlanLinkInfo]()
            
            availableAirplanesByModel.filter{
              case (model, _) => fromAirport.allowsModel(model) && toAirport.allowsModel(model) 
            }.foreach {
              case(model, airplaneList) => 
                val duration = Computation.calculateDuration(model, distance)
                val existingSlotsUsedByThisModel= if (assignedModel.isDefined && assignedModel.get.id == model.id) { existingLink.get.frequency } else { 0 } 
                val maxFrequencyByModel : Int = Computation.calculateMaxFrequency(model, distance)
                
                planLinkInfoByModel.append(ModelPlanLinkInfo(model, duration, maxFrequencyByModel, assignedModel.isDefined && assignedModel.get.id == model.id, airplaneList.toList))
            }
            
            
            val suggestedPrice : LinkClassValues = LinkClassValues.getInstance(Pricing.computeStandardPrice(distance, Computation.getFlightType(fromAirport, toAirport), ECONOMY),
                                                                   Pricing.computeStandardPrice(distance, Computation.getFlightType(fromAirport, toAirport), BUSINESS),
                                                                   Pricing.computeStandardPrice(distance, Computation.getFlightType(fromAirport, toAirport), FIRST))
            
            val directBusinessDemand = DemandGenerator.computeDemandBetweenAirports(fromAirport, toAirport, PassengerType.BUSINESS) + DemandGenerator.computeDemandBetweenAirports(toAirport, fromAirport, PassengerType.BUSINESS)
            val directTouristDemand = DemandGenerator.computeDemandBetweenAirports(fromAirport, toAirport, PassengerType.TOURIST) + DemandGenerator.computeDemandBetweenAirports(toAirport, fromAirport, PassengerType.TOURIST)
            
            val directDemand = directBusinessDemand + directTouristDemand
            val airportLinkCapacity = LinkSource.loadLinksByToAirport(fromAirport.id, LinkSource.ID_LOAD).map { _.capacity.total }.sum + LinkSource.loadLinksByFromAirport(fromAirport.id, LinkSource.ID_LOAD).map { _.capacity.total }.sum 
                                                                   
            var resultObject = Json.obj("fromAirportName" -> fromAirport.name,
                                        "fromAirportCity" -> fromAirport.city,
                                        "fromCountryCode" -> fromAirport.countryCode,
                                        "toAirportName" -> toAirport.name,
                                        "toAirportCity" -> toAirport.city,
                                        "toCountryCode" -> toAirport.countryCode,
                                        "distance" -> distance, 
                                        "suggestedPrice" -> suggestedPrice,  
                                        "maxFrequencyFromAirport" -> maxFrequencyFromAirport, 
                                        "maxFrequencyToAirport" -> maxFrequencyToAirport,
                                        "directDemand" -> directDemand,
                                        "businessPassengers" -> directBusinessDemand.total,
                                        "touristPassengers" -> directTouristDemand.total,
                                        "airportLinkCapacity" -> airportLinkCapacity).+("modelPlanLinkInfo", Json.toJson(planLinkInfoByModel.toList))
             
            if (existingLink.isDefined) {
              resultObject = resultObject + ("existingLink", Json.toJson(existingLink))
            }
            
            Ok(resultObject)
          case None => BadRequest("unknown toAirport")
        }
        case None => BadRequest("unknown toAirport")
    }
  }
  
  def getVipRoutes() = Action {
    Ok(Json.toJson(RouteHistorySource.loadVipRoutes()))
  }
  
  def setWatchedLink(airlineId : Int, linkId : Int) = AuthenticatedAirline(airlineId) {
    LinkHistorySource.updateWatchedLinkId(airlineId, linkId)
    Ok(Json.toJson(linkId))
  }
  
  def getWatchedLink(airlineId : Int) = AuthenticatedAirline(airlineId) {
    Ok(LinkHistorySource.loadWatchedLinkIdByAirline(airlineId) match {
      case Some(watchedLinkId) => Json.toJson(watchedLinkId)
      case None => Json.obj()
    })
  }
  
  def getLinkHistory(airlineId : Int) = AuthenticatedAirline(airlineId) {
    LinkHistorySource.loadWatchedLinkIdByAirline(airlineId) match {
      case Some(watchedLinkId) =>
        LinkHistorySource.loadLinkHistoryByWatchedLinkId(watchedLinkId) match {
          case Some(linkHistory) => Ok(Json.toJson(linkHistory))
          case None => Ok(Json.obj())
        }
      case None => Ok(Json.obj())
    }
  }
  
  def updateServiceFunding(airlineId : Int) = AuthenticatedAirline(airlineId) { request =>
    if (request.body.isInstanceOf[AnyContentAsJson]) {
      val serviceFunding = request.body.asInstanceOf[AnyContentAsJson].json.\("serviceFunding").as[Int]
      
      val airline = request.user
      airline.setServiceFunding(serviceFunding)
      AirlineSource.saveAirlineInfo(airline)
      Ok(Json.obj())
    } else {
      BadRequest("Cannot Update service funding")
    }
  }
  
  def updateMaintenanceQuality(airlineId : Int) = AuthenticatedAirline(airlineId) { request =>
    if (request.body.isInstanceOf[AnyContentAsJson]) {
      val maintenanceQuality = request.body.asInstanceOf[AnyContentAsJson].json.\("maintenanceQuality").as[Int]
      
      val airline = request.user
      airline.setMaintainenceQuality(maintenanceQuality)
      AirlineSource.saveAirlineInfo(airline)
      Ok(Json.obj())
    } else {
      BadRequest("Cannot Update maintenance quality")
    }
  }

  
  class PlanLinkResult(distance : Double, availableAirplanes : List[Airplane])
  //case class AirplaneWithPlanRouteInfo(airplane : Airplane, duration : Int, maxFrequency : Int, limitingFactor : String, isAssigned : Boolean)
  case class ModelPlanLinkInfo(model: Model, duration : Int, maxFrequency : Int, isAssigned : Boolean, airplanes : List[(Airplane, Boolean)])
  
  private def getMaxFrequencyByAirports(fromAirport : Airport, toAirport : Airport, airline : Airline, existingLink : Option[Link]) : (Int, Int) =  {
    val airlineId = airline.id
    
    val existingSlotsByThisLink = existingLink.fold(0)(_.frequency)
    val maxFrequencyFromAirport : Int = fromAirport.getMaxSlotAssignment(airlineId) - fromAirport.getAirlineSlotAssignment(airlineId) + existingSlotsByThisLink 
    val maxFrequencyToAirport : Int = toAirport.getMaxSlotAssignment(airlineId) - toAirport.getAirlineSlotAssignment(airlineId) + existingSlotsByThisLink
    
    (maxFrequencyFromAirport, maxFrequencyToAirport)
  }
}
