package model

import model.Domain.TrainCode
import model.Train.{highSpeed, highSpeedTrain, normalSpeed, normalTrain}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class TrainTest extends AnyFlatSpec:

  "A Train" should "be created with code" in {
    val train = normalTrain(101)
    train.speed should be (normalSpeed)
    train.code should be (TrainCode.fromInt(101))

    val fastTrain = highSpeedTrain(202)
    fastTrain.code should be (TrainCode.fromInt(202))
    fastTrain.speed should be (highSpeed)
  }
  
  