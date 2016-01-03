/**
 * Created by canara on 9/11/2015.
 */

import java.io.Serializable
import java.security.MessageDigest
import akka.actor.{ActorSelection, Actor, ActorSystem, Props}
import akka.routing.RoundRobinRouter
import scala.collection.mutable.ArrayBuffer
import util.Random


object project1 extends App{
  var leadingZeros:Int=args(0).toInt
  val system=ActorSystem("BitcoinMiner")
//  for(i<-1 to 4){
//    val remoteMiner=system.actorOf(Props[BitcoinMiner], name="RemoteMiner"+i)
//    remoteMiner ! connectRemoteSystem("192.168.10.111",leadingZeros)
//  }
  val masterMiner=system.actorOf(Props(new MasterMiner(4)),name="MasterMiner")
 // masterMiner ! startMiner(leadingZeros)

}
class MasterMiner (totalWorkers : Int) extends Actor{
  var workerCount:Int=0
  var noOfZeros:Int=0
  var count:Int=0
  val workerMiner =context.actorOf(Props[BitcoinMiner].withRouter(RoundRobinRouter(totalWorkers)),name="BitcoinMiner")
  def receive = {

    case startMiner(leadingZeros : Int) => {
      noOfZeros=leadingZeros
      for(k<-1 to totalWorkers)
        workerMiner ! startMining(leadingZeros, totalWorkers)
    }
    case minedCoins(minedBitCoins: ArrayBuffer[String]) => {
      workerCount+=1
      println("Worker "+workerCount+" has finished mining process and reproted the status")
      //println("Total number of processed input by worker "+workerCount+" is "+processedInput)
      println("Total number of bitcoins mined by worker "+workerCount+" is "+minedBitCoins.length)
      for(i<-0 until minedBitCoins.length){
        println(minedBitCoins(i))
      }
    }
  }
}
 class BitcoinMiner extends Actor{
   var minedBitcoins: ArrayBuffer[String]=new ArrayBuffer[String]()
   //var processedInput:Int=0
   val msgDigest = MessageDigest.getInstance("SHA-256")
   var remoteActor : ActorSelection = null
     def receive = {
       case connectRemoteSystem(ipAddress,zerosCount) =>
         remoteActor= context.actorSelection("akka.tcp://BitcoinMiner@"+ipAddress+":2552/user/MasterMiner")
         remoteActor ! startMiner(zerosCount)

       case startMining(noOfZeros: Int, totalWorkers :Int) => {
         for (j <- 0 to 1000000) {
           var input: String = "mshanmugam" + ";" + Random.alphanumeric.take(12).mkString
           var hashVal = msgDigest.digest(input.getBytes("UTF-8"))
           var bitcoin: String = hashVal.map("%02x" format _).mkString
           if (bitcoin.startsWith("0" * noOfZeros)) {
             minedBitcoins += input + " " + bitcoin
           }
           //processedInput+=1
         }
          sender ! minedCoins(minedBitcoins)
           //sender ! finishedMining(processedInput)
         }
       }


 }


case class startMiner(noOfZeros : Int)
case class minedCoins(minedBitCoins : ArrayBuffer[String])
//case class finishedMining(processedInput: Int)
case class startMining(noOfZeros : Int, totalWorkers: Int)
case class connectRemoteSystem(ipAddress : String, zerosCount : Int)
