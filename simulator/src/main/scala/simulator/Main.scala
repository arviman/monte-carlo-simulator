package simulator

import cats.effect.{ExitCode, IO, IOApp}
import cats.data.Reader

case class Odds(val winOdds: Double, val winIncrease: Double, val loseDecrease: Double)
case class RunStats(val winNum: Int, val loseNum: Int, val leastBalance: BigDecimal)
case class StateModel (val startAmount: BigDecimal, odds: Odds , iterations: Int, runStats: Option[RunStats]=None)


//run 10000 0.9 0.01 -0.05 1000
object Main extends IOApp {
  val argsReader: Reader[List[String], Option[StateModel]] = 
    Reader(inputs =>
      if(inputs.length == 5)
        Some(
          StateModel( 
            inputs(0).toDouble,
            Odds (inputs(1).toDouble,(inputs(2)).toDouble,(inputs(3)).toDouble),
            inputs(4).toInt
            )
        )
      else None
    )
  

  def calculate(cur: StateModel): Option[StateModel] = {

    if(cur.iterations <= 0)
      None
    else {
      val rng = new scala.util.Random()
      val foo = rng.nextInt(100)
      val baseDouble: Double = (100.0)
      val res = foo.toDouble/baseDouble
      val winOdds = cur.odds.winOdds
      //(println(s"${res} ${winOdds}"))

      val winRes = res < winOdds
      val nextAmount = if (winRes) cur.startAmount * (1 + cur.odds.winIncrease) else cur.startAmount * (1 - cur.odds.loseDecrease)

     val newStateModel = StateModel(
          nextAmount, 
          cur.odds, 
          cur.iterations-1, 
          Some(
            RunStats(
              cur.runStats.map(_.winNum).getOrElse(0) + (if(winRes) 1 else 0), 
              cur.runStats.map(_.loseNum).getOrElse(0) + (if(winRes) 0 else 1),
              if (nextAmount < cur.runStats.map(_.leastBalance).getOrElse(Double.MaxValue)) nextAmount else cur.runStats.map(_.leastBalance).getOrElse(Double.MaxValue)
            )
            )
          )
      Some(newStateModel)
    }

  }
  def printFinalState(cur: StateModel): IO[Unit] = {
    IO(cur.runStats.foreach(stat => println(s"final output is ${cur.startAmount} Win Nums: ${stat.winNum} Lose Nums: ${stat.loseNum} Least Amount ${stat.leastBalance}" ))) 
  }

  def simulate(cur: StateModel): IO[Unit] = {
    
    calculate(cur) match {
      case Some(r) =>
        simulate(r)        
      case None =>
        printFinalState(cur)
    }
  }

  override def run(args: List[String]): IO[ExitCode] =
   argsReader.run(args) match {
      case Some(req) =>
        simulate(req).as(ExitCode.Success)
      case None =>
        IO(System.err.println("Args should be of form: StartAmount winOdds winIncrease loseDecrease iterations")).as(ExitCode(2))
    }
}
