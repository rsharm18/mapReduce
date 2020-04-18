package mapreduce

import scala.collection.mutable.HashMap
import akka.actor.{Actor, ActorRef}
import com.typesafe.config.ConfigFactory
import util.message.{Done, Flush, ReduceNameTitlePair}

import scala.collection.mutable

class ReduceActor extends Actor {
  var remainingMappers = ConfigFactory.load.getInt("number-mappers")
  var reduceMap = HashMap[String, Int]()

  var reduceMapTitle = HashMap[String, List[String]]()


  def receive = {
    case ReduceNameTitlePair(name,title) =>
      {
        produceNameTitleData(name,title)
      }
    case Flush =>
      remainingMappers -= 1

      if (remainingMappers == 0) {
        var stringBuilder = new StringBuilder();

        stringBuilder
          .append(s"\n *** ${self.path.toStringWithoutAddress} :: START ***\n")
          //.append(s"\n \t ${self.path.toStringWithoutAddress} :: Proper Name(Count) : ${reduceMap.toString()}")
          //.append(s"\n\n \t ${self.path.toStringWithoutAddress} :: Word(Title)        : ${reduceMapTitle.toString()}")
        reduceMapTitle.foreach((data:(String,List[String]))=>{

          var iCount = data._2.size
          stringBuilder.append("(").append(data._1).append(",[")
          data._2.foreach((title:String)=>{
           stringBuilder.append(title)
            if(iCount>1){
              stringBuilder.append(",")
            }
            iCount -=1

          })
          stringBuilder.append("]").append(")  ")
        })
        stringBuilder.append(s"\n\n **** ${self.path.toStringWithoutAddress} :: END ***\n")
        println(stringBuilder.toString)
        context.parent ! Done
      }
  }

  def produceNameTitleData(word:String,title:String) = {
    if (reduceMap.contains(word))
    {
      reduceMap += (word -> (reduceMap(word) + 1))
      reduceMapTitle put ( word,  title :: reduceMapTitle(word))
    }
    else {
      reduceMap += (word -> 1)
      reduceMapTitle.put( word,List(title))
    }
  }
}
