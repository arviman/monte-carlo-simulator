package simulator

import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.data.Reader
import cats.effect.implicits.concurrentParTraverseOps

case class Odds(winOdds: Double, winIncrease: Double, loseDecrease: Double, percPos: Double)
case class RunStats(winNum: Int, loseNum: Int, leastBalance: BigDecimal)
case class StateModel (startAmount: BigDecimal, odds: Odds, iterations: Int, runStats: Option[RunStats]=None)


//run 10000 0.9 0.01 0.05 0.1 1000
object Main extends IOApp {
  val argsReader: Reader[List[String], Option[StateModel]] =
    Reader(inputs =>
      if(inputs.length == 6)
        Some(
          StateModel( 
            inputs.head.toDouble,
            Odds (inputs(1).toDouble,(inputs(2)).toDouble,(inputs(3)).toDouble, inputs(4).toDouble),
            inputs(5).toInt
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
      val nextAmount = cur.startAmount + (cur.startAmount * cur.odds.percPos * (if (winRes)  (cur.odds.winIncrease) else (-cur.odds.loseDecrease)) )

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
  def storeFinalState(cur: StateModel, resIO: Ref[IO, List[StateModel]]): IO[List[StateModel]] =
    resIO.getAndUpdate((x: List[StateModel]) => x :+ cur)

  def printFinalState(cur: StateModel): IO[Unit] = {
    IO(cur.runStats.foreach(stat => 
      println(s"final output is ${cur.startAmount} Win Nums: ${stat.winNum} Lose Nums: ${stat.loseNum} Least Amount ${stat.leastBalance}" 
      ))) 
  }
  def printSummary(resIO: Ref[IO, List[StateModel]] ):IO[Unit]={
    resIO.get.map(lst => {
      val sm = lst.map(_.startAmount).sum
      println(s"Average: ${sm/lst.size}")
    })
  }

  def simulate(cur: StateModel, resIO: Ref[IO, List[StateModel]] ): IO[Unit] = {
    
    calculate(cur) match {
      case Some(r) =>
        simulate(r, resIO)
      case None =>
        for {
          _ <- storeFinalState(cur, resIO)
          _ <- printFinalState(cur)
        }
        yield ()
    }
  }

  override def run(args: List[String]): IO[ExitCode] =
    {
      Ref.of[IO, List[StateModel]](List[StateModel]()).flatMap(resIO => {
        argsReader.run(args) match {
          case Some(req) =>
            (1 until 100).toList.parTraverseN(10)(_=>simulate(req, resIO)) >> printSummary(resIO).as(ExitCode.Success)
          case None =>
            IO(System.err.println("Args should be of form: StartAmount winOdds winIncrease loseDecrease percPerBet iterations")).as(ExitCode.Error)
        }
      })
    }

   
}
