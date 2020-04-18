package mapreduce

import java.io.{FileNotFoundException, IOException}

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import util.helper.MapperHelper
import util.message.{Done,  Flush, Init_Map, ReduceNameTitlePair}

import scala.collection.mutable
import scala.io.Source


class MapActor(reduceActors: List[ActorRef]) extends Actor {

  val STOP_WORDS_LIST = List("a", "am", "an", "and", "are", "as", "at", "be",
    "do", "go", "if", "in", "is", "it", "of", "on", "the", "to")
  val numReducers = reduceActors.size


  val helper = MapperHelper

  def receive = {
    case Init_Map(title,url) =>
      {
        var nameTitlePair = helper.extractProperName(title,helper.readFromFile(url))
        nameTitlePair.foreach((dataSet:(String,String))=>{
          var index = getReducerIndex(dataSet._1)
          reduceActors(index) ! ReduceNameTitlePair(dataSet._1,dataSet._2)
        })
     }
    case Flush =>
      for (i <- 0 until numReducers) {
        reduceActors(i) ! Flush
      }

  }
  def getReducerIndex(word: String) = {

    Math.abs((word.hashCode)%numReducers)
  }


}
